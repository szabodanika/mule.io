package common;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSA {

    private static int keySize = 2018;
    private static KeyPair keyPair;
    private static PublicKey publicKey;

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair;
    }

    public byte[] encrypt(byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());

        return cipher.doFinal(message);
    }

    byte[] encrypt(PublicKey publicKey, byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message);
    }

    public byte[] decrypt(byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

        return cipher.doFinal(encrypted);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setKeyPair(KeyPair keyPair) {
        RSA.keyPair = keyPair;
        setPublicKey(keyPair.getPublic());
    }

    public static PublicKey bytesToPublicKey(byte[] bytes) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bytes);
        return keyFactory.generatePublic(encodedKeySpec);
    }

    public byte[] publicKeyToBytes(PublicKey key) {
        return key.getEncoded();
    }

    public void setPublicKey(PublicKey publicKey) {
        RSA.publicKey = publicKey;
    }
}
