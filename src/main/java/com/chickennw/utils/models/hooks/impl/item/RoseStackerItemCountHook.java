package com.chickennw.utils.models.hooks.impl.item;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.ItemCountHook;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.entity.Item;

@SuppressWarnings("unused")
public class RoseStackerItemCountHook extends AbstractPluginHook implements ItemCountHook {

    private RoseStackerAPI roseStackerAPI;

    public RoseStackerItemCountHook() {
        super("RoseStacker Item Count Hook", true, "RoseStacker");
    }

    @Override
    public void load() {
        roseStackerAPI = RoseStackerAPI.getInstance();
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksFile.getItemCountHooks().isRoseStacker();
    }

    @Override
    public int getAmount(Item item) {
        StackedItem stackedItem = getStackedItem(item);
        if (stackedItem == null) return item.getItemStack().getAmount();

        return Math.max(stackedItem.getStackSize(), 0);
    }

    @Override
    public void removeAmount(Item item, int amount) {
        StackedItem stackedItem = getStackedItem(item);
        if (stackedItem == null) {
            removeVanillaAmount(item, amount);
            return;
        }

        int remaining = stackedItem.getStackSize() - amount;
        if (remaining > 0) {
            stackedItem.setStackSize(remaining);
            return;
        }

        roseStackerAPI.removeItemStack(stackedItem);
        item.remove();
    }

    private StackedItem getStackedItem(Item item) {
        if (roseStackerAPI == null || !roseStackerAPI.isItemStacked(item)) return null;

        return roseStackerAPI.getStackedItem(item);
    }
}
