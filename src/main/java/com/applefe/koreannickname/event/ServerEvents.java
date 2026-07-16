package com.applefe.koreannickname.event;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.SocietyKoreanNicknameMod;
import com.applefe.koreannickname.command.KoreanNicknameCommands;
import com.applefe.koreannickname.data.NicknameSavedData;
import com.applefe.koreannickname.data.NicknameSavedData.Profile;
import com.applefe.koreannickname.service.NicknamePresentation;
import com.applefe.koreannickname.service.SkillLevelService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side profile lifecycle and display-name hooks. */
@Mod.EventBusSubscriber(modid = SocietyKoreanNicknameMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private static final int LEVEL_REFRESH_TICKS = 100;

    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        KoreanNicknameCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            profile(player).ifPresent(value -> NicknamePresentation.apply(player, value));
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            profile(player).ifPresent(value -> NicknamePresentation.apply(player, value));
        }
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity().getCustomName() != null
                && Platform.fromTabName(event.getEntity().getCustomName()).isPresent()) {
            event.setDisplayname(event.getEntity().getCustomName());
        }
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            profile(player).ifPresent(value -> event.setDisplayName(
                    NicknamePresentation.tabName(value, SkillLevelService.highestLevel(player))));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % LEVEL_REFRESH_TICKS != 0) {
            return;
        }
        if (profile(player).isEmpty()) {
            return;
        }

        int level = SkillLevelService.highestLevel(player);
        int previous = player.getPersistentData().getInt(NicknamePresentation.LAST_LEVEL_KEY);
        if (level != previous) {
            player.getPersistentData().putInt(NicknamePresentation.LAST_LEVEL_KEY, level);
            player.refreshTabListName();
        }
    }

    private static java.util.Optional<Profile> profile(ServerPlayer player) {
        return NicknameSavedData.get(player.server).find(player.getUUID());
    }
}
