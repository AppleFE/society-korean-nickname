package com.applefe.koreannickname.service;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

/** Applies a stored profile and builds the synchronized tab-list component. */
public final class NicknamePresentation {
    public static final String LAST_LEVEL_KEY = "societyKoreanNicknameLastLevel";

    private NicknamePresentation() {
    }

    public static void apply(ServerPlayer player, Profile profile) {
        player.setCustomName(styledNickname(profile));
        player.refreshDisplayName();
        player.getPersistentData().putInt(LAST_LEVEL_KEY, SkillLevelService.highestLevel(player));
        player.refreshTabListName();
    }

    public static MutableComponent styledNickname(Profile profile) {
        return gradientText(profile.nickname(), profile.platform());
    }

    private static MutableComponent gradientText(String text, Platform platform) {
        int codePointCount = text.codePointCount(0, text.length());
        MutableComponent result = Component.empty()
                .withStyle(style -> style.withInsertion(platform.marker()));

        int codePointIndex = 0;
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            int color = gradientColor(
                    platform.gradientStartColor(),
                    platform.gradientEndColor(),
                    codePointIndex,
                    codePointCount);
            result.append(Component.literal(new String(Character.toChars(codePoint)))
                    .withStyle(style -> style.withColor(TextColor.fromRgb(color))));
            offset += Character.charCount(codePoint);
            codePointIndex++;
        }
        return result;
    }

    static int gradientColor(int startColor, int endColor, int index, int count) {
        if (count <= 1) {
            return startColor;
        }

        double ratio = (double) index / (count - 1);
        int red = interpolateChannel(startColor >> 16, endColor >> 16, ratio);
        int green = interpolateChannel(startColor >> 8, endColor >> 8, ratio);
        int blue = interpolateChannel(startColor, endColor, ratio);
        return red << 16 | green << 8 | blue;
    }

    private static int interpolateChannel(int startColor, int endColor, double ratio) {
        int start = startColor & 0xFF;
        int end = endColor & 0xFF;
        return (int) Math.round(start + (end - start) * ratio);
    }

    public static MutableComponent tabName(Profile profile, int level) {
        String text = "Lv. " + Math.max(0, level) + " " + profile.nickname();
        return gradientText(text, profile.platform());
    }
}
