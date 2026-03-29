README.txt

COMO EXECUTAR


1. Compilar

(na pasta onde estão os ficheiros .java)

javac *.java


2. Iniciar o servidor

java mySaudeServer <porto>

Exemplo:
java mySaudeServer 12345


3. Executar o cliente

Formato geral:
java mySaude -s <ip>:<porto> -u <username> [opções]

Exemplo:
java mySaude -s 127.0.0.1:12345 -u user1 ...


4. Estrutura necessária

Devem existir as pastas:

../servidor/
../eu/
../keystore/

No servidor já existem as pastas dos utilizadores:

../servidor/user1/
../servidor/user2/

Keystores:
../keystore/keystore.<username>


5. Preparação das keystores e utilizadores (OBRIGATÓRIO)

Já existem keystores criadas para os seguintes utilizadores:

- user1 (password: passUser1)
- user2 (password: passUser2)

As keystores encontram-se em:
../keystore/keystore.user1
../keystore/keystore.user2

Os certificados entre os utilizadores já foram importados.

Caso se pretenda criar novos utilizadores:

Criar keystore:
bash ./create_keystore.sh <username> <password>

Importar certificados:
bash importar_certs.sh <user_from> <user_to> <password_to>


6. Comandos principais

Legenda:
- <path_ficheiro> → caminho local (relativo ou absoluto)
- <nome_ficheiro> → apenas nome (ficheiro no servidor)


Enviar ficheiros:
java mySaude -s <ip>:<porto> -u <user> -e <path_ficheiro> -t <destinatario>

Receber ficheiros:
java mySaude -s <ip>:<porto> -u <user> -r <nome_ficheiro>

Cifrar:
java mySaude -u <user> -p <password> -c <path_ficheiro> -t <destinatario>

Decifrar:
java mySaude -u <user> -p <password> -d <path_ficheiro.cifrado>

Cifrar e enviar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -ce <path_ficheiro> -t <destinatario>

Receber e decifrar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -rd <nome_ficheiro>

Assinar:
java mySaude -u <user> -p <password> -a <path_ficheiro>

Verificar assinatura:
java mySaude -u <user> -p <password> -v <path_ficheiro> -t <assinante>

Assinar e enviar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -ae <path_ficheiro> -t <destinatario>

Receber e verificar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -rv <nome_ficheiro> -t <assinante>

Assinar + cifrar + enviar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -ace <path_ficheiro> -t <destinatario>

Receber + decifrar + verificar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -rdv <nome_ficheiro> -t <assinante>


7. Ficheiros de teste

Na pasta ../eu/ já existem os seguintes ficheiros para teste:

- teste.txt
- exame1.pdf
- exame2.pdf
- exame3.pdf


8. Notas finais

- O servidor deve estar ativo antes do cliente
- <path_ficheiro> deve existir no sistema local
- <nome_ficheiro> refere-se a ficheiros no servidor
- Ficheiros recebidos são guardados em ../eu/
- As pastas dos utilizadores devem existir no servidor