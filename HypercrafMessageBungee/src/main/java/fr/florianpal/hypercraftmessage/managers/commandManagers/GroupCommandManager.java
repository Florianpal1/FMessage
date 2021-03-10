package fr.florianpal.hypercraftmessage.managers.commandManagers;

import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.objects.Group;
import fr.florianpal.hypercraftmessage.queries.GroupeQueries;
import fr.florianpal.hypercraftmessage.queries.IgnoreQueries;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class GroupCommandManager {
    private HypercraftMessage plugin;
    private GroupeQueries groupeQueries;

    public GroupCommandManager(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.groupeQueries = plugin.getGroupeQueries();
    }

    public Map<Integer, Group> getGroups() {
        return groupeQueries.getGroups();
    }

    public void addGroup(ProxiedPlayer playerSender, String name)  {
        groupeQueries.addGroupe(playerSender, name);
    }

    public int getGroupId(ProxiedPlayer playerSender, String name)  {
        return groupeQueries.getGroupId(playerSender, name);
    }
    public int getGroupId(String name)  {
        return groupeQueries.getGroupId(name);
    }


    public void removeGroup(int id_group) {
        groupeQueries.removeGroup(id_group);
    }

    public boolean groupExist(ProxiedPlayer playerSender, String name) {
        return groupeQueries.groupExist(playerSender, name);
    }

    public boolean groupExist(String name) {
        return groupeQueries.groupExist(name);
    }
}