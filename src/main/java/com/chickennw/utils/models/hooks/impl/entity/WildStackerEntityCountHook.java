package com.chickennw.utils.models.hooks.impl.entity;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.EntityCountHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

@SuppressWarnings("unused")
public class WildStackerEntityCountHook extends AbstractPluginHook implements EntityCountHook {

    public WildStackerEntityCountHook() {
        super("WildStacker Entity Count Hook", true, "WildStacker");
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isEnabled() {
        return hooksFile.getEntityCountHooks().isWildStacker();
    }

    @Override
    public int getStackCount(UUID requester, LivingEntity entity) {
        return WildStackerAPI.getEntityAmount(entity);
    }

    @Override
    public void removeEntity(UUID requester, LivingEntity entity) {
        StackedEntity stackedEntity = WildStackerAPI.getStackedEntity(entity);
        if (stackedEntity != null) {
            stackedEntity.remove();
        } else {
            FixedMetadataValue metadataValue = new FixedMetadataValue(ChickenUtils.getPlugin(), requester);
            entity.setMetadata(VanillaEntityCountHook.KILLED_BY_VANILLA_HOOK_KEY, metadataValue);
            entity.damage(Integer.MAX_VALUE);
        }
    }
}
