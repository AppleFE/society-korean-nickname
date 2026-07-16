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
        assertTrue(Platform.parse("트위치").isEmpty());
    }

    @Test
    void findsMarkerInNestedTabComponent() {
        Component component = Component.empty()
                .append(Component.literal("Lv. 7 "))
                .append(Component.literal("사과")
                        .withStyle(style -> style.withInsertion(Platform.CHZZK.marker())));

        assertEquals(Platform.CHZZK, Platform.fromTabName(component).orElseThrow());
    }
}
