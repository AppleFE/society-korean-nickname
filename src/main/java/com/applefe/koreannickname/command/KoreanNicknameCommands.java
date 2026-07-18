package com.applefe.koreannickname.command;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import com.applefe.koreannickname.network.ModNetwork;
import com.applefe.koreannickname.service.NicknameService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
    private KoreanNicknameCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("한글닉")
                .executes(KoreanNicknameCommands::openNicknameScreen)
                .then(Commands.literal("강제")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("플레이어", EntityArgument.player())
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
        Profile currentProfile = NicknameSavedData.get(context.getSource().getServer())
                .find(player.getUUID())
                .orElse(new Profile("", Platform.CHZZK));
        ModNetwork.openNicknameScreen(player, currentProfile.nickname(), currentProfile.platform());
        return 1;
    }

    private static int setNickname(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        NicknameCommandInput.Result input = NicknameCommandInput.parse(
                StringArgumentType.getString(context, "입력"));
        if (!input.valid()) {
            context.getSource().sendFailure(Component.literal(input.error()));
            return 0;
        }

        Platform platform = Platform.parse(input.platform()).orElse(null);
        NicknameService.Result result = NicknameService.update(player, input.nickname(), platform);
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
        NicknameCommandInput.Result input = NicknameCommandInput.parse(
                StringArgumentType.getString(context, "입력"));
        if (!input.valid()) {
            context.getSource().sendFailure(Component.literal(input.error()));
            return 0;
        }

        Platform platform = Platform.parse(input.platform()).orElse(null);
        NicknameService.Result result = NicknameService.update(target, input.nickname(), platform);
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
        int platformStart = NicknameCommandInput.trailingTokenStart(builder.getRemaining());
        if (platformStart == 0) {
            return builder.buildFuture();
        }

        SuggestionsBuilder platformBuilder = builder.createOffset(builder.getStart() + platformStart);
        return SharedSuggestionProvider.suggest(
                Arrays.stream(Platform.values())
                        .flatMap(platform -> java.util.stream.Stream.of(platform.koreanName(), platform.id())),
                platformBuilder);
    }
}
