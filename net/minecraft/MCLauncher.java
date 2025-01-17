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
    public Map<String, String> customParameters = new HashMap<>();
    private Image image;
    private VolatileImage volatileImage;
    private int context = 0;
    private boolean active = false;
    private boolean minecraftUpdaterStarted = false;
    private Applet applet;
    private MCUpdater minecraftUpdater;

    public MCLauncher() {
        System.setProperty("http.proxyHost", "betacraft.uk");
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public void paint(Graphics g2) {
        mcLauncherGraphics.paint(g2);
    }

    public void update(Graphics g) {
        mcLauncherGraphics.update(g);
    }

    public boolean isActive() {
        switch (this.context) {
            case 0:
                this.context = -1;
                try {
                    if (getAppletContext() != null) {
                        this.context = 1;
                    }
                } catch (Exception ignored) {}
            case -1:
                return this.active;
        }
        return super.isActive();
    }

    public void init(String username, String sessionId) {
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("dirt.png"))).getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.customParameters.put("username", username);
        this.customParameters.put("sessionid", sessionId);
        this.minecraftUpdater = new MCUpdater();
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
        } else if (!minecraftUpdaterStarted) {
            Thread thread = new Thread(() -> {
                this.minecraftUpdater.run();
                try {
                    if (!this.minecraftUpdater.fatalError) {
                        this.replace(this.minecraftUpdater.createApplet());
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
            this.minecraftUpdaterStarted = true;
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
    public void appletResize(int width, int height) {}

    public URL getCodeBase() {
        try {
            return new URL("https://www.minecraft.net/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public URL getDocumentBase() {
        try {
            return new URL("https://github.com/sojlabjoi");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getParameter(String name) {
        if (this.customParameters.get(name) != null) {
            return this.customParameters.get(name);
        } else {
            try {
                return super.getParameter(name);
            } catch (Exception e) {
                this.customParameters.put(name, null);
                return null;
            }
        }
    }

    public MCUpdater getMinecraftUpdater() {
        return this.minecraftUpdater;
    }

    public Applet getApplet() {
        return this.applet;
    }

    public Image getImage() {
        return image;
    }

    public VolatileImage getVolatileImage() {
        return this.volatileImage;
    }

    public void setVolatileImage(VolatileImage volatileImage) {
        this.volatileImage = volatileImage;
    }

    public boolean canPlayOffline() {
        return this.minecraftUpdater.canPlayOffline();
    }
}