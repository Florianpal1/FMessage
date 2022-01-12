
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
 * Last modification : 20/10/2021 19:57
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.objects;

import java.util.UUID;

public class Member {
    private final UUID uuid;
    private final boolean toggle;

    public Member(UUID uuid, boolean toggle) {
        this.uuid = uuid;
        this.toggle = toggle;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isToggle() {
        return toggle;
    }
}
