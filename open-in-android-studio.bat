@echo off
echo Opening project in Android Studio...

rem Check common Android Studio installation locations
set "FOUND=0"

rem Check default locations
if exist "C:\Program Files\Android\Android Studio\bin\studio64.exe" (
    echo Found Android Studio in default location...
    start "" "C:\Program Files\Android\Android Studio\bin\studio64.exe" "%CD%"
    set "FOUND=1"
) else if exist "C:\Program Files\Android Studio\bin\studio64.exe" (
    echo Found Android Studio in alternate location...
    start "" "C:\Program Files\Android Studio\bin\studio64.exe" "%CD%"
    set "FOUND=1"
)

if "%FOUND%"=="0" (
    echo Android Studio not found in default locations.
    echo Please open Android Studio manually and open this project.
)

echo Done! 