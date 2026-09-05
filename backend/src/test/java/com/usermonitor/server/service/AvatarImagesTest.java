package com.usermonitor.server.service;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
class AvatarImagesTest {
    @Test void acceptsPngAndJpegByActualContent() throws Exception {
        for (String format : new String[]{"png", "jpeg"}) {
            var output = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, output);
            assertEquals("image/" + format, AvatarImages.contentType(output.toByteArray()));
        }
    }
    @Test void rejectsActiveContentAndOversizedData() {
        for (byte[] data : new byte[][]{new byte[0], new byte[201 * 1024],
                "<svg onload='alert(1)'/>".getBytes(), "<html>test</html>".getBytes()}) {
            assertThrows(IllegalArgumentException.class, () -> AvatarImages.contentType(data));
        }
    }
    @Test void rejectsExcessiveDimensionsBeforeDecodingPixels() throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4097, 1, BufferedImage.TYPE_INT_RGB), "png", output);
        assertThrows(IllegalArgumentException.class, () -> AvatarImages.contentType(output.toByteArray()));
    }
}