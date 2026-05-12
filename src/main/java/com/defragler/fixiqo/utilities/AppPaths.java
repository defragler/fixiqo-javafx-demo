package com.defragler.fixiqo.utilities;

import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;

import java.nio.file.*;

public final class AppPaths {

    private AppPaths() {}

    public static Path getBaseDir() {

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return Paths.get(System.getenv("APPDATA"), "Fixiqo");

        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"),
                  "Library", "Application Support", "Fixiqo");

        } else {
            return Paths.get(System.getProperty("user.home"), ".fixiqo");
        }
    }

    public static Path ensureDir(Path path) {
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            throw new UtilityException(ExceptionLevel.ERROR, "Cannot create directory: " + path, e);
        }
        return path;
    }

    public static Path dbPath() {
        return ensureDir(getBaseDir()).resolve("Database.db");
    }

    public static Path sessionPath() {
        return ensureDir(getBaseDir()).resolve("Session.json");
    }

    public static Path logPath() {
        return ensureDir(getBaseDir()).resolve("logs.log");
    }
}