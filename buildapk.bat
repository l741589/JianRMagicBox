setlocal ENABLEDELAYEDEXPANSION
cd /d %~dp0
set d=%~dp0
set ce=call :SYS_CHECK_ERROR
set src=app\src\main\
set ajar="%ANDROID_HOME%\platforms\android-23\android.jar"
set z="D:\Program Files\7-Zip\7z.exe"
set gradle=gradlew.bat
set aapt=%d%tools\aapt.exe
set keystore=%d%tools\debug.keystore
set tmp=%d%target\
set sdkver=23.0.3
set java="%JAVA_HOME%\bin\java.exe"
set javac="%JAVA_HOME%\bin\javac.exe"
set jar="%JAVA_HOME%\bin\jar.exe"
set jarsigner="%JAVA_HOME%\bin\jarsigner.exe"
set buildtools=%ANDROID_HOME%\build-tools\%sdkver%
set dx=%buildtools%\lib\dx.jar
set apkbuilder="%ANDROID_HOME%\tools\lib\sdklib.jar"
set zipalign="%buildtools%\zipalign.exe"
set res=-S %src%res
set ndkbuild="D:\Android\android-ndk-r10e\ndk-build.cmd"
::%java% -cp %apkbuilder% com.android.sdklib.build.ApkBuilderMain
::exit /B
:start
rd /S /Q %tmp%
md %tmp%
call :copyDenpendency
call :aapt
call :ndkbuild
call :compile
call :dex
call :build
call :zipalign
call :sign
call :install
exit /B

:copyDenpendency
rem task copyDependencies(type: Copy) {
rem     from configurations.compile
rem     into 'dependencies'
rem }
if not exist app\dependencies call %gradle% copyDependencies
md %tmp%jar
md %tmp%aar
md %tmp%sysjar
pushd app\dependencies
	for %%i in (*.jar) do (
		copy %%i %tmp%jar\%%~nxi
	)
	for %%i in (*.aar) do (
		rd /S /Q %%~dpni
		call %z% x -y -o%tmp%aar\%%~ni %%i
		set res=!res! -S %tmp%aar\%%~ni\res
		copy %tmp%aar\%%~ni\classes.jar %tmp%jar\%%~ni.jar
		for %%j in (%tmp%aar\%%~ni\libs\*.jar) do copy %%~dpnxj %tmp%sysjar\%%~nxj
	)
)
popd
exit /B

:aapt
md %tmp%\gen
%aapt% package -f -m -J %tmp%\gen !res! -I %ajar% -M %d%tools\AndroidManifest.xml -F %tmp%\output.ap  --auto-add-overlay --customized-package-id 99
%CE%
exit /B

:compile
del %tmp%\src.txt
pushd %tmp%gen
for /R %%i in (*.java) do echo %%~dpnxi >> %tmp%\src.txt
popd
pushd %src%\java
for /R %%i in (*.java) do echo %%~dpnxi >> %tmp%\src.txt
popd
echo %d%tools\BuildConfig.java >> %tmp%\src.txt
md %tmp%\classes
%javac% -g -encoding UTF-8 -target 1.6 -source 1.6 -bootclasspath %ajar% -d %tmp%classes -cp %tmp%jar\*;%tmp%sysjar\* @%tmp%src.txt
%CE%
for %%i in (%tmp%jar\*.jar) do %z% x -y -o%tmp%classes %%i
exit /B

:dex
%java% -jar %dx% --dex --output=%tmp%\classes.dex %tmp%\classes
%CE%
exit /B

:build
java -cp %apkbuilder% com.android.sdklib.build.ApkBuilderMain %tmp%output_noalign.apk -u -z %tmp%output.ap -f %tmp%\classes.dex -nf %d%\libs -rf %d%\tools\rf
::call %z% a %tmp%output.ap %tmp%classes.dex
%CE%
exit /B

:ndkbuild
pushd target
call %ndkbuild% NDK_PROJECT_PATH=%d% APP_BUILD_SCRIPT=%d%/tools/Android.mk APP_ABI="armeabi-v7a armeabi x86" APP_STL=gnustl_shared
%ce%
popd
exit /B

:zipalign
%zipalign% -f -v 4 "%tmp%output_noalign.apk" "%tmp%output.apk"
%CE%
exit /B

:sign
call %jarsigner% -digestalg SHA1 -sigalg SHA1withRSA -keystore %keystore% -storepass android %tmp%output.apk androiddebugkey -signedjar %tmp%output.apk
%CE%
exit /B

:install
call adb install -r %tmp%output.apk
call adb shell am start -D -n "com.bigzhao.jianrmagicbox/com.bigzhao.jianrmagicbox.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
exit /B

:SYS_CHECK_ERROR
if "%1"=="" (
	set sys_ce_code=0
) else (
	set sys_ce_code=%1
)
if not "%errorlevel%"=="!sys_ce_code!" call :ERROR %2
exit /B

:ERROR
@echo [TASK]=========== ERROR ===========
echo=ERROR^^! %*
pause
echo on
@exit 1 