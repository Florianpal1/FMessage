
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private final int id;
    private final UUID owner;
    private final String name;
    private List<Member> member;

    public Group(int id, UUID owner, String name) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.member = new ArrayList<>();
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public List<Member> getMember() {
        return member;
    }

    public void setMember(List<Member> member) {
        this.member = member;
    }

    public int getId() {
        return id;
    }
}
