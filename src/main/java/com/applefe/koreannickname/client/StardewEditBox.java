package com.applefe.koreannickname.client;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.service.NicknamePresentation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Edit box with Stardew-style chrome, a platform icon, and per-character gradient text. */
final class StardewEditBox extends EditBox {
    private static final int ICON_SIZE = 16;
    private static final int FONT_HEIGHT = 8;

    private final int backgroundX;
    private final int backgroundY;
    private final int backgroundWidth;
    private final int backgroundHeight;
    private final Supplier<Platform> platformSupplier;

    StardewEditBox(Font font, int x, int y, int width, int height,
            Component narration, Supplier<Platform> platformSupplier) {
        super(font, x + 32, y + (height - FONT_HEIGHT) / 2,
                width - 40, FONT_HEIGHT, narration);
        this.backgroundX = x;
        this.backgroundY = y;
        this.backgroundWidth = width;
        this.backgroundHeight = height;
        this.platformSupplier = platformSupplier;
        setBordered(false);
        setFormatter(this::formatGradient);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        NicknameUiTextures.Sprite background = !active
                ? NicknameUiTextures.INPUT_DISABLED
                : isFocused() ? NicknameUiTextures.INPUT_FOCUSED : NicknameUiTextures.INPUT_NORMAL;
        graphics.blit(background.texture(), backgroundX, backgroundY, backgroundWidth, backgroundHeight,
                0.0F, 0.0F, background.width(), background.height(), background.width(), background.height());

        Platform platform = platformSupplier.get();
        int sourceSize = NicknameUiTextures.platformIconSize(platform);
        graphics.blit(NicknameUiTextures.platformIcon(platform), backgroundX + 9, backgroundY + 6,
                ICON_SIZE, ICON_SIZE, 0.0F, 0.0F, sourceSize, sourceSize, sourceSize, sourceSize);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible
                && mouseX >= backgroundX
                && mouseX < backgroundX + backgroundWidth
                && mouseY >= backgroundY
                && mouseY < backgroundY + backgroundHeight;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(Math.max(mouseX, getX()), mouseY);
    }

    private FormattedCharSequence formatGradient(String visibleText, Integer displayOffset) {
        if (visibleText.isEmpty()) {
            return FormattedCharSequence.EMPTY;
        }

        String fullText = getValue();
        int safeOffset = Math.max(0, Math.min(displayOffset, fullText.length()));
        int colorIndex = fullText.codePointCount(0, safeOffset);
        int colorCount = fullText.codePointCount(0, fullText.length());
        Platform platform = platformSupplier.get();
        List<FormattedCharSequence> characters = new ArrayList<>();
        for (int offset = 0; offset < visibleText.length(); ) {
            int codePoint = visibleText.codePointAt(offset);
            int color = NicknamePresentation.gradientColor(
                    platform.gradientStartColor(), platform.gradientEndColor(), colorIndex, colorCount);
            characters.add(FormattedCharSequence.codepoint(codePoint, Style.EMPTY.withColor(color)));
            offset += Character.charCount(codePoint);
            colorIndex++;
        }
        return FormattedCharSequence.composite(characters);
    }
}
