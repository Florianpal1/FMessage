/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 20/10/2021 19:57
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage;

import fr.florianpal.fmessage.commands.StaffCommand;
import fr.florianpal.fmessage.listeners.ChatListener;
import fr.florianpal.fmessage.managers.CommandManager;
import fr.florianpal.fmessage.managers.ConfigurationManager;
import fr.florianpal.fmessage.managers.VaultIntegrationManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class FMessage extends JavaPlugin {

    private ChatListener chatListener;
    private ConfigurationManager configurationManager;
    private VaultIntegrationManager vaultIntegrationManager;

    private CommandManager commandManager;
    @Override
    public void onEnable() {

        configurationManager = new ConfigurationManager(this);

        File languageFile = new File(getDataFolder(), "lang_" + configurationManager.getChat().getLang() + ".yml");
        createDefaultConfiguration(languageFile, "lang_" + configurationManager.getChat().getLang() + ".yml");

        vaultIntegrationManager = new VaultIntegrationManager(this);

        chatListener = new ChatListener(this);
        Bukkit.getPluginManager().registerEvents(chatListener, this);

        commandManager = new CommandManager(this);
        commandManager.registerCommand(new StaffCommand(this));

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "fmessage:chatbungee");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "fmessage:chatbukkit", chatListener);
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

    public VaultIntegrationManager getVaultIntegrationManager() {
        return vaultIntegrationManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
