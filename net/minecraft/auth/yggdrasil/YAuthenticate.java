package net.minecraft.auth.yggdrasil;

import net.minecraft.MCLauncher;
import net.minecraft.auth.AFrame;
import net.minecraft.launcher.LauncherFrame;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

public class YAuthenticate implements Serializable {
    private final YParameters yParameters = new YParameters();
    private final LauncherFrame launcherFrame;
    protected String authenticateUrl = "https://authserver.mojang.com/authenticate";
    public String sessionId;
    public AFrame aFrame;
    public MCLauncher mcLauncher;

    public YAuthenticate(LauncherFrame launcherFrame) {
        this.launcherFrame = launcherFrame;
        this.aFrame = new AFrame(this);
    }

    public static void main(String[] args) {
        LauncherFrame launcherFrame = new LauncherFrame();
        launcherFrame.setVisible(true);
    }

    public void playOffline(String username) {
        try {
            if (username.matches("^\\w+$") && username.length() < 3 || username.length() > 16) {
                username = "Player";
            }
            this.mcLauncher = new MCLauncher();
            this.mcLauncher.params.put("username", username);
            this.mcLauncher.init();
            this.launcherFrame.removeAll();
            this.launcherFrame.add(this.mcLauncher, "Center");
            this.launcherFrame.validate();
            this.mcLauncher.start();
            this.aFrame.getLastLogin();
            this.aFrame = null;
            this.launcherFrame.setTitle("Minecraft");
        } catch (Exception e) {
            e.printStackTrace();
            this.aFrame.setError(e.toString());
        }
    }

    public void playOnline(String username, String sessionId) {
        this.mcLauncher = new MCLauncher();
        this.mcLauncher.params.put("username", username);
        this.mcLauncher.params.put("sessionid", sessionId);
        this.mcLauncher.init();
        this.launcherFrame.removeAll();
        this.launcherFrame.add(this.mcLauncher, "Center");
        this.launcherFrame.validate();
        this.mcLauncher.start();
        this.aFrame.getLastLogin();
        this.aFrame = null;
        this.launcherFrame.setTitle("Minecraft");
    }

    public void login(String username, String password) {
        if (!checkConnection()) {
            this.launcherFrame.showError("Can't connect to minecraft.net");
            this.launcherFrame.showOffline();
        } else {
            try {
                JSONObject jsonParameters = yParameters.setAuthenticateParams(username, password);

                JSONObject jsonResponse = YUtils.excutePost(authenticateUrl, String.valueOf(jsonParameters));
                if (!jsonResponse.has("errorMessage")) {
                    if (jsonResponse.getJSONArray("availableProfiles").length() == 0) {
                        this.playOffline(username);
                    } else {
                        username = jsonResponse.getJSONObject("selectedProfile").getString("name");
                        this.sessionId = jsonResponse.getString("clientToken") + ":"
                                + jsonResponse.getString("accessToken") + ":"
                                + jsonResponse.getJSONObject("selectedProfile").getString("id");

                        System.out.println("Username is '" + username + "'");
                        this.playOnline(username, sessionId);
                    }
                } else {
                    switch (jsonResponse.getString("errorMessage")) {
                        case "Forbidden":
                            if (username.matches("^\\w+$") && username.length() > 2 && username.length() < 17) {
                                this.sessionId = "mockToken" + ":" + "mockAccessToken" + ":" + "mockUUID";

                                System.out.println("Username is '" + username + "'");
                                this.playOnline(username, sessionId);
                            } else if (username.isEmpty()) {
                                this.launcherFrame.showError("Can't connect to minecraft.net");
                                this.launcherFrame.showOffline();
                                return;
                            } else {
                                this.launcherFrame.showError("Login failed");
                                return;
                            }
                            break;
                        case "Invalid credentials. Invalid username or password.":
                        case "Invalid credentials. Legacy account is non-premium account.":
                        case "Invalid credentials. Account migrated, use email as username.":
                            this.launcherFrame.showError("Login failed");
                            break;
                        case "Migrated":
                            this.launcherFrame.showError("Migrated");
                            break;
                        default:
                            this.launcherFrame.showError(jsonResponse.getString(String.valueOf(jsonResponse)));
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.launcherFrame.showError(e.toString());
                this.launcherFrame.showOffline();
            }
        }
    }

    public boolean checkConnection() {
        try {
            URL url = new URL("https://minecraft.net/");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean canPlayOffline(String username) {
        MCLauncher mcLauncher = new MCLauncher();
        mcLauncher.init(username, null);
        return mcLauncher.canPlayOffline();
    }
}