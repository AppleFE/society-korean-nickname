package com.applefe.koreannickname.client;

import com.applefe.koreannickname.NicknameValidator;
import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import com.applefe.koreannickname.network.ModNetwork;
import com.applefe.koreannickname.service.NicknamePresentation;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Client-side Stardew-inspired editor for a player's Korean nickname profile. */
public final class NicknameScreen extends net.minecraft.client.gui.screens.Screen {
    private static final int PANEL_WIDTH = 330;
    private static final int PANEL_HEIGHT = 220;
    private static final int LEGACY_BUTTON_GAP_X = 90;
    private static final int LEGACY_BUTTON_GAP_Y = 117;
    private static final int LEGACY_BUTTON_GAP_WIDTH = 6;
    private static final int LEGACY_BUTTON_GAP_HEIGHT = 24;
    private static final int CLEAN_GAP_SOURCE_X = 936;
    private static final int CLEAN_GAP_SOURCE_Y = 468;
    private static final int CLEAN_GAP_SOURCE_WIDTH = 24;
    private static final int CLEAN_GAP_SOURCE_HEIGHT = 96;

    private final String initialNickname;
    private final Map<Platform, StardewButton> platformButtons = new EnumMap<>(Platform.class);
    private Platform selectedPlatform;
    private StardewEditBox nicknameField;
    private StardewButton saveButton;
    private String statusMessage = "";
    private boolean statusIsError;
    private int panelLeft;
    private int panelTop;

    public NicknameScreen(String nickname, Platform platform) {
        super(Component.literal("한글 닉네임 설정"));
        this.initialNickname = nickname == null ? "" : nickname;
        this.selectedPlatform = platform == null || !platform.isUserSelectable() ? Platform.CHZZK : platform;
    }

    @Override
    protected void init() {
        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - PANEL_HEIGHT) / 2;

        nicknameField = addRenderableWidget(new StardewEditBox(font,
                panelLeft + 24, panelTop + 66, PANEL_WIDTH - 48, 28,
                Component.literal("닉네임"), () -> selectedPlatform));
        nicknameField.setMaxLength(64);
        nicknameField.setValue(initialNickname);
        nicknameField.setResponder(value -> {
            statusMessage = "";
            updateSaveButton();
        });
        setInitialFocus(nicknameField);

        int firstButtonX = panelLeft + 24;
        int buttonY = panelTop + 117;
        int buttonWidth = 66;
        int buttonSpacing = 72;
        for (Platform platform : Platform.values()) {
            if (!platform.isUserSelectable()) {
                continue;
            }
            int buttonX = firstButtonX + platform.ordinal() * buttonSpacing;
            StardewButton button = addRenderableWidget(new StardewButton(
                    buttonX, buttonY, buttonWidth, 24, Component.literal(platform.koreanName()),
                    clicked -> selectPlatform(platform), StardewButton.Skin.PLATFORM));
            platformButtons.put(platform, button);
        }

        saveButton = addRenderableWidget(new StardewButton(
                panelLeft + 79, panelTop + 186, 82, 24, Component.literal("저장"),
                clicked -> submit(), StardewButton.Skin.ACTION));
        addRenderableWidget(new StardewButton(
                panelLeft + 169, panelTop + 186, 82, 24, Component.literal("취소"),
                clicked -> onClose(), StardewButton.Skin.ACTION));
        updatePlatformButtons();
        updateSaveButton();
    }

    private void selectPlatform(Platform platform) {
        selectedPlatform = platform;
        statusMessage = "";
        updatePlatformButtons();
    }

    private void updatePlatformButtons() {
        for (Map.Entry<Platform, StardewButton> entry : platformButtons.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == selectedPlatform);
        }
    }

    private void updateSaveButton() {
        if (saveButton != null) {
            saveButton.active = !nicknameField.getValue().strip().isEmpty();
        }
    }

    private void submit() {
        String nickname = nicknameField.getValue();
        NicknameValidator.Result validation = NicknameValidator.validate(nickname);
        if (!validation.valid()) {
            statusMessage = validation.error();
            statusIsError = true;
            return;
        }

        statusMessage = "저장 중...";
        statusIsError = false;
        saveButton.active = false;
        ModNetwork.submitNickname(nickname, selectedPlatform);
    }

    /** Displays a result returned from the authoritative server update. */
    public void handleResult(boolean success, String message) {
        statusMessage = message == null ? "" : message;
        statusIsError = !success;
        if (success) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal(statusMessage), false);
            }
            onClose();
            return;
        }
        updateSaveButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        drawSprite(graphics, NicknameUiTextures.PANEL, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);
        coverLegacyPlatformButtonGap(graphics);
        drawSprite(graphics, NicknameUiTextures.PREVIEW,
                panelLeft + 24, panelTop + 152, PANEL_WIDTH - 48, 26);
        drawSprite(graphics, NicknameUiTextures.ICON_BADGE,
                panelLeft + 27, panelTop + 153, 24, 24);
        drawPlatformIcon(graphics, panelLeft + 31, panelTop + 157, 16);

        graphics.drawCenteredString(font, title, panelLeft + PANEL_WIDTH / 2, panelTop + 17, 0xFF5B351F);
        String subtitle = statusMessage.isEmpty()
                ? "표시할 닉네임과 플랫폼을 선택해 주세요."
                : statusMessage;
        int subtitleColor = statusMessage.isEmpty()
                ? 0xFF805637
                : statusIsError ? 0xFFB33A2D : 0xFF4F7B3E;
        graphics.drawCenteredString(font, subtitle,
                panelLeft + PANEL_WIDTH / 2, panelTop + 36, subtitleColor);
        graphics.drawString(font, "닉네임", panelLeft + 24, panelTop + 51, 0xFF694127, false);
        graphics.drawString(font, "플랫폼", panelLeft + 24, panelTop + 103, 0xFF694127, false);

        String previewNickname = nicknameField.getValue().strip();
        Component preview = NicknamePresentation.tabName(
                new Profile(previewNickname.isEmpty() ? "닉네임" : previewNickname, selectedPlatform), 0);
        graphics.drawCenteredString(font, preview,
                panelLeft + PANEL_WIDTH / 2 + 8, panelTop + 160, 0xFFFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** Covers the exposed right edge of the obsolete 86px button baked into the panel. */
    private void coverLegacyPlatformButtonGap(GuiGraphics graphics) {
        NicknameUiTextures.Sprite panel = NicknameUiTextures.PANEL;
        graphics.blit(panel.texture(),
                panelLeft + LEGACY_BUTTON_GAP_X,
                panelTop + LEGACY_BUTTON_GAP_Y,
                LEGACY_BUTTON_GAP_WIDTH, LEGACY_BUTTON_GAP_HEIGHT,
                CLEAN_GAP_SOURCE_X, CLEAN_GAP_SOURCE_Y,
                CLEAN_GAP_SOURCE_WIDTH, CLEAN_GAP_SOURCE_HEIGHT,
                panel.width(), panel.height());
    }

    private void drawPlatformIcon(GuiGraphics graphics, int x, int y, int size) {
        int sourceSize = NicknameUiTextures.platformIconSize(selectedPlatform);
        graphics.blit(NicknameUiTextures.platformIcon(selectedPlatform), x, y, size, size,
                0.0F, 0.0F, sourceSize, sourceSize, sourceSize, sourceSize);
    }

    private static void drawSprite(GuiGraphics graphics, NicknameUiTextures.Sprite sprite,
            int x, int y, int width, int height) {
        graphics.blit(sprite.texture(), x, y, width, height,
                0.0F, 0.0F, sprite.width(), sprite.height(), sprite.width(), sprite.height());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
