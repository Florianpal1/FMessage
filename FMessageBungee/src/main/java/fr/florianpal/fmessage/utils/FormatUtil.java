package fr.florianpal.fmessage.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class FormatUtil {

    public static @NotNull TextComponent format(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }
}
