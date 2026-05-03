
### CRIAR USERS
java criarUser user1 medico user11 -f user1.cer
java criarUser user2 utente user22 -f user2.cer

**depois NAO vai ter os certs importados, mas é ok!**
### ENVIAR E RECEBER
java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -e "/mnt/c/Users/guiac/Downloads/FCUL/SI/SI/proj1_enunciado.pdf" "/home/guimbreon/mySaude/eu/exame1.pdf"

Eliminar o proj1_enunciado.pdf do /eu se já existir.

java mySaude -s localhost:12345 -u user2 -p user22 -r proj1_enunciado.pdf exame1.pdf
### CIFRAR E DECIFRAR
java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -c "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"


java mySaude -s localhost:12345 -u user2 -p user22 -d "/home/guimbreon/mySaude/eu/exame1.pdf.cifrado" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf.cifrado"



### CIFRAR e ENIVAR & RECEBER e DECIFRAR
java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -ce "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"


java mySaude -s localhost:12345 -u user2 -p user22 -rd exame1.pdf.cifrado exame1.pdf.chave.user2 proj1_enunciado.pdf.cifrado proj1_enunciado.pdf.chave.user2

### ASSINAR e VALIDAR
java mySaude -u user1 -p user11 -a "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"


java mySaude -s localhost:12345 -u user2 -p user22 -t user1 -v "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"


### ASSINAR e ENVIAR & VALIDAR e RECEBER
java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -ae "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"


java mySaude -s localhost:12345 -u user2 -p user22 -t user1 -rv  exame1.pdf.assinado exame1.pdf.assinatura.user1 proj1_enunciado.pdf.assinado proj1_enunciado.pdf.assinatura.user1


### ASSINAR, CIFRAR e ENVIAR & RECEBER, DECIFRAR e VALIDAR
java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -ace "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"


java mySaude -s localhost:12345 -u user2 -p user22 -t user1 -rdv exame1.pdf proj1_enunciado.pdf

EM -RV e -RD devia de só meter tipo proj1_enunciado.pdf, sem ter de meter tudo.

## Notes Project

- [ ] Verificar novamente o erro do 1º projeto, de nao conseguir rodar varios ficheiros na parte do .envelope
