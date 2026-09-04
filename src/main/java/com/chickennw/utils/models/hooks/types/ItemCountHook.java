package com.chickennw.utils.models.hooks.types;

import com.chickennw.utils.models.hooks.PluginHook;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public interface ItemCountHook extends PluginHook {

    int getAmount(Item item);

    void removeAmount(Item item, int amount);

    default void removeVanillaAmount(Item item, int amount) {
        ItemStack itemStack = item.getItemStack();
        int remaining = itemStack.getAmount() - amount;
        if (remaining <= 0) {
            item.remove();
            return;
        }

        itemStack.setAmount(remaining);
        item.setItemStack(itemStack);
    }
}
