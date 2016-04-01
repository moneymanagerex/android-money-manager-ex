set lib=d:\lib\selendroid
set binpath=..\..\app\build\outputs\apk
set apk=app-debug-unaligned.apk

java -jar %lib%\selendroid-standalone-0.17.0-with-dependencies.jar -app %binpath%\%apk%