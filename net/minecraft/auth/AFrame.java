package net.minecraft.auth;

import net.minecraft.auth.yggdrasil.YAuthenticate;
import net.minecraft.launcher.LauncherUpdater;

import javax.imageio.ImageIO;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
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
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class AFrame extends Panel {
    private final ALastLogin aLastLogin = new ALastLogin(this);
    private final net.minecraft.auth.AFrameGraphics aFrameGraphics = new AFrameGraphics(this);
    public Label errorLabel = new Label("", 1);
    public TextField emailTextField = new TextField(20);
    public TextField passwordTextField = new TextField(20);
    public Checkbox rememberCheckbox = new Checkbox("Remember password");
    public Button loginButton = new Button("Login");
    public Button retryButton = new Button("Try again");
    public Button offlineButton = new Button("Play offline");
    private Image image;
    private VolatileImage volatileImage;

    public AFrame(final YAuthenticate yAuthenticate) {
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("dirt.png")))
                    .getScaledInstance(32, 32, Image.SCALE_AREA_AVERAGING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setLayout(new GridBagLayout());
        this.add(this.loginPanel());
        this.aLastLogin.readLastLogin();
        this.loginButton.addActionListener(ae -> {
            try {
                yAuthenticate.login(this.emailTextField.getText(), this.passwordTextField.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.offlineButton.addActionListener(e -> yAuthenticate.playOffline(this.emailTextField.getText()));
        this.retryButton.addActionListener(ae -> {
            this.setError("");
            this.removeAll();
            this.add(this.loginPanel());
            this.validate();
        });
    }

    public Panel loginPanel() {
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
            if (LauncherUpdater.checkForUpdate()) {
                Label label = new Label("You need to update the launcher!", 1) {
                    public void update(Graphics g) {
                        this.paint(g);
                    }

                    public void paint(Graphics g) {
                        super.paint(g);
                        g.setColor(Color.BLUE);
                        g.drawLine(this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2, this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1, this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2 + g.getFontMetrics().stringWidth(this.getText()), this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1);
                    }
                };
                label.setForeground(Color.BLUE);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                onlinePanel.add(label, BorderLayout.WEST);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        try {
                            Desktop.getDesktop().browse(new URI("https://github.com/sojlabjoi/AlphacraftLauncher/releases/latest"));
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
                        g.drawLine(this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2, this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1, this.getBounds().width / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2 + g.getFontMetrics().stringWidth(this.getText()), this.getBounds().height / 2 + g.getFontMetrics().getHeight() / 2 - 1);
                    }
                };
                label.setForeground(Color.BLUE);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                onlinePanel.add(label, BorderLayout.WEST);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        try {
                            Desktop.getDesktop().browse(new URI("https://signup.live.com/signup" + "?cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d" + "&client_id=000000004420578E" + "&lic=1"));
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

    public Panel offlinePanel() {
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

        boolean canPlayOffline = YAuthenticate.canPlayOffline(this.emailTextField.getText());
        this.offlineButton.setEnabled(canPlayOffline);
        if (!canPlayOffline) {
            panel.add(new Label("Play online once to enable setOffline", 0));
        }
        return panel;
    }

    public void paint(Graphics g2) {

        aFrameGraphics.paint(g2);
    }

    public void update(Graphics g) {
        aFrameGraphics.update(g);
    }

    public void setVolatileImage(VolatileImage volatileImage) {
        this.volatileImage = volatileImage;
    }

    public void setError(String error) {
        this.removeAll();
        this.add(this.loginPanel());
        this.errorLabel.setText(error);
        this.validate();
    }

    public void setOffline() {
        this.removeAll();
        this.add(this.offlinePanel());
        this.validate();
    }

    public void getLastLogin() {
        aLastLogin.writeLastLogin();
    }

    public Image getImage() {
        return image;
    }

    public VolatileImage getVolatileImage() {
        return volatileImage;
    }

    public TextField getEmailTextField() {
        return this.emailTextField;
    }

    public TextField getPasswordTextField() {
        return this.passwordTextField;
    }

    public Checkbox getRememberCheckbox() {
        return this.rememberCheckbox;
    }
}