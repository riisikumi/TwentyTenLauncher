package net.minecraft.auth.yggdrasil;

import org.json.JSONObject;

import java.io.Serializable;

public class YAgent implements Serializable {
    JSONObject setAgentParams() {
        JSONObject agentParams = new JSONObject();
        agentParams.put("name", "Minecraft");
        agentParams.put("version", 1);
        return agentParams;
    }
}