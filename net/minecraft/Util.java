package net.minecraft;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

public class Util
{
	public static final String OPERATING_SYSTEM = System.getProperty("os.name").toLowerCase();
	private static final String USER_HOME = System.getProperty("user.home", ".");

	public static File getWorkingDirectory()
	{
		File workingDirectory;
		switch (getPlatform())
		{
			case linux:
				workingDirectory = new File(USER_HOME, ".minecraft/");
				break;
			case osx:
				workingDirectory = new File(USER_HOME, "Library/Application Support/minecraft/");
				break;
			case windows:
				String applicationData = System.getenv("APPDATA");
				if (applicationData != null)
				{
					workingDirectory = new File(applicationData, ".minecraft/");
					break;
				}
				workingDirectory = new File(USER_HOME, ".minecraft/");
				break;
			default:
				workingDirectory = new File(USER_HOME, "minecraft/");
				break;
		}
		if (!workingDirectory.exists() && !workingDirectory.mkdirs())
			throw new RuntimeException("The working directory could not be created: " + workingDirectory);

		return workingDirectory;
	}

	public static OS getPlatform()
	{
		if (OPERATING_SYSTEM.contains("mac"))
		{
			return OS.osx;
		} else if (OPERATING_SYSTEM.contains("nix")
				|| OPERATING_SYSTEM.contains("nux")
				|| OPERATING_SYSTEM.contains("aix"))
		{
			return OS.linux;
		} else if (OPERATING_SYSTEM.contains("win"))
		{
			return OS.windows;
		} else
		{
			throw new RuntimeException("Unknown OS: " + OPERATING_SYSTEM);
		}
	}

	public static String executePost(String url, String jsonParameters) throws IOException
	{
		String agent = "{\"agent\":{\"name\":\"Minecraft\",\"version\":1},";

		HttpClient client = HttpClientBuilder.create().build();

		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(agent + jsonParameters));
		post.setHeader("Content-type", "application/json");

		HttpResponse response = client.execute(post);

		return response != null ? EntityUtils.toString(response.getEntity()) : null;
	}

    public enum OS
	{
		osx,
		linux,
		windows
	}
}