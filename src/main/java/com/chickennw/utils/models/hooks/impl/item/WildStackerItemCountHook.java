package com.chickennw.utils.models.hooks.impl.item;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ItemCountHook;
import org.bukkit.entity.Item;

@SuppressWarnings("unused")
public class WildStackerItemCountHook extends AbstractPluginHook implements ItemCountHook {

    public WildStackerItemCountHook() {
        super("WildStacker Item Count Hook", true, "WildStacker");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksFile.getItemCountHooks().isWildStacker();
    }

    @Override
    public int getAmount(Item item) {
        int amount = WildStackerAPI.getItemAmount(item);
        return amount > 0 ? amount : item.getItemStack().getAmount();
    }

    @Override
    public void removeAmount(Item item, int amount) {
        StackedItem stackedItem = WildStackerAPI.getStackedItem(item);
        if (stackedItem == null) {
            removeVanillaAmount(item, amount);
            return;
        }

        int remaining = stackedItem.getStackAmount() - amount;
        if (remaining > 0) {
            stackedItem.setStackAmount(remaining, true);
            return;
        }

        stackedItem.remove();
        item.remove();
    }
}
