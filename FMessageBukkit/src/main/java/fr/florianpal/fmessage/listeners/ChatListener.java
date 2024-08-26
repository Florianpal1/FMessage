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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        double result = (double)(nbr_maj)/nbr_min;


        if(unflood(e.getMessage())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(plugin.getConfigurationManager().getChat().getFloodFormat());
        }

        if(!e.isCancelled()) {
            if(e.getMessage().length() > 3) {
                if (result > 1) {
                    e.getPlayer().sendMessage(plugin.getConfigurationManager().getChat().getSpamFormat());

                    e.setMessage(e.getMessage().toLowerCase());
                }
            }

            e.setCancelled(true);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(e.getPlayer().getUniqueId().toString());

            String format = plugin.getConfigurationManager().getChat().getChatFormat();
            format = plugin.setPlaceHolders(e.getPlayer(), format);

            out.writeUTF(format.replace("{displayName}", e.getPlayer().getDisplayName()));
            out.writeUTF("" + e.getMessage());
            out.writeBoolean(plugin.getVaultIntegrationManager().getPerms().has(e.getPlayer(), "fmessage.colors"));
            e.getPlayer().sendPluginMessage(plugin, "fmessage:chatbungee", out.toByteArray());
        }
    }

    private static int nbr_min(String chaine) {
        int compteur=0;
        for(int i = 0; i<chaine.length(); i++){
            char ch = chaine.charAt(i);
            if(!(ch == ' ') && !(ch == '!') && !(ch == '?')) {
                if (Character.isLowerCase(ch)) compteur++;
            }
        }
        return compteur;
    }

    private static int nbr_maj(String chaine) {
        int compteur=0;
        for(int i = 0; i<chaine.length(); i++){
            char ch = chaine.charAt(i);
            if(!(ch == ' ') && !(ch == '!') && !(ch == '?')) {
                if (Character.isUpperCase(ch)) compteur++;
            }
        }
        return compteur;
    }

    private boolean unflood(String msg)
    {
        int tolerance = 7;
        char prev = msg.charAt(0);
        int occur = 1;

        for (int i = 1; i < msg.length(); ++i)
        {
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
                TextComponent formatWithPlaceholder = format(in.readUTF());
                String messageRecieved = in.readUTF();

                String[] uuids = in.readUTF().split(";");
                List<UUID> ignores = new ArrayList<UUID>();

                for(String uuid : uuids) {
                    if(!uuid.equalsIgnoreCase("")) {
                        ignores.add(UUID.fromString(uuid));
                    }
                }

                boolean colors = in.readBoolean();

                TextReplacementConfig textReplacementConfigMessageColored = TextReplacementConfig.builder()
                        .matchLiteral("{message}")
                        .replacement(format(messageRecieved))
                        .build();

                TextReplacementConfig textReplacementConfigMessageNonColored = TextReplacementConfig.builder()
                        .matchLiteral("{message}")
                        .replacement(messageRecieved)
                        .build();

                plugin.getLogger().info(LegacyComponentSerializer.legacyAmpersand().serialize(formatWithPlaceholder.replaceText(textReplacementConfigMessageColored)));
                for(Player player1 : plugin.getServer().getOnlinePlayers()) {
                    if(!ignores.contains(player1.getUniqueId())) {
                        if(colors) {
                            player1.sendMessage(formatWithPlaceholder.replaceText(textReplacementConfigMessageColored));
                        } else {
                            player1.sendMessage(formatWithPlaceholder.replaceText(textReplacementConfigMessageNonColored));
                        }

                    } else {
                        player1.sendMessage(format(plugin.getConfigurationManager().getChat().getIgnoreFormat()));
                    }
                }
            } else if (subchannel.equalsIgnoreCase("StaffMessage")) {
                String playerUUID = in.readUTF();
                TextComponent messageFinalWithoutMessage = format(in.readUTF());
                String playerMessage = in.readUTF();

                TextReplacementConfig textReplacementConfigMessageColored = TextReplacementConfig.builder()
                        .matchLiteral("{message}")
                        .replacement(format(playerMessage))
                        .build();

                TextComponent messageFinalWithMessage = (TextComponent) messageFinalWithoutMessage.replaceText(textReplacementConfigMessageColored);

                plugin.getLogger().info(LegacyComponentSerializer.legacyAmpersand().serialize(messageFinalWithMessage));
                for(Player player1 : plugin.getServer().getOnlinePlayers()) {
                    if(player1.hasPermission("fmessage.staffchat")) {
                        player1.sendMessage(messageFinalWithMessage);
                    }
                }
            } else if(subchannel.equalsIgnoreCase("song")) {
                String uuid = in.readUTF();

                Sound sound = Sound.sound(Key.key("entity.wither.death"), Sound.Source.MUSIC, 1f, 1f);
                Bukkit.getPlayer(UUID.fromString(uuid)).playSound(sound);
            }
        }
    }
    private TextComponent format(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }
}
