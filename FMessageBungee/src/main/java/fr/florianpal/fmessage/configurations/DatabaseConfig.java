
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

package fr.florianpal.fmessage.configurations;

import net.md_5.bungee.config.Configuration;

public class DatabaseConfig {

    private String url;
    private String user;
    private String password;

    public void load(Configuration config) {
        url = config.getString("database.url");
        user = config.getString("database.user");
        password = config.getString("database.password");
    }

    public void save(Configuration config) {
        config.set("database.url", url);
        config.set("database.user", user);
        config.set("database.password", password);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
