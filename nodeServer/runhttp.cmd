@echo off
set SCRIPTPATH=%~dp0
call "%SCRIPTPATH%\..\apienv.cmd"
node "%SCRIPTPATH%server\server.js"
