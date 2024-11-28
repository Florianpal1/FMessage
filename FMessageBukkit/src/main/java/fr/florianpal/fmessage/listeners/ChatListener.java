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
 * Last modification : 12/01/2022 17:42
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.listeners;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.utils.StringUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener, PluginMessageListener {

    private final FMessage plugin;

    public ChatListener(FMessage plugin) {
        this.plugin = plugin;

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChatSync(PlayerChatEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChatAsync(AsyncPlayerChatEvent e) {

        int nbr_maj = nbr_maj(e.getMessage());
        int nbr_min = nbr_min(e.getMessage());
        double result = (double) (nbr_maj) / nbr_min;


        if (!e.getPlayer().hasPermission("fmessage.bypass.flood") && unflood(e.getMessage())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(plugin.getConfigurationManager().getChat().getFloodFormat());
        }

        if (!e.isCancelled()) {
            if (!e.getPlayer().hasPermission("fmessage.bypass.spam") && e.getMessage().length() > 3) {
                if (result > 1) {
                    e.getPlayer().sendMessage(plugin.getConfigurationManager().getChat().getSpamFormat());

                    e.setMessage(e.getMessage().toLowerCase());
                }
            }

            e.setCancelled(true);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(e.getPlayer().getUniqueId().toString());
            out.writeUTF(e.getPlayer().getDisplayName());

            String format = plugin.getConfigurationManager().getChat().getChatFormat();
            format = plugin.setPlaceHolders(e.getPlayer(), format);

            out.writeUTF(format);
            out.writeUTF("" + e.getMessage());
            out.writeBoolean(e.getPlayer().hasPermission("fmessage.colors"));
            out.writeBoolean(e.getPlayer().hasPermission("fmessage.nick.colors"));
            e.getPlayer().sendPluginMessage(plugin, "fmessage:chatbungee", out.toByteArray());
        }
    }

    private static int nbr_min(String chaine) {
        int compteur = 0;
        for (int i = 0; i < chaine.length(); i++) {
            char ch = chaine.charAt(i);
            if (!(ch == ' ') && !(ch == '!') && !(ch == '?')) {
                if (Character.isLowerCase(ch)) compteur++;
            }
        }
        return compteur;
    }

    private static int nbr_maj(String chaine) {
        int compteur = 0;
        for (int i = 0; i < chaine.length(); i++) {
            char ch = chaine.charAt(i);
            if (!(ch == ' ') && !(ch == '!') && !(ch == '?')) {
                if (Character.isUpperCase(ch)) compteur++;
            }
        }
        return compteur;
    }

    private boolean unflood(String msg) {
        int tolerance = 7;
        char prev = msg.charAt(0);
        int occur = 1;

        for (int i = 1; i < msg.length(); ++i) {
            if (msg.charAt(i) == prev && prev != ' ') {
                occur++;
            } else {
                occur = 1;
                prev = msg.charAt(i);
            }
            if (occur >= tolerance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (channel.equalsIgnoreCase("fmessage:chatbukkit")) {

            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();

            if (subchannel.equalsIgnoreCase("Message")) {
                String playerUUID = in.readUTF();
                String displayName = in.readUTF();
                String nickName = in.readUTF();
                TextComponent formatWithPlaceholder = StringUtils.format(in.readUTF());
                String messageRecieved = in.readUTF();

                String[] uuids = in.readUTF().split(";");
                List<UUID> ignores = new ArrayList<UUID>();

                for (String uuid : uuids) {
                    if (!uuid.equalsIgnoreCase("")) {
                        ignores.add(UUID.fromString(uuid));
                    }
                }

                boolean colors = in.readBoolean();
                boolean nickColors = in.readBoolean();

                String finalName = StringUtils.isNullOrEmpty(nickName) ? displayName : nickName;

                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (!ignores.contains(p.getUniqueId())) {

                        formatWithPlaceholder = StringUtils.replace(formatWithPlaceholder, "{displayName}", finalName, nickColors);
                        p.sendMessage(StringUtils.replace(formatWithPlaceholder, "{message}", messageRecieved, colors));
                    } else {
                        p.sendMessage(StringUtils.format(plugin.getConfigurationManager().getChat().getIgnoreFormat()));
                    }
                }
            } else if (subchannel.equalsIgnoreCase("StaffMessage")) {
                String playerUUID = in.readUTF();
                String displayName = in.readUTF();
                String nickName = in.readUTF();
                TextComponent messageFinalWithoutMessage = StringUtils.format(in.readUTF());
                String playerMessage = in.readUTF();

                String finalName = StringUtils.isNullOrEmpty(nickName) ? nickName : displayName;

                TextComponent messageFinalWithMessage = StringUtils.replace(messageFinalWithoutMessage, "{message}", playerMessage, true);
                messageFinalWithMessage = StringUtils.replace(messageFinalWithMessage, "{displayName}", finalName, true);

                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("fmessage.staffchat")) {
                        p.sendMessage(messageFinalWithMessage);
                    }
                }
            }
        }
    }
}
