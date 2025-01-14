package com.bizfns.services.Serviceimpl;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AES {

    private static final String SECRET_KEY = "bizfnskeyforencryption##==@@%%&&";
    private static final String SALT = "Yml6Zm5z##";

    public  String encrypt(String strToEncrypt) {
        try {
            byte[] saltBytes = SALT.getBytes(StandardCharsets.UTF_8);
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), saltBytes, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedBytesWithIv = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedBytesWithIv, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedBytesWithIv, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedBytesWithIv);
        } catch (Exception e) {
            //System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public  String decrypt(String strToDecrypt) {
        try {
            byte[] encryptedBytesWithIv = Base64.getDecoder().decode(strToDecrypt);
            byte[] saltBytes = SALT.getBytes(StandardCharsets.UTF_8);
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[encryptedBytesWithIv.length - iv.length];

            System.arraycopy(encryptedBytesWithIv, 0, iv, 0, iv.length);
            System.arraycopy(encryptedBytesWithIv, iv.length, encrypted, 0, encrypted.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), saltBytes, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
           // System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}

