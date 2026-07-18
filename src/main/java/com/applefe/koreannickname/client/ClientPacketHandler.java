package com.applefe.koreannickname.client;

import com.applefe.koreannickname.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Client-only receivers for nickname editor packets. */
public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void openNicknameScreen(String nickname, String platformId) {
        Platform platform = Platform.parse(platformId).orElse(Platform.CHZZK);
        Minecraft.getInstance().setScreen(new NicknameScreen(nickname, platform));
    }

    public static void handleNicknameResult(boolean success, String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof NicknameScreen nicknameScreen) {
            nicknameScreen.handleResult(success, message);
            return;
        }

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(message), false);
        }
    }
}
