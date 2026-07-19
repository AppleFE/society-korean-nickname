package com.applefe.koreannickname.command;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import com.applefe.koreannickname.network.ModNetwork;
import com.applefe.koreannickname.service.NicknameService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Registers nickname editing and operator administration commands. */
public final class KoreanNicknameCommands {
    static final String USAGE_ERROR = "사용법: /한글닉 <닉네임> <플랫폼>";

    private KoreanNicknameCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("한글닉")
                .executes(KoreanNicknameCommands::openNicknameScreen)
                .then(Commands.literal("강제")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("플레이어", EntityArgument.player())
                                .executes(KoreanNicknameCommands::openTargetNicknameScreen)
                                .then(Commands.argument("입력", StringArgumentType.greedyString())
                                        .suggests(KoreanNicknameCommands::suggestPlatforms)
                                        .executes(KoreanNicknameCommands::forceNickname))))
                .then(Commands.argument("입력", StringArgumentType.greedyString())
                        .suggests(KoreanNicknameCommands::suggestPlatforms)
                        .executes(KoreanNicknameCommands::setNickname)));
    }

    private static int openNicknameScreen(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        openNicknameScreen(context.getSource(), player);
        return 1;
    }

    private static int openTargetNicknameScreen(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "플레이어");
        openNicknameScreen(context.getSource(), target);
        context.getSource().sendSuccess(
                () -> Component.literal(target.getGameProfile().getName() + "님에게 닉네임 설정 창을 열었습니다."),
                true);
        return 1;
    }

    private static void openNicknameScreen(CommandSourceStack source, ServerPlayer player) {
        Profile currentProfile = NicknameSavedData.get(source.getServer())
                .find(player.getUUID())
                .orElse(new Profile("", Platform.CHZZK));
        ModNetwork.openNicknameScreen(player, currentProfile.nickname(), currentProfile.platform());
    }

    private static int setNickname(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String[] input = parseOrReport(context, StringArgumentType.getString(context, "입력"));
        if (input == null) {
            return 0;
        }

        Platform platform = Platform.parse(input[1]).orElse(null);
        NicknameService.Result result = NicknameService.update(player, input[0], platform);
        if (!result.success()) {
            context.getSource().sendFailure(Component.literal(result.message()));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal(result.message()), false);
        return 1;
    }

    private static int forceNickname(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "플레이어");
        String[] input = parseOrReport(context, StringArgumentType.getString(context, "입력"));
        if (input == null) {
            return 0;
        }

        Platform platform = Platform.parse(input[1]).orElse(null);
        NicknameService.Result result = NicknameService.update(target, input[0], platform);
        if (!result.success()) {
            context.getSource().sendFailure(Component.literal(result.message()));
            return 0;
        }

        target.sendSystemMessage(Component.literal("관리자가 " + result.message()));
        context.getSource().sendSuccess(
                () -> Component.literal(target.getGameProfile().getName() + "님의 " + result.message()), true);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestPlatforms(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        int platformStart = trailingTokenStart(builder.getRemaining());
        if (platformStart == 0) {
            return builder.buildFuture();
        }

        SuggestionsBuilder platformBuilder = builder.createOffset(builder.getStart() + platformStart);
        return SharedSuggestionProvider.suggest(
                Arrays.stream(Platform.values())
                        .flatMap(platform -> java.util.stream.Stream.of(platform.koreanName(), platform.id())),
                platformBuilder);
    }

    private static String[] parseOrReport(CommandContext<CommandSourceStack> context, String rawInput) {
        try {
            return parseNicknameInput(rawInput);
        } catch (IllegalArgumentException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return null;
        }
    }

    static String[] parseNicknameInput(String rawInput) {
        String input = rawInput == null ? "" : rawInput.strip();
        int platformStart = trailingTokenStart(input);
        if (platformStart == 0) {
            throw new IllegalArgumentException(USAGE_ERROR);
        }

        String nicknameExpression = input.substring(0, platformStart).strip();
        String platform = input.substring(platformStart).strip();
        if (nicknameExpression.isEmpty() || platform.isEmpty()) {
            throw new IllegalArgumentException(USAGE_ERROR);
        }

        try {
            return new String[] {parseNickname(nicknameExpression), platform};
        } catch (CommandSyntaxException exception) {
            throw new IllegalArgumentException("닉네임 따옴표를 올바르게 닫아 주세요.", exception);
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
}
