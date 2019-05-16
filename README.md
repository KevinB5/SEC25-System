# SEC25-System

$ -> comando no terminal

compilar e correr :
$cd ..\SEC25-System\PubLib\
$mvn install
$cd ..\SEC25-System\proj-Server\
$mvn install
$cd ..\SEC25-System\proj-Client\
$mvn install

supondo que estamos a utilizar o valor "f"=1, abrir 4 consolas e por a correr os 4 Servidores da seguinte forma:
$cd ..\SEC25-System\proj-Server\
(linhas de comandos 1)
$mvn clean compile exec:java -Dexec.args="8000 1"
(linhas de comandos 2)
$mvn clean compile exec:java -Dexec.args="8001 2"
(linhas de comandos 3)
$mvn clean compile exec:java -Dexec.args="8002 3"
(linhas de comandos 4)
$mvn clean compile exec:java -Dexec.args="8003 4"

após iniciar cada servidor, decidir o valor de "f" e se estamos a utilizar o cartão de cidadão "s/n"
(em cada servidor, exemplo)
$<valor de f> (por exemplo "1")
$<decisão sobre cartão de cidadão> (por exemplo "s")

após configurar os servidores, estabelecer a conexão entre eles:
(em cada servidor)
$connect

(abrir número de clientes que pretender, dos 2 possíveis)
$cd ..\SEC25-System\proj-Cliente\
$mvn clean compile exec:java
$<userID> (por exemplo "1")

(se pretender vender um objecto)
$sell <goodID>

(se pretender comprar um objecto)
$state <goodID>

(caso o good esteja disponível)
$connect
$buy <sellerID> <goodID>  (para que seja possível comprar, o seller tem que executar "$connect" antes)
  
 
# Testes de ataque

Replay Attack:
	(server starts)
	(cliente: user1)
		$ 1 (login)
		$ sell good1
		$ state good1
	(cliente: user2)
		$ connect
		$ state good1
	
	*loop*	$ buy user1 good1
	
Fake Sell:
	(server starts)
	(cliente: user1)
		$ 1 (login)
		$ sell good1
		$ state good1
	(cliente: user2)
		$ connect
		$ state good1
		$ buy user1 good1
	(cliente: user1)
		$ sell good1

