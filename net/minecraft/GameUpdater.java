package net.minecraft;

import java.applet.Applet;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameUpdater implements Runnable
{
    public int percentage;
    public int currentSizeDownload;
    public int totalSizeDownload;
    public int currentSizeExtract;
    public int totalSizeExtract;
    protected int state;
    public String fatalErrorDescription;
    protected String subtaskMessage = "";
    protected String clientVersion = "client";
    protected String assetsUrl = "http://files.betacraft.uk/launcher/assets/";
    protected String clientUrl = "https://piston-data.mojang.com/v1/objects/e1c682219df45ebda589a557aadadd6ed093c86c/"
            + this.clientVersion + ".jar";
    public boolean fatalError;
    protected static boolean natives_loaded = false;
    protected URL[] urlList;
    protected Thread lThread;
    private static ClassLoader cLoader;

    public void init()
    {
        this.state = 1;
    }

    protected String getDescriptionForState()
    {
        switch (this.state)
        {
            case 1:
                return "Initializing loader";
            case 2:
                return "Determining packages to load";
            case 3:
                return "Checking cache for existing files";
            case 4:
                return "Downloading packages";
            case 5:
                return "Extracting downloaded packages";
            case 6:
                return "Updating classpath";
            case 7:
                return "Done loading";
            default:
                return "Unknown state";
        }
    }

    protected void loadFileURLs() throws MalformedURLException
    {
        this.state = 2;

        String libs;
        String natives;
        switch (Util.getPlatform())
        {
            case osx:
                libs = "libs-osx.zip";
                natives = "natives-osx.zip";
                break;
            case linux:
                libs = "libs-linux.zip";
                natives = "natives-linux.zip";
                break;
            case windows:
                libs = "libs-windows.zip";
                natives = "natives-windows.zip";
                break;
            default:
                throw new RuntimeException("Unknown OS: " + Util.OPERATING_SYSTEM);
        }
        this.urlList = new URL[]
                {
                        new URL(assetsUrl + libs),
                        new URL(assetsUrl + natives),
                        new URL(clientUrl),
                };
    }

    public void run()
    {
        this.init();
        this.state = 3;
        this.percentage = 5;
        try
        {
            this.loadFileURLs();
            String path = AccessController.doPrivileged((PrivilegedExceptionAction<String>) ()
                    -> Util.getWorkingDirectory() + File.separator + "bin" + File.separator);

            File dir = new File(path);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            boolean cacheAvailable = false;
            if (this.canPlayOffline())
            {
                cacheAvailable = true;
                this.percentage = 90;
            }
            if (!cacheAvailable)
            {
                this.downloadFiles(path);
                this.renameJar(path);
                this.extractZipArchives(path);
            }
            this.updateClasspath(dir);
            this.state = 7;
        } catch (MalformedURLException | PrivilegedActionException e)
        {
            this.fatalException(e.getMessage(), e);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        } finally
        {
            this.lThread = null;
        }
    }
    protected void updateClasspath(File dir) throws MalformedURLException
    {
        this.state = 6;
        this.percentage = 95;

        Vector<URL> urls = new Vector<>();
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.getName().endsWith("jinput.jar")
                        || file.getName().endsWith("lwjgl.jar")
                        || file.getName().endsWith("lwjgl_util.jar")
                        || file.getName().endsWith("minecraft.jar"))
                {
                    urls.add(file.toURI().toURL());
                }
            }
        }
        URL[] urlArray = new URL[urls.size()];
        urls.copyInto(urlArray);

        if (cLoader == null)
        {
            cLoader = new URLClassLoader(urlArray, GameUpdater.class.getClassLoader());
        } else
        {
            try
            {
                cLoader.loadClass("net.minecraft.client.Minecraft");
            } catch (ClassNotFoundException e)
            {
                this.fatalException("Failed to load Minecraft class", e);
            }
        }


        String path;
        if (!(path = dir.getAbsolutePath()).endsWith(File.separator))
        {
            path = path + File.separator;
        }
        this.unloadNatives(path);
        System.setProperty("org.lwjgl.librarypath", path + "natives");
        System.setProperty("net.java.games.input.librarypath", path + "natives");
        natives_loaded = true;
    }

    private void unloadNatives(String nativePath)
    {
        if (natives_loaded)
        {
            try
            {
                Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
                field.setAccessible(true);

                Vector<?> libs = (Vector<?>) field.get(this.getClass().getClassLoader());
                String path = new File(nativePath).getCanonicalPath();
                for (int i = 0; i < libs.size(); i++)
                {
                    String s = (String) libs.get(i);
                    if (s.startsWith(path))
                    {
                        libs.remove(i);
                        --i;
                    }
                }
            } catch (Exception e)
            {
                this.fatalException("Error while unloading natives", e);
            }
        }
    }

    public Applet createApplet() throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return (Applet) cLoader.loadClass("net.minecraft.client.MinecraftApplet").newInstance();
    }

    protected void downloadFiles(String path) throws Exception
    {
        this.state = 4;

        int initialPercentage;
        int[] fileSizes = new int[this.urlList.length];
        byte[] buffer = new byte[1024];

        URLConnection connection;
        for (initialPercentage = 0; initialPercentage < this.urlList.length; initialPercentage++)
        {
            connection = this.urlList[initialPercentage].openConnection();
            connection.setDefaultUseCaches(false);

            fileSizes[initialPercentage] = connection.getContentLength();
            this.totalSizeDownload += fileSizes[initialPercentage];
        }
        initialPercentage = this.percentage = 10;

        for (URL url : this.urlList)
        {
            boolean downloadFile = true;

            while (downloadFile)
            {
                downloadFile = false;
                connection = url.openConnection();

                String currentFile = this.getFileName(url);

                InputStream is = this.getJarInputStream(currentFile, connection);
                FileOutputStream fos = new FileOutputStream(path + currentFile);

                long downloadStartTime = System.currentTimeMillis();
                int downloadedAmount = 0;
                int bufferSize;

                for (String downloadSpeedMessage = "";
                     (bufferSize = is.read(buffer, 0, buffer.length)) != -1;
                     this.subtaskMessage = this.subtaskMessage + downloadSpeedMessage)
                {
                    fos.write(buffer, 0, bufferSize);

                    this.currentSizeDownload += bufferSize;
                    this.percentage = initialPercentage + this.currentSizeDownload * 45 / this.totalSizeDownload;
                    this.subtaskMessage = "Retrieving: " + currentFile + " " + this.currentSizeDownload * 100 / this.totalSizeDownload + "%";
                    downloadedAmount += bufferSize;

                    long timeLapse = System.currentTimeMillis() - downloadStartTime;
                    if (timeLapse >= 1000L)
                    {
                        float downloadSpeed = (float) downloadedAmount / (float) timeLapse;
                        downloadSpeed = (float) ((int) (downloadSpeed * 100.0F)) / 100.0F;
                        downloadSpeedMessage = " @ " + downloadSpeed + " KB/sec";
                        downloadedAmount = 0;
                        downloadStartTime += 1000L;
                    }
                }
                is.close();
                fos.close();
            }
        } this.subtaskMessage = "";
    }

    protected InputStream getJarInputStream(String currentFile, final URLConnection urlconnection) throws Exception {
        final InputStream[] is = new InputStream[1];

        for (int j = 0; j < 3 && is[0] == null; j++)
        {
            try
            {
                is[0] = urlconnection.getInputStream();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        if (is[0] == null)
        {
            throw new Exception("Unable to download " + currentFile);
        } else
        {
            return is[0];
        }
    }

    protected void extractZIP(String path, String natives)
    {
        this.state = 5;

        int initialPercentage = this.percentage;

        try
        {
            ZipFile zipFile = new ZipFile(path);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();

                String name = entry.getName();
                if (entry.isDirectory())
                {
                    new File(natives + File.separator + name).mkdirs();
                } else
                {
                    totalSizeExtract = (int)((long) totalSizeExtract + entry.getSize());
                    File file = new File(natives + File.separator + name);
                    file.getParentFile().mkdirs();

                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(file);

                    int bufferSize;
                    for (byte[] buffer = new byte[1024];
                         (bufferSize = is.read(buffer, 0, buffer.length)) != -1;
                         this.subtaskMessage = "Extracting: " + entry.getName() + " " + currentSizeExtract * 100 / totalSizeExtract + "%")
                    {
                        fos.write(buffer, 0, bufferSize);
                        currentSizeExtract += bufferSize;
                        this.percentage = initialPercentage + currentSizeExtract * 20 / totalSizeExtract;
                    }
                    fos.close();
                    is.close();
                }
            }
            zipFile.close();
            new File(path).delete();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void extractZipArchives(String path)
    {
        String libsZip;
        String nativesZip;
        try
        {
            File libsDir = new File(path);
            if (!libsDir.exists())
            {
                libsDir.mkdirs();
            } else
            {
                switch (Util.getPlatform())
                {
                    case windows:
                        libsZip = "libs-windows.zip";
                        nativesZip = "natives-windows.zip";
                        break;
                    case linux:
                        libsZip = "libs-linux.zip";
                        nativesZip = "natives-linux.zip";
                        break;
                    case osx:
                        libsZip = "libs-osx.zip";
                        nativesZip = "natives-osx.zip";
                        break;
                    default:
                        throw new RuntimeException("Unknown OS: " + Util.OPERATING_SYSTEM);
                }
                extractZIP(path + libsZip, String.valueOf(libsDir));
                extractZIP(path + nativesZip, libsDir + File.separator + "natives");
            }
            this.subtaskMessage = "";
        } catch (Exception e)
        {
            this.fatalException("Error while extracting archives", e);
        }
    }

    protected String getFileName(URL url)
    {
        String file = url.getFile();
        if (file.contains("?"))
        {
            file = file.substring(0, file.indexOf("?"));
        }
        return file.substring(file.lastIndexOf("/") + 1);
    }

    protected void fatalException(String error, Exception e)
    {
        e.printStackTrace();
        this.fatalError = true;
        this.fatalErrorDescription = "Fatal error occurred (" + this.state + "): " + error;
    }

    protected void renameJar(String path)
    {
        File jarFile = new File(path + this.clientVersion + ".jar");
        File minecraftJar = new File(path + "minecraft.jar");
        jarFile.renameTo(minecraftJar);
    }

    public boolean canPlayOffline()
    {
        File dir;
        try
        {
            String path = AccessController.doPrivileged((PrivilegedExceptionAction<String>) ()
                    -> Util.getWorkingDirectory() + File.separator + "bin" + File.separator);
            dir = new File(path);
            if (!dir.exists() || Objects.requireNonNull(dir.list()).length == 0)
            {
                return false;
            }
        } catch (PrivilegedActionException e)
        {
            this.fatalException("Failed to create working directory", e);
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null)
        {
            return false;
        } else
        {
            for (File file : files)
            {
                if (file.getName().endsWith(".jar"))
                {
                    if (!file.exists())
                    {
                        return false;
                    }
                }
            }
        }
        return dir.exists();
    }
}