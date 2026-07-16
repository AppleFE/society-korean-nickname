package com.applefe.koreannickname;

import java.text.Normalizer;

/** Validation shared by commands and persisted-data loading. */
public final class NicknameValidator {
    public static final int MAX_CODE_POINTS = 16;

    private NicknameValidator() {
    }

    public static Result validate(String input) {
        if (input == null) {
            return Result.invalid("닉네임을 입력해 주세요.");
        }

        String nickname = Normalizer.normalize(input.strip(), Normalizer.Form.NFC);
        if (nickname.isEmpty()) {
            return Result.invalid("닉네임은 비워 둘 수 없습니다.");
        }
        if (nickname.codePointCount(0, nickname.length()) > MAX_CODE_POINTS) {
            return Result.invalid("닉네임은 16자 이하여야 합니다.");
        }
        if (nickname.codePoints().anyMatch(NicknameValidator::isDisallowedCodePoint)) {
            return Result.invalid("닉네임에는 글자, 숫자, 공백, 밑줄만 사용할 수 있습니다.");
        }
        if (nickname.contains("  ")) {
            return Result.invalid("닉네임에는 연속된 공백을 사용할 수 없습니다.");
        }
        return Result.valid(nickname);
    }

    private static boolean isDisallowedCodePoint(int codePoint) {
        return codePoint != ' '
                && codePoint != '_'
                && !Character.isLetterOrDigit(codePoint);
    }

    public record Result(boolean valid, String nickname, String error) {
        private static Result valid(String nickname) {
            return new Result(true, nickname, "");
        }

        private static Result invalid(String error) {
            return new Result(false, "", error);
        }
    }
}
