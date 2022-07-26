package net.minecraft.launcher;

import net.minecraft.auth.yggdrasil.YAuthenticate;

import java.util.ArrayList;

public class Launcher {
    public static void main(String[] args) {
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 511L) {
            try {
                String jarPath = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();

                ArrayList<String> argument = new ArrayList<>();
                argument.add("java");
                argument.add("-Xms512M");
                argument.add("-Dsun.java2d.d3d=false");
                argument.add("-Dsun.java2d.opengl=false");
                argument.add("-Dsun.java2d.noddraw=true");
                argument.add("-Dsun.java2d.pmoffscreen=false");
                argument.add("-cp");
                argument.add(jarPath);
                argument.add("net.minecraft.launcher.LauncherFrame");

                ProcessBuilder pb = new ProcessBuilder(argument);
                Process process = pb.start();
                if (process.waitFor() != 0) {
                    throw new Exception("Process exited with error code " + process.exitValue());
                } else {
                    System.exit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                YAuthenticate.main(args);
            }
        } else {
            YAuthenticate.main(args);
        }
    }
}
