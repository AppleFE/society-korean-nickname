package com.applefe.koreannickname.network;

import com.applefe.koreannickname.Platform;
import com.applefe.koreannickname.SocietyKoreanNicknameMod;
import com.applefe.koreannickname.service.NicknameService;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Network packets used by the nickname editor. */
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final int MAX_NICKNAME_LENGTH = 64;
    private static final int MAX_PLATFORM_LENGTH = 16;
    private static final int MAX_RESULT_MESSAGE_LENGTH = 256;
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(SocietyKoreanNicknameMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();
    private static boolean registered;

    private ModNetwork() {
    }

    /** Registers packet codecs during common setup. */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        int id = 0;
        CHANNEL.registerMessage(id++, OpenNicknameScreenPacket.class,
                ModNetwork::encodeOpenNicknameScreen,
                ModNetwork::decodeOpenNicknameScreen,
                ModNetwork::handleOpenNicknameScreen);
        CHANNEL.registerMessage(id++, SubmitNicknamePacket.class,
                ModNetwork::encodeSubmitNickname,
                ModNetwork::decodeSubmitNickname,
                ModNetwork::handleSubmitNickname);
        CHANNEL.registerMessage(id, NicknameResultPacket.class,
                ModNetwork::encodeNicknameResult,
                ModNetwork::decodeNicknameResult,
                ModNetwork::handleNicknameResult);
    }

    /** Opens the nickname editor for a player with the supplied current profile values. */
    public static void openNicknameScreen(ServerPlayer player, String nickname, Platform platform) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(platform, "platform");
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenNicknameScreenPacket(nickname == null ? "" : nickname, platform.id()));
    }

    /** Sends the nickname editor's current selection to the server. */
    public static void submitNickname(String nickname, Platform platform) {
        Objects.requireNonNull(platform, "platform");
        CHANNEL.sendToServer(new SubmitNicknamePacket(nickname == null ? "" : nickname, platform.id()));
    }

    private static void encodeOpenNicknameScreen(OpenNicknameScreenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.nickname(), MAX_NICKNAME_LENGTH);
        buffer.writeUtf(packet.platformId(), MAX_PLATFORM_LENGTH);
    }

    private static OpenNicknameScreenPacket decodeOpenNicknameScreen(FriendlyByteBuf buffer) {
        return new OpenNicknameScreenPacket(
                buffer.readUtf(MAX_NICKNAME_LENGTH),
                buffer.readUtf(MAX_PLATFORM_LENGTH));
    }

    private static void handleOpenNicknameScreen(OpenNicknameScreenPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.applefe.koreannickname.client.ClientPacketHandler
                        .openNicknameScreen(packet.nickname(), packet.platformId())));
        context.setPacketHandled(true);
    }

    private static void encodeSubmitNickname(SubmitNicknamePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.nickname(), MAX_NICKNAME_LENGTH);
        buffer.writeUtf(packet.platformId(), MAX_PLATFORM_LENGTH);
    }

    private static SubmitNicknamePacket decodeSubmitNickname(FriendlyByteBuf buffer) {
        return new SubmitNicknamePacket(
                buffer.readUtf(MAX_NICKNAME_LENGTH),
                buffer.readUtf(MAX_PLATFORM_LENGTH));
    }

    private static void handleSubmitNickname(SubmitNicknamePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            Platform platform = Platform.parse(packet.platformId()).orElse(null);
            if (platform == null) {
                sendResult(player, false, "플랫폼을 다시 선택해 주세요.");
                return;
            }

            NicknameService.Result result = NicknameService.update(player, packet.nickname(), platform);
            sendResult(player, result.success(), result.message());
        });
        context.setPacketHandled(true);
    }

    private static void encodeNicknameResult(NicknameResultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success());
        buffer.writeUtf(packet.message(), MAX_RESULT_MESSAGE_LENGTH);
    }

    private static NicknameResultPacket decodeNicknameResult(FriendlyByteBuf buffer) {
        return new NicknameResultPacket(buffer.readBoolean(), buffer.readUtf(MAX_RESULT_MESSAGE_LENGTH));
    }

    private static void handleNicknameResult(NicknameResultPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.applefe.koreannickname.client.ClientPacketHandler
                        .handleNicknameResult(packet.success(), packet.message())));
        context.setPacketHandled(true);
    }

    private static void sendResult(ServerPlayer player, boolean success, String message) {
        String safeMessage = message == null ? "" : message;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new NicknameResultPacket(success, safeMessage));
    }

    private record OpenNicknameScreenPacket(String nickname, String platformId) {
    }

    private record SubmitNicknamePacket(String nickname, String platformId) {
    }

    private record NicknameResultPacket(boolean success, String message) {
    }
}
