package net.minecraft;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class LauncherFrame extends Frame
{
    public static String launcherVersion = "0.7.2322";
	public String sessionId;
	public Launcher launcher;
	public AuthFrame authFrame;
	public LauncherFrame launcherFrame;

	public LauncherFrame()
	{
		this.launcherFrame = this;
		try
		{
			this.setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/favicon.png"))));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		this.setTitle("Minecraft Launcher" + " " + launcherVersion);
		this.setLayout(new BorderLayout());
		this.setBackground(Color.BLACK);
		this.setMinimumSize(new Dimension(320, 200));
		this.setLocationRelativeTo(null);

		this.authFrame = new AuthFrame(this);
		this.add(this.authFrame, BorderLayout.CENTER);
		this.authFrame.setPreferredSize(new Dimension(854, 480));
		this.pack();

		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent we)
			{
				if (LauncherFrame.this.launcher != null)
				{
					LauncherFrame.this.launcher.stop();
					LauncherFrame.this.launcher.destroy();
				}
				dispose();

				System.exit(0);
			}
		});
	}

	public void playOffline(String username)
	{
		try
		{
			if (username.matches("^\\w+$")
					&& username.length() < 3 || username.length() > 16)
			{
				username = "Player";
			}
			this.launcher = new Launcher();
			this.launcher.parameters.put("username", username);
			this.launcher.init();
			this.removeAll();
			this.add(this.launcher, "Center");
			this.validate();
			this.launcher.start();
			this.authFrame = null;
			this.setTitle("Minecraft");
		} catch (Exception e)
		{
			e.printStackTrace();
			this.authFrame.setError(e.toString());
		}
	}

	public void login(String username, String password)
	{
		if (!checkConnection())
		{
			this.authFrame.setError("Can't connect to minecraft.net");
			this.authFrame.offline();
		} else
		{
			try
			{
				JSONObject agentParameters = new JSONObject();
				agentParameters.put("name", "Minecraft");
				agentParameters.put("version", 1);

				JSONObject jsonParameters = new JSONObject();
				jsonParameters.put("agent", agentParameters);
				jsonParameters.put("username", username);
				jsonParameters.put("password", password);
				jsonParameters.put("requestUser", true);

				JSONObject jsonResponse = Util.excuteAuth("https://authserver.mojang.com/authenticate", String.valueOf(jsonParameters));
				if (!jsonResponse.has("errorMessage"))
				{
					if (jsonResponse.getJSONArray("availableProfiles").length() == 0)
					{
						this.playOffline(username);
						this.authFrame.getLastLogin();
					} else
					{
						username = jsonResponse.getJSONObject("selectedProfile").getString("name");
						sessionId = jsonResponse.getString("clientToken") + ":"
								+ jsonResponse.getString("accessToken") + ":"
								+ jsonResponse.getJSONObject("selectedProfile").getString("id");

						System.out.println("Username is '" + username + "'");
						this.launcher = new Launcher();
						this.launcher.parameters.put("username", username);
						this.launcher.parameters.put("sessionid", sessionId);
						this.launcher.init();
						this.removeAll();
						this.add(this.launcher, "Center");
						this.validate();
						this.launcher.start();
						this.refresh();
					}
					this.authFrame = null;
					this.setTitle("Minecraft");
				} else
				{
					switch (jsonResponse.getString("errorMessage"))
					{
						case "Forbidden":
							if (username.matches("^\\w+$")
									&& username.length() > 2 && username.length() < 17)
							{
								sessionId = "mockToken" + ":" + "mockAccessToken" + ":" + "mockUUID";

								System.out.println("Username is '" + username + "'");
								this.launcher = new Launcher();
								this.launcher.parameters.put("username", username);
								this.launcher.parameters.put("sessionid", sessionId);
								this.launcher.init();
								this.removeAll();
								this.add(this.launcher, "Center");
								this.validate();
								this.launcher.start();
								this.authFrame.getLastLogin();
							} else if (username.isEmpty())
							{
								this.authFrame.setError("Can't connect to minecraft.net");
								this.authFrame.offline();
								return;
							} else
							{
								this.authFrame.setError("Login failed");
								return;
							}
							break;
						case "Invalid credentials. Invalid username or password.":
						case "Invalid credentials. Legacy account is non-premium account.":
						case "Invalid credentials. Account migrated, use email as username.":
							this.authFrame.setError("Login failed");
							break;
						case "Migrated":
							this.authFrame.setError("Migrated");
							break;
						default:
							this.authFrame.setError(jsonResponse.getString(String.valueOf(jsonResponse)));
							break;
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				this.authFrame.setError(e.toString());
				this.authFrame.offline();
			}
		}
	}

	private void refresh()
	{
		try
		{
			JSONObject jsonResponse = Util.excuteAuth("https://authserver.mojang.com/refresh", sessionId);
			if (!jsonResponse.has("errorMessage"))
			{
				sessionId = jsonResponse.getString("clientToken") + ":"
						+ jsonResponse.getString("accessToken");
				this.launcher.parameters.put("sessionid", sessionId);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean checkConnection()
	{
		try
		{
			URL url = new URL("https://minecraft.net/");
			URLConnection connection = url.openConnection();
			connection.connect();
			return true;
		} catch (IOException e)
		{
			return false;
		}
	}

	protected static boolean checkForUpdate()
	{
		try
		{
			URL url = new URL("https://api.github.com/repos/sojlabjoi/AlphacraftLauncher/releases/latest");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			if (connection.getResponseCode() != 200)
			{
				return false;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder sb = new StringBuilder();

			String line;
			while ((line = br.readLine()) != null)
			{
				sb.append(line);
			}
			br.close();
			connection.disconnect();

			JSONObject json = new JSONObject(sb.toString());
			String tag_name = json.getString("tag_name");
			if (tag_name.compareTo(LauncherFrame.launcherVersion) > 0)
			{
				return true;
			} else if (tag_name.compareTo(LauncherFrame.launcherVersion) == 0)
			{
				return false;
			} else
			{
				return false;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean canPlayOffline(String username)
	{
		Launcher launcher = new Launcher();
		launcher.init(username, null);
		return launcher.canPlayOffline();
	}

	public static void main(String[] args)
	{
		System.setProperty("http.proxyHost", "betacraft.uk");
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		LauncherFrame lFrame = new LauncherFrame();
		lFrame.setVisible(true);
	}
}