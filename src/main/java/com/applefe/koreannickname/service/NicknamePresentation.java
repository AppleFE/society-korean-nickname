package com.applefe.koreannickname.service;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        return Component.literal(profile.nickname())
                .withStyle(style -> style
                        .withColor(profile.platform().color())
                        .withInsertion(profile.platform().marker()));
    }

    public static MutableComponent tabName(Profile profile, int level) {
        return Component.empty()
                .withStyle(style -> style.withInsertion(profile.platform().marker()))
                .append(Component.literal("Lv. ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(Integer.toString(Math.max(0, level)))
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal(" "))
                .append(styledNickname(profile));
    }
}
