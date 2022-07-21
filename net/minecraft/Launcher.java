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
    public Map<String, String> customParameters = new HashMap<>();

    private Image img;

    private BufferedImage bImg;

    private Applet applet;

    private int context = 0;

    private boolean active = false;
    private boolean gUpdaterStarted = false;
    private GameUpdater gUpdater;

    public Launcher()
    {
        System.setProperty("http.proxyHost", "betacraft.uk");
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

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

    public void init(String username)
    {
        try
        {
            img = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/dirt.png")))
                    .getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.customParameters.put("username", username);
        this.gUpdater = new GameUpdater(".jar" + username);
    }

    public boolean canPlayOffline()
    {
        return this.gUpdater.canPlayOffline();
    }

    public void init()
    {
        if (this.applet != null)
        {
            this.applet.init();
            return;
        }
        this.init(this.getParameter("username"));
    }

    @Override
    public void start()
    {
        if (this.applet != null)
        {
            this.applet.start();
        } else if (!gUpdaterStarted)
        {
            Thread t = new Thread(() ->
            {
                Launcher.this.gUpdater.run();
                try
                {
                    if (!Launcher.this.gUpdater.fatalError)
                    {
                        Launcher.this.replace(Launcher.this.gUpdater.createApplet());
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
            this.gUpdaterStarted = true;
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
        if (this.gUpdater.fatalError)
        {
            title = "Failed to launch";
        }
        g2d.setFont(new Font(null, Font.BOLD, 20));
        g2d.drawString(title,
                (getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (getHeight() / 2) / 2 - (g2d.getFontMetrics().getHeight() * 2));
        g2d.setFont(new Font(null, Font.PLAIN, 12));
        title = this.gUpdater.getDescriptionForState();
        if (this.gUpdater.fatalError)
        {
            title = this.gUpdater.fatalErrorDescription;
        }
        g2d.drawString(title,
                (getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (getHeight() / 2) / 2 + (g2d.getFontMetrics().getHeight()));
        title = this.gUpdater.subtaskMessage;
        g2d.drawString(title,
                (getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (getHeight() / 2) / 2 + (g2d.getFontMetrics().getHeight() * 2));
        if (!this.gUpdater.fatalError)
        {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(64, (getHeight() / 2) - 64, (getWidth() / 2) - 128 + 1, 5);
            g2d.setColor(new Color(0, 128, 0));
            g2d.fillRect(64, (getHeight() / 2) - 64, this.gUpdater.percentage * ((getWidth() / 2) - 128) / 100, 4);
            g2d.setColor(new Color(32, 160, 32));
            g2d.fillRect(64, (getHeight() / 2) - 64 + 1, this.gUpdater.percentage * ((getWidth() / 2) - 128) / 100 - 2, 1);
        }
        g2d.dispose();
        g2.drawImage(bImg, 0, 0, (getWidth() / 2) * 2, (getHeight() / 2) * 2, null);
    }

    public String getParameter(String name)
    {
        if (this.customParameters.containsKey(name))
        {
            return this.customParameters.get(name);
        }
        try
        {
            return super.getParameter(name);
        } catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void appletResize(int width, int height)
    {
        if (this.applet != null)
        {
            this.applet.resize(854, 480);
        }
    }

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