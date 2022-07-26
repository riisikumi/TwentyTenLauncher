package net.minecraft.auth.yggdrasil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

public class YUtils {
    public static JSONObject excutePost(String url, String jsonParameters) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();

        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(jsonParameters));

        HttpResponse response = client.execute(post);
        return new JSONObject(response != null ? EntityUtils.toString(response.getEntity()) : "");
    }
}