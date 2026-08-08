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
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.core;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import fr.florianpal.fmessage.core.acf.AcfLanguageLoader;
import fr.florianpal.fmessage.core.acf.AcfMessenger;
import fr.florianpal.fmessage.core.acf.Messenger;
import fr.florianpal.fmessage.core.commands.GroupCommand;
import fr.florianpal.fmessage.core.commands.IgnoreCommand;
import fr.florianpal.fmessage.core.commands.MSGCommand;
import fr.florianpal.fmessage.core.commands.MSGSpyCommand;
import fr.florianpal.fmessage.core.commands.MSGToggleCommand;
import fr.florianpal.fmessage.core.commands.NickCommand;
import fr.florianpal.fmessage.core.commands.RCommand;
import fr.florianpal.fmessage.core.commands.ReloadCommand;
import fr.florianpal.fmessage.core.commands.UnIgnoreCommand;
import fr.florianpal.fmessage.core.service.ChatRelayService;
import fr.florianpal.fmessage.core.service.CompletionProviders;
import fr.florianpal.fmessage.core.service.PrivateMessageService;
import fr.florianpal.fmessage.core.state.SessionState;
import fr.florianpal.fmessage.core.util.ResourceExporter;
import fr.florianpal.fmessage.managers.ConfigurationManager;
import fr.florianpal.fmessage.managers.DatabaseManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.NickNameCommandManager;
import fr.florianpal.fmessage.managers.commandManagers.PlayerCommandManager;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.platform.ProxyPlatform;
import fr.florianpal.fmessage.platform.ProxyPlayer;
import fr.florianpal.fmessage.queries.GroupeMemberQueries;
import fr.florianpal.fmessage.queries.GroupeQueries;
import fr.florianpal.fmessage.queries.IgnoreQueries;
import fr.florianpal.fmessage.queries.NickNameQueries;
import fr.florianpal.fmessage.queries.PlayerQueries;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Everything FMessage does on a proxy, independent of which proxy it is.
 *
 * <p>Startup is deliberately split in two: the constructor brings up configuration, the
 * database and the caches, then {@link #registerCommands(CommandManager)} wires the ACF
 * manager in. The ACF manager cannot be built earlier — it needs the {@code lang} option
 * read from the configuration — and the services cannot be built earlier either, because
 * they need the ACF manager to resolve issuers.</p>
 *
 * <p>That ordering also fixes a long-standing bug: both previous implementations built
 * {@code MessageManager} <em>before</em> the command manager, so it captured a null
 * reference and threw as soon as a message hit an ignore or a msgtoggle.</p>
 */
public class FMessageCore {

    private final ProxyPlatform platform;

    private final ConfigurationManager configurationManager;
    private final DatabaseManager databaseManager;

    private final IgnoreQueries ignoreQueries;
    private final GroupeQueries groupeQueries;
    private final GroupeMemberQueries groupeMemberQueries;
    private final NickNameQueries nickNameQueries;
    private final PlayerQueries playerQueries;

    private final PlayerCommandManager playerCommandManager;
    private final IgnoreCommandManager ignoreCommandManager;
    private final GroupCommandManager groupCommandManager;
    private final GroupMemberCommandManager groupMemberCommandManager;
    private final NickNameCommandManager nickNameCommandManager;

    private final SessionState sessionState = new SessionState();

    private Map<Integer, Group> groups = new HashMap<>();
    private Map<UUID, List<UUID>> ignores = new HashMap<>();

    private CommandManager<?, ?, ?, ?, ?, ?> acf;
    private Messenger messenger;
    private PrivateMessageService privateMessageService;
    private ChatRelayService chatRelayService;
    private CompletionProviders completionProviders;

    public FMessageCore(ProxyPlatform platform) {
        this.platform = platform;

        File dataFolder = platform.getDataFolder();
        this.configurationManager = new ConfigurationManager(dataFolder);

        String languageFile = languageFileName();
        ResourceExporter.copyIfAbsent(languageFile, new File(dataFolder, languageFile), platform.getLogger());

        this.databaseManager = new DatabaseManager(configurationManager);

        this.ignoreQueries = new IgnoreQueries(databaseManager);
        this.groupeQueries = new GroupeQueries(databaseManager);
        this.groupeMemberQueries = new GroupeMemberQueries(databaseManager);
        this.nickNameQueries = new NickNameQueries(databaseManager);
        this.playerQueries = new PlayerQueries(databaseManager);

        databaseManager.addRepository(ignoreQueries);
        databaseManager.addRepository(groupeQueries);
        databaseManager.addRepository(groupeMemberQueries);
        databaseManager.addRepository(nickNameQueries);
        databaseManager.addRepository(playerQueries);
        databaseManager.initializeTables();

        this.playerCommandManager = new PlayerCommandManager(playerQueries);
        this.ignoreCommandManager = new IgnoreCommandManager(ignoreQueries);
        this.groupCommandManager = new GroupCommandManager(groupeQueries);
        this.groupMemberCommandManager = new GroupMemberCommandManager(groupeMemberQueries);
        this.nickNameCommandManager = new NickNameCommandManager(nickNameQueries);

        updateIgnores();
        updateGroups();
    }

    /**
     * Builds the services and hands the commands to ACF.
     *
     * @param acf the platform's command manager, already configured with its formats and
     *            language file
     */
    public void registerCommands(CommandManager<?, ?, ?, ?, ?, ?> acf) {
        this.acf = acf;
        this.messenger = new AcfMessenger(acf);

        this.privateMessageService = new PrivateMessageService(
                platform, messenger, configurationManager::getChat,
                ignoreCommandManager, nickNameCommandManager, sessionState);

        this.chatRelayService = new ChatRelayService(
                platform, messenger, groupMemberCommandManager,
                ignoreCommandManager, nickNameCommandManager, this::getGroups);

        this.completionProviders = new CompletionProviders(platform, this::getGroups);

        acf.registerDependency(ConfigurationManager.class, configurationManager);

        acf.registerCommand(new MSGSpyCommand(this));
        acf.registerCommand(new MSGCommand(this));
        acf.registerCommand(new RCommand(this));
        acf.registerCommand(new IgnoreCommand(this));
        acf.registerCommand(new UnIgnoreCommand(this));
        acf.registerCommand(new GroupCommand(this));
        acf.registerCommand(new NickCommand(this));
        acf.registerCommand(new MSGToggleCommand(this));
        acf.registerCommand(new ReloadCommand(this));
    }

    /**
     * Re-reads the language file selected by the {@code lang} option. Used by
     * {@code /fmessage reload}, and shared by both platforms: the loader only touches
     * ACF APIs declared on the base command manager.
     */
    public void reloadLanguage() {
        try {
            AcfLanguageLoader.load(acf, platform.getDataFolder(), languageFileName(), Locale.FRENCH);
        } catch (IOException e) {
            platform.getLogger().error("Failed to reload the language file", e);
        }
    }

    /**
     * @return {@code lang_xx.yml} for the language currently configured
     */
    public String languageFileName() {
        return "lang_" + configurationManager.getChat().getLang() + ".yml";
    }

    /**
     * Resolves the player behind a command issuer.
     *
     * @return empty when the command came from the console
     */
    public Optional<ProxyPlayer> playerOf(CommandIssuer issuer) {
        if (!issuer.isPlayer()) {
            return Optional.empty();
        }
        return platform.getPlayer(issuer.getUniqueId());
    }

    // -- Caches ------------------------------------------------------------------------

    public Map<Integer, Group> getGroups() {
        return groups;
    }

    public void updateGroups() {
        groups = groupCommandManager.getGroups();
        // getGroups() only returns the groups themselves: reload their members too,
        // otherwise the cache is left with empty member lists after every group command.
        for (Map.Entry<Integer, Group> group : groups.entrySet()) {
            groupMemberCommandManager.setGroupsMembers(group.getValue());
        }
    }

    public Map<UUID, List<UUID>> getIgnores() {
        return ignores;
    }

    public void updateIgnores() {
        ignores = ignoreCommandManager.getIgnores();
    }

    // -- Accessors ---------------------------------------------------------------------

    public ProxyPlatform getPlatform() {
        return platform;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public CommandManager<?, ?, ?, ?, ?, ?> getCommandManager() {
        return acf;
    }

    public PrivateMessageService getPrivateMessageService() {
        return privateMessageService;
    }

    public ChatRelayService getChatRelayService() {
        return chatRelayService;
    }

    public CompletionProviders getCompletionProviders() {
        return completionProviders;
    }

    public PlayerCommandManager getPlayerCommandManager() {
        return playerCommandManager;
    }

    public IgnoreCommandManager getIgnoreCommandManager() {
        return ignoreCommandManager;
    }

    public GroupCommandManager getGroupCommandManager() {
        return groupCommandManager;
    }

    public GroupMemberCommandManager getGroupMemberCommandManager() {
        return groupMemberCommandManager;
    }

    public NickNameCommandManager getNickNameCommandManager() {
        return nickNameCommandManager;
    }

    public IgnoreQueries getIgnoreQueries() {
        return ignoreQueries;
    }

    public GroupeQueries getGroupeQueries() {
        return groupeQueries;
    }

    public GroupeMemberQueries getGroupeMemberQueries() {
        return groupeMemberQueries;
    }

    public NickNameQueries getNickNameQueries() {
        return nickNameQueries;
    }

    public PlayerQueries getPlayerQueries() {
        return playerQueries;
    }
}
