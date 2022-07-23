package net.minecraft;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Launcher extends Applet implements AppletStub
{
    public Map<String, String> parameters = new HashMap<>();
    public String serverAddress = null;
    public String serverPort = "25565";
    public String mppass = null;
    private Image img;

    private BufferedImage bImg;
    private int context = 0;
    private boolean active = false;
    private boolean gameUpdaterStarted = false;


    private Applet applet;
    private GameUpdater gameUpdater;
    @Override
    public boolean isActive()
    {
        switch (this.context)
        {
            case 0:
                this.context = -1;
                try
                {
                    if (getAppletContext() != null)
                    {
                        this.context = 1;
                    }
                } catch (Exception ignored) {}
            case -1:
                return this.active;
        }
        return super.isActive();
    }

    public void init(String username, String sessionId)
    {
        try
        {
            img = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/dirt.png")))
                    .getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.parameters.put("username", username);
        this.parameters.put("sessionid", sessionId);
        this.gameUpdater = new GameUpdater();
    }

    public boolean canPlayOffline()
    {
        return this.gameUpdater.canPlayOffline();
    }

    public void init()
    {
        if (this.applet != null)
        {
            this.applet.init();
            return;
        }
        this.init(this.getParameter("username"), this.getParameter("sessionid"));
    }

    @Override
    public void start()
    {
        if (this.applet != null)
        {
            this.applet.start();
        } else if (!gameUpdaterStarted)
        {
            Thread t = new Thread(() ->
            {
                Launcher.this.gameUpdater.run();
                try
                {
                    if (!Launcher.this.gameUpdater.fatalError)
                    {
                        Launcher.this.replace(Launcher.this.gameUpdater.createApplet());
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
            t.setDaemon(true);
            t.start();
            t = new Thread(() ->
            {
                while (Launcher.this.applet == null)
                {
                    Launcher.this.repaint();
                }
            });
            t.setDaemon(true);
            t.start();
            this.gameUpdaterStarted = true;
        }
    }

    @Override
    public void stop()
    {
        if (this.applet != null)
        {
            this.active = false;
            this.applet.stop();
        }
    }

    @Override
    public void destroy()
    {
        if (this.applet != null)
        {
            this.applet.destroy();
        }
    }

    public void replace(Applet applet)
    {
        this.applet = applet;

        applet.setStub(this);
        applet.setSize(this.getWidth(), this.getHeight());

        this.setLayout(new BorderLayout());
        this.add(applet, "Center");

        applet.init();
        this.active = true;
        applet.start();
        this.validate();
    }

    public void update(Graphics g)
    {
        this.paint(g);
    }

    public void paint(Graphics g2)
    {
        if (this.applet != null)
        {
            return;
        }
        if (bImg == null
                || bImg.getWidth() != getWidth() / 2
                || bImg.getHeight() != getHeight() / 2)
        {
            bImg = new BufferedImage(getWidth() / 2, getHeight() / 2, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2d = (Graphics2D) bImg.getGraphics();

        for (int i = 0; i <= (getWidth() / 2) / 32; i++)
        {
            for (int j = 0; j <= (getHeight() / 2) / 32; j++)
            {
                g2d.drawImage(img, i * 32, j * 32, null);
            }
        }
        g2d.setColor(Color.LIGHT_GRAY);
        String title = "Updating Minecraft";
        if (this.gameUpdater.fatalError)
        {
            title = "Failed to launch";
        }
        g2d.setFont(new Font(null, Font.BOLD, 20));
        g2d.drawString(title,
                (getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (getHeight() / 2) / 2 - (g2d.getFontMetrics().getHeight() * 2));
        g2d.setFont(new Font(null, Font.PLAIN, 12));
        title = this.gameUpdater.getDescriptionForState();
        if (this.gameUpdater.fatalError)
        {
            title = this.gameUpdater.fatalErrorDescription;
        }
        g2d.drawString(title,
                (getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (getHeight() / 2) / 2 + (g2d.getFontMetrics().getHeight()));
        title = this.gameUpdater.subtaskMessage;
        g2d.drawString(title,
                (getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (getHeight() / 2) / 2 + (g2d.getFontMetrics().getHeight() * 2));
        if (!this.gameUpdater.fatalError)
        {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(64, (getHeight() / 2) - 64, (getWidth() / 2) - 128 + 1, 5);
            g2d.setColor(new Color(0, 128, 0));
            g2d.fillRect(64, (getHeight() / 2) - 64, this.gameUpdater.percentage * ((getWidth() / 2) - 128) / 100, 4);
            g2d.setColor(new Color(32, 160, 32));
            g2d.fillRect(64, (getHeight() / 2) - 64 + 1, this.gameUpdater.percentage * ((getWidth() / 2) - 128) / 100 - 2, 1);
        }
        g2d.dispose();
        g2.drawImage(bImg, 0, 0, (getWidth() / 2) * 2, (getHeight() / 2) * 2, null);
    }

    public String getParameter(String name)
    {
        if (this.parameters.get(name) != null)
        {
            return this.parameters.get(name);
        } else
        {
            try
            {
                return super.getParameter(name);
            } catch (Exception e)
            {
                this.parameters.put(name, null);
                return null;
            }
        }
    }

    @Override
    public void appletResize(int width, int height) {}

    public URL getDocumentBase()
    {
        try
        {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public URL getCodeBase()
    {
        try
        {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}