# SEC25-System

compilar e correr :

mvn clean compile exec:java
(em cada módulo)

correr servidor primeiro

testar:
correr clientes(cada um num terminal diferente)

 1-inserir id(numero) 
 2-connect
 
 
 
 
 ----
 Garantia de frescura, 
  contador: 
   nao começar do zero: começar de um numero aleatório e ir incrementando
   
   numero tem de ser acordado entre cliente e notário:
   
   1- conectam-se
   2- Notário: userX counter = random
   3- UserX: OK
   4- User: not counter = random2
   5 - Notário: OK
   
  
  Escrita/Persistência de Dados:
  
  FALTA: garantir que se a maquina do servidor falhar o ficheiro nao fica escrito a meio-- se falhar volta ao estado anterior; (guardar em dois ficheiros maybe(?))
   
 
