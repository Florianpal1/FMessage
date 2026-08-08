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

package fr.florianpal.fmessage.core.util;

import fr.florianpal.fmessage.platform.ProxyLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Writes a bundled default file to the data folder the first time the plugin runs.
 *
 * <p>Replaces the old {@code FileUtils}, which opened the plugin jar through
 * {@code ProtectionDomain} to find its own resources. Going through the class loader does
 * the same job, works identically on both proxies, and can be exercised from a test.</p>
 */
public final class ResourceExporter {

    private ResourceExporter() {
    }

    /**
     * Copies {@code resourceName} from the jar to {@code target}, unless it already exists.
     *
     * @return true when the file was actually written
     */
    public static boolean copyIfAbsent(String resourceName, File target, ProxyLogger logger) {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            logger.error("Unable to create the data folder: " + parent.getAbsolutePath());
            return false;
        }

        if (target.exists()) {
            return false;
        }

        try (InputStream input = ResourceExporter.class.getResourceAsStream("/" + resourceName)) {
            if (input == null) {
                logger.error("Unable to read default configuration: " + resourceName);
                return false;
            }
            try (OutputStream output = Files.newOutputStream(target.toPath())) {
                input.transferTo(output);
            }
            logger.info("Default configuration file written: " + target.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Unable to write default configuration: " + resourceName, e);
            return false;
        }
    }
}
