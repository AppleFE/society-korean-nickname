package com.applefe.koreannickname.command;

import com.applefe.koreannickname.NicknameValidator;
import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import com.applefe.koreannickname.service.NicknamePresentation;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Registers the self-service /한글닉 command. */
public final class KoreanNicknameCommands {
    private KoreanNicknameCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("한글닉")
                .then(Commands.argument("입력", StringArgumentType.greedyString())
                        .suggests(KoreanNicknameCommands::suggestPlatforms)
                        .executes(KoreanNicknameCommands::setNickname)));
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

        NicknameValidator.Result nickname = NicknameValidator.validate(input.nickname());
        if (!nickname.valid()) {
            context.getSource().sendFailure(Component.literal(nickname.error()));
            return 0;
        }

        Platform platform = Platform.parse(input.platform()).orElse(null);
        if (platform == null) {
            context.getSource().sendFailure(Component.literal("플랫폼은 치지직, 유튜브, 씨미 중 하나여야 합니다."));
            return 0;
        }

        NicknameSavedData data = NicknameSavedData.get(context.getSource().getServer());
        if (data.isNicknameUsedByOther(nickname.nickname(), player.getUUID())) {
            context.getSource().sendFailure(Component.literal("이미 다른 플레이어가 사용 중인 닉네임입니다."));
            return 0;
        }

        Profile profile = new Profile(nickname.nickname(), platform);
        data.put(player.getUUID(), profile);
        NicknamePresentation.apply(player, profile);
        context.getSource().sendSuccess(() -> Component.literal("닉네임을 ")
                .append(NicknamePresentation.styledNickname(profile))
                .append(Component.literal("(으)로 변경했습니다. [" + platform.koreanName() + "]")), false);
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
