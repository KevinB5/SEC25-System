@echo off
SET /A i = 1
SET /B p = 8000
:loop

IF %i%==5 GOTO END 
	start cmd.exe /k "mvn clean compile exec:java -Dexec.args=%p% %i%"
	SET /a i=%i%+1
	SET /b p=%p%+1
GOTO :LOOP
:END


