package cz.cyberrange.platform.userandgroup.service;

import cz.cyberrange.platform.userandgroup.definition.exceptions.IconGenerationException;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

/** Generates a deterministic icon image from a hash of the given text. */
@Service
public class IdenticonService {

  /**
   * Generates an icon image for the given text, scaled to the given size. The pattern and color
   * come from the SHA-256 hash of the text, so the same text always produces the same image.
   *
   * @param text text to derive the icon from
   * @param image_width width of the generated image, in pixels
   * @param image_height height of the generated image, in pixels
   * @return the icon encoded as PNG bytes
   * @throws IconGenerationException when the image cannot be encoded as PNG
   */
  public byte[] generateIdenticons(String text, int image_width, int image_height) {
    String sha256hexIcon = DigestUtils.sha256Hex(text);
    int width = 5, height = 5;

    byte[] hash = sha256hexIcon.getBytes();

    BufferedImage identicon = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    WritableRaster raster = identicon.getRaster();

    int[] background = new int[] {255, 255, 255, 0};
    int[] foreground = new int[] {hash[0] & 255, hash[1] & 255, hash[2] & 255, 255};

    for (int x = 0; x < width; x++) {
      int i = x < 3 ? x : 4 - x;
      for (int y = 0; y < height; y++) {
        int[] pixelColor;
        if ((hash[i] >> y & 1) == 1) pixelColor = foreground;
        else pixelColor = background;
        raster.setPixel(x, y, pixelColor);
      }
    }

    BufferedImage finalImage =
        new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_ARGB);

    AffineTransform at = new AffineTransform();
    at.scale(image_width / width, image_height / height);
    AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    finalImage = op.filter(identicon, finalImage);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      ImageIO.write(finalImage, "png", outputStream);
    } catch (IOException ex) {
      throw new IconGenerationException(
          "Cannot generate icon image of the current user. Error is: " + ex.getMessage());
    }
    return outputStream.toByteArray();
  }
}
