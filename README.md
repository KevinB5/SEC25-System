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

______________________________________________________________________________

#TODO:
	#Client:
- ClientApp apenas tem o main, library tem vários métodos onde está a criar mensagens e a enviar (User podia criar essas tais mensagens e pedir ao ClientApp para enviar a partir de um método "send"), além de ClientApp poder fazer connectUsers diretamente em vez de existir uma cadeia de métodos entre as 3 classes
- GoodState não devia estar no PubLib?
	
	#Server:
- Good não é usado
- Notary: 
	1. métodos do log devia usar uma classe que escreve e lê o ficheiro log no PubLib (verificar se é o melhor) ?
	2. Todos os métodos de encriptação, desencriptar, assinar, tem que ser usado a partir de uma classe de PubLib
	3. Apagar printGoods e por o sysout diretamente
	4. Em vez de usar LibraryServer para criação de threads, podemos fazer uma Nested Class(classe dentro de uma classe, não alterava nada, apenas ficava mais organizado) no App e assim reduzimos quantidade de classes
	
	#PubLib:
- Organizar os métodos de encriptação, desencriptação, assinatura e do login de cartão de cidadão (evitando código repetido e colocar em classes corretas)
- Corrigir a geração de chaves privadas e públicas
- Utilizar os métodos de RSA corretamente nas mensagens
- Utilizar a assinatura nas mensagens
	


