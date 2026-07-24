package com.applefe.koreannickname.client;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.SocietyKoreanNicknameMod;
import net.minecraft.resources.ResourceLocation;

/** Texture locations and source dimensions exported by web-to-mc-ui-assets. */
final class NicknameUiTextures {
    private static final String SPRITE_ROOT = "ui/nickname_editor/sprites/";

    static final Sprite PANEL = sprite("nickname_panel__normal.png", 1320, 880);
    static final Sprite INPUT_NORMAL = sprite("nickname_input__normal.png", 1128, 112);
    static final Sprite INPUT_FOCUSED = sprite("nickname_input__focused.png", 1128, 112);
    static final Sprite INPUT_DISABLED = sprite("nickname_input__disabled.png", 1128, 112);
    static final Sprite PREVIEW = sprite("preview_panel__normal.png", 1128, 104);
    static final Sprite ICON_BADGE = sprite("icon_badge__normal.png", 96, 96);

    private NicknameUiTextures() {
    }

    static Sprite button(String kind, String state, int sourceWidth, int sourceHeight) {
        return sprite(kind + "__" + state + ".png", sourceWidth, sourceHeight);
    }

    static ResourceLocation platformIcon(Platform platform) {
        return ResourceLocation.fromNamespaceAndPath(SocietyKoreanNicknameMod.MOD_ID,
                "textures/gui/platform/" + platform.id() + ".png");
    }

    static int platformIconSize(Platform platform) {
        return switch (platform) {
            case CHZZK -> 512;
            case YOUTUBE -> 32;
            case CIME -> 180;
            case SOOP -> 256;
            case ADMIN -> 2048;
        };
    }

    private static Sprite sprite(String filename, int sourceWidth, int sourceHeight) {
        return new Sprite(ResourceLocation.fromNamespaceAndPath(
                SocietyKoreanNicknameMod.MOD_ID, SPRITE_ROOT + filename), sourceWidth, sourceHeight);
    }

    record Sprite(ResourceLocation texture, int width, int height) {
    }
}
