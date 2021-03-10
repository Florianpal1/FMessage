package fr.florianpal.hypercraftmessage.listeners;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.florianpal.hypercraftmessage.HypercraftMessage;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener, PluginMessageListener {

    private HypercraftMessage plugin;
    public ChatListener(HypercraftMessage plugin) {
        this.plugin = plugin;

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent e) {

        int nbr_maj = nbr_maj(e.message().toString());
        int nbr_min = nbr_min(e.message().toString());
        double result = (double)(nbr_maj)/nbr_min;


        if(unflood(e.message().toString())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.YELLOW + "Votre message contient un flood. Il a été automatiquement supprimé.");
        }

        if(!e.isCancelled()) {
            if(e.message().toString().length() > 3) {
                if (result > 1) {
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "Votre message contient trop de majuscules. Il a été automatiquement mit en minuscule.");

                    e.message(Component.text(e.message().toString().toLowerCase()));
                }
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(e.getPlayer().getUniqueId().toString());

            String format = plugin.getConfigurationManager().getChat().getChatFormat();

            format = format.replace("{message}", e.message().toString());
            format = format.replace("{displayName}", e.getPlayer().getDisplayName());

            format = plugin.setPlaceHolders(e.getPlayer(), format);

            out.writeUTF(format);
            out.writeUTF(e.message().toString());
            e.getPlayer().sendPluginMessage(plugin, "hc:chatbungee", out.toByteArray());

            e.setCancelled(true);
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

        if (channel.equalsIgnoreCase("hc:chatbukkit")) {

            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();

            if (subchannel.equalsIgnoreCase("Message")) {
                String messageRecieved = in.readUTF();
                messageRecieved = format(messageRecieved);

                String[] uuids = in.readUTF().split(";");
                List<UUID> ignores = new ArrayList<UUID>();

                for(String uuid : uuids) {
                    if(!uuid.equalsIgnoreCase("")) {
                        ignores.add(UUID.fromString(uuid));
                    }
                }

                plugin.getLogger().info(messageRecieved);
                for(Player player1 : plugin.getServer().getOnlinePlayers()) {
                    if(!ignores.contains(player1.getUniqueId())) {
                        player1.sendMessage(messageRecieved);
                    } else {
                        player1.sendMessage(format(plugin.getConfigurationManager().getChat().getIgnoreFormat()));
                    }
                }
            }
        }
    }
    private String format(String msg) {
        Pattern pattern = Pattern.compile("[{]#[a-fA-F0-9]{6}[}]");
        if (Bukkit.getVersion().contains("1.16")) {
            Matcher match = pattern.matcher(msg);
            while (match.find()) {

                String color = msg.substring(match.start(), match.end());
                String replace = color;
                color = color.replace("{", "");
                color = color.replace("}", "");
                msg = msg.replace(replace, ChatColor.of(color) + "");
                match = pattern.matcher(msg);

            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
