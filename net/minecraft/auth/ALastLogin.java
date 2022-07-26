package net.minecraft.auth;

import net.minecraft.MCUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Random;

public class ALastLogin implements Serializable {
    private final AFrame AFrame;

    public ALastLogin(AFrame AFrame) {
        this.AFrame = AFrame;
    }

    public void readLastLogin() {
        try {
            File lastLogin = new File(MCUtils.getWorkingDirectory(), "lastlogin");
            Cipher cipher = getCipher(2);

            DataInputStream dis = new DataInputStream(
                    new CipherInputStream(Files.newInputStream(lastLogin.toPath()), cipher));
            AFrame.getEmailTextField().setText(dis.readUTF());
            AFrame.getPasswordTextField().setText(dis.readUTF());
            AFrame.getRememberCheckbox().setState(AFrame.getPasswordTextField().getText().length() > 0);
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeLastLogin() {
        try {
            File lastLogin = new File(MCUtils.getWorkingDirectory(), "lastlogin");
            Cipher cipher = getCipher(1);

            DataOutputStream dos = new DataOutputStream(new CipherOutputStream(Files.newOutputStream(lastLogin.toPath()), cipher));
            dos.writeUTF(AFrame.getEmailTextField().getText());
            dos.writeUTF(AFrame.getRememberCheckbox().getState() ? AFrame.getPasswordTextField().getText() : "");
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}