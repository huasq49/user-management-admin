package com.usermonitor.server.service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.ByteArrayInputStream;
import java.util.Locale;

/** Never trust an upload's MIME type; prohibit active HTML/SVG content on the app origin. */
public final class AvatarImages {
    private AvatarImages() {}

    public static String contentType(byte[] data) {
        if (data == null || data.length == 0 || data.length > 200 * 1024) {
            throw new IllegalArgumentException("头像不能为空且不能超过200KB");
        }
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("头像必须为PNG或JPEG图片");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("png") && !format.equals("jpeg") && !format.equals("jpg")) {
                    throw new IllegalArgumentException("头像必须为PNG或JPEG图片");
                }
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > 4096 || height > 4096) {
                    throw new IllegalArgumentException("头像尺寸不能超过4096像素");
                }
                return format.equals("png") ? "image/png" : "image/jpeg";
            } finally { reader.dispose(); }
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("无法读取头像图片");
        }
    }
}