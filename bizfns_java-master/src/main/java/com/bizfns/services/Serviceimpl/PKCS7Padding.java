package com.bizfns.services.Serviceimpl;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
public class PKCS7Padding {
    private final String key = "0123456789abcdef0123456789abcdef"; // 32 characters for a 16-byte key
    private final String iv = "abcdef0123456789"; // 16 characters for a 16-byte IV

    public void sayan() {
        TextEncryptor encryptor = Encryptors.text(key, iv);

        String plaintext = "123456";
        String ciphertext = encryptor.encrypt(plaintext);

        System.out.println("Ciphertext: " + ciphertext);

        // Decrypt the ciphertext back to the original plaintext
        String decryptedText = encryptor.decrypt(ciphertext);
        System.out.println("Decrypted Text: " + decryptedText);
    }
}
