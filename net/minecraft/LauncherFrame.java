package net.minecraft;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class LauncherFrame extends Frame
{
    public static String launcherVersion = "0.7.2222";
	public AuthFrame aFrame;
	public Launcher launcher;
	public LauncherFrame lFrame;

	public LauncherFrame()
	{
		this.lFrame = this;
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

		this.aFrame = new AuthFrame(this);
		this.add(this.aFrame, BorderLayout.CENTER);
		this.aFrame.setPreferredSize(new Dimension(854, 480));
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
			this.launcher.customParameters.put("username", username);
			this.launcher.init();
			this.removeAll();
			this.add(this.launcher, "Center");
			this.validate();
			this.launcher.start();
			this.aFrame = null;
			this.setTitle("Minecraft");
		} catch (Exception e)
		{
			e.printStackTrace();
			this.aFrame.getError(e.toString());
		}
	}

	public void login(String username, String password)
	{
		if (!checkConnection())
		{
			this.aFrame.getError("Can't connect to minecraft.net");
			this.aFrame.offline();
		} else
		{
			try
			{
				String jsonParameters = "\"username\":\"" + username
						+ "\",\"password\":\"" + password
						+ "\",\"requestUser\":true}";
				String response = Util.executePost("https://authserver.mojang.com/authenticate", jsonParameters);
				if (response == null)
				{
					this.aFrame.getError("Can't connect to minecraft.net");
					this.aFrame.offline();
					return;
				}

				JSONObject json = new JSONObject(response);

				if (json.has("errorMessage"))
				{
					switch (json.getString("errorMessage"))
					{
						case "Invalid credentials. Account migrated, use email as username.":
						case "Invalid credentials. Legacy account is non-premium account.":
						case "Invalid credentials. Invalid username or password.":
							this.aFrame.getError("Login failed");
							break;
						case "Forbidden":
							if (username.isEmpty())
							{
								this.aFrame.getError("Can't connect to minecraft.net");
								this.aFrame.offline();
								return;
							} else if (username.matches("^\\w+$")
									&& username.length() > 2 && username.length() < 17)
							{
								this.playOffline(username);
							} else
							{
								this.aFrame.getError("Login failed");
								return;
							}
							break;
						case "Migrated":
							this.aFrame.getError("Migrated");
							break;
						default:
							this.aFrame.getError(json.getString(response));
							break;
					}
				} else
				{
					if (json.getJSONArray("availableProfiles").length() == 0)
					{
						this.playOffline(username);
					} else
					{
						username = json.getJSONObject("selectedProfile").getString("name");

						System.out.println("Username is '" + username + "'");
						this.launcher = new Launcher();
						this.launcher.customParameters.put("username", username);
						this.launcher.init();
						this.removeAll();
						this.add(this.launcher, "Center");
						this.validate();
						this.launcher.start();
						this.aFrame.getLastLogin();
						this.aFrame = null;
						this.setTitle("Minecraft");
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				this.aFrame.getError(e.toString());
				this.aFrame.offline();
			}
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

	public static boolean canPlayOffline(String username)
	{
		Launcher launcher = new Launcher();
		launcher.init(username);
		return launcher.canPlayOffline();
	}

	public static void main(String[] args)
	{
		if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 511L)
		{
			System.setProperty("sun.java2d.d3d", "false");
			System.setProperty("sun.java2d.pmoffscreen", "false");
		}
		LauncherFrame lFrame = new LauncherFrame();
		lFrame.setVisible(true);
	}
}