package com.defragler.fixiqo.services.enums;

/**
 * Enumeration of supported encryption algorithms used in the Fixiqo application.
 *
 * <p>Currently only AES is supported, but the enum structure allows easy future extension
 * (e.g., AES-256-GCM, ChaCha20-Poly1305, etc.) without breaking existing code.</p>
 *
 * <p>This enum is typically used when configuring encryption services, key generation,
 * or selecting the algorithm for symmetric encryption of sensitive data (e.g. passwords,
 * tokens, or client information before storage).</p>
 */
public enum EncryptionAlgorithm {
    AES
}
