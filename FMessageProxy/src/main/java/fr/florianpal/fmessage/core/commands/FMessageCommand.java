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

package fr.florianpal.fmessage.core.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.platform.ProxyPlayer;

/**
 * Shared plumbing for every FMessage command.
 *
 * <p>Commands take a {@link CommandIssuer} rather than a platform player type — that single
 * change is what lets the nine command classes be written once instead of twice. ACF injects
 * it natively on both platforms through the issuer-only context registered by its core.</p>
 *
 * <p>The trade-off is that ACF no longer rejects console senders on our behalf, so any
 * command that needs a player must go through {@link #requirePlayer(CommandIssuer)}.</p>
 */
public abstract class FMessageCommand extends BaseCommand {

    protected final FMessageCore core;

    protected FMessageCommand(FMessageCore core) {
        this.core = core;
    }

    /**
     * @return the player behind the issuer, or null after telling the console it cannot run this
     */
    protected ProxyPlayer requirePlayer(CommandIssuer issuer) {
        ProxyPlayer player = core.playerOf(issuer).orElse(null);
        if (player == null) {
            issuer.sendError(co.aikar.commands.MessageKeys.NOT_ALLOWED_ON_CONSOLE);
        }
        return player;
    }
}
