package com.chickennw.utils.models.config.menu.bedrock;

import com.chickennw.utils.configurations.menu.bedrock.BedrockMenuIconedButton;

import java.util.LinkedHashMap;

public interface BedrockSimpleMenuConfiguration {

    String getTitle();

    String getOpenSound();

    String getContent();

    LinkedHashMap<String, BedrockMenuIconedButton> getButtons();
}
