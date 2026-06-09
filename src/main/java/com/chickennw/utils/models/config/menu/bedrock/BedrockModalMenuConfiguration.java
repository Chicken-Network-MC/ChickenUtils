package com.chickennw.utils.models.config.menu.bedrock;

import com.chickennw.utils.configurations.menu.bedrock.BedrockMenuBaseButton;

public interface BedrockModalMenuConfiguration {

    String getTitle();

    String getOpenSound();

    String getContent();

    BedrockMenuBaseButton getButton1();

    BedrockMenuBaseButton getButton2();
}
