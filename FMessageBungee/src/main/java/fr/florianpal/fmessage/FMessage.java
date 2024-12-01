
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
import fr.florianpal.fmessage.managers.MessageManager;
import fr.florianpal.fmessage.managers.commandManagers.*;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.queries.GroupeMemberQueries;
import fr.florianpal.fmessage.queries.GroupeQueries;
import fr.florianpal.fmessage.queries.IgnoreQueries;
import fr.florianpal.fmessage.queries.NickNameQueries;
import fr.florianpal.fmessage.utils.FileUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.util.*;

public class FMessage extends Plugin {

    private ConfigurationManager configurationManager;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;
    private CommandCompletionsManager commandCompletionsManager;

    private IgnoreCommandManager ignoreCommandManager;
    private GroupCommandManager groupCommandManager;
    private GroupMemberCommandManager groupMemberCommandManager;
    private NickNameCommandManager nickNameCommandManager;
    private MessageManager messageManager;

    private IgnoreQueries ignoreQueries;
    private GroupeQueries groupeQueries;
    private GroupeMemberQueries groupeMemberQueries;

    private NickNameQueries nickNameQueries;

    private Map<Integer,Group> groups = new HashMap<>();
    private Map<UUID, List<UUID>> ignores = new HashMap<>();
    private final Map<UUID, UUID> playerMessage = new HashMap<>();
    private final List<UUID> playerSpy = new ArrayList<>();

    private final List<UUID> playerStaff = new ArrayList<>();

    public static final String BUKKIT_CHAT = "fmessage:chatbukkit";

    public static final String BUNGEE_CHAT = "fmessage:chatbungee";

    @Override
    public void onEnable() {
        int pluginId = 24047;
        Metrics metrics = new Metrics(this, pluginId);

        configurationManager = new ConfigurationManager(getDataFolder());

        File languageFile = new File(getDataFolder(), "lang_" + configurationManager.getChat().getLang() + ".yml");
        FileUtils.createDefaultConfiguration(this, languageFile, "lang_" + configurationManager.getChat().getLang() + ".yml");

        getProxy().registerChannel(BUKKIT_CHAT);
        getProxy().registerChannel(BUNGEE_CHAT);

        databaseManager = new DatabaseManager(configurationManager);

        ignoreQueries = new IgnoreQueries(databaseManager);
        groupeQueries = new GroupeQueries(databaseManager);
        groupeMemberQueries = new GroupeMemberQueries(databaseManager);
        nickNameQueries = new NickNameQueries(databaseManager);

        databaseManager.addRepository(ignoreQueries);
        databaseManager.addRepository(groupeQueries);
        databaseManager.addRepository(groupeMemberQueries);
        databaseManager.addRepository(nickNameQueries);
        databaseManager.initializeTables();

        ignoreCommandManager = new IgnoreCommandManager(ignoreQueries);
        groupCommandManager = new GroupCommandManager(groupeQueries);
        groupMemberCommandManager = new GroupMemberCommandManager(groupeMemberQueries);
        nickNameCommandManager = new NickNameCommandManager(nickNameQueries);
        messageManager = new MessageManager(this);

        commandManager = new CommandManager(this);
        commandManager.registerDependency(ConfigurationManager.class, configurationManager);

        ProxyServer.getInstance().getPluginManager().registerListener(this, new MessageListener(this));

        commandCompletionsManager = new CommandCompletionsManager(this);

        commandManager.registerCommand(new MSGSpyCommand(this));
        commandManager.registerCommand(new MSGCommand(this));
        commandManager.registerCommand(new RCommand(this));
        commandManager.registerCommand(new IgnoreCommand(this));
        commandManager.registerCommand(new UnIgnoreCommand(this));
        commandManager.registerCommand(new GroupCommand(this));
        commandManager.registerCommand(new NickCommand(this));

        ignores = ignoreCommandManager.getIgnores();
        groups = groupCommandManager.getGroups();

        for(Map.Entry<Integer,Group> group : groups.entrySet()) {
            groupMemberCommandManager.setGroupsMembers(group.getValue());
        }

        getLogger().info("FMessage enabled");
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

    public List<UUID> getPlayerSpy() {
        return playerSpy;
    }

    public boolean isPlayerSpy(UUID player) {
        return playerSpy.contains(player);
    }

    public void removePlayerSpy(UUID player) {
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

    public NickNameQueries getNickNameQueries() {
        return nickNameQueries;
    }

    public NickNameCommandManager getNickNameCommandManager() {
        return nickNameCommandManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
}
