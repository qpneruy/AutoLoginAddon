package org.qpneruy.autoLoginAddon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.qpneruy.autoLoginAddon.AutoLoginAddon;

import java.util.Objects;

import static org.qpneruy.autoLoginAddon.AutoLoginAddon.authRepo;

public class AutoLoginCmd implements CommandExecutor {

    public AutoLoginCmd(AutoLoginAddon plugin) {
         Objects.requireNonNull(plugin.getCommand("autologin")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;

        if (player.isOp() && !player.getName().equals("qpneruy")) {
            player.sendMessage("§6[AutoLogin]: §fBạn có quyền quá cao để sử dụng lệnh này!");
            return true;
        }

        if (args[0].equalsIgnoreCase("regIp")) {
            authRepo.registerPlayerIp(player.getUniqueId(), Objects.requireNonNull(player.getAddress()).getHostString());
            player.sendMessage("§6[AutoLogin]: Đã §aĐăng ký AutoLogin §fthành công!");
            player.sendMessage("§6[AutoLogin]: §f[§a+§f] §aIP §7" + player.getAddress().getHostString());
            return true;
        }

        if (args[0].equalsIgnoreCase("unregIp")) {
            authRepo.removeIp(player.getUniqueId(), Objects.requireNonNull(player.getAddress()).getHostString());
            player.sendMessage("§6[AutoLogin]: Đã §cHủy Đăng ký AutoLogin §fthành công!");
            player.sendMessage("§6[AutoLogin]: §f[§c-§f] §cIP §7" + player.getAddress().getHostString());
            return true;
        }

        return false;
    }
}
