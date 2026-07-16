package com.applefe.koreannickname.service;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.SkillsAPI;

/** Reads the highest Sunlit Valley progression level from Pufferfish's Skills. */
public final class SkillLevelService {
    private static final List<ResourceLocation> TRACKED_CATEGORIES = List.of(
            ResourceLocation.fromNamespaceAndPath("society", "mining"),
            ResourceLocation.fromNamespaceAndPath("society", "adventuring"),
            ResourceLocation.fromNamespaceAndPath("society", "fishing"),
            ResourceLocation.fromNamespaceAndPath("society", "farming"),
            ResourceLocation.fromNamespaceAndPath("society", "husbandry"));

    private SkillLevelService() {
    }

    public static int highestLevel(ServerPlayer player) {
        return TRACKED_CATEGORIES.stream()
                .map(SkillsAPI::getCategory)
                .flatMap(java.util.Optional::stream)
                .map(Category::getExperience)
                .flatMap(java.util.Optional::stream)
                .mapToInt(experience -> experience.getLevel(player))
                .max()
                .orElse(0);
    }
}
