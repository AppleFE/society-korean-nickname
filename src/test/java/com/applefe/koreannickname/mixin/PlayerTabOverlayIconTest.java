package com.applefe.koreannickname.mixin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.applefe.koreannickname.Platform;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PlayerTabOverlayIconTest {
    @Test
    void declaresTheFullDimensionsOfEveryPlatformTexture() throws IOException {
        for (Platform platform : Platform.values()) {
            String path = "/assets/society_korean_nickname/textures/gui/platform/"
                    + platform.id() + ".png";
            try (InputStream stream = getClass().getResourceAsStream(path)) {
                assertNotNull(stream, () -> "누락된 플랫폼 아이콘: " + path);
                BufferedImage image = ImageIO.read(stream);
                int sourceSize = expectedTextureSize(platform);

                assertNotNull(image, () -> "읽을 수 없는 플랫폼 아이콘: " + path);
                assertEquals(sourceSize, image.getWidth(), path);
                assertEquals(sourceSize, image.getHeight(), path);
                assertTrue(hasVisiblePixel(image), () -> "비어 있는 플랫폼 아이콘: " + path);
            }
        }
    }

    @Test
    void packagesStardewEditorAssetsAndLayout() throws IOException {
        String root = "/assets/society_korean_nickname/ui/nickname_editor/";
        try (InputStream stream = getClass().getResourceAsStream(root + "layout.json")) {
            assertNotNull(stream, "누락된 닉네임 UI 레이아웃");
            assertTrue(JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)).isJsonObject());
        }

        assertImage(root + "sprites/nickname_panel__normal.png", 1320, 880);
        for (String state : new String[] {"normal", "focused", "disabled"}) {
            assertImage(root + "sprites/nickname_input__" + state + ".png", 1128, 112);
        }
        for (String state : new String[] {"normal", "hover", "pressed", "disabled", "selected"}) {
            assertImage(root + "sprites/platform_button__" + state + ".png", 344, 96);
        }
        for (String state : new String[] {"normal", "hover", "pressed", "disabled"}) {
            assertImage(root + "sprites/action_button__" + state + ".png", 328, 96);
        }
        assertImage(root + "sprites/preview_panel__normal.png", 1128, 104);
        assertImage(root + "sprites/icon_badge__normal.png", 96, 96);
    }

    @Test
    void keepsMixinStaticHelpersPrivate() throws NoSuchMethodException {
        int modifiers = PlayerTabOverlayMixin.class
                .getDeclaredMethod("textureSize", Platform.class)
                .getModifiers();

        assertTrue(Modifier.isPrivate(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
    }

    private static int expectedTextureSize(Platform platform) {
        return switch (platform) {
            case CHZZK -> 512;
            case YOUTUBE -> 32;
            case CIME -> 180;
        };
    }

    private static void assertImage(String path, int width, int height) throws IOException {
        try (InputStream stream = PlayerTabOverlayIconTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, () -> "누락된 UI 이미지: " + path);
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, () -> "읽을 수 없는 UI 이미지: " + path);
            assertEquals(width, image.getWidth(), path);
            assertEquals(height, image.getHeight(), path);
        }
    }

    private static boolean hasVisiblePixel(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
