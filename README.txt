README.txt

COMO EXECUTAR


1. Compilar

(na pasta onde estão os ficheiros .java, o /src)

javac *.java


2. Iniciar o servidor

java mySaudeServer <porto>

Exemplo:
java mySaudeServer 12345


depois aparecerá para inserir duas password, que são: 123456

3. Executar o cliente

Formato geral:
java mySaude -s <ip>:<porto> -u <username> [opções]

Exemplo:
java mySaude -s 127.0.0.1:12345 -u user1 ...

pedirá uma password: 123456


4. Estrutura necessária

Devem existir as pastas:

../servidor/
../eu/
../keystore/

No src, para criar os users:

java criarUser user1 medico user11 -f user1.cer
java criarUser user2 utente user22 -f user2.cer


5. Preparação das keystores e utilizadores (OBRIGATÓRIO)

Já existem keystores criadas para os seguintes utilizadores:

- user1 (password: passUser1)
- user2 (password: passUser2)

As keystores encontram-se em:
../keystore/keystore.user1
../keystore/keystore.user2


Caso se pretenda criar novos utilizadores:

Criar keystore:
bash ./create_keystore.sh <username> <password>

Importar certificados:
bash importar_certs.sh <user_from> <user_to> <password_to>


6. Comandos principais

Legenda:
- <path_ficheiro> → caminho local (relativo ou absoluto)
- <nome_ficheiro> → apenas nome (ficheiro no servidor)
- o combo  -u <user>-p <password>, tem de pertencer a um dos users criados com o criarUser

Enviar ficheiros:
java mySaude -s <ip>:<porto> -u <user>-p <password> -e <path_ficheiros> -t <destinatario>

Receber ficheiros:
java mySaude -s <ip>:<porto> -u <user>-p <password> -r <nome_ficheiros>

Cifrar:
java mySaude -u <user> -p <password> -c <path_ficheiros> -t <destinatario>

Decifrar:
java mySaude -u <user> -p <password> -d <path_ficheiros.cifrado>

Cifrar e enviar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -ce <path_ficheiros> -t <destinatario>

Receber e decifrar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -rd <nome_ficheiros>

Assinar:
java mySaude -u <user> -p <password> -a <path_ficheiros>

Verificar assinatura:
java mySaude -u <user> -p <password> -v <path_ficheiros> -t <assinante>

Assinar e enviar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -ae <path_ficheiros> -t <destinatario>

Receber e verificar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -rv <nome_ficheiros> -t <assinante>

Assinar + cifrar + enviar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -ace <path_ficheiros> -t <destinatario>

Nota: neste comando indica-se apenas o nome do ficheiro original. 
O programa constrói automaticamente os nomes dos ficheiros associados (.envelope, .chave.<username> e .assinatura.<assinante>),

Receber + decifrar + verificar:
java mySaude -s <ip>:<porto> -u <user> -p <password> -rdv <nome_ficheiros> -t <assinante>

Nota: neste comando indica-se apenas o nome do ficheiro original. O programa vai buscar automaticamente os ficheiros associados no servidor (.envelope, .chave e .assinatura).


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