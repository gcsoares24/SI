# mySaude — Sistema Seguro de Gestão de Relatórios Médicos

Este repositório contém o projeto prático desenvolvido no âmbito da unidade curricular de **Segurança Informática** na **Faculdade de Ciências da Universidade de Lisboa (FCUL)**. O projeto obteve a classificação de **19 valores** e foca-se na implementação de um sistema cliente-servidor seguro em Java para a gestão, transferência e validação de relatórios médicos eletrónicos (eID, assinaturas digitais e PKI).

---

## 🔒 Contexto Técnico e Funcionalidades

O objetivo do **mySaude** é garantir os pilares fundamentais da segurança da informação — **Confidencialidade, Integridade, Autenticação e Não-Repúdio** — num cenário de comunicação médica crítica através de redes não confiáveis.

### Principais Funcionalidades:
*   **Comunicação Cliente-Servidor Segura:** Implementação de sockets TCP em Java com proteção criptográfica.
*   **Autenticação forte via eID & Certificados:** Autenticação mútua e validação baseada em Infraestrutura de Chaves Públicas (PKI) com certificados digitais X.509.
*   **Gestão de Identidades (Keystores):** Repositórios de chaves seguros (`KeyStore` Java) configurados individualmente por utilizador/servidor.
*   **Criptografia e Assinatura Digital:** Proteção e integridade de documentos médicos sigilosos (exames, relatórios em PDF) e garantias de não-repúdio nas transações.
*   **Concorrência no Servidor:** Processamento multi-threaded (`ServerThread`) para suportar múltiplos clientes em simultâneo de forma isolada e segura.

---

## 📁 Estrutura do Repositório

```text
├── src/                          # Código-fonte Java do projeto
│   ├── mySaude.java              # Aplicação Cliente
│   ├── mySaudeServer.java        # Aplicação Servidor Principal
│   └── mySaudeServer$ServerThread.class
├── keystore/                     # Infraestrutura de chaves e certificados
│   ├── create_keystore.sh        # Script automatizado para geração de chaves e CSR
│   ├── importar_certs.sh         # Script para importação e confiança mútua de certificados
│   ├── keystore.user1 / user2    # Armazéns de chaves privados dos utilizadores
│   └── user1.cer / user2.cer     # Certificados digitais públicos exportados
├── eu/                           # Diretório local do utilizador (Simulação)
│   ├── exame1.pdf / exame2.pdf   # Relatórios médicos de teste
│   └── teste.txt                 # Ficheiro de validação de ambiente
└── bin/                          # Ficheiros binários compilados (.class)
```

---

## 🛠️ Instalação e Configuração da Infraestrutura de Segurança

Antes de executar as aplicações, é necessário gerar o par de chaves assimétricas e configurar a confiança entre as entidades utilizando os utilitários da pasta `keystore/`.

### 1. Gerar Chaves e Parâmetros Criptográficos
Execute o script de criação das keystores para inicializar as identidades criptográficas:
```bash
cd keystore
chmod +x create_keystore.sh
./create_keystore.sh
```

### 2. Configurar a Confiança Mútua (Importar Certificados)
Para que o cliente e o servidor confiem nos certificados um do outro, execute o script de importação:
```bash
chmod +x importar_certs.sh
./importar_certs.sh
```

---

## 🚀 Como Executar o Projeto

Com as chaves configuradas, compile e corra a aplicação a partir da raiz do projeto:

### Passo 1: Compilar o Código
```bash
javac src/mySaudeServer.java src/mySaude.java -d bin/
```

### Passo 2: Iniciar o Servidor Seguro
O servidor ficará à escuta de ligações seguras inbound, gerando uma thread isolada para cada sessão:
```bash
java -cp bin mySaudeServer
```

### Passo 3: Executar o Cliente
Inicie a consola do utilizador para interagir com o sistema de gestão de exames:
```bash
java -cp bin mySaude
```

---

## 📝 Tecnologias Utilizadas
*   **Linguagem Principal:** Java (Java Security API, Java Cryptography Architecture - JCA)
*   **Segurança e Infraestrutura:** Java KeyStore (JKS), Criptografia Assimétrica, Certificados Digitais X.509
*   **Automação:** Shell Scripting (Bash) para gestão de PKI local

---
*Projeto desenvolvido por Guilherme Soares no âmbito da Licenciatura em Tecnologias de Informação (FCUL).*
