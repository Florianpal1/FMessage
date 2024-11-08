

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
 * Last modification : 07/01/2022 23:05
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.florianpal.fmessage.FMessage;
import org.bukkit.entity.Player;


@CommandAlias("mc|staffchat")
public class StaffCommand extends BaseCommand {
    private final FMessage plugin;

    private static final String STAFF_CHAT = "StaffMessage";

    public StaffCommand(FMessage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("fmessage.staffchat")
    @Description("{@@fmessage.staffchat_help_description}")
    @Syntax("[message]")
    public void onStaffChat(Player playerSender, String message) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(STAFF_CHAT);
        out.writeUTF(playerSender.getUniqueId().toString());
        out.writeUTF(playerSender.getName());
        String format = plugin.getConfigurationManager().getChat().getStaffChatFormat();
        format = plugin.setPlaceHolders(playerSender, format);
        out.writeUTF(format.replace("{displayName}", playerSender.getDisplayName()));
        out.writeUTF(message);
        playerSender.sendPluginMessage(plugin, "fmessage:chatbungee", out.toByteArray());
    }
}
