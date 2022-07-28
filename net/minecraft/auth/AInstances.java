package net.minecraft.auth;

import net.minecraft.MCLauncher;
import net.minecraft.auth.yggdrasil.YAuthenticate;

import java.io.Serializable;

public class AInstances implements Serializable {
    private final YAuthenticate yggdrasilAuthenticate;

    public AInstances(YAuthenticate yggdrasilAuthenticate) {
        this.yggdrasilAuthenticate = yggdrasilAuthenticate;
    }

    public void playOnline(String username, String sessionId) {
        try {
            if (username.matches("^\\w+$") && username.length() < 3 || username.length() > 16) {
                username = "Player";
            }
            yggdrasilAuthenticate.getLauncherFrame().setMinecraftLauncher(new MCLauncher());
            yggdrasilAuthenticate.getLauncherFrame().getMinecraftLauncher().customParameters.put("username", username);
            yggdrasilAuthenticate.getLauncherFrame().getMinecraftLauncher().customParameters.put("sessionid", sessionId);
            yggdrasilAuthenticate.getLauncherFrame().getMinecraftLauncher().init();
            yggdrasilAuthenticate.getLauncherFrame().removeAll();
            yggdrasilAuthenticate.getLauncherFrame().add(yggdrasilAuthenticate.getLauncherFrame().getMinecraftLauncher(), "Center");
            yggdrasilAuthenticate.getLauncherFrame().validate();
            yggdrasilAuthenticate.getLauncherFrame().getMinecraftLauncher().start();
            yggdrasilAuthenticate.getLauncherFrame().getAuthPanel().getLogin();
            yggdrasilAuthenticate.getLauncherFrame().setAuthPanel(null);
            yggdrasilAuthenticate.getLauncherFrame().setTitle("Minecraft");
        } catch (Exception e) {
            e.printStackTrace();
            yggdrasilAuthenticate.getLauncherFrame().getAuthPanel().setError(e.toString());
        }
    }

    public static boolean canPlayOffline(String username) {
        MCLauncher minecraftLauncher = new MCLauncher();
        minecraftLauncher.init(username, null);
        return minecraftLauncher.canPlayOffline();
    }
}