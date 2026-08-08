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

package fr.florianpal.fmessage.platform;

/**
 * Console logging, bridged to {@code java.util.logging} on BungeeCord and to SLF4J on
 * Velocity — the single reason {@code logger.severe} and {@code logger.error} used to be
 * the only difference between two otherwise identical files.
 */
public interface ProxyLogger {

    void info(String message);

    void warn(String message);

    void error(String message);

    void error(String message, Throwable throwable);
}
