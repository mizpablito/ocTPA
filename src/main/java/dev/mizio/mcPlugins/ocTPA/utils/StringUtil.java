package dev.mizio.mcPlugins.ocTPA.utils;

import dev.mizio.mcPlugins.ocTPA.MainOcTPA;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.Map;

public class StringUtil {

    public final static Component PLUGIN_INFO_COMPONENT = Component.text()
            .append(Component.text("Dla ", NamedTextColor.YELLOW))
            .append(Component.text("open",  NamedTextColor.WHITE))
            .append(Component.text("Craft", NamedTextColor.DARK_GREEN))

            .append(Component.text(" | ", NamedTextColor.GRAY))

            .append(Component.text("Wersja: ", NamedTextColor.YELLOW))
            .append(Component.text(
                    MainOcTPA.instance().getPluginMeta().getVersion(),
                    NamedTextColor.DARK_GREEN
            ))

            .append(Component.text(" | ", NamedTextColor.GRAY))

            .append(Component.text(
                    MainOcTPA.instance().getPluginMeta().getAuthors().size() == 1
                            ? "Autor: "
                            : "Autorzy: ",
                    NamedTextColor.YELLOW
            ))
            .append(Component.text(
                    String.join(", ", MainOcTPA.instance().getPluginMeta().getAuthors()),
                    NamedTextColor.WHITE
            ))
            .appendNewline()
            .build();

    public static Tag prefixTag(final ArgumentQueue args, final Context ctx) {
        String rawPrefix     = MainOcTPA.instance().getPluginConfig().getPluginPrefix();
        Component mmPrefix   = ctx.deserialize(rawPrefix);

        return Tag.selfClosingInserting(mmPrefix.append(Component.space()));
    }

    public static Tag miniPrefixTag(final ArgumentQueue args, final Context ctx) {
        String rawPrefix     = MainOcTPA.instance().getPluginConfig().getPluginMiniPrefix();
        Component mmPrefix   = ctx.deserialize(rawPrefix);

        return Tag.selfClosingInserting(mmPrefix.append(Component.space()));
    }

    public static Component textFormatting(String text) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults()).build())
                .editTags(a -> a.tag("prefix", StringUtil::prefixTag))
                .editTags(b -> b.tag("miniprefix", StringUtil::miniPrefixTag))
                .build()
                .deserialize(text);
    }

    public static Component textFormatting(String text, Map<String, String> replacements) {
        String tmpText = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            tmpText = tmpText.replaceAll("%" + entry.getKey() + "%",  entry.getValue());
        }
        return textFormatting(tmpText);
    }
}
