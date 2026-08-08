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

package fr.florianpal.fmessage.core.service;

import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;
import fr.florianpal.fmessage.platform.ProxyPlatform;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The data behind tab completion.
 *
 * <p>Registration itself stays in the adapters: ACF's {@code getCommandCompletions()}
 * returns a wildcard {@code CommandCompletions<?>} on the shared base class, so the handler
 * can only be typed against the platform-specific context. That leaves about twenty lines
 * per platform — everything that could be shared is here.</p>
 */
public class CompletionProviders {

    private final ProxyPlatform platform;
    private final Supplier<Map<Integer, Group>> groups;

    public CompletionProviders(ProxyPlatform platform, Supplier<Map<Integer, Group>> groups) {
        this.platform = platform;
        this.groups = groups;
    }

    /**
     * Every player connected to the proxy, all servers included.
     */
    public List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (ProxyPlayer player : platform.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    /**
     * Groups the player belongs to: the only ones {@code /group msg} and
     * {@code /group toggle} accept.
     */
    public List<String> groupsOf(UUID player) {
        List<String> names = new ArrayList<>();
        for (Group group : groups.get().values()) {
            for (Member member : group.getMember()) {
                if (member.getUuid().equals(player)) {
                    names.add(group.getName());
                    break;
                }
            }
        }
        return names;
    }

    /**
     * Groups the player owns: the ones {@code /group remove} and {@code /group member} accept.
     */
    public List<String> ownedGroupsOf(UUID player) {
        List<String> names = new ArrayList<>();
        for (Group group : groups.get().values()) {
            if (group.getOwner().equals(player)) {
                names.add(group.getName());
            }
        }
        return names;
    }
}
