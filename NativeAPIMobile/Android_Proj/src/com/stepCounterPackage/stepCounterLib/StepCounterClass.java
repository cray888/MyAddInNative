
package com.stepCounterPackage.stepCounterLib;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import static java.lang.Math.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.lang.String;

public class StepCounterClass implements Runnable, SensorEventListener
{
    Activity     m_Activity;
    long         m_CallObject;
    boolean      isAllLoaded;
    
    final double THRESHOLD_LEVEL_MAX = 0.4;            // [rad]
    final double THRESHOLD_LEVEL_MIN = 0.001;          // [rad]
    final int    ORDER_FILTER = 3;                     // []
    final int    REFRESH_INTERVAL_SENSOR_DATA = 100;   // [ms]
    final int    LENGTH_HISTORY_SENSOR_DATA = 5;       // [s]
    final int    MINIMAL_LONG_STEP = 20;               // [100ms]
    int          TAKT_COUNT_NOT_CALC;                  // [100ms]
    
    private double curAccelOX;
    private double curAccelOY;
    private double curAccelOZ;
    private int    curStepCount;
    private int    sizeVector;
    private int    itData;
    private int    nTakt;
    
    enum TypeValues { METERING, SMOOTH, MINSTAT, INSTEP };
    
    private Vector<Double> listAngleMetering;
    private Vector<Double> listAngleSmooth;
    private Vector<Double> listAngleMin;
    private Vector<Double> listInStep;

    private Timer         m_SensorData;
    private TimerTask     m_RefreshSensorData;
    private SensorManager m_SensorManager = null;
    
    private static native void NativeTrace(String str);
    
    private void trace(String str)
    {
        if (isAllLoaded)
            NativeTrace(str);
    }
   
    private static native void OnRefreshData(long pObject, double orientXOZ, double orientXOY, double orientYOZ, long stepCount);
    
    private void refreshData(double orientOX, double orientOY, double orientOZ, long stepCount)
    {
        if (isAllLoaded)
            OnRefreshData(m_CallObject, atan2(orientOY, orientOZ), atan2(orientOX, orientOY), atan2(orientOX, orientOZ), stepCount);
    }
    
    private void setNextElement(TypeValues typeVal, double newValue)
    {
        // Shift iterator
        if (itData >= (sizeVector - 1))
            itData = 0;
        else
            ++itData;
        // Memory a new value
        switch (typeVal)
        {
            case METERING: listAngleMetering.setElementAt(newValue, itData); break;
            case SMOOTH:   listAngleSmooth.setElementAt(newValue, itData); break;
            case MINSTAT:  listAngleMin.setElementAt(newValue, itData); break;
            case INSTEP:   listInStep.setElementAt(newValue, itData); break;
            default: ;
        }
    }
    
    private void setElement(int itElem, TypeValues typeVal, double newValue)
    {
        if (itElem < 0)
            return;
        if (itElem >= sizeVector)
            return;
        int itCurr = itData - (sizeVector - itElem - 1);
        // Calculate iterator
        if (itCurr < 0)
            itCurr = sizeVector + itCurr;
        // Memory a new value
        switch (typeVal)
        {
            case METERING: listAngleMetering.setElementAt(newValue, itCurr); break;
            case SMOOTH:   listAngleSmooth.setElementAt(newValue, itCurr); break;
            case MINSTAT:  listAngleMin.setElementAt(newValue, itCurr); break;
            case INSTEP:   listInStep.setElementAt(newValue, itCurr); break;
            default: ;
        }
    }
    
    private double getElement(int itElem, TypeValues typeVal)
    {
        if (itElem < 0)
            return 0.0;
        if (itElem >= sizeVector)
            return 0.0;
        int itCurr = itData - (sizeVector - itElem - 1);
        // Calculate iterator
        if (itCurr < 0)
            itCurr = sizeVector + itCurr;
        // Get value from memory
        double result = 0.0;
        switch (typeVal)
        {
            case METERING: result = listAngleMetering.get(itCurr); break;
            case SMOOTH:   result = listAngleSmooth.get(itCurr); break;
            case MINSTAT:  result = listAngleMin.get(itCurr); break;
            case INSTEP:   result = listInStep.get(itCurr); break;
            default: ;
        }
        return result;
    }
    
