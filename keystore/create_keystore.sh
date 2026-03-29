#!/bin/bash
# rodem isto assim: bash ./create_keystore.sh username password

USERNAME="$1"
PASSWORD="$2"

if [ -z "$USERNAME" ] || [ -z "$PASSWORD" ]; then
    echo "Usage: $0 <username> <password>"
    exit 1
fi

KEYSTORE_FILE="keystore.$USERNAME"
CERT_FILE="$USERNAME.cer"

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

# Export certificate
keytool -exportcert \
    -alias "$USERNAME" \
    -keystore "$KEYSTORE_FILE" \
    -file "$CERT_FILE" \
    -storepass "$PASSWORD" \
    -rfc

echo "Keystore criada: $KEYSTORE_FILE"
echo "Certificado exportado: $CERT_FILE"