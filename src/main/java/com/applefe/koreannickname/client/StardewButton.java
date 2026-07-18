package com.applefe.koreannickname.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Button rendered from the deterministic web-to-Minecraft state sprites. */
final class StardewButton extends Button {
    enum Skin {
        PLATFORM("platform_button", 344, 96),
        ACTION("action_button", 328, 96);

        private final String assetName;
        private final int sourceWidth;
        private final int sourceHeight;

        Skin(String assetName, int sourceWidth, int sourceHeight) {
            this.assetName = assetName;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
        }
    }

    private final Skin skin;
    private boolean selected;

    StardewButton(int x, int y, int width, int height, Component message, OnPress onPress, Skin skin) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.skin = skin;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        String state;
        if (!active) {
            state = "disabled";
        } else if (selected && skin == Skin.PLATFORM) {
            state = "selected";
        } else if (isHovered && Minecraft.getInstance().mouseHandler.isLeftPressed()) {
            state = "pressed";
        } else if (isHoveredOrFocused()) {
            state = "hover";
        } else {
            state = "normal";
        }

        NicknameUiTextures.Sprite sprite = NicknameUiTextures.button(
                skin.assetName, state, skin.sourceWidth, skin.sourceHeight);
        graphics.blit(sprite.texture(), getX(), getY(), width, height,
                0.0F, 0.0F, sprite.width(), sprite.height(), sprite.width(), sprite.height());
        renderString(graphics, Minecraft.getInstance().font, active ? 0x4A2C18 : 0x8B7A61);
    }
}
