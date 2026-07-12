# mySaude — Secure Medical Report Management System
(Existe este README também em [Português](README_PT.md))


This repository contains the practical project developed for the **Information Security** (Segurança Informática) course at the **Faculty of Sciences of the University of Lisbon (FCUL)**. The project achieved a final grade of **19/20** and focuses on implementing a secure Java-based client-server system for managing, transferring, and validating electronic medical reports (utilizing eID, digital signatures, and PKI).

---

## 🔒 Technical Context & Features

The core objective of **mySaude** is to guarantee the fundamental pillars of information security — **Confidentiality, Integrity, Authentication, and Non-Repudiation** — in a critical medical communication environment over untrusted networks.

### Key Features:
*   **Secure Client-Server Communication:** Implementation of Java TCP sockets wrapped with cryptographic protection layers.
*   **Strong Authentication via eID & Certificates:** Mutual authentication and validation built on a Public Key Infrastructure (PKI) with X.509 digital certificates.
*   **Identity Management (Keystores):** Secure key repositories (Java `KeyStore`) configured individually for each user and server thread.
*   **Encryption and Digital Signatures:** Comprehensive protection and integrity checks for confidential medical documents (e.g., PDF exams/reports) ensuring non-repudiation during data transmittal.
*   **Server Concurrency:** Multi-threaded architecture (`ServerThread`) capable of securely isolating and handling multiple clients concurrently.

---

## 📁 Repository Structure

```text
├── src/                          # Java project source code
│   ├── mySaude.java              # Client Application
│   ├── mySaudeServer.java        # Main Server Application
│   └── mySaudeServer$ServerThread.class
├── keystore/                     # Security keys and certificates infrastructure
│   ├── create_keystore.sh        # Automated script for key pairs and CSR generation
│   ├── importar_certs.sh         # Script to import and establish mutual trust between entities
│   ├── keystore.user1 / user2    # Users' private cryptographic key stores
│   └── user1.cer / user2.cer     # Exported public digital certificates
├── eu/                           # User local mock directory
│   ├── exame1.pdf / exame2.pdf   # Mock medical reports for testing
│   └── teste.txt                 # Environment validation file
└── bin/                          # Compiled binary classes (.class)
```

---

## 🛠️ Installation & Security Infrastructure Configuration

Before running the applications, you must generate the asymmetric key pairs and configure mutual trust between the entities using the utilities within the `keystore/` directory.

### 1. Generate Cryptographic Keys and Parameters
Run the keystore creation script to initialize the cryptographic identities:
```bash
cd keystore
chmod +x create_keystore.sh
./create_keystore.sh
```

### 2. Establish Mutual Trust (Import Certificates)
To enable the client and server to verify and trust each other's certificates, execute the import tool:
```bash
chmod +x importar_certs.sh
./importar_certs.sh
```

---

## 🚀 How to Run the Project

Once the infrastructure keys are configured, compile and execute the system from the root directory:

### Step 1: Compile the Source Code
```bash
javac src/mySaudeServer.java src/mySaude.java -d bin/
```

### Step 2: Launch the Secure Server
The server will listen for inbound secure socket connections and spin up an isolated execution thread for each session:
```bash
java -cp bin mySaudeServer
```

### Step 3: Run the Client Application
Launch the interactive command-line interface for the user session:
```bash
java -cp bin mySaude
```

---

## 📝 Technologies Used
*   **Core Language:** Java (Java Security API, Java Cryptography Architecture - JCA)
*   **Security & Infrastructure:** Java KeyStore (JKS), Asymmetric Encryption, X.509 Digital Certificates
*   **Automation:** Shell Scripting (Bash) for local PKI orchestration

---
*Project developed by Guilherme Soares as part of the BSc in Information Technology program (FCUL).*
