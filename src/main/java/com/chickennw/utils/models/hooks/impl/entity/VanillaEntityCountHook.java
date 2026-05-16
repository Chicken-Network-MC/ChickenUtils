package com.chickennw.utils.models.hooks.impl.entity;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.EntityCountHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

@SuppressWarnings("unused")
public class VanillaEntityCountHook extends AbstractPluginHook implements EntityCountHook {

    public static final String KILLED_BY_VANILLA_HOOK_MINION_KEY = "KILLED_BY_VANILLA_HOOK_MINION";
    public static final String KILLED_BY_VANILLA_HOOK_AMOUNT_KEY = "KILLED_BY_VANILLA_HOOK_AMOUNT";

    public VanillaEntityCountHook() {
        super("Vanilla Entity Count Hook", false, "vanilla");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksFile.getEntityCountHooks().isVanilla();
    }

    @Override
    public int getStackCount(UUID requester, LivingEntity entity) {
        return 1;
    }

    @Override
    public void removeEntity(UUID requester, LivingEntity entity, int stack) {
        FixedMetadataValue minionMetadata = new FixedMetadataValue(ChickenUtils.getPlugin(), requester);
        FixedMetadataValue amountMetadata = new FixedMetadataValue(ChickenUtils.getPlugin(), stack);
        entity.setMetadata(VanillaEntityCountHook.KILLED_BY_VANILLA_HOOK_MINION_KEY, minionMetadata);
        entity.setMetadata(VanillaEntityCountHook.KILLED_BY_VANILLA_HOOK_AMOUNT_KEY, amountMetadata);
        entity.damage(Integer.MAX_VALUE);
    }
}
