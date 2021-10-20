package fr.florianpal.fmessage.managers.commandManagers;

import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.queries.IgnoreQueries;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;


public class IgnoreCommandManager {
    private FMessage plugin;
    private IgnoreQueries ignoreQueries;

    public IgnoreCommandManager(FMessage plugin) {
        this.plugin = plugin;
        this.ignoreQueries = plugin.getIgnoreQueries();
    }

    public Map<UUID, List<UUID>> getIgnores() {
        return ignoreQueries.getIgnores();
    }

    public List<UUID> getAreIgnores(UUID uuid) {
        return ignoreQueries.getAreIgnores(uuid);
    }

    public void addIgnore(ProxiedPlayer playerSender, ProxiedPlayer playerTarget)  {
        ignoreQueries.addIgnore(playerSender, playerTarget);
    }


    public void removeIgnore(ProxiedPlayer playerSender, ProxiedPlayer playerTarget) {
        ignoreQueries.removeIgnore(playerSender, playerTarget);
    }

    public boolean ignoreExist(ProxiedPlayer playerSender, ProxiedPlayer playerTarget) {
        return ignoreQueries.ignoreExist(playerSender, playerTarget);
    }
}