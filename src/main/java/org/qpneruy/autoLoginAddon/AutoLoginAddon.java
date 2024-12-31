package org.qpneruy.autoLoginAddon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.qpneruy.autoLoginAddon.Events.JoinListener;
import org.qpneruy.autoLoginAddon.commands.AutoLoginCmd;
import org.qpneruy.autoLoginAddon.commands.AutoLoginTab;
import org.qpneruy.autoLoginAddon.data.Database.DatabaseManager;
import org.qpneruy.autoLoginAddon.data.Database.PlayerAuthRepository;

import java.util.logging.Level;

public final class AutoLoginAddon extends JavaPlugin {
    public static AutoLoginAddon instance;
    public static PlayerAuthRepository authRepo;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().log(Level.INFO, "AutoLoginAddon has been enabled!");
        getLogger().log(Level.INFO, "Version: 2025.1.1 - Author: qpneruy");

        intializeHooks();
        Bukkit.getServer().getPluginManager().registerEvents(new JoinListener(), this);

        new AutoLoginCmd(this);
        new AutoLoginTab(this);


    }

    @Override
    public void onDisable() {
        DatabaseManager.shutdown();
        getLogger().log(Level.INFO, "AutoLoginAddon has been disabled!");
    }

    private void intializeHooks() {
        if (this.getServer().getPluginManager().isPluginEnabled("AuthMe")) {
            getLogger().info("Successful Hook into AuthMe!");
            authRepo = PlayerAuthRepository.getINSTANCE();
        } else {
            getLogger().info("AuthMe not found! Disabling plugin");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }
}
