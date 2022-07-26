package net.minecraft;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Serializable;

public class MCLauncherGraphics implements Serializable {
    private final MCLauncher mcLauncher;

    public MCLauncherGraphics(MCLauncher mcLauncher) {
        this.mcLauncher = mcLauncher;
    }

    public void paint(Graphics g2) {
        if (mcLauncher.getApplet() == null) {
            if (mcLauncher.getVolatileImage() == null || mcLauncher.getVolatileImage().getWidth() != mcLauncher.getWidth() / 2
                    || mcLauncher.getVolatileImage().getHeight() != mcLauncher.getHeight() / 2) {
                mcLauncher.setVolatileImage(mcLauncher.createVolatileImage(mcLauncher.getWidth() / 2, mcLauncher.getHeight() / 2));
            }

            Graphics g2d = mcLauncher.getVolatileImage().createGraphics();
            for (int i = 0; i <= (mcLauncher.getWidth() / 2) / 32; i++) {
                for (int j = 0; j <= (mcLauncher.getHeight() / 2) / 32; j++) {
                    g2d.drawImage(mcLauncher.getImage(), i * 32, j * 32, null);
                }
            }

            String title = "Updating Minecraft";
            g2d.setFont(new Font(null, Font.BOLD, 20));
            g2d.setColor(Color.LIGHT_GRAY);

            if (mcLauncher.getMcUpdater().fatalError) {
                title = "Failed to launch";
            }
            g2d.drawString(title, (mcLauncher.getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                    (mcLauncher.getHeight() / 2) / 2 - (g2d.getFontMetrics().getHeight() * 2));
            g2d.setFont(new Font(null, Font.PLAIN, 12));
            title = mcLauncher.getMcUpdater().getDescriptionForState();

            if (mcLauncher.getMcUpdater().fatalError) {
                title = mcLauncher.getMcUpdater().fatalErrorDescription;
            }
            g2d.drawString(title, (mcLauncher.getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                    (mcLauncher.getHeight() / 2) / 2 + (g2d.getFontMetrics().getHeight()));
            title = mcLauncher.getMcUpdater().subtaskMessage;
            g2d.drawString(title, (mcLauncher.getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                    (mcLauncher.getHeight() / 2) / 2 + (g2d.getFontMetrics().getHeight() * 2));

            if (!mcLauncher.getMcUpdater().fatalError) {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(64, (mcLauncher.getHeight() / 2) - 64, (mcLauncher.getWidth() / 2) - 128 + 1, 5);
                g2d.setColor(new Color(0, 128, 0));
                g2d.fillRect(64, (mcLauncher.getHeight() / 2) - 64,
                        mcLauncher.getMcUpdater().percentage * ((mcLauncher.getWidth() / 2) - 128) / 100, 4);
                g2d.setColor(new Color(32, 160, 32));
                g2d.fillRect(64, (mcLauncher.getHeight() / 2) - 64 + 1,
                        mcLauncher.getMcUpdater().percentage * ((mcLauncher.getWidth() / 2) - 128) / 100 - 2, 1);
            }
            g2d.dispose();
            g2.drawImage(mcLauncher.getVolatileImage(), 0, 0, (mcLauncher.getWidth() / 2) * 2, (mcLauncher.getHeight() / 2) * 2, null);
        }
    }

    public void update(Graphics g) {
        mcLauncher.paint(g);
    }
}