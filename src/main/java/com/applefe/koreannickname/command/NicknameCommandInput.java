package com.applefe.koreannickname.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/** Splits the free-form nickname command input from its trailing platform. */
final class NicknameCommandInput {
    static final String USAGE_ERROR = "사용법: /한글닉 <닉네임> <플랫폼>";

    private NicknameCommandInput() {
    }

    static Result parse(String rawInput) {
        String input = rawInput == null ? "" : rawInput.strip();
        int platformStart = trailingTokenStart(input);
        if (platformStart == 0) {
            return Result.invalid(USAGE_ERROR);
        }

        String nicknameExpression = input.substring(0, platformStart).strip();
        String platform = input.substring(platformStart).strip();
        if (nicknameExpression.isEmpty() || platform.isEmpty()) {
            return Result.invalid(USAGE_ERROR);
        }

        try {
            return Result.valid(parseNickname(nicknameExpression), platform);
        } catch (CommandSyntaxException exception) {
            return Result.invalid("닉네임 따옴표를 올바르게 닫아 주세요.");
        }
    }

    static int trailingTokenStart(String input) {
        int cursor = input.length();
        while (cursor > 0) {
            int codePoint = input.codePointBefore(cursor);
            if (Character.isWhitespace(codePoint)) {
                break;
            }
            cursor -= Character.charCount(codePoint);
        }
        return cursor;
    }

    private static String parseNickname(String expression) throws CommandSyntaxException {
        if (!expression.startsWith("\"")) {
            return expression;
        }

        StringReader reader = new StringReader(expression);
        String nickname = reader.readQuotedString();
        reader.skipWhitespace();
        if (reader.canRead()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(reader);
        }
        return nickname;
    }

    record Result(boolean valid, String nickname, String platform, String error) {
        private static Result valid(String nickname, String platform) {
            return new Result(true, nickname, platform, "");
        }

        private static Result invalid(String error) {
            return new Result(false, "", "", error);
        }
    }
}
