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

package fr.florianpal.fmessage.core.acf;

import co.aikar.commands.CommandIssuer;
import fr.florianpal.fmessage.platform.ProxyPlayer;

/**
 * Resolves the ACF issuer of a player, so the core can send localized {@code MessageKeys}
 * to someone who is not the command sender.
 *
 * <p>{@link CommandIssuer} is part of ACF's shared core, present identically in
 * {@code acf-bungee} and {@code acf-velocity}, so depending on it from the core is safe.</p>
 */
public interface Messenger {

    CommandIssuer issuerOf(ProxyPlayer player);
}
