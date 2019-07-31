@REM ----------------------------------------------------------------------------
@REM CanonicalJson Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Arguments: 
@REM  Folder path with input.json (and expected.json if a validation is to be done on the result)
@REM  of File containing JSON to serialize.  
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' 
@echo off
@REM set title of command window
title %0 
   
  
@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

if "%JAVA_HOME%" == "" ( 
SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0_131
 )

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:OkJHome

if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo Error: JAVA_HOME is set to an invalid directory. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

@REM ==== END VALIDATION ====

:init
@REM  Echo Building classes  
@REM  dir .\src\main /s /B *.java > sources.txt
@REM  "%JAVA_HOME%\bin\javac.exe" -cp src\ -d target\classes\  -sourcepath src/**/*.java
@REM dir .\src\test /s /B *.java > sources.txt
@REM  "%JAVA_HOME%\bin\javac.exe"  -d.\target\test-classes\ @sources.txt

SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"
%JAVA_EXE%  -cp .\target\classes;.\target\test-classes com.stratumn.canonicaljson.CanonicalJsonTest %1