    public void onAddSensorData()
    {
        // Acceleration vector              
        double sizeAccel = Math.sqrt(
                Math.pow(curAccelOX, 2) +
                Math.pow(curAccelOY, 2) +
                Math.pow(curAccelOZ, 2));
        
        double cosAngle = curAccelOX / sizeAccel;
        double angleAccel = Math.acos(cosAngle);    
            
        // Memory new value
        setNextElement(TypeValues.METERING, angleAccel);

        if ((++nTakt) % TAKT_COUNT_NOT_CALC == 0)
        {
            // Execute filter
            for (int i = sizeVector - TAKT_COUNT_NOT_CALC; i < sizeVector; ++i)
            {
                double s = 0.0;
                for (int j = i - ORDER_FILTER + 1; j <= i; ++j)
                    s += getElement(j, TypeValues.METERING);
                setElement(i, TypeValues.SMOOTH, s / ORDER_FILTER);
            }
        
            // Calculate minimum values
            for (int i = sizeVector - TAKT_COUNT_NOT_CALC; i < sizeVector; ++i)
            {
                setElement(i, TypeValues.MINSTAT, 10000.0); // Obviously large number
                int minPos = Math.max(0, i - TAKT_COUNT_NOT_CALC * 2 + 1);
                for (int j = minPos; j <= i; ++j)
                    setElement(i, TypeValues.MINSTAT, Math.min(getElement(i, TypeValues.MINSTAT), getElement(j, TypeValues.SMOOTH)));
            }

            // Calculation step's phases
            for (int i = sizeVector - TAKT_COUNT_NOT_CALC; i < sizeVector; ++i)
            {
                setElement(i, TypeValues.INSTEP, 0.0);
                if ((Math.abs(getElement(i - 1, TypeValues.MINSTAT) - getElement(i, TypeValues.MINSTAT)) < THRESHOLD_LEVEL_MIN)
                   && (getElement(i - 1, TypeValues.MINSTAT) > THRESHOLD_LEVEL_MAX))
                {
                    int longPredValue = 1;
                    int j = i - 2;
                    while ((j >= 0) && (Math.abs(getElement(i - 1, TypeValues.MINSTAT) - getElement(j, TypeValues.MINSTAT)) < THRESHOLD_LEVEL_MIN))
                    {
                        ++longPredValue;
                        --j;
                    }
                    if (longPredValue > MINIMAL_LONG_STEP)
                        setElement(i, TypeValues.INSTEP, 1.0);
                }
            }

            // Count steps
            boolean isUp = false;
            for (int i = sizeVector - TAKT_COUNT_NOT_CALC - 1; i < sizeVector; ++i)
            {
                if (isUp && (getElement(i, TypeValues.INSTEP) < 0.5))
                {
                    // Every cycle is two steps
                    curStepCount += 2;
                    refreshData(curAccelOX, curAccelOY, curAccelOZ, curStepCount);                   
                }
                isUp = (getElement(i, TypeValues.INSTEP) > 0.5);
            }
        }
    }

    public StepCounterClass(Activity activity, long pObject)
    {
        m_Activity = activity;
        m_CallObject = pObject;
        isAllLoaded = false;

        m_SensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        // Create the vector for sensor's data
        sizeVector = (int) Math.round(LENGTH_HISTORY_SENSOR_DATA /
                                                (REFRESH_INTERVAL_SENSOR_DATA / 1000.0));
        TAKT_COUNT_NOT_CALC = (int)(2.0 * sizeVector / LENGTH_HISTORY_SENSOR_DATA);   // []  2 - count of seconds
        
        listAngleMetering = new Vector<Double>(sizeVector);
        listAngleSmooth = new Vector<Double>(sizeVector);
        listAngleMin = new Vector<Double>(sizeVector);
        listInStep = new Vector<Double>(sizeVector);
        for (int i = 0; i < sizeVector; ++i) 
        {
            listAngleMetering.addElement(Double.valueOf(0.0));
            listAngleSmooth.addElement(Double.valueOf(0.0));
            listAngleMin.addElement(Double.valueOf(0.0));
            listInStep.addElement(Double.valueOf(0.0));
        }
        
        // Iterator sensor's data
        itData = 0;
        
        // Initial values
        curAccelOX = 0.0;
        curAccelOY = 0.0;
        curAccelOZ = 0.0;
        nTakt = 0;
        curStepCount = 0;
        
        // Timer for refresh sensor's data
        m_SensorData = new Timer();
        m_RefreshSensorData = new TimerTask()
        {
            public void run()
            {
                onAddSensorData();
            }
        };
        m_SensorData.schedule(m_RefreshSensorData, REFRESH_INTERVAL_SENSOR_DATA, REFRESH_INTERVAL_SENSOR_DATA);
    }

    public void registerListener()
    {
        // Set event handler accelerometer's sensor data
        m_SensorManager.registerListener(this,
                                        m_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                        SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener()
    {
        // Set event handler accelerometer's sensor data is null
        m_SensorManager.unregisterListener(this);
    }
    
    public void show()
    {
        m_Activity.runOnUiThread(this);
    }

    public void run()
    {   
        try 
        {
            System.loadLibrary("com_1c_StepCounter");
        }
        catch (UnsatisfiedLinkError e) 
        {
            e.printStackTrace();
        }
        isAllLoaded = true;
        System.out.println(this + ".run() END");
    }
    
    public void resetCounter()
    {
        curStepCount = 0;
        refreshData(curAccelOX, curAccelOY, curAccelOZ, curStepCount);
    }
    
    public int getStepCount()
    {
        return curStepCount;
    }
    
    public float getOrientXOZ()
    {
        return (float)atan2(curAccelOY, curAccelOZ);
    }
    
    public float getOrientXOY()
    {
        return (float)atan2(curAccelOX, curAccelOY);
    }
    
    public float getOrientYOZ()
    {
        return (float)atan2(curAccelOX, curAccelOZ);
    }

    @Override
    public void onSensorChanged(SensorEvent event) 
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
        {
            curAccelOX = event.values[0];
            curAccelOY = event.values[1];
            curAccelOZ = event.values[2];
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    
}
