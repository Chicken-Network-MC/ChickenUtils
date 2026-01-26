package com.chickennw.utils.models.hooks.types;

import com.chickennw.utils.models.hooks.PluginHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface EntityLootHook extends PluginHook {

    List<ItemStack> getDrops(UUID requester, LivingEntity entity, int count, ItemStack itemStack);
}
