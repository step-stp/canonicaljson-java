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
title "Build Canonical and Run Parameter"
@setlocal

set ERROR_CODE=0
 

@REM Add manual JAVA Folder.
SET JAVA_FOLDER=C:\Program Files\Java\jdk1.8.0_131
 
@REM ==== START JAVAHOME VALIDATION ====
if not "%JAVA_HOME%" == "" IF exist "%JAVA_HOME%\bin\java.exe" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment or is an invalid directory. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.

if not "%JAVA_FOLDER%" == "" IF exist "%JAVA_FOLDER%\bin\java.exe" (
	echo Using %JAVA_FOLDER% as java home 
	set JAVA_HOME=%JAVA_FOLDER%
 	goto OkJHome
)
goto error

:OkJHome

@REM ==== END VALIDATION ====
  
 SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"

 
Echo Clean Building classes
	 Echo BUILDING AND INSTALLING CANONICAL  
	 call mvnw.cmd  clean
	 call mvnw.cmd  package install -DskipTests
	 Echo TESTING 
	 call mvnw.cmd test 
Echo Building complete

 
Echo Executing CanonicalJson on %1
%JAVA_EXE%  -cp .\target\canonicaljson\CanonicalJson.jar;.\target\test-classes com.stratumn.canonicaljson.CanonicalJsonTest %1  
@REM %JAVA_EXE%  -cp .\target\classes;.\target\test-classes com.stratumn.canonicaljson.CanonicalJsonTest %1

Goto success

:error
Echo No test are run.

:success
Echo Test running ended.
