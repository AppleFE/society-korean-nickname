package com.applefe.koreannickname;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/** Supported streaming platforms and their tab-list metadata. */
public enum Platform {
    CHZZK("chzzk", "치지직", 0x00FFA3, 0x00B371),
    YOUTUBE("youtube", "유튜브", 0xFF0033, 0xA90021),
    CIME("cime", "씨미", 0x7B34F3, 0x9633F3),
    SOOP("soop", "숲", 0x0545B1, 0x0545B1);

    public static final String MARKER_PREFIX = "society-korean-nickname:";

    private final String id;
    private final String koreanName;
    private final int gradientStartColor;
    private final int gradientEndColor;

    Platform(String id, String koreanName, int gradientStartColor, int gradientEndColor) {
        this.id = id;
        this.koreanName = koreanName;
        this.gradientStartColor = gradientStartColor;
        this.gradientEndColor = gradientEndColor;
    }

    public String id() {
        return id;
    }

    public String koreanName() {
        return koreanName;
    }

    public int gradientStartColor() {
        return gradientStartColor;
    }

    public int gradientEndColor() {
        return gradientEndColor;
    }

    public String marker() {
        return MARKER_PREFIX + id;
    }

    public static Optional<Platform> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String normalized = value.strip().toLowerCase(Locale.ROOT);
        if (normalized.equals("afreecatv")
                || normalized.equals("아프리카tv")
                || normalized.equals("아프리카")) {
            return Optional.of(SOOP);
        }
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
