package com.chickennw.utils.models.config.menu.bedrock;

import com.chickennw.utils.configurations.menu.bedrock.BedrockMenuIconedButton;

import java.util.LinkedHashSet;

public interface BedrockCustomMenuConfiguration {

    String getTitle();

    String getOpenSound();

    LinkedHashSet<BedrockMenuIconedButton> getItems();
}
