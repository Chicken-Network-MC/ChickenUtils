package com.chickennw.utils.models.hooks.impl.other;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.OtherHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.lynuxcraft.deadsilenceiv.advancedchests.AdvancedChestsAPI;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.gui.page.ChestPage;

@SuppressWarnings("unused")
public class AdvancedChestsHook extends AbstractPluginHook implements OtherHook {

    private Economy econ;

    public AdvancedChestsHook() {
        super("AdvancedChests Hook", true, "AdvancedChests");
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

    public boolean isAdvancedChest(Location location) {
        AdvancedChest<?, ?> advancedChest = AdvancedChestsAPI.getChestManager().getAdvancedChest(location);
        return advancedChest != null;
    }

    public boolean canAddItem(Location location, ItemStack itemStack) {
        AdvancedChest<?, ?> advancedChest = AdvancedChestsAPI.getChestManager().getAdvancedChest(location);
        return advancedChest != null && advancedChest.getSlotsLeft() >= itemStack.getAmount();
    }

    public void addItem(Location location, ItemStack itemStack) {
        AdvancedChest<?, ?> advancedChest = AdvancedChestsAPI.getChestManager().getAdvancedChest(location);
        if (advancedChest == null) return;

        int remaining = itemStack.getAmount();
        int maxStackSize = itemStack.getMaxStackSize();

        for (ChestPage<?> page : advancedChest.getPages()) {
            Inventory inventory = page.getBukkitInventory();
            if (inventory == null) continue;

            for (int slot : page.getInputSlots()) {
                if (remaining <= 0) return;

                ItemStack current = inventory.getItem(slot);
                if (current == null || !current.isSimilar(itemStack) || current.getAmount() >= maxStackSize) continue;

                int transfer = Math.min(remaining, maxStackSize - current.getAmount());
                current.setAmount(current.getAmount() + transfer);
                inventory.setItem(slot, current);
                remaining -= transfer;
            }
        }

        for (ChestPage<?> page : advancedChest.getPages()) {
            Inventory inventory = page.getBukkitInventory();
            if (inventory == null) continue;

            for (int slot : page.getInputSlots()) {
                if (remaining <= 0) return;

                ItemStack current = inventory.getItem(slot);
                if (current != null && !current.getType().isAir()) continue;

                int amount = Math.min(remaining, maxStackSize);
                ItemStack toPlace = itemStack.clone();
                toPlace.setAmount(amount);
                inventory.setItem(slot, toPlace);
                remaining -= amount;
            }
        }
    }
}
