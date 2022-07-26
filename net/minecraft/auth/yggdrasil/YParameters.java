package net.minecraft.auth.yggdrasil;

import org.json.JSONObject;

import java.io.Serializable;

public class YParameters implements Serializable {
    private final YAgent yAgent = new YAgent();

    JSONObject setAuthenticateParams(String username, String password) {
        JSONObject agentParams = yAgent.setAgentParams();

        JSONObject params = new JSONObject();
        params.put("agent", agentParams);
        params.put("username", username);
        params.put("password", password);
        params.put("requestUser", true);
        return params;
    }
}