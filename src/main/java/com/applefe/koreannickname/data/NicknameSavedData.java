package com.applefe.koreannickname.data;

import com.applefe.koreannickname.NicknameValidator;
import com.applefe.koreannickname.Platform;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent nickname profiles keyed by the player's UUID. */
public final class NicknameSavedData extends SavedData {
    public static final String DATA_NAME = "society_korean_nicknames";
    private static final String PROFILES_KEY = "Profiles";
    private static final String UUID_KEY = "Uuid";
    private static final String NICKNAME_KEY = "Nickname";
    private static final String PLATFORM_KEY = "Platform";

    private final Map<UUID, Profile> profiles = new LinkedHashMap<>();

    public static NicknameSavedData get(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        return server.overworld().getDataStorage().computeIfAbsent(
                NicknameSavedData::load, NicknameSavedData::new, DATA_NAME);
    }

    public static NicknameSavedData load(CompoundTag tag) {
        NicknameSavedData data = new NicknameSavedData();
        ListTag profiles = tag.getList(PROFILES_KEY, Tag.TAG_COMPOUND);
        for (Tag raw : profiles) {
            CompoundTag profileTag = (CompoundTag) raw;
            if (!profileTag.hasUUID(UUID_KEY)) {
                continue;
            }
            NicknameValidator.Result nickname = NicknameValidator.validate(profileTag.getString(NICKNAME_KEY));
            Optional<Platform> platform = Platform.parse(profileTag.getString(PLATFORM_KEY));
            if (nickname.valid() && platform.isPresent()) {
                data.profiles.put(profileTag.getUUID(UUID_KEY), new Profile(nickname.nickname(), platform.get()));
            }
        }
        return data;
    }

    @Override
    public synchronized CompoundTag save(CompoundTag tag) {
        ListTag serialized = new ListTag();
        for (Map.Entry<UUID, Profile> entry : profiles.entrySet()) {
            CompoundTag profileTag = new CompoundTag();
            profileTag.putUUID(UUID_KEY, entry.getKey());
            profileTag.putString(NICKNAME_KEY, entry.getValue().nickname());
            profileTag.putString(PLATFORM_KEY, entry.getValue().platform().id());
            serialized.add(profileTag);
        }
        tag.put(PROFILES_KEY, serialized);
        return tag;
    }

    public synchronized Optional<Profile> find(UUID playerId) {
        return Optional.ofNullable(profiles.get(Objects.requireNonNull(playerId, "playerId")));
    }

    public synchronized boolean isNicknameUsedByOther(String nickname, UUID playerId) {
        String key = nickname.toLowerCase(Locale.ROOT);
        return profiles.entrySet().stream()
                .anyMatch(entry -> !entry.getKey().equals(playerId)
                        && entry.getValue().nickname().toLowerCase(Locale.ROOT).equals(key));
    }

    public synchronized void put(UUID playerId, Profile profile) {
        profiles.put(Objects.requireNonNull(playerId, "playerId"), Objects.requireNonNull(profile, "profile"));
        setDirty();
    }

    public record Profile(String nickname, Platform platform) {
        public Profile {
            Objects.requireNonNull(nickname, "nickname");
            Objects.requireNonNull(platform, "platform");
        }
    }
}
