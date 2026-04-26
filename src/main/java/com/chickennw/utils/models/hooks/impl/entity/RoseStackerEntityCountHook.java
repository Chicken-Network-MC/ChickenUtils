package com.chickennw.utils.models.hooks.impl.entity;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.types.EntityCountHook;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

@SuppressWarnings("unused")
public class RoseStackerEntityCountHook extends AbstractPluginHook implements EntityCountHook {

    private RoseStackerAPI roseStackerAPI;

    public RoseStackerEntityCountHook() {
        super("RoseStacker Entity Count Hook", true, "RoseStacker");
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
        return hooksFile.getEntityCountHooks().isRoseStacker();
    }

    @Override
    public int getStackCount(UUID requester, LivingEntity entity) {
        if (roseStackerAPI != null && roseStackerAPI.isEntityStacked(entity)) {
            StackedEntity stackedEntity = roseStackerAPI.getStackedEntity(entity);
            if (stackedEntity != null) {
                return stackedEntity.getStackSize();
            }
        }
        return 1;
    }

    @Override
    public void removeEntity(UUID requester, LivingEntity entity) {
        if (roseStackerAPI != null && roseStackerAPI.isEntityStacked(entity)) {
            StackedEntity stackedEntity = roseStackerAPI.getStackedEntity(entity);
            if (stackedEntity != null) {
                roseStackerAPI.removeEntityStack(stackedEntity);
            }
        }
        FixedMetadataValue metadataValue = new FixedMetadataValue(ChickenUtils.getPlugin(), requester);
        entity.setMetadata(VanillaEntityCountHook.KILLED_BY_VANILLA_HOOK_KEY, metadataValue);
        entity.damage(Integer.MAX_VALUE);
    }
}
