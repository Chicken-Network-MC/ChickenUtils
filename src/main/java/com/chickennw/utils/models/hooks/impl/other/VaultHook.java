package com.chickennw.utils.models.hooks.impl.other;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.OtherHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

@SuppressWarnings("unused")
public class VaultHook extends AbstractPluginHook implements OtherHook {

    private Economy econ;

    public VaultHook() {
        super("Vault Hook", true, "Vault");
        setupEconomy();
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new NullPointerException("Economy plugin is not found!");
        }

        econ = rsp.getProvider();
    }

    public void giveMoney(OfflinePlayer offlinePlayer, double amount) {
        econ.depositPlayer(offlinePlayer, amount);
    }

    public void takeMoney(OfflinePlayer offlinePlayer, double amount) {
        econ.withdrawPlayer(offlinePlayer, amount);
    }

    public double getPlayerMoney(OfflinePlayer offlinePlayer) {
        return econ.getBalance(offlinePlayer);
    }
}
