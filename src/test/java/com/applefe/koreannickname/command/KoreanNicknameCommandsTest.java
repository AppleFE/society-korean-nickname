package com.applefe.koreannickname.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

class KoreanNicknameCommandsTest {
    @Test
    void consumesKoreanAndEnglishNicknamesWithoutTrailingData() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        KoreanNicknameCommands.register(dispatcher);

        assertFullyParsed(dispatcher, "한글닉 햇살농부 치지직");
        assertFullyParsed(dispatcher, "한글닉 sunny youtube");
    }

    @Test
    void registersUiEntryPointAndForcedNicknameBranch() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        KoreanNicknameCommands.register(dispatcher);

        var root = dispatcher.getRoot().getChild("한글닉");
        assertNotNull(root);
        assertNotNull(root.getCommand());
        var force = root.getChild("강제");
        assertNotNull(force);
        var target = force.getChild("플레이어");
        assertNotNull(target);
        assertNotNull(target.getCommand());
        assertNotNull(target.getChild("입력"));
    }

    @Test
    void splitsTheTrailingPlatformFromKoreanAndSpacedNicknames() {
        NicknameCommandInput.Result korean = NicknameCommandInput.parse("햇살농부 치지직");
        NicknameCommandInput.Result spaced = NicknameCommandInput.parse("햇살 농부 씨미");
        NicknameCommandInput.Result quoted = NicknameCommandInput.parse("\"햇살 농부\" youtube");

        assertTrue(korean.valid());
        assertEquals("햇살농부", korean.nickname());
        assertEquals("치지직", korean.platform());
        assertTrue(spaced.valid());
        assertEquals("햇살 농부", spaced.nickname());
        assertEquals("씨미", spaced.platform());
        assertTrue(quoted.valid());
        assertEquals("햇살 농부", quoted.nickname());
        assertEquals("youtube", quoted.platform());
    }

    @Test
    void rejectsInputWithoutATrailingPlatform() {
        NicknameCommandInput.Result result = NicknameCommandInput.parse("햇살농부");

        assertFalse(result.valid());
        assertEquals(NicknameCommandInput.USAGE_ERROR, result.error());
    }

    @Test
    void suggestsKoreanPlatformFromTheFinalToken() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        KoreanNicknameCommands.register(dispatcher);
        ParseResults<CommandSourceStack> parse = dispatcher.parse("한글닉 햇살농부 치", null);

        Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

        assertTrue(suggestions.getList().stream()
                .anyMatch(suggestion -> suggestion.getText().equals("치지직")));
    }

    private static void assertFullyParsed(
            CommandDispatcher<CommandSourceStack> dispatcher, String command) {
        ParseResults<CommandSourceStack> parse = dispatcher.parse(command, null);

        assertTrue(parse.getExceptions().isEmpty(), () -> parse.getExceptions().toString());
        assertFalse(parse.getReader().canRead(), () -> "후행 데이터: " + parse.getReader().getRemaining());
    }
}
