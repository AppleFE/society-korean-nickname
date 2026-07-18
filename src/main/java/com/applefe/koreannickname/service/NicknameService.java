package com.applefe.koreannickname.service;

import com.applefe.koreannickname.NicknameValidator;
import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.data.NicknameSavedData;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import net.minecraft.server.level.ServerPlayer;

/** Validates and persists nickname changes from commands and network requests. */
public final class NicknameService {
    private NicknameService() {
    }

    public static Result update(ServerPlayer player, String rawNickname, Platform platform) {
        NicknameValidator.Result nickname = NicknameValidator.validate(rawNickname);
        if (!nickname.valid()) {
            return Result.failure(nickname.error());
        }
        if (platform == null) {
            return Result.failure("플랫폼은 치지직, 유튜브, 씨미, 숲 중 하나여야 합니다.");
        }

        NicknameSavedData data = NicknameSavedData.get(player.server);
        if (data.isNicknameUsedByOther(nickname.nickname(), player.getUUID())) {
            return Result.failure("이미 다른 플레이어가 사용 중인 닉네임입니다.");
        }

        Profile profile = new Profile(nickname.nickname(), platform);
        data.put(player.getUUID(), profile);
        NicknamePresentation.apply(player, profile);
        return Result.success("닉네임을 " + nickname.nickname() + "(으)로 변경했습니다. ["
                + platform.koreanName() + "]");
    }

    public record Result(boolean success, String message) {
        private static Result success(String message) {
            return new Result(true, message);
        }

        private static Result failure(String message) {
            return new Result(false, message);
        }
    }
}
