package fr.florianpal.hypercraftmessage;

import fr.florianpal.hypercraftmessage.listeners.ChatListener;
import fr.florianpal.hypercraftmessage.managers.ConfigurationManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class HypercraftMessage extends JavaPlugin {

    private ChatListener chatListener;
    private ConfigurationManager configurationManager;
    @Override
    public void onEnable() {
        configurationManager = new ConfigurationManager(this);

        chatListener = new ChatListener(this);
        Bukkit.getPluginManager().registerEvents(chatListener, this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "hc:chatbungee");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "hc:chatbukkit", chatListener);
    }

    public void createDefaultConfiguration(File actual, String defaultName) {
        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists()) {
            return;
        }

        InputStream input = null;
        try {
            JarFile file = new JarFile(this.getFile());
            ZipEntry copy = file.getEntry(defaultName);
            if (copy == null) throw new FileNotFoundException();
            input = file.getInputStream(copy);
        } catch (IOException e) {
            getLogger().severe("Unable to read default configuration: " + defaultName);
        }

        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                getLogger().info("Default configuration file written: " + actual.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException ignored) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public String setPlaceHolders(OfflinePlayer player, String message) {
        return PlaceholderAPI.setPlaceholders(player, message);
    }
}
