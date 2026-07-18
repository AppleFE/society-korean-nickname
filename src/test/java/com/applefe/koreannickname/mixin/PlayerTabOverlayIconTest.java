package com.applefe.koreannickname.mixin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.applefe.koreannickname.Platform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
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
    void packagesGeneratedNicknameEditorPanelAtExpectedSize() throws IOException {
        String path = "/assets/society_korean_nickname/textures/gui/nickname_editor.png";
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, () -> "누락된 닉네임 편집 패널: " + path);
            BufferedImage image = ImageIO.read(stream);

            assertNotNull(image, () -> "읽을 수 없는 닉네임 편집 패널: " + path);
            assertEquals(768, image.getWidth());
            assertEquals(512, image.getHeight());
            assertEquals(3 * image.getHeight(), 2 * image.getWidth());
        }
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
