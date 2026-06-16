package com.chickennw.utils.models.config.menu.bedrock;

import com.chickennw.utils.configurations.menu.bedrock.BedrockMenuButton;

import java.util.LinkedHashMap;

public interface BedrockCustomMenuConfiguration {

    String getTitle();

    String getOpenSound();

    LinkedHashMap<String, BedrockMenuButton> getButtons();
}
