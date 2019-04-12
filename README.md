# SEC25-System

$ -> comando no terminal

compilar e correr :
$cd ..\SEC25-System\
$mvn install

correr cada módulo (Server e Clientes, abrir linha de comandos para cada):
$cd ..\SEC25-System\proj-Server\
$mvn clean compile exec:java

(abrir número de clientes que pretender, dos quatro possíveis)
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
