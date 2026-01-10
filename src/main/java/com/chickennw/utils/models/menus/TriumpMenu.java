package com.chickennw.utils.models.menus;

import com.chickennw.utils.models.menus.triumph.MenuItem;
import dev.triumphteam.gui.components.GuiAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class TriumpMenu {

    private String title;
    private List<MenuItem> items;
    private HashMap<String, GuiAction<?>> actions;
}
