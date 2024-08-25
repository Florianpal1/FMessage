
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
 * Last modification : 07/01/2022 23:07
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import fr.florianpal.fmessage.commands.*;
import fr.florianpal.fmessage.managers.ConfigurationManager;
import fr.florianpal.fmessage.managers.DatabaseManager;
import fr.florianpal.fmessage.managers.MessageListener;
import fr.florianpal.fmessage.managers.commandManagers.*;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.queries.GroupeMemberQueries;
import fr.florianpal.fmessage.queries.GroupeQueries;
import fr.florianpal.fmessage.queries.IgnoreQueries;
import org.slf4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Plugin(id = "fmessage", name = "FMessage", version = "1.0.0-SNAPSHOT",
        url = "https://florianpal.fr", description = "FMessage", authors = {"Florianpal"})
public class FMessage {

    private final ProxyServer server;

    private final org.slf4j.Logger logger;

    private final Path dataDirectory;

    private ConfigurationManager configurationManager;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;
    private CommandCompletionsManager commandCompletionsManager;

    private IgnoreCommandManager ignoreCommandManager;
    private GroupCommandManager groupCommandManager;
    private GroupMemberCommandManager groupMemberCommandManager;

    private IgnoreQueries ignoreQueries;
    private GroupeQueries groupeQueries;
    private GroupeMemberQueries groupeMemberQueries;

    private Map<Integer,Group> groups = new HashMap<>();
    private Map<UUID, List<UUID>> ignores = new HashMap<>();
    private final Map<UUID, UUID> playerMessage = new HashMap<>();
    private final List<UUID> playerSpy = new ArrayList<>();

    private final List<UUID> playerStaff = new ArrayList<>();

    public static final MinecraftChannelIdentifier BUKKIT_CHAT = MinecraftChannelIdentifier.from("fmessage:chatbukkit");

    public static final MinecraftChannelIdentifier BUNGEE_CHAT = MinecraftChannelIdentifier.from("fmessage:chatbungee");


    @Inject
    public FMessage(ProxyServer proxyServer, org.slf4j.Logger logger, @DataDirectory Path dataDirectory) {
        this.server = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {

        File languageFile = new File(dataDirectory.toFile(), "lang_fr.yml");
        createDefaultConfiguration(languageFile, "lang_fr.yml");

        server.getChannelRegistrar().register(BUKKIT_CHAT);
        server.getChannelRegistrar().register(BUNGEE_CHAT);

        configurationManager = new ConfigurationManager(this);

        databaseManager = new DatabaseManager(this);

        ignoreQueries = new IgnoreQueries(this);
        groupeQueries = new GroupeQueries(this);
        groupeMemberQueries = new GroupeMemberQueries(this);

        databaseManager.addRepository(ignoreQueries);
        databaseManager.addRepository(groupeQueries);
        databaseManager.addRepository(groupeMemberQueries);
        databaseManager.initializeTables();

        ignoreCommandManager = new IgnoreCommandManager(this);
        groupCommandManager = new GroupCommandManager(this);
        groupMemberCommandManager = new GroupMemberCommandManager(this);

        commandManager = new CommandManager(server, this);
        commandManager.registerDependency(ConfigurationManager.class, configurationManager);

        server.getEventManager().register(this, new MessageListener(this));

        commandCompletionsManager = new CommandCompletionsManager(this);

        commandManager.registerCommand(new MSGSpyCommand(this));
        commandManager.registerCommand(new MSGCommand(this));
        commandManager.registerCommand(new RCommand(this));
        commandManager.registerCommand(new IgnoreCommand(this));
        commandManager.registerCommand(new UnIgnoreCommand(this));
        commandManager.registerCommand(new GroupCommand(this));

        ignores = ignoreCommandManager.getIgnores();
        groups = groupCommandManager.getGroups();

        for(Map.Entry<Integer,Group> group : groups.entrySet()) {
            groupMemberCommandManager.setGroupsMembers(group.getValue());
        }

        getLogger().info("FMessage enabled");
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public void setPreviousPlayer(Player sender, Player target) {
        playerMessage.put(sender.getUniqueId(), target.getUniqueId());
    }

    public boolean isPreviousPlayerOnline(Player proxiedPlayer) {
        Optional<Player> proxiedPlayer1 = server.getPlayer(playerMessage.get(proxiedPlayer.getUniqueId()));
        return proxiedPlayer1.isPresent();
    }

    public Player getPreviousPlayer(Player proxiedPlayer) {
        return getServer().getPlayer(playerMessage.get(proxiedPlayer.getUniqueId())).get();
    }

    public boolean havePreviousPlayer(Player proxiedPlayer) {
        return playerMessage.containsKey(proxiedPlayer.getUniqueId());
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
            JarFile file = new JarFile(new File(FMessage.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
            ZipEntry copy = file.getEntry(defaultName);
            if (copy == null) throw new FileNotFoundException();
            input = file.getInputStream(copy);
        } catch (IOException e) {
            getLogger().error("Unable to read default configuration: " + defaultName);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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

    public List<UUID> getPlayerSpy() {
        return playerSpy;
    }

    public boolean isPlayerSpy(UUID player) {
        return playerSpy.contains(player);
    }

    public void supPlayerSpy(UUID player) {
        playerSpy.remove(player);
    }

    public void addPlayerSpy(UUID player) {
        this.playerSpy.add(player);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Map<UUID, List<UUID>> getIgnores() {
        return ignores;
    }
    public void updateIgnores() {
        ignores = ignoreCommandManager.getIgnores();
    }

    public IgnoreQueries getIgnoreQueries() {
        return ignoreQueries;
    }

    public IgnoreCommandManager getIgnoreCommandManager() {
        return ignoreCommandManager;
    }

    public GroupeQueries getGroupeQueries() {
        return groupeQueries;
    }

    public GroupeMemberQueries getGroupeMemberQueries() {
        return groupeMemberQueries;
    }

    public GroupCommandManager getGroupCommandManager() {
        return groupCommandManager;
    }

    public GroupMemberCommandManager getGroupMemberCommandManager() {
        return groupMemberCommandManager;
    }

    public Map<Integer, Group> getGroups() {
        return groups;
    }
    public void updateGroups() {
        groups = groupCommandManager.getGroups();
    }

    public boolean isPlayerStaff(UUID player) {
        return playerStaff.contains(player);
    }

    public void supPlayerStaff(UUID player) {
        playerStaff.remove(player);
    }

    public void addPlayerStaff(UUID player) {
        this.playerStaff.add(player);
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
