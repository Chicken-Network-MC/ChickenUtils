package com.chickennw.utils.models.hooks.impl.entity;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.EntityLootHook;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class VanillaEntityLootHook extends AbstractPluginHook implements EntityLootHook {

    public VanillaEntityLootHook() {
        super("Vanilla Entity Loot Hook", false, "vanilla");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksFile.getEntityLootHooks().isVanilla();
    }

    @Override
    public List<ItemStack> getDrops(UUID requester, LivingEntity entity, int count, ItemStack itemStack) {
        return new ArrayList<>();
    }

    private int getLootingLevel(ItemStack itemStack) {
        try {
            return itemStack.getEnchantmentLevel(Enchantment.LOOTING);
        } catch (NoSuchFieldError e) {
            try {
                Enchantment lootingEnchant = Enchantment.getByName("LOOT_BONUS_MOBS");
                return lootingEnchant != null ? itemStack.getEnchantmentLevel(lootingEnchant) : 0;
            } catch (Exception ex) {
                return 0;
            }
        }
    }
}
