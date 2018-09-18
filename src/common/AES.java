package common;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AES {

    private SecretKeySpec secretKey;
    private byte[] key;
    private final int keySize = 16, ivSize = 16;
    private IvParameterSpec iv;

    public byte[] init(){
        this.key = ByteUtils.generateRandom(keySize);
        iv = new IvParameterSpec(ByteUtils.generateRandom(ivSize));
        return key;
    }

    public void setKey(byte[] key) throws NoSuchAlgorithmException {
        MessageDigest messageDigest  = MessageDigest.getInstance("SHA-1");
        this.key = messageDigest.digest(key);
        this.key = Arrays.copyOf(key, 16);
        secretKey = new SecretKeySpec(key, "AES");
    }

    public byte[] encrypt(byte[] bytes) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(bytes);
            return encrypted;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt(byte[] bytes) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(bytes);
            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }

    public byte[] getKey() {
        return key;
    }

    public SecretKeySpec getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKeySpec secretKey) {
        this.secretKey = secretKey;
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    public void setIv(IvParameterSpec iv) {
        this.iv = iv;
    }
}