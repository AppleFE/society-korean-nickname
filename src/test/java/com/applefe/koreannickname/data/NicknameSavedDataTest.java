package com.applefe.koreannickname.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class NicknameSavedDataTest {
    @Test
    void roundTripsProfilesAndEnforcesCrossPlayerUniqueness() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        NicknameSavedData data = new NicknameSavedData();
        data.put(first, new Profile("햇살농부", Platform.CHZZK));

        CompoundTag serialized = data.save(new CompoundTag());
        NicknameSavedData loaded = NicknameSavedData.load(serialized);

        assertEquals(new Profile("햇살농부", Platform.CHZZK), loaded.find(first).orElseThrow());
        assertFalse(loaded.isNicknameUsedByOther("햇살농부", first));
        assertTrue(loaded.isNicknameUsedByOther("햇살농부", second));
    }

    @Test
    void skipsMalformedStoredProfiles() {
        CompoundTag serialized = new CompoundTag();
        net.minecraft.nbt.ListTag profiles = new net.minecraft.nbt.ListTag();
        CompoundTag malformed = new CompoundTag();
        malformed.putUUID("Uuid", UUID.randomUUID());
        malformed.putString("Nickname", "금지§닉");
        malformed.putString("Platform", "chzzk");
        profiles.add(malformed);
        serialized.put("Profiles", profiles);

        assertTrue(NicknameSavedData.load(serialized).save(new CompoundTag())
                .getList("Profiles", net.minecraft.nbt.Tag.TAG_COMPOUND).isEmpty());
    }
}
