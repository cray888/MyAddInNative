@if NOT DEFINED ANDROID_NDK (
@echo Please set enviroment variable ANDROID_NDK. For example "c:\android\android-ndk-r10e"
goto :eof
)
@if NOT DEFINED ANDROID_HOME (
@echo Please set enviroment variable ANDROID_HOME. For example "c:\android\sdk"
goto :eof
)
@if NOT DEFINED ANT_HOME (
@echo Please set enviroment variable ANT_HOME. For example "c:\android\apache-ant-1.9.4"
goto :eof
)

set COMPONENT_NAME=com_1c_StepCounter
set PATH=%ANT_HOME%\bin;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\bin;%PATH%

call rmdir /S /Q "%~dp0libs"  2>nul

call "%ANDROID_HOME%\tools\android.bat" -v update project -n "%COMPONENT_NAME%" -t android-22 -p %~dp0

@rem ==== Build Java Code ====
call ant.bat clean
call ant.bat debug

if not %errorlevel%==0 exit /b %errorlevel%

@rem ==== Build C/C++ Code ====
call %ANDROID_NDK%\ndk-build.cmd
@rem V=1

if not %errorlevel%==0 exit /b %errorlevel%

@rem ==== copy libraries ====
call mkdir "%~dp0publish"          2>nul
call mkdir "%~dp0publish\armeabi"  2>nul
call mkdir "%~dp0publish\x86"      2>nul

call copy /Y "%~dp0bin\%COMPONENT_NAME%-debug.apk" "%~dp0publish\%COMPONENT_NAME%.apk"
call copy /Y "%~dp0libs\armeabi\lib%COMPONENT_NAME%.so" "%~dp0publish\armeabi\lib%COMPONENT_NAME%_ARM.so"
call copy /Y "%~dp0libs\x86\lib%COMPONENT_NAME%.so" "%~dp0publish\x86\lib%COMPONENT_NAME%_i386.so"
