# README - Projeto mySaude (Trabalho 2)

## 1. Identificação do Grupo

- **Curso:** Licenciatura em Tecnologias da Informação (LTI) - FCUL
    
- **Disciplina:** Segurança Informática, 2025/2026
    
- **Grupo:** 7
    
- **Elementos:**
    
    - Guilherme Soares (62372)
        
    - Vitória Correia
        
    - Duarte Soares
        

---

## 2. Preparação e Estrutura

> **IMPORTANTE:** Os caminhos (paths) para os ficheiros no sistema local têm de ser completos.

Devem existir as seguintes pastas no ambiente de execução:

- `../servidor/` - Ficheiros armazenados no servidor.
    
- `../eu/` - Ficheiros locais do utilizador.
    
- `../keystore/` - Localização das chaves (ex: `keystore.user1`).
    

---

## 3. Como Executar

## 0. Passwords e Configs
- Por simplificacao, todas as palavras passes(ex: keystore.server, keystore.users,  .mac), foram setadas como 123456.
- O projeto tem de ser executado apartir da /src, e com o projeto no /home.
### 1. Compilação

Bash

```
javac *.java
```

### 2. Iniciar o Servidor

O servidor utiliza o porto 12345 e solicita passwords para o MAC 

Bash

```
java mySaudeServer 12345
```

### 3. Criar Utilizadores

Registo de utilizadores e importação de certificados para a `keystore.users` do servidor.

Bash

```
java criarUser user1 medico user11 -f user1.cer
java criarUser user2 utente user22 -f user2.cer
```

---

## 4. Exemplos de Comandos (Casos de Teste)

### Enviar e Receber (Simples)

- **Enviar:** `java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -e "/home/guimbreon/mySaude/proj1_enunciado.pdf" "/home/guimbreon/mySaude/eu/exame1.pdf"`
    
- **Receber:** `java mySaude -s localhost:12345 -u user2 -p user22 -r proj1_enunciado.pdf exame1.pdf`
    

### Cifrar e Decifrar (Local/Servidor)

- **Cifrar e Enviar:** `java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -ce "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"`
    
- **Receber e Decifrar:** `java mySaude -s localhost:12345 -u user2 -p user22 -rd exame1.pdf.cifrado exame1.pdf.chave.user2 proj1_enunciado.pdf.cifrado proj1_enunciado.pdf.chave.user2`
    

### Assinar e Validar

- **Assinar e Enviar:** `java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -ae "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"`
    
- **Receber e Validar:** `java mySaude -s localhost:12345 -u user2 -p user22 -t user1 -rv exame1.pdf.assinado exame1.pdf.assinatura.user1 proj1_enunciado.pdf.assinado proj1_enunciado.pdf.assinatura.user1`
    

### Operação Completa (Assinar + Cifrar + Enviar)

- **Enviar:** `java mySaude -s localhost:12345 -u user1 -p user11 -t user2 -ace "/home/guimbreon/mySaude/eu/exame1.pdf" "/home/guimbreon/mySaude/eu/proj1_enunciado.pdf"`
    
- **Receber:** `java mySaude -s localhost:12345 -u user2 -p user22 -t user1 -rdv exame1.pdf proj1_enunciado.pdf`
    

---
