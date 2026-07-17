package com.applefe.koreannickname.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class NicknamePresentationTest {
    @Test
    void buildsRequestedTabFormatWithPlatformMarker() {
        Component tabName = NicknamePresentation.tabName(new Profile("햇살농부", Platform.CIME), 23);

        assertEquals("Lv. 23 햇살농부", tabName.getString());
        assertEquals(Platform.CIME, Platform.fromTabName(tabName).orElseThrow());
    }

    @Test
    void appliesEachPlatformGradientAcrossNickname() {
        for (Platform platform : Platform.values()) {
            Component nickname = NicknamePresentation.styledNickname(new Profile("가나다", platform));

            assertEquals(platform.gradientStartColor(), colorOf(nickname.getSiblings().get(0)));
            assertEquals(platform.gradientEndColor(), colorOf(nickname.getSiblings().get(2)));
        }
    }

    @Test
    void gradientsUnicodeCodePointsWithoutSplittingSurrogatePairs() {
        Component nickname = NicknamePresentation.styledNickname(new Profile("A𐐀Z", Platform.CHZZK));

        assertEquals("A𐐀Z", nickname.getString());
        assertEquals(3, nickname.getSiblings().size());
        assertEquals(0x00FFA3, colorOf(nickname.getSiblings().get(0)));
        assertEquals(0x00D98A, colorOf(nickname.getSiblings().get(1)));
        assertEquals(0x00B371, colorOf(nickname.getSiblings().get(2)));
    }

    @Test
    void clampsNegativeLevelToZero() {
        Component tabName = NicknamePresentation.tabName(new Profile("광부", Platform.YOUTUBE), -4);

        assertEquals("Lv. 0 광부", tabName.getString());
    }
    private static int colorOf(Component component) {
        assertNotNull(component.getStyle().getColor());
        return component.getStyle().getColor().getValue();
    }
}