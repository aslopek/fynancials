package de.as.fynancials.common.image;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ImageServiceImpl implements ImageService {

  /**
   * Maximum height/width of a logo in pixels.
   */
  private static final int MAX_LOGO_SIZE = 256;

  private static final byte[] PNG_MAGIC_BYTES = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

  @Override
  public boolean isPng(byte[] imageBytes) {
    if (imageBytes == null || imageBytes.length < PNG_MAGIC_BYTES.length) {
      return false;
    }
    for (int i = 0; i < PNG_MAGIC_BYTES.length; i++) {
      if (imageBytes[i] != PNG_MAGIC_BYTES[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public byte[] scaleImage(byte[] imageBytes) {
    BufferedImage image;
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
      image = ImageIO.read(inputStream);
    } catch (IOException e) {
      log.warn(e.getMessage());
      return imageBytes;
    }

    int targetHeight;
    int targetWidth;

    if (image.getWidth() > image.getHeight()) {
      if (image.getWidth() <= MAX_LOGO_SIZE) {
        return imageBytes;
      }
      targetWidth = MAX_LOGO_SIZE;
      double factor = (double) MAX_LOGO_SIZE / image.getWidth();
      targetHeight = (int) (image.getHeight() * factor);
    } else {
      if (image.getHeight() <= MAX_LOGO_SIZE) {
        return imageBytes;
      }
      targetHeight = MAX_LOGO_SIZE;
      double factor = (double) MAX_LOGO_SIZE / image.getHeight();
      targetWidth = (int) (image.getWidth() * factor);
    }

    Image scaled = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    output.getGraphics().drawImage(scaled, 0, 0, null);

    byte[] result;
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      ImageIO.write(output, "png", outputStream);
      result = outputStream.toByteArray();
    } catch (IOException e) {
      log.warn(e.getMessage());
      result = imageBytes;
    }
    return result;
  }
}
