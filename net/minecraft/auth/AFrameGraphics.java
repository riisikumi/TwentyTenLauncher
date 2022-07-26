package net.minecraft.auth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Serializable;

public class AFrameGraphics implements Serializable {
    private final AFrame aFrame;

    public AFrameGraphics(AFrame aFrame) {
        this.aFrame = aFrame;
    }

    public void paint(Graphics g2) {
        if (aFrame.getVolatileImage() == null
                || aFrame.getVolatileImage().getWidth() != aFrame.getWidth() / 2
                || aFrame.getVolatileImage().getHeight() != aFrame.getHeight() / 2) {
            aFrame.setVolatileImage(aFrame.createVolatileImage(aFrame.getWidth() / 2, aFrame.getHeight() / 2));
        }

        Graphics g2d = aFrame.getVolatileImage().createGraphics();
        for (int i = 0; i <= (aFrame.getWidth() / 2) / 32; i++) {
            for (int j = 0; j <= (aFrame.getHeight() / 2) / 32; j++) {
                g2d.drawImage(aFrame.getImage(), i * 32, j * 32, null);
            }
        }

        String title = "Minecraft Launcher";
        g2d.setFont(new Font(null, Font.BOLD, 20));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(title, (aFrame.getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (aFrame.getHeight() / 2) / 2 - (g2d.getFontMetrics().getHeight() * 2));
        g2d.dispose();
        g2.drawImage(aFrame.getVolatileImage(), 0, 0, (aFrame.getWidth() / 2) * 2, (aFrame.getHeight() / 2) * 2, null);
    }

    public void update(Graphics g) {
        aFrame.paint(g);
    }
}