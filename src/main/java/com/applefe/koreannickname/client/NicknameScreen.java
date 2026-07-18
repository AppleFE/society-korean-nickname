package com.applefe.koreannickname.client;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.NicknameValidator;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import com.applefe.koreannickname.network.ModNetwork;
import com.applefe.koreannickname.service.NicknamePresentation;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Client-side editor for a player's Korean nickname profile. */
public final class NicknameScreen extends Screen {
    private static final int PANEL_WIDTH = 330;
    private static final int PANEL_HEIGHT = 218;

    private final String initialNickname;
    private final Map<Platform, Button> platformButtons = new EnumMap<>(Platform.class);
    private Platform selectedPlatform;
    private EditBox nicknameField;
    private Button saveButton;
    private String statusMessage = "";
    private boolean statusIsError;
    private int panelLeft;
    private int panelTop;

    public NicknameScreen(String nickname, Platform platform) {
        super(Component.literal("한글 닉네임 설정"));
        this.initialNickname = nickname == null ? "" : nickname;
        this.selectedPlatform = platform == null ? Platform.CHZZK : platform;
    }

    @Override
    protected void init() {
        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - PANEL_HEIGHT) / 2;

        nicknameField = addRenderableWidget(new EditBox(font, panelLeft + 24, panelTop + 67,
                PANEL_WIDTH - 48, 20, Component.literal("닉네임")));
        nicknameField.setMaxLength(64);
        nicknameField.setValue(initialNickname);
        nicknameField.setResponder(value -> {
            statusMessage = "";
            updateSaveButton();
        });
        setInitialFocus(nicknameField);

        int buttonWidth = 88;
        int firstButtonX = panelLeft + 24;
        int buttonY = panelTop + 121;
        for (Platform platform : Platform.values()) {
            int buttonX = firstButtonX + platform.ordinal() * (buttonWidth + 9);
            Button button = addRenderableWidget(Button.builder(Component.literal(platform.koreanName()),
                    clicked -> selectPlatform(platform)).bounds(buttonX, buttonY, buttonWidth, 20).build());
            platformButtons.put(platform, button);
        }

        saveButton = addRenderableWidget(Button.builder(Component.literal("저장"), clicked -> submit())
                .bounds(panelLeft + 78, panelTop + 177, 82, 20).build());
        addRenderableWidget(Button.builder(Component.literal("취소"), clicked -> onClose())
                .bounds(panelLeft + 170, panelTop + 177, 82, 20).build());
        updatePlatformButtons();
        updateSaveButton();
    }

    private void selectPlatform(Platform platform) {
        selectedPlatform = platform;
        statusMessage = "";
        updatePlatformButtons();
    }

    private void updatePlatformButtons() {
        for (Map.Entry<Platform, Button> entry : platformButtons.entrySet()) {
            Platform platform = entry.getKey();
            entry.getValue().setMessage(Component.literal((platform == selectedPlatform ? "✓ " : "")
                    + platform.koreanName()));
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
        graphics.fill(panelLeft - 3, panelTop - 3, panelLeft + PANEL_WIDTH + 3, panelTop + PANEL_HEIGHT + 3,
                0x90000000);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xF01B1D29);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 2, 0xFF6E74A8);

        graphics.drawCenteredString(font, title, panelLeft + PANEL_WIDTH / 2, panelTop + 18, 0xFFF4F5FF);
        graphics.drawCenteredString(font, "표시할 닉네임과 플랫폼을 선택해 주세요.",
                panelLeft + PANEL_WIDTH / 2, panelTop + 38, 0xFFB9BDD6);
        graphics.drawString(font, "닉네임", panelLeft + 24, panelTop + 55, 0xFFD9DBEA, false);
        graphics.drawString(font, "플랫폼", panelLeft + 24, panelTop + 108, 0xFFD9DBEA, false);

        int previewStart = 0xFF000000 | selectedPlatform.gradientStartColor();
        int previewEnd = 0xFF000000 | selectedPlatform.gradientEndColor();
        int previewLeft = panelLeft + 24;
        int previewRight = panelLeft + PANEL_WIDTH - 24;
        graphics.fill(previewLeft, panelTop + 151, previewRight, panelTop + 166, 0xFF10121A);
        graphics.fill(previewLeft, panelTop + 151, previewLeft + 2, panelTop + 166, previewStart);
        graphics.fill(previewRight - 2, panelTop + 151, previewRight, panelTop + 166, previewEnd);
        String previewNickname = nicknameField.getValue().strip();
        Component preview = NicknamePresentation.tabName(
                new Profile(previewNickname.isEmpty() ? "닉네임" : previewNickname, selectedPlatform), 0);
        graphics.drawCenteredString(font, preview, panelLeft + PANEL_WIDTH / 2, panelTop + 154,
                0xFFFFFFFF);

        if (!statusMessage.isEmpty()) {
            int statusColor = statusIsError ? 0xFFFF7777 : 0xFF91E3A0;
            graphics.drawCenteredString(font, statusMessage, panelLeft + PANEL_WIDTH / 2, panelTop + 202,
                    statusColor);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
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
