#!/bin/bash

# 1. Cleanup
echo "--- Cleaning up old files ---"
# Remove keystore files
cd ~/mySaude/keystore
rm -f keystore.users keystore.user1 keystore.user2 user1.cer user1.cert user2.cer user2.cert

# Remove server files and user directories
cd ~/mySaude/servidor
rm -f mySaude.mac users.txt
rm -rf user1 user2

# 2. Keystore Creation
echo "--- Creating new keystores ---"
cd ~/mySaude/keystore
bash create_keystore.sh user1 user11
bash create_keystore.sh user2 user22

# 3. Server User Registration
echo "--- Registering users on server ---"
# Assuming criarUser is in the bin or src directory 
# and userX.cer was generated in the keystore folder
cd ~/mySaude/src


echo "--- Registering users on server ---"
cd ~/mySaude/src

# O comando <<EOF envia as linhas seguintes como input para o Java
java criarUser user1 medico user11 -f ../keystore/user1.cer

java criarUser user2 utente user22 -f ../keystore/user2.cer

echo "--- Setup Complete! ---"