@echo off
cd /d "%~dp0"
call mvnw.cmd -q exec:java -Dexec.mainClass="dk.fklub.fkult.presentation.cli.DatabaseSetupMain"
pause