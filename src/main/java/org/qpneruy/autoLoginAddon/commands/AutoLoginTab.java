package org.qpneruy.autoLoginAddon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.qpneruy.autoLoginAddon.AutoLoginAddon;

import java.util.List;
import java.util.Objects;

public class AutoLoginTab implements TabCompleter {

    public AutoLoginTab(AutoLoginAddon plugin) {
        Objects.requireNonNull(plugin.getCommand("autologin")).setTabCompleter(this);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
