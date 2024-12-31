package org.qpneruy.autoLoginAddon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.qpneruy.autoLoginAddon.AutoLoginAddon;

import java.util.Objects;

public class AutoLoginCmd implements CommandExecutor {

    public AutoLoginCmd(AutoLoginAddon plugin) {
         Objects.requireNonNull(plugin.getCommand("autologin")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}
