package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.references.UserRole;

/**
 * Represents a user (employee or administrator) of the Fixiqo service center system.
 *
 * <p>This is an immutable value object that holds authentication and authorization information.
 * All fields are final and set through the constructor — the class is fully immutable.</p>
 *
 * <p>Important security note: This class stores the password in plain text. 
 * In a real production system, passwords should never be stored unhashed. 
 * Consider using this class only for in-memory representation and mapping to a secure DTO or entity 
 * that stores hashed passwords (e.g. BCrypt, Argon2).</p>
 */
@Table(name = "Users")
@Indexes({
      @Index(name = "idx_users_username", columns = {"username"}, unique = true),
      @Index(name = "idx_users_email", columns = {"email"}, unique = true)
})
@UniqueConstraints({
      @UniqueConstraint(columns = {"username"}),
      @UniqueConstraint(columns = {"email"})
})
public class User {
    @Id(autoIncrement = true)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "role_id", nullable = false)
    @ForeignKey(target = UserRole.class, column = "id")
    private Integer roleId;

    @Column(name = "avatar", nullable = true)
    private byte[] avatar;
    
    @Column(name = "is_active", nullable = false, defaultValue = "true")
    private Boolean isActive;

    public User() { }
    
    /**
     * Creates a new User instance.
     *
     * @param username     unique login name used for authentication
     * @param passwordHash user's password (hashed password)
     * @param email        user's email address (used for notifications, recovery, etc.)
     * @param roleId       user's role determining access rights in the system
     * @param avatar       user's avatar image
     * @param isActive     user's activity status (safe delete)
     */
    public User(String username, String passwordHash, String email, Integer roleId, byte[] avatar, Boolean isActive) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.roleId = roleId;
        this.avatar = avatar;
        this.isActive = isActive;
    }

    public long getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRole() {
        return roleId;
    }
    public void setRole(Integer roleId) {
        this.roleId = roleId;
    }

    public byte[] getAvatar() {
        return avatar;
    }
    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
