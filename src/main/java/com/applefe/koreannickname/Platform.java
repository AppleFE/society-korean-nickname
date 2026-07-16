package com.applefe.koreannickname;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Supported streaming platforms and their tab-list metadata. */
public enum Platform {
    CHZZK("chzzk", "치지직", ChatFormatting.GREEN),
    YOUTUBE("youtube", "유튜브", ChatFormatting.RED),
    CIME("cime", "씨미", ChatFormatting.LIGHT_PURPLE);

    public static final String MARKER_PREFIX = "society-korean-nickname:";

    private final String id;
    private final String koreanName;
    private final ChatFormatting color;

    Platform(String id, String koreanName, ChatFormatting color) {
        this.id = id;
        this.koreanName = koreanName;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public String koreanName() {
        return koreanName;
    }

    public ChatFormatting color() {
        return color;
    }

    public String marker() {
        return MARKER_PREFIX + id;
    }

    public static Optional<Platform> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String normalized = value.strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(platform -> platform.id.equals(normalized) || platform.koreanName.equals(normalized))
                .findFirst();
    }

    public static Optional<Platform> fromTabName(Component component) {
        if (component == null) {
            return Optional.empty();
        }
        String insertion = component.getStyle().getInsertion();
        if (insertion != null && insertion.startsWith(MARKER_PREFIX)) {
            Optional<Platform> platform = parse(insertion.substring(MARKER_PREFIX.length()));
            if (platform.isPresent()) {
                return platform;
            }
        }
        for (Component sibling : component.getSiblings()) {
            Optional<Platform> platform = fromTabName(sibling);
            if (platform.isPresent()) {
                return platform;
            }
        }
        return Optional.empty();
    }
}
