package net.minecraft.launcher;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public class LauncherUpdater implements Serializable {
    public static String launcherVersion = "0.7.2622";

    public static boolean checkForUpdate() {
        try {
            URL url = new URL("https://api.github.com/repos/sojlabjoi/AlphacraftLauncher/releases/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            if (connection.getResponseCode() != 200) {
                return false;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            connection.disconnect();

            JSONObject json = new JSONObject(sb.toString());
            String tag_name = json.getString("tag_name");
            if (tag_name.compareTo(launcherVersion) > 0) {
                return true;
            } else if (tag_name.compareTo(launcherVersion) == 0) {
                return false;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}