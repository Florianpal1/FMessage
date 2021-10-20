package fr.florianpal.fmessage.managers.commandManagers;

import fr.florianpal.fmessage.HypercraftMessage;
import fr.florianpal.fmessage.queries.IgnoreQueries;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;


public class IgnoreCommandManager {
    private HypercraftMessage plugin;
    private IgnoreQueries ignoreQueries;

    public IgnoreCommandManager(HypercraftMessage plugin) {
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