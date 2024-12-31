package org.qpneruy.autoLoginAddon.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import fr.xephi.authme.api.v3.AuthMeApi;

import java.util.Objects;
import java.util.UUID;

import static org.qpneruy.autoLoginAddon.AutoLoginAddon.authRepo;

public class JoinListener implements Listener {

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        String ipAddress = Objects.requireNonNull(player.getAddress()).getHostString();

        authRepo.registerPlayer(playerUuid);

        if (authRepo.isIpRegisteredForPlayer(playerUuid, ipAddress)) {
            AuthMeApi.getInstance().forceLogin(player);
        } else authRepo.registerPlayerIp(playerUuid, ipAddress);
        authRepo.updateLastLogin(playerUuid);
    }
}
