package com.chickennw.utils.models.config.menu.triumph;

import com.chickennw.utils.configurations.menu.triumph.MenuFiller;
import com.chickennw.utils.configurations.menu.triumph.SymboledMenuItem;

import java.util.List;
import java.util.Map;

public interface TriumphMenuConfiguration {

    String getTitle();

    List<String> getPattern();

    String getOpenSound();

    MenuFiller getFiller();

    Map<String, SymboledMenuItem> getItems();

    SymboledMenuItem getTemplateItem();
}
