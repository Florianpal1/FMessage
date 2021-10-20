package fr.florianpal.fmessage.managers.commandManagers;

import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.queries.GroupeQueries;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;


public class GroupCommandManager {
    private FMessage plugin;
    private GroupeQueries groupeQueries;

    public GroupCommandManager(FMessage plugin) {
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