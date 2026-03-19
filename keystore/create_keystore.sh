#!/bin/bash
# rodem isto assim: ./create_keystore.sh username password
# só para facilitar e ser auto
USERNAME="$1"
PASSWORD="$2"

if [ -z "$USERNAME" ] || [ -z "$PASSWORD" ]; then
    echo "Usage: $0 <username> <password>"
    exit 1
fi

KEYSTORE_FILE="keystore.$USERNAME"

# Generate keystore automatically
keytool -genkeypair \
    -alias "$USERNAME" \
    -keyalg RSA \
    -keysize 2048 \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$PASSWORD" \
    -keypass "$PASSWORD" <<EOF
$USERNAME
Seguranca Informatica
FCUL
Lisbon
Lisbon
PT
yes
EOF