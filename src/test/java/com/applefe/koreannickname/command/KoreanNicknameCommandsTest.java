package com.applefe.koreannickname.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        String[] korean = KoreanNicknameCommands.parseNicknameInput("햇살농부 치지직");
        String[] spaced = KoreanNicknameCommands.parseNicknameInput("햇살 농부 씨미");
        String[] quoted = KoreanNicknameCommands.parseNicknameInput("\"햇살 농부\" youtube");

        assertEquals("햇살농부", korean[0]);
        assertEquals("치지직", korean[1]);
        assertEquals("햇살 농부", spaced[0]);
        assertEquals("씨미", spaced[1]);
        assertEquals("햇살 농부", quoted[0]);
        assertEquals("youtube", quoted[1]);
    }

    @Test
    void rejectsInputWithoutATrailingPlatform() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> KoreanNicknameCommands.parseNicknameInput("햇살농부"));

        assertEquals(KoreanNicknameCommands.USAGE_ERROR, exception.getMessage());
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
