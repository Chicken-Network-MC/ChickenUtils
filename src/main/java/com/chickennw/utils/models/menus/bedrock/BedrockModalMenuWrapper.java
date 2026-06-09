package com.chickennw.utils.models.menus.bedrock;

import com.chickennw.utils.ChickenUtils;
import com.chickennw.utils.logger.LoggerFactory;
import com.chickennw.utils.models.config.menu.bedrock.BedrockModalMenuConfiguration;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.slf4j.Logger;

@Getter
public abstract class BedrockModalMenuWrapper {

    protected static final Logger logger = LoggerFactory.getLogger();

    protected final BedrockModalMenuConfiguration menuConfiguration;

    public BedrockModalMenuWrapper(BedrockModalMenuConfiguration menuConfiguration) {
        this.menuConfiguration = menuConfiguration;
    }

    public void open(Player player) {
        ChickenUtils.getFoliaLib().getScheduler().runAsync((task) -> {
            ModalForm.Builder builder = ModalForm.builder()
                .title(menuConfiguration.getTitle())
                .content(menuConfiguration.getContent())
                .button1(menuConfiguration.getButton1().getText())
                .button2(menuConfiguration.getButton2().getText())
                .validResultHandler(response -> {
                    int buttonId = response.clickedButtonId();

                    if (buttonId == 0) onButton1Click(player);
                    else if (buttonId == 1) onButton2Click(player);
                });
            FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
        });
    }

    public abstract void onButton1Click(Player player);

    public abstract void onButton2Click(Player player);
}
