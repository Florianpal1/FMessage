package fr.florianpal.fmessage.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class FormatUtil {

    public static @NotNull TextComponent formatToTextComponent(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    public static @NotNull String format(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
    }
}
