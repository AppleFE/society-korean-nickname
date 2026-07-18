package com.applefe.koreannickname;

import com.applefe.koreannickname.network.ModNetwork;
import net.minecraftforge.fml.common.Mod;

/** Society: Sunlit Valley nickname integration entry point. */
@Mod(SocietyKoreanNicknameMod.MOD_ID)
public final class SocietyKoreanNicknameMod {
    public static final String MOD_ID = "society_korean_nickname";

    public SocietyKoreanNicknameMod() {
        ModNetwork.register();
    }
}
