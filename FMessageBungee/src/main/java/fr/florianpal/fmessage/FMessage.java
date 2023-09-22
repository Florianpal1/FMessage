
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

import fr.florianpal.fmessage.commands.*;
import fr.florianpal.fmessage.managers.ConfigurationManager;
import fr.florianpal.fmessage.managers.DatabaseManager;
import fr.florianpal.fmessage.managers.MessageListener;
import fr.florianpal.fmessage.managers.commandManagers.*;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.queries.GroupeMemberQueries;
import fr.florianpal.fmessage.queries.GroupeQueries;
import fr.florianpal.fmessage.queries.IgnoreQueries;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class FMessage extends Plugin {

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

    @Override
    public void onEnable() {

        File languageFile = new File(getDataFolder(), "lang_fr.yml");
        createDefaultConfiguration(languageFile, "lang_fr.yml");

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

        commandManager = new CommandManager(this);
        commandManager.registerDependency(ConfigurationManager.class, configurationManager);

        getProxy().registerChannel("fmessage:chatbukkit");
        getProxy().registerChannel("fmessage:chatbungee");
        ProxyServer.getInstance().getPluginManager().registerListener(this, new MessageListener(this));

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

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public void setPreviousPlayer(ProxiedPlayer sender, ProxiedPlayer target) {
        playerMessage.put(sender.getUniqueId(), target.getUniqueId());
    }

    public boolean isPreviousPlayerOnline(ProxiedPlayer proxiedPlayer) {
        ProxiedPlayer proxiedPlayer1 = getProxy().getPlayer(playerMessage.get(proxiedPlayer.getUniqueId()));
        return proxiedPlayer1 != null;
    }

    public ProxiedPlayer getPreviousPlayer(ProxiedPlayer proxiedPlayer) {
        return getProxy().getPlayer(playerMessage.get(proxiedPlayer.getUniqueId()));
    }

    public boolean havePreviousPlayer(ProxiedPlayer proxiedPlayer) {
        return playerMessage.containsKey(proxiedPlayer.getUniqueId());
    }

    public String format(String msg) {
        Pattern pattern = Pattern.compile("[{]#[a-fA-F0-9]{6}[}]");
        Matcher match = pattern.matcher(msg);
        while (match.find()) {
            String color = msg.substring(match.start(), match.end());
            String replace = color;
            color = color.replace("{", "");
            color = color.replace("}", "");
            msg = msg.replace(replace, ChatColor.of(color).toString());
            match = pattern.matcher(msg);
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
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
}
