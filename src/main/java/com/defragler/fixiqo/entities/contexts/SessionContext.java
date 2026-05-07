package com.defragler.fixiqo.entities.contexts;

import com.defragler.fixiqo.services.enums.*;
import com.fasterxml.jackson.annotation.*;

/**
 * Represents persisted user session data.
 *
 * <p>This object is stored in a JSON file and used for restoring
 * authenticated user state between application launches.</p>
 */
public class SessionContext {

    @JsonProperty("userId")
    private long userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("createdAt")
    private long createdAt;

    @JsonProperty("theme")
    private ThemeMode theme;

    public SessionContext() {}

    public SessionContext(long userId, String username, long createdAt, ThemeMode theme) {
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
        this.theme = theme;
    }

    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public long getCreatedAt() { return createdAt; }

    public ThemeMode getTheme() {
        return theme == null ? ThemeMode.AUTO : theme;
    }

    public void setTheme(ThemeMode theme) {
        this.theme = theme;
    }
}