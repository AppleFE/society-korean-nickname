package com.applefe.koreannickname.event;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.SocietyKoreanNicknameMod;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Ensures synchronized custom nicknames are used above player skins. */
@Mod.EventBusSubscriber(
        modid = SocietyKoreanNicknameMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Player player
                && player.getCustomName() != null
                && Platform.fromTabName(player.getCustomName()).isPresent()) {
            event.setContent(player.getCustomName());
        }
    }
}
