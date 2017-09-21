@if NOT DEFINED ANDROID_NDK (
@echo Please set enviroment variable ANDROID_NDK. For example "c:\android\android-ndk-r10e"
goto :eof
)

%ANDROID_NDK%\ndk-build.cmd
@rem V=1
