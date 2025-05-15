@echo off
REM ----------------------------------------------------------------------------
REM Apache Maven Wrapper for Windows
REM ----------------------------------------------------------------------------

SETLOCAL
set MVNW_VERBOSE=%MVNW_VERBOSE%

REM JAVA 설정
IF NOT "%JAVA_HOME%" == "" (
  set "JAVACMD=%JAVA_HOME%\bin\java"
) ELSE (
  for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
)

IF NOT EXIST "%JAVACMD%" (
  echo JAVA_HOME is not set and java could not be found in PATH.
  exit /b 1
)

REM .mvn/wrapper 경로 설정
set WRAPPER_DIR=%~dp0.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar

REM 파일 확인
IF NOT EXIST "%WRAPPER_JAR%" (
  echo Wrapper JAR not found: %WRAPPER_JAR%
  exit /b 1
)

REM Maven 실행
"%JAVACMD%" -jar "%WRAPPER_JAR%" %*
EXIT /B %ERRORLEVEL%
