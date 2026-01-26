package com.chickennw.utils.models.hooks.types;

import com.chickennw.utils.models.hooks.PluginHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;

public interface EntityCountHook extends PluginHook {

    int getStackCount(UUID requester, LivingEntity entity);

    void removeEntity(UUID requester, LivingEntity entity);
}
