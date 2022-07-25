package net.minecraft;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Random;

public class AuthFrame extends Panel {
    public Label errorLabel = new Label("", 1);
    public TextField emailTextField = new TextField(20);
    public TextField passwordTextField = new TextField(20);
    public Checkbox rememberCheckbox = new Checkbox("Remember password");
    public Button loginButton = new Button("Login");
    public Button retryButton = new Button("Try again");
    public Button offlineButton = new Button("Play offline");
    private Image img;
    private BufferedImage bImg;

    public AuthFrame(final LauncherFrame lFrame) {
        try {
            img = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/dirt.png")))
                    .getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setLayout(new GridBagLayout());
        this.add(this.loginPanel());

        this.readLastLogin();
        this.loginButton.addActionListener(ae -> {
            try {
                lFrame.login(this.emailTextField.getText(), this.passwordTextField.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.offlineButton.addActionListener(e -> lFrame.playOffline(this.emailTextField.getText()));
        this.retryButton.addActionListener(ae -> {
            this.setError("");
            this.removeAll();
            this.add(this.loginPanel());
            this.validate();
        });
    }

    private static Cipher getCipher(int mode) throws Exception {
        Random random = new Random(43287234L);
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        PBEParameterSpec ps = new PBEParameterSpec(salt, 5);
        SecretKey sk = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
                .generateSecret(new PBEKeySpec("passwordfile".toCharArray()));

        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, sk, ps);
        return cipher;
    }

    private Panel loginPanel() {
        Panel panel = new Panel() {
            public Insets getInsets() {
                return new Insets(12, 24, 16, 32);
            }

            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
                g.drawRect(1, 1, this.getWidth() - 3, this.getHeight() - 3);
                g.setColor(Color.WHITE);
                g.drawRect(2, 2, this.getWidth() - 5, this.getHeight() - 5);
            }

            public void update(Graphics g) {
                this.paint(g);
            }
        };
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBackground(Color.GRAY);
        panel.add(this.errorLabel, BorderLayout.NORTH);
        this.errorLabel.setFont(new Font(null, Font.ITALIC, 16));
        this.errorLabel.setForeground(new Color(128, 0, 0));

        Panel text = new Panel(new GridLayout(0, 1, 0, 2));
        panel.add(text, BorderLayout.WEST);
        text.add(new Label("Email:", 2));
        text.add(new Label("Password:", 2));
        text.add(new Label(""));

        Panel textField = new Panel(new GridLayout(0, 1, 0, 2));
        panel.add(textField, BorderLayout.CENTER);
        textField.add(this.emailTextField);
        textField.add(this.passwordTextField);
        textField.add(this.rememberCheckbox);
        this.passwordTextField.setEchoChar('*');

        Panel onlinePanel = new Panel(new BorderLayout());
        try {
            if (LauncherFrame.checkForUpdate()) {
                Label label = new Label("You need to update the launcher!", 1) {
                    public void update(Graphics g) {
                        this.paint(g);
                    }

                    public void paint(Graphics g) {
                        super.paint(g);
                        g.setColor(Color.BLUE);
                        g.drawLine(this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2,
                                this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1,
                                this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2
                                        + g.getFontMetrics().stringWidth(this.getText()),
                                this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1);
                    }
                };
                label.setForeground(Color.BLUE);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                onlinePanel.add(label, BorderLayout.WEST);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        try {
                            Desktop.getDesktop().browse(
                                    new URI("https://github.com/sojlabjoi/AlphacraftLauncher/releases/latest"));
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Label label = new Label("Need account?", 1) {
                    public void update(Graphics g) {
                        this.paint(g);
                    }

                    public void paint(Graphics g) {
                        super.paint(g);
                        g.drawLine(this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2,
                                this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1,
                                this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2
                                        + g.getFontMetrics().stringWidth(this.getText()),
                                this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1);
                    }
                };
                label.setForeground(Color.BLUE);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                onlinePanel.add(label, BorderLayout.WEST);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        try {
                            Desktop.getDesktop().browse(new URI("https://signup.live.com/signup"
                                    + "?cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d"
                                    + "&client_id=000000004420578E"
                                    + "&lic=1"));
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (HeadlessException e) {
            throw new RuntimeException(e);
        }
        panel.add(onlinePanel, BorderLayout.SOUTH);
        onlinePanel.add(this.loginButton, BorderLayout.EAST);
        return panel;
    }

    private Panel offlinePanel() {
        Panel panel = new Panel() {
            public Insets getInsets() {
                return new Insets(12, 24, 16, 32);
            }

            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
                g.drawRect(1, 1, this.getWidth() - 3, this.getHeight() - 3);
                g.setColor(Color.WHITE);
                g.drawRect(2, 2, this.getWidth() - 5, this.getHeight() - 5);
            }

            public void update(Graphics g) {
                this.paint(g);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.GRAY);
        panel.add(new Panel(), BorderLayout.CENTER);
        panel.add(this.errorLabel, BorderLayout.NORTH);
        this.errorLabel.setFont(new Font(null, Font.ITALIC, 16));
        this.errorLabel.setForeground(new Color(128, 0, 0));

        Panel offlinePanel = new Panel(new BorderLayout());
        offlinePanel.add(new Panel(), BorderLayout.CENTER);
        offlinePanel.add(this.retryButton, BorderLayout.EAST);
        offlinePanel.add(this.offlineButton, BorderLayout.WEST);
        panel.add(offlinePanel, BorderLayout.SOUTH);

        boolean canPlayOffline = LauncherFrame.canPlayOffline(this.emailTextField.getText());
        this.offlineButton.setEnabled(canPlayOffline);
        if (!canPlayOffline) {
            panel.add(new Label("Play online once to enable offline", 0));
        }
        return panel;
    }

    public void setError(String error) {
        this.removeAll();
        this.add(this.loginPanel());
        this.errorLabel.setText(error);
        this.validate();
    }

    public void offline() {
        this.removeAll();
        this.add(this.offlinePanel());
        this.validate();
    }

    public void getLastLogin() {
        writeLastLogin();
    }

    public void writeLastLogin() {
        try {
            File lastLogin = new File(Util.getWorkingDirectory(), "lastlogin");
            Cipher cipher = getCipher(1);

            DataOutputStream dos = new DataOutputStream(
                    new CipherOutputStream(Files.newOutputStream(lastLogin.toPath()), cipher));
            dos.writeUTF(emailTextField.getText());
            dos.writeUTF(this.rememberCheckbox.getState() ? passwordTextField.getText() : "");
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLastLogin() {
        try {
            File lastLogin = new File(Util.getWorkingDirectory(), "lastlogin");
            Cipher cipher = getCipher(2);

            DataInputStream dis = new DataInputStream(
                    new CipherInputStream(Files.newInputStream(lastLogin.toPath()), cipher));
            this.emailTextField.setText(dis.readUTF());
            this.passwordTextField.setText(dis.readUTF());

            this.rememberCheckbox.setState(passwordTextField.getText().length() > 0);
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paint(Graphics g2) {
        if (bImg == null || bImg.getWidth() != getWidth() / 2 || bImg.getHeight() != getHeight() / 2) {
            bImg = new BufferedImage(getWidth() / 2, getHeight() / 2, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2d = (Graphics2D) bImg.getGraphics();
        for (int i = 0; i <= (getWidth() / 2) / 32; i++) {
            for (int j = 0; j <= (getHeight() / 2) / 32; j++) {
                g2d.drawImage(img, i * 32, j * 32, null);
            }
        }

        String title = "Minecraft Launcher";
        g2d.setFont(new Font(null, Font.BOLD, 20));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(title, (getWidth() / 2) / 2 - (g2d.getFontMetrics().stringWidth(title) / 2),
                (getHeight() / 2) / 2 - (g2d.getFontMetrics().getHeight() * 2));
        g2d.dispose();
        g2.drawImage(bImg, 0, 0, (getWidth() / 2) * 2, (getHeight() / 2) * 2, null);
    }

    public void update(Graphics g) {
        this.paint(g);
    }
}

