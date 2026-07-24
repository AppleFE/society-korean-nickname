package com.applefe.koreannickname.mixin;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.SocietyKoreanNicknameMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces vanilla latency bars with the player's selected streaming-platform icon. */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Inject(method = "renderPingIcon", at = @At("HEAD"), cancellable = true)
    private void societyKoreanNickname$renderPlatformIcon(
            GuiGraphics graphics, int rowWidth, int rowX, int rowY, PlayerInfo playerInfo, CallbackInfo callback) {
        Platform.fromTabName(playerInfo.getTabListDisplayName()).ifPresent(platform -> {
            int sourceSize = textureSize(platform);
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 100.0F);
            graphics.blit(texture(platform), rowX + rowWidth - 10, rowY,
                    8, 8, 0.0F, 0.0F, sourceSize, sourceSize, sourceSize, sourceSize);
            graphics.pose().popPose();
            callback.cancel();
        });
    }

    private static ResourceLocation texture(Platform platform) {
        return ResourceLocation.fromNamespaceAndPath(SocietyKoreanNicknameMod.MOD_ID,
                "textures/gui/platform/" + platform.id() + ".png");
    }

    private static int textureSize(Platform platform) {
        return switch (platform) {
            case CHZZK -> 512;
            case YOUTUBE -> 32;
            case CIME -> 180;
            case SOOP -> 256;
            case ADMIN -> 2048;
        };
    }
}
