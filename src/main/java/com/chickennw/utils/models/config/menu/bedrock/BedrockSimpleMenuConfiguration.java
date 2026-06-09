package com.chickennw.utils.models.config.menu.bedrock;

import com.chickennw.utils.configurations.menu.bedrock.BedrockMenuBaseButton;
import com.chickennw.utils.configurations.menu.bedrock.BedrockMenuIconedButton;

import java.util.List;

public interface BedrockSimpleMenuConfiguration {

    String getTitle();

    String getOpenSound();

    String getContent();

    List<BedrockMenuIconedButton> getButtons();
}
