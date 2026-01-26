package com.chickennw.utils.models.hooks.impl.entity;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.EntityCountHook;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

@SuppressWarnings("unused")
public class VanillaEntityCountHook extends AbstractPluginHook implements EntityCountHook {

    @Getter
    private static final String KILLED_BY_VANILLA_HOOK_KEY = "KILLED_BY_VANILLA_HOOK";

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
    public void removeEntity(UUID requester, LivingEntity entity) {
        FixedMetadataValue metadataValue = new FixedMetadataValue(ChickenUtils.getPlugin(), requester);
        entity.setMetadata(KILLED_BY_VANILLA_HOOK_KEY, metadataValue);
        entity.damage(Integer.MAX_VALUE);
    }
}
