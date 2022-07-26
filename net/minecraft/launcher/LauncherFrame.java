package net.minecraft.launcher;

import net.minecraft.MCLauncher;
import net.minecraft.auth.AFrame;
import net.minecraft.auth.yggdrasil.YAuthenticate;

import javax.imageio.ImageIO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;

public class LauncherFrame extends Frame {
    public final YAuthenticate yAuthenticate = new YAuthenticate(this);
    public AFrame aFrame;
    public LauncherFrame launcherFrame;
    public MCLauncher mcLauncher;

    public LauncherFrame() {
        this.launcherFrame = this;
        try {
            this.setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("favicon.png"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setTitle("Minecraft Launcher" + " " + LauncherUpdater.launcherVersion);
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);
        this.setMinimumSize(new Dimension(320, 200));
        this.aFrame = new AFrame(yAuthenticate);
        this.add(this.aFrame, BorderLayout.CENTER);
        this.aFrame.setPreferredSize(new Dimension(854, 480));
        this.pack();
        this.setLocationRelativeTo(null);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if (LauncherFrame.this.mcLauncher != null) {
                    LauncherFrame.this.mcLauncher.stop();
                    LauncherFrame.this.mcLauncher.destroy();
                }
                dispose();

                System.exit(0);
            }
        });
    }

    public void showOffline()
    {
        this.removeAll();
        this.add(this.aFrame);
        this.aFrame.setOffline();
        this.validate();
    }

    public void showError(String error) {
        this.removeAll();
        this.add(this.aFrame);
        this.aFrame.setError(error);
        this.validate();
    }
}