#!/bin/bash
# uso: ./import_cert.sh user_from user_to password_to

FROM_USER="$1"
TO_USER="$2"
TO_PASSWORD="$3"

if [ -z "$FROM_USER" ] || [ -z "$TO_USER" ] || [ -z "$TO_PASSWORD" ]; then
    echo "Usage: $0 <from_user> <to_user> <to_user_password>"
    exit 1
fi

CERT_FILE="$FROM_USER.cer"
KEYSTORE_FILE="keystore.$TO_USER"

if [ ! -f "$CERT_FILE" ]; then
    echo "Erro: certificado $CERT_FILE não existe"
    exit 1
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Erro: keystore $KEYSTORE_FILE não existe"
    exit 1
fi

# Import certificate
keytool -importcert \
    -alias "$FROM_USER" \
    -file "$CERT_FILE" \
    -keystore "$KEYSTORE_FILE" \
    -storepass "$TO_PASSWORD" \
    -noprompt

echo "Certificado de $FROM_USER importado na keystore de $TO_USER"