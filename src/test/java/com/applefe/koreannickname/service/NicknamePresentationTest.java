package com.applefe.koreannickname.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void clampsNegativeLevelToZero() {
        Component tabName = NicknamePresentation.tabName(new Profile("광부", Platform.YOUTUBE), -4);

        assertEquals("Lv. 0 광부", tabName.getString());
    }
}
