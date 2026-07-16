package com.applefe.koreannickname;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NicknameValidatorTest {
    @Test
    void acceptsKoreanNicknameAndNormalizesWhitespace() {
        NicknameValidator.Result result = NicknameValidator.validate("  햇살_농부  ");

        assertTrue(result.valid());
        assertEquals("햇살_농부", result.nickname());
    }

    @Test
    void rejectsFormattingAndOverlongNames() {
        assertFalse(NicknameValidator.validate("빨강§c닉").valid());
        assertFalse(NicknameValidator.validate("가".repeat(17)).valid());
        assertFalse(NicknameValidator.validate("두  칸").valid());
    }

    @Test
    void countsUnicodeCodePointsInsteadOfUtf16Units() {
        assertTrue(NicknameValidator.validate("농부A1").valid());
        assertFalse(NicknameValidator.validate("𐐀".repeat(17)).valid());
    }
}
