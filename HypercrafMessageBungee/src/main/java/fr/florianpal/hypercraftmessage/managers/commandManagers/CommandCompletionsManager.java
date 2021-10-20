package fr.florianpal.fmessage.managers.commandManagers;


import co.aikar.commands.BungeeCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import fr.florianpal.fmessage.HypercraftMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.stream.Collectors;

public class CommandCompletionsManager {
    private HypercraftMessage plugin;

    public CommandCompletionsManager(HypercraftMessage plugin) {
        this.plugin = plugin;
        registerCommandCompletions();
    }

    private void registerCommandCompletions() {
        CommandCompletions<BungeeCommandCompletionContext> commandCompletions = plugin.getCommandManager().getCommandCompletions();
        commandCompletions.registerAsyncCompletion("bungeeplayers", c -> {
            CommandSender sender = c.getSender();
            if (sender instanceof ProxiedPlayer) {
                System.out.println(plugin.getProxy().getPlayers());
                return plugin.getProxy().getPlayers()
                        .stream()
                        .map(ProxiedPlayer::getDisplayName)
                        .collect(Collectors.toList());
            }
            return null;
        });
    }
}
