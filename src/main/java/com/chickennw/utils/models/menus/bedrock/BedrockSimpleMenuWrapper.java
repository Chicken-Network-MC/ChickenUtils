package com.chickennw.utils.models.menus.bedrock;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.configurations.menu.bedrock.BedrockMenuIconedButton;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.config.menu.bedrock.BedrockSimpleMenuConfiguration;
import com.chickennw.utils.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import org.slf4j.Logger;

@Getter
public abstract class BedrockSimpleMenuWrapper {

    protected static final Logger logger = LoggerFactory.getLogger();

    protected final BedrockSimpleMenuConfiguration menuConfiguration;

    public BedrockSimpleMenuWrapper(BedrockSimpleMenuConfiguration menuConfiguration) {
        this.menuConfiguration = menuConfiguration;
    }

    public void open(Player player) {
        ChickenUtils.getFoliaLib().getScheduler().runAsync((task) -> {
            SimpleForm.Builder builder = SimpleForm.builder()
                .title(menuConfiguration.getTitle())
                .content(menuConfiguration.getContent())
                .validResultHandler(response -> {
                    int buttonId = response.clickedButtonId();
                    String buttonText = response.clickedButton().text();

                    onButtonClick(player, buttonId, buttonText);
                });

            for (BedrockMenuIconedButton button : menuConfiguration.getButtons().values()) {
                String text = ChatUtils.colorizeLegacy(button.getText());
                if (button.getPath() != null) builder = builder.button(text, FormImage.Type.PATH, button.getPath());
                else builder = builder.button(text);
            }

            FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
        });
    }

    public abstract void onButtonClick(Player player, int buttonId, String buttonText);
}
