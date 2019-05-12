@echo off
SET /A i = 1
:loop

IF %i%==5 GOTO END 
	start cmd.exe /k "mvn clean compile exec:java -Dexec.args=8000 %i%"
	SET /a i=%i%+1
GOTO :LOOP
:END


