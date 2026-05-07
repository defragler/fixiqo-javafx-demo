package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.services.enums.*;

/**
 * Interface defining cryptographic operations for hashing, symmetric encryption and decryption
 * used throughout the Fixiqo application.
 *
 * <p>This service provides:
 * <ul>
 *     <li>Secure hashing (for passwords, tokens, integrity checks)</li>
 *     <li>Symmetric encryption/decryption (for sensitive data protection)</li>
 * </ul></p>
 *
 * <p><strong>Security guidelines for implementations:</strong>
 * <ul>
 *     <li>Password hashing: MUST use {@link HashAlgorithm#BCrypt} (or Argon2/PBKDF2/scrypt)</li>
 *     <li>General hashing: {@link HashAlgorithm#SHA256} is acceptable for non-password data</li>
 *     <li>Encryption: {@link EncryptionAlgorithm#AES} should use secure mode (GCM preferred over CBC)</li>
 *     <li>Keys: should be securely generated/stored (never hard-coded)</li>
 *     <li>Output: encrypted data usually Base64-encoded for string representation</li>
 * </ul></p>
 *
 * <p>All methods have overloads with default algorithms for convenience.</p>
 */
public interface IEncryptionService {
    /**
     * Computes a secure hash of the input data using the specified algorithm.
     *
     * <p>For passwords use {@link HashAlgorithm#BCrypt}. For other purposes (checksums, tokens)
     * {@link HashAlgorithm#SHA256} is usually sufficient.</p>
     *
     * @param data      plain text data to hash
     * @param algorithm hashing algorithm to use
     * @return hex-encoded or Base64-encoded hash string (implementation-defined format)
     */
    String hashData(String data, HashAlgorithm algorithm);

    /**
     * Convenience overload — hashes data using default algorithm ({@link HashAlgorithm#SHA256}).
     *
     * @param data plain text data to hash
     * @return hashed string
     */
    default String hashData(String data) {
        return hashData(data, HashAlgorithm.SHA256);
    }

    /**
     * Computes hash of binary data using specified algorithm.
     *
     * @param data      binary input
     * @param algorithm hashing algorithm
     * @return raw hash bytes
     */
    byte[] hashData(byte[] data, HashAlgorithm algorithm);

    /**
     * Convenience overload — hashes binary data with default algorithm.
     */
    default byte[] hashData(byte[] data) {
        return hashData(data, HashAlgorithm.SHA256);
    }

    /**
     * Verifies that the provided data matches the given hash.
     *
     * <p>Important: for {@link HashAlgorithm#BCrypt} this method should use constant-time comparison.</p>
     *
     * @param data       original plain text
     * @param hashedData previously computed hash
     * @param algorithm  algorithm used to create the hash
     * @return {@code true} if data matches the hash, {@code false} otherwise
     */
    boolean verifyData(String data, String hashedData, HashAlgorithm algorithm);

    /**
     * Convenience overload — verifies using default algorithm.
     */
    default boolean verifyData(String data, String hashedData) {
        return verifyData(data, hashedData, HashAlgorithm.SHA256);
    }

    /**
     * Verifies binary data against a binary hash.
     */
    boolean verifyData(byte[] data, byte[] hashedData, HashAlgorithm algorithm);

    /**
     * Convenience overload for binary verification.
     */
    default boolean verifyData(byte[] data, byte[] hashedData) {
        return verifyData(data, hashedData, HashAlgorithm.SHA256);
    }

    /**
     * Encrypts string data using the specified symmetric algorithm and key.
     *
     * <p>Output is usually Base64-encoded for safe string storage/transmission.</p>
     *
     * @param data      plain text to encrypt
     * @param key       encryption key (string representation)
     * @param algorithm encryption algorithm (currently only {@link EncryptionAlgorithm#AES})
     * @return Base64-encoded encrypted string
     */
    String encryptData(String data, String key, EncryptionAlgorithm algorithm);

    /**
     * Convenience overload — encrypts using default algorithm ({@link EncryptionAlgorithm#AES}).
     */
    default String encryptData(String data, String key) {
        return encryptData(data, key, EncryptionAlgorithm.AES);
    }

    /**
     * Encrypts binary data.
     *
     * @param data      binary plaintext
     * @param key       binary key
     * @param algorithm encryption algorithm
     * @return encrypted bytes
     */
    byte[] encryptData(byte[] data, byte[] key, EncryptionAlgorithm algorithm);

    /**
     * Convenience overload for binary encryption.
     */
    default byte[] encryptData(byte[] data, byte[] key) {
        return encryptData(data, key, EncryptionAlgorithm.AES);
    }

    /**
     * Decrypts Base64-encoded encrypted string back to plain text.
     *
     * @param encryptedData Base64-encoded ciphertext
     * @param key           decryption key (must match encryption key)
     * @param algorithm     algorithm used during encryption
     * @return decrypted plain text
     * @throws RuntimeException if decryption fails (wrong key, corrupted data, etc.)
     */
    String decryptData(String encryptedData, String key, EncryptionAlgorithm algorithm);

    /**
     * Convenience overload — decrypts using default algorithm.
     */
    default String decryptData(String encryptedData, String key) {
        return decryptData(encryptedData, key, EncryptionAlgorithm.AES);
    }

    /**
     * Decrypts binary encrypted data.
     */
    byte[] decryptData(byte[] encryptedData, byte[] key, EncryptionAlgorithm algorithm);

    /**
     * Convenience overload for binary decryption.
     */
    default byte[] decryptData(byte[] encryptedData, byte[] key) {
        return decryptData(encryptedData, key, EncryptionAlgorithm.AES);
    }
}
