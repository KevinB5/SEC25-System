# SEC25-System

compilar e correr :

mvn clean compile exec:java
(em cada módulo)

correr servidor primeiro

testar:
correr clientes(cada um num terminal diferente)

 1-inserir id(numero) 
 2-connect
 
comandos para as operacoes:
(Terminal)

state + <goodID>

sell + <goodID>

buy + <sellerID> + <goodID> 
 
 
--------------OBJETIVOS--------------------

 A) Garantia de frescura:

 1.Good counter:
 -começa num dado valor e é incrementado sempre que o good muda de estado
 -todos os pedidos BuyGood ou TransferGood devem fazer referencia ao counter respetivo
 
 2. Timestamp:
 -cada mensagem deve conter um timestamp
 -temos de decidir um intervalo que é tolerado para receber mensagens ou se fica ao critério do user
 Mário: acho que deixar ao critério do user é seguro (mas não é ideal, uma mensagem por exemplo com 5 min de avanço de certeza que foi repetida, por exemplo), porque acho que o notário não tem de se preocupar com timestamps, porque não recebe atualizações de estados, só pedidos que já estão garantidos pelo good counter. 
 //PROF DISSE QUE NÃO É MUITO ÚTIL. MELHOR IDEIA: DESAFIOS
 
Enviar mensagem (ao Notário, neste caso) com desafio (por exemplo, enviar um certo número num campo da mensagem) para se certificar que a mensagem é fresca.
 
 
 B) Certificados (e assinaturas?) do Notário com Cartão do Cidadão
 
 C) Persistência de dados (como garantir que uma transação certificada não é travada antes de ficar na memória do Notário?)
 possível solução: Apenas emitir a certificação *após* estar guardada num documento a transação, bem como o pedido assinado dos users.
 
 -----------------------------------------------
 
 
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
   
 
