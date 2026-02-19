package org.kleypas.muc.io

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class CipherService {
    private static final String ALGO = "AES/GCM/NoPadding"
    private static final int TAG_BIT_LENGTH = 128
    private static final int IV_SIZE = 12

    static String encrypt(String plainText, Object key) {
        byte[] iv = new byte[IV_SIZE]
        new SecureRandom().nextBytes(iv)

        byte[] rawKey = (key instanceof String) ? key.getBytes("UTF-8") : (byte[]) key

        // GRIND THE KEY: Hash the user string to get exactly 32 bytes
        byte[] keyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(rawKey)
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES")

        Cipher cipher = Cipher.getInstance(ALGO)
        // Use keySpec instead of raw key.bytes
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_BIT_LENGTH, iv))

        byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"))
        byte[] combined = (iv as List) + (cipherText as List)
        return combined.encodeBase64().toString()
    }

    static String decrypt(String base64Payload, Object key) {
        byte[] decoded = base64Payload.decodeBase64()
        byte[] iv = decoded[0..IV_SIZE-1]
        byte[] cipherText = decoded[IV_SIZE..-1]

        byte[] rawKey = (key instanceof String) ? key.getBytes("UTF-8") : (byte[]) key
        // GRIND THE KEY: Same hash logic for decryption
        byte[] keyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(rawKey)
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES")

        Cipher cipher = Cipher.getInstance(ALGO)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_BIT_LENGTH, iv))
        return new String(cipher.doFinal(cipherText), "UTF-8")
    }
}