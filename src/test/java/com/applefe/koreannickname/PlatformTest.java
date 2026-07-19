package com.applefe.koreannickname;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class PlatformTest {
    @Test
    void parsesKoreanAndStableIds() {
        assertEquals(Platform.CHZZK, Platform.parse("치지직").orElseThrow());
        assertEquals(Platform.YOUTUBE, Platform.parse("YOUTUBE").orElseThrow());
        assertEquals(Platform.CIME, Platform.parse("씨미").orElseThrow());
        assertEquals(Platform.SOOP, Platform.parse("숲").orElseThrow());
        assertEquals(Platform.SOOP, Platform.parse("SOOP").orElseThrow());
        assertEquals(Platform.SOOP, Platform.parse("아프리카TV").orElseThrow());
        assertTrue(Platform.parse("트위치").isEmpty());
    }

    @Test
    void exposesRequestedChatGradientColors() {
        assertGradient(Platform.CIME, 0x7B34F3, 0x9633F3);
        assertGradient(Platform.YOUTUBE, 0xFF0033, 0xA90021);
        assertGradient(Platform.CHZZK, 0x00FFA3, 0x00B371);
        assertGradient(Platform.SOOP, 0x34C8FF, 0x3B82F6);
    }

    @Test
    void findsMarkerInNestedTabComponent() {
        Component component = Component.empty()
                .append(Component.literal("Lv. 7 "))
                .append(Component.literal("사과")
                        .withStyle(style -> style.withInsertion(Platform.CHZZK.marker())));

        assertEquals(Platform.CHZZK, Platform.fromTabName(component).orElseThrow());
    }
    private static void assertGradient(Platform platform, int startColor, int endColor) {
        assertEquals(startColor, platform.gradientStartColor());
        assertEquals(endColor, platform.gradientEndColor());
    }
}
