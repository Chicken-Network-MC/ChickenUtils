package com.chickennw.utils.models.menus;

import de.themoep.inventorygui.GuiElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@AllArgsConstructor
public class LitMenuItemHolder {

    private String actionName;
    private ItemStack itemStack;
    private GuiElement item;
}
