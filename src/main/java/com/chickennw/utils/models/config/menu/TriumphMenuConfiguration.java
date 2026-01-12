package com.chickennw.utils.models.config.menu;

import com.chickennw.utils.configurations.menu.MenuFiller;
import com.chickennw.utils.configurations.menu.SymboledMenuItem;

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
