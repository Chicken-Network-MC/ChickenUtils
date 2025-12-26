package com.chickennw.utils.models.npc.types;

import com.chickennw.utils.models.npc.NPC;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Getter
@Setter
public class PlayerNPC extends NPC {

    private String skinProfile;

    public PlayerNPC(String name, String worldName, double x, double y, double z, Consumer<Player> onClickAction, String skinProfile) {
        super(name, worldName, x, y, z, EntityTypes.PLAYER, onClickAction);

        this.skinProfile = skinProfile;
    }

}
