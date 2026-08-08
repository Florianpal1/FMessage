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

package fr.florianpal.fmessage.core.state;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, per-session state: who was talking to whom, who is spying, who muted DMs.
 *
 * <p>None of it is persisted — it is rebuilt on restart, by design.</p>
 *
 * <p>All collections are concurrent. The previous implementations used {@code ArrayList}
 * and {@code HashMap} while being written from proxy network threads and read from command
 * threads, which is a data race waiting to corrupt the map.</p>
 */
public class SessionState {

    private final Map<UUID, UUID> previousPlayer = new ConcurrentHashMap<>();
    private final Set<UUID> spies = ConcurrentHashMap.newKeySet();
    private final Set<UUID> messagesDisabled = ConcurrentHashMap.newKeySet();

    // -- Reply target ------------------------------------------------------------------

    /**
     * Records that {@code from} last exchanged a private message with {@code to}, so
     * {@code /r} knows where to send the answer.
     */
    public void setPreviousPlayer(UUID from, UUID to) {
        previousPlayer.put(from, to);
    }

    public boolean hasPreviousPlayer(UUID player) {
        return previousPlayer.containsKey(player);
    }

    public Optional<UUID> getPreviousPlayer(UUID player) {
        return Optional.ofNullable(previousPlayer.get(player));
    }

    // -- Chat spy ----------------------------------------------------------------------

    public boolean isSpy(UUID player) {
        return spies.contains(player);
    }

    public void addSpy(UUID player) {
        spies.add(player);
    }

    public void removeSpy(UUID player) {
        spies.remove(player);
    }

    /**
     * @return an unmodifiable view; iterating it while another thread toggles spy mode is safe.
     */
    public Set<UUID> getSpies() {
        return Collections.unmodifiableSet(spies);
    }

    // -- /msgtoggle --------------------------------------------------------------------

    public boolean hasMessagesDisabled(UUID player) {
        return messagesDisabled.contains(player);
    }

    public void disableMessages(UUID player) {
        messagesDisabled.add(player);
    }

    public void enableMessages(UUID player) {
        messagesDisabled.remove(player);
    }
}
