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

import co.aikar.commands.CommandManager;
import co.aikar.locales.MessageKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

/**
 * Feeds a {@code lang_*.yml} into ACF's locale registry.
 *
 * <p>This is the bulk of what used to be duplicated in the two {@code CommandManager}
 * classes. It only touches {@code getLocales()} and {@code addSupportedLanguage()}, both
 * declared on ACF's shared base class, so it works against either platform manager.</p>
 */
public final class AcfLanguageLoader {

    private AcfLanguageLoader() {
    }

    /**
     * @param dataFolder where the file lives on disk, created from the bundled default
     * @param fileName   e.g. {@code lang_fr.yml}
     * @return true when at least one message was registered
     */
    public static boolean load(CommandManager<?, ?, ?, ?, ?, ?> commandManager,
                               File dataFolder,
                               String fileName,
                               Locale locale) throws IOException {
        InputStream defaults = AcfLanguageLoader.class.getResourceAsStream("/" + fileName);
        YamlDocument document = YamlDocument.create(
                new File(dataFolder, fileName),
                Objects.requireNonNull(defaults, "Missing bundled language file: " + fileName),
                GeneralSettings.DEFAULT,
                DumperSettings.DEFAULT
        );
        return register(commandManager, document, locale);
    }

    /**
     * Flattens the two-level YAML into {@code parent.key} message keys.
     */
    static boolean register(CommandManager<?, ?, ?, ?, ?, ?> commandManager,
                            YamlDocument config,
                            Locale locale) {
        boolean loaded = false;
        for (Object parentKey : config.getKeys()) {
            Section inner = config.getSection(Route.from(parentKey));
            if (inner == null) {
                continue;
            }
            for (Object key : inner.getKeys()) {
                String value = inner.getString(Route.from(key));
                if (value != null && !value.isEmpty()) {
                    commandManager.getLocales().addMessage(locale, MessageKey.of(parentKey + "." + key), value);
                    loaded = true;
                }
            }
        }
        return loaded;
    }
}
