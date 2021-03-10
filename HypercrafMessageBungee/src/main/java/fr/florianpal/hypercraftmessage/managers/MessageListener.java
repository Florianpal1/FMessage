package fr.florianpal.hypercraftmessage.managers;

import co.aikar.commands.CommandIssuer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.florianpal.hypercraftmessage.HypercraftMessage;
import fr.florianpal.hypercraftmessage.languages.MessageKeys;
import fr.florianpal.hypercraftmessage.managers.commandManagers.CommandManager;
import fr.florianpal.hypercraftmessage.managers.commandManagers.GroupMemberCommandManager;
import fr.florianpal.hypercraftmessage.managers.commandManagers.IgnoreCommandManager;
import fr.florianpal.hypercraftmessage.objects.Member;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessageListener implements Listener {

    private HypercraftMessage plugin;
    private GroupMemberCommandManager groupMemberCommandManager;
    private IgnoreCommandManager ignoreCommandManager;
    private CommandManager commandManager;

    public MessageListener(HypercraftMessage plugin) {
        this.plugin = plugin;
        this.groupMemberCommandManager = plugin.getGroupMemberCommandManager();
        this.ignoreCommandManager = plugin.getIgnoreCommandManager();
        this.commandManager = plugin.getCommandManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(PluginMessageEvent event) {
        if (!event.isCancelled()) {
            if (event.getTag().equalsIgnoreCase("hc:chatbungee")) {

                ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

                String subchannel = in.readUTF();
                String uuid = in.readUTF();
                String messageWithFormat = in.readUTF();
                String message = in.readUTF();


                if (groupMemberCommandManager.alreadyToggle(UUID.fromString(uuid))) {
                    int id = groupMemberCommandManager.getGroupByToggle(ProxyServer.getInstance().getPlayer(UUID.fromString(uuid)));
                    for (Member member : plugin.getGroups().get(id).getMember()) {
                        ProxiedPlayer playerTarget = plugin.getProxy().getPlayer(member.getUuid());
                        if (playerTarget != null) {
                            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerTarget);
                            issuerTarget.sendInfo(MessageKeys.GROUP_MSG, "{group}", plugin.getGroups().get(id).getName(), "{player}", playerTarget.getDisplayName(), "{message}", message);
                            plugin.getLogger().info("[{" + plugin.getGroups().get(id).getName() + "}] " + playerTarget.getDisplayName() + " : " + message);
                        }
                    }
                } else {
                    for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF(subchannel);
                        out.writeUTF(messageWithFormat);
                        List<UUID> ignores = new ArrayList<>(ignoreCommandManager.getAreIgnores(UUID.fromString(uuid)));

                        String uuids = "";
                        for (UUID uuid1 : ignores) {
                            uuids = uuid1.toString() + ";";
                        }
                        out.writeUTF(uuids);
                        entry.getValue().sendData("hc:chatbukkit", out.toByteArray());
                    }
                }
            }
        }
    }
}
