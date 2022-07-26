package net.minecraft;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MCLauncher extends Applet implements AppletStub {
    private final MCLauncherGraphics mcLauncherGraphics = new MCLauncherGraphics(this);
    public Map<String, String> params = new HashMap<>();
    private Image image;
    private VolatileImage volatileImage;
    private int context = 0;
    private boolean active = false;
    private boolean gameUpdaterStarted = false;
    private Applet applet;
    private MCUpdater mcUpdater;

    public MCLauncher() {
        System.setProperty("http.proxyHost", "betacraft.uk");
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public boolean isActive() {
        switch (this.context) {
            case 0:
                this.context = -1;
                try {
                    if (getAppletContext() != null) {
                        this.context = 1;
                    }
                } catch (Exception ignored) {
                }
            case -1:
                return this.active;
        }
        return super.isActive();
    }

    public void init(String username, String sessionId) {
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("dirt.png")))
                    .getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.params.put("username", username);
        this.params.put("sessionid", sessionId);
        this.mcUpdater = new MCUpdater();
    }

    public boolean canPlayOffline() {
        return this.mcUpdater.canPlayOffline();
    }

    public void init() {
        if (this.applet != null) {
            this.applet.init();
            return;
        }
        this.init(this.getParameter("username"), this.getParameter("sessionid"));
    }

    public void start() {
        if (this.applet != null) {
            this.applet.start();
        } else if (!gameUpdaterStarted) {
            Thread thread = new Thread(() -> {
                this.mcUpdater.run();
                try {
                    if (!this.mcUpdater.fatalError) {
                        this.replace(this.mcUpdater.createApplet());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            thread.start();
            thread = new Thread(() ->
            {
                while (this.applet == null)
                {
                    this.repaint();
                }
            });
            thread.setDaemon(true);
            thread.start();
            this.gameUpdaterStarted = true;
        }
    }

    public void stop() {
        if (this.applet != null) {
            this.active = false;
            this.applet.stop();
        }
    }

    public void destroy() {
        if (this.applet != null) {
            this.applet.destroy();
        }
    }

    public void replace(Applet applet) {
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

    public void paint(Graphics g2) {
        mcLauncherGraphics.paint(g2);
    }

    public void update(Graphics g) {
        mcLauncherGraphics.update(g);
    }

    public String getParameter(String name) {
        if (this.params.get(name) != null) {
            return this.params.get(name);
        } else {
            try {
                return super.getParameter(name);
            } catch (Exception e) {
                this.params.put(name, null);
                return null;
            }
        }
    }

    public void appletResize(int width, int height) {
        if (this.applet != null) {
            this.applet.resize(width, height);
        }
    }

    public URL getDocumentBase() {
        try {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public URL getCodeBase() {
        try {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Image getImage() {
        return image;
    }
    public VolatileImage getVolatileImage() {
        return volatileImage;
    }

    public MCUpdater getMcUpdater() {
        return mcUpdater;
    }

    public Applet getApplet() {
        return applet;
    }

    public void setVolatileImage(VolatileImage volatileImage) {
        this.volatileImage = volatileImage;
    }
}