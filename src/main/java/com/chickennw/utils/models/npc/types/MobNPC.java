package com.chickennw.utils.models.npc.types;

import com.chickennw.utils.models.npc.NPC;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Getter
@Setter
public class MobNPC extends NPC {

    public MobNPC(String name, String worldName, double x, double y, double z, EntityType entityType, Consumer<Player> onClickAction) {
        super(name, worldName, x, y, z, entityType, onClickAction);
    }
}
