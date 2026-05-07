package com.defragler.fixiqo.services.enums;

/**
 * Enumeration of supported hashing algorithms used in the Fixiqo application,
 * primarily for securely storing user passwords and other sensitive data.
 *
 * <p>This enum distinguishes between cryptographic hash functions (e.g. SHA-256)
 * and password-specific key derivation functions (e.g. BCrypt).</p>
 *
 * <p><strong>Recommendation:</strong> For password hashing, always prefer {@link #BCrypt}
 * or similar slow, adaptive algorithms (Argon2, PBKDF2, scrypt) over plain SHA-256.
 * SHA-256 is fast and suitable only for data integrity checks, not for passwords.</p>
 */
public enum HashAlgorithm {
    SHA256,
    BCrypt
}
