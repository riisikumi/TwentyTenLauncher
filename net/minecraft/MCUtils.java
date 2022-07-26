package net.minecraft;

import java.io.File;

public class MCUtils {
    public static final String osName = System.getProperty("os.name").toLowerCase();
    private static final String userHome = System.getProperty("user.home", ".");

    public static File getWorkingDirectory() {
        File workingDirectory;
        switch (getPlatform()) {
            case linux:
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            case osx:
                workingDirectory = new File(userHome, "Library/Application Support/minecraft/");
                break;
            case windows:
                String applicationData = System.getenv("APPDATA");
                if (applicationData != null) {
                    workingDirectory = new File(applicationData, ".minecraft/");
                    break;
                }
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            default:
                workingDirectory = new File(userHome, "minecraft/");
                break;
        }
        if (!workingDirectory.exists() && !workingDirectory.mkdirs())
            throw new RuntimeException("The working directory could not be created: " + workingDirectory);

        return workingDirectory;
    }

    public static OS getPlatform() {
        if (osName.contains("mac")) {
            return OS.osx;
        } else if (osName.contains("nix")
                || osName.contains("nux")
                || osName.contains("aix")) {
            return OS.linux;
        } else if (osName.contains("win")) {
            return OS.windows;
        } else {
            throw new RuntimeException("Unknown OS: " + osName);
        }
    }

    public enum OS {
        osx, linux, windows
    }
}