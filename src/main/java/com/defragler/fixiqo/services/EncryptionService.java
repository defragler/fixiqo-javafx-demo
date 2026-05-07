package com.defragler.fixiqo.services;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.services.enums.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.io.*;
import java.util.*;
import java.nio.charset.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;

import org.mindrot.jbcrypt.*;

/**
 * Implementation of the {@link IEncryptionService} interface providing secure hashing,
 * password verification, and symmetric AES encryption/decryption.
 *
 * <p>Supported features:
 * <ul>
 *     <li>Hashing: SHA-256 (general purpose) and BCrypt (passwords)</li>
 *     <li>Verification: constant-time comparison for BCrypt</li>
 *     <li>Encryption/Decryption: AES/CBC/PKCS5Padding with PBKDF2-derived keys</li>
 * </ul></p>
 *
 * <p><strong>Security considerations:</strong>
 * <ul>
 *     <li>BCrypt is used exclusively for password storage/verification (recommended)</li>
 *     <li>AES uses CBC mode with PKCS5 padding — consider migrating to AES/GCM for authenticated encryption</li>
 *     <li>Key derivation via PBKDF2 (10,000 iterations) — salt is static (should be unique per key in production)</li>
 *     <li>IV is prepended to ciphertext (standard practice)</li>
 *     <li>All string outputs/inputs use UTF-8 and Base64 for binary-safe transport</li>
 * </ul></p>
 *
 * <p><strong>Important note:</strong> This implementation uses a static salt for PBKDF2 — in production
 * use per-user/per-key unique salts and store them alongside encrypted data.</p>
 */
public class EncryptionService implements IEncryptionService {

    /**
     * Hashes a string using the specified algorithm.
     *
     * <p>For {@link HashAlgorithm#BCrypt} returns BCrypt-formatted hash string.
     * For {@link HashAlgorithm#SHA256} returns Base64-encoded SHA-256 digest.</p>
     *
     * @param data      input string to hash (must not be null or empty)
     * @param algorithm hashing algorithm to use
     * @return hashed value as string (BCrypt format or Base64)
     * @throws IllegalArgumentException if data is empty
     * @throws ServiceException      if algorithm is not supported or hashing fails
     */
    @Override
    public String hashData(String data, HashAlgorithm algorithm) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty.");
        }
        if (algorithm == HashAlgorithm.BCrypt) {
            return BCrypt.hashpw(data, BCrypt.gensalt());
        }
        return Base64.getEncoder().encodeToString(hashData(data.getBytes(StandardCharsets.UTF_8), algorithm));
    }

    /**
     * Hashes binary data using the specified algorithm.
     *
     * @param data      input bytes to hash
     * @param algorithm hashing algorithm
     * @return raw hash bytes
     * @throws IllegalArgumentException if data is empty
     * @throws ServiceException      if algorithm not supported or provider error
     */
    @Override
    public byte[] hashData(byte[] data, HashAlgorithm algorithm) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be empty.");
        }
        switch (algorithm) {
            case SHA256:
                try {
                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    return sha256.digest(data);
                } catch (NoSuchAlgorithmException e) {
                    throw new ServiceException(ExceptionLevel.ERROR,"SHA-256 algorithm is not available.", e);
                }
            case BCrypt:
                String dataStr = new String(data, StandardCharsets.UTF_8);
                String hashed = BCrypt.hashpw(dataStr, BCrypt.gensalt());
                return hashed.getBytes(StandardCharsets.UTF_8);
            default:
                throw new ServiceException(ExceptionLevel.ERROR,"Hash algorithm " + algorithm + " is not supported.");
        }
    }

    /**
     * Verifies that the provided plain text matches the stored hash.
     *
     * @param data       plain text to verify
     * @param hashedData stored hash (BCrypt format or Base64 for SHA256)
     * @param algorithm  algorithm used to create the hash
     * @return {@code true} if verification succeeds, {@code false} otherwise
     */
    @Override
    public boolean verifyData(String data, String hashedData, HashAlgorithm algorithm) {
        if (data == null || data.isEmpty() || hashedData == null || hashedData.isEmpty()) {
            return false;
        }
        if (algorithm == HashAlgorithm.BCrypt) {
            return BCrypt.checkpw(data, hashedData);
        }
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] hashedBytes = Base64.getDecoder().decode(hashedData);
        return verifyData(dataBytes, hashedBytes, algorithm);
    }

    /**
     * Verifies binary data against a stored hash.
     *
     * @param data       original binary data
     * @param hashedData stored hash bytes
     * @param algorithm  hashing algorithm
     * @return {@code true} if match, {@code false} otherwise
     */
    @Override
    public boolean verifyData(byte[] data, byte[] hashedData, HashAlgorithm algorithm) {
        if (data == null || data.length == 0 || hashedData == null || hashedData.length == 0) {
            return false;
        }
        switch (algorithm) {
            case SHA256:
                byte[] computedHash = hashData(data, algorithm);
                return Arrays.equals(hashedData, computedHash);
            case BCrypt:
                String dataStr = new String(data, StandardCharsets.UTF_8);
                String hashedStr = new String(hashedData, StandardCharsets.UTF_8);
                return BCrypt.checkpw(dataStr, hashedStr);
            default:
                throw new ServiceException(ExceptionLevel.ERROR,"Hash algorithm " + algorithm + " is not supported.");
        }
    }

    /**
     * Encrypts plain text string using AES/CBC/PKCS5Padding.
     * IV is prepended to ciphertext; result is Base64-encoded.
     *
     * @param data      plain text to encrypt
     * @param key       encryption key (string)
     * @param algorithm encryption algorithm (only AES supported)
     * @return Base64-encoded IV + ciphertext
     * @throws IllegalArgumentException if data or key is empty
     * @throws ServiceException      if encryption fails
     */
    @Override
    public String encryptData(String data, String key, EncryptionAlgorithm algorithm) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be empty.");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be empty.");
        }
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encryptData(dataBytes, keyBytes, algorithm);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Encrypts binary data using AES/CBC/PKCS5Padding.
     * IV (16 bytes) is prepended to the ciphertext.
     *
     * @param data      binary plaintext
     * @param key       binary key
     * @param algorithm encryption algorithm
     * @return IV + encrypted bytes
     */
    @Override
    public byte[] encryptData(byte[] data, byte[] key, EncryptionAlgorithm algorithm) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be empty.");
        }
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be empty.");
        }
        if (algorithm == EncryptionAlgorithm.AES) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                byte[] derivedKey = deriveKey(key, 32); // 256-bit key
                SecretKey secretKey = new SecretKeySpec(derivedKey, "AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] iv = cipher.getIV();
                byte[] cipherText = cipher.doFinal(data);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(iv);
                outputStream.write(cipherText);
                return outputStream.toByteArray();
            } catch (Exception e) {
                throw new ServiceException(ExceptionLevel.ERROR,"Failed to encrypt data.", e);
            }
        }
        throw new ServiceException(ExceptionLevel.ERROR,"Encryption algorithm " + algorithm + " is not supported.");
    }

    /**
     * Decrypts Base64-encoded AES-encrypted string.
     *
     * @param encryptedData Base64 string (IV + ciphertext)
     * @param key           decryption key
     * @param algorithm     decryption algorithm
     * @return decrypted plain text
     */
    @Override
    public String decryptData(String encryptedData, String key, EncryptionAlgorithm algorithm) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new IllegalArgumentException("Encrypted data cannot be empty.");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be empty.");
        }
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] decrypted = decryptData(encryptedBytes, keyBytes, algorithm);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Decrypts AES-encrypted binary data (IV prepended).
     *
     * @param encryptedData IV + ciphertext bytes
     * @param key           binary key
     * @param algorithm     algorithm
     * @return decrypted bytes
     */
    @Override
    public byte[] decryptData(byte[] encryptedData, byte[] key, EncryptionAlgorithm algorithm) {
        if (encryptedData == null || encryptedData.length == 0) {
            throw new IllegalArgumentException("Encrypted data cannot be empty.");
        }
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be empty.");
        }
        if (algorithm == EncryptionAlgorithm.AES) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                byte[] derivedKey = deriveKey(key, 32); // 256-bit key
                SecretKey secretKey = new SecretKeySpec(derivedKey, "AES");
                byte[] iv = new byte[16];
                System.arraycopy(encryptedData, 0, iv, 0, iv.length);
                AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
                byte[] cipherText = new byte[encryptedData.length - 16];
                System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
                return cipher.doFinal(cipherText);
            } catch (Exception e) {
                throw new ServiceException(ExceptionLevel.ERROR,"Failed to decrypt data.", e);
            }
        }
        throw new ServiceException(ExceptionLevel.ERROR,"Encryption algorithm " + algorithm + " is not supported.");
    }

    /**
     * Derives a fixed-length key from input key material using PBKDF2 with HMAC-SHA256.
     *
     * <p>Uses static salt and 10,000 iterations — in production use unique per-key salts.</p>
     *
     * @param key     input key bytes
     * @param keySize desired key length in bytes (e.g. 32 for AES-256)
     * @return derived key bytes
     * @throws ServiceException if derivation fails
     */
    private byte[] deriveKey(byte[] key, int keySize) {
        try {
            byte[] salt = new byte[16];
            KeySpec keySpec = new PBEKeySpec(new String(key, StandardCharsets.UTF_8).toCharArray(), salt, 10000, keySize * 8);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (Exception e) {
            throw new ServiceException(ExceptionLevel.ERROR,"Failed to derive key.", e);
        }
    }
}
