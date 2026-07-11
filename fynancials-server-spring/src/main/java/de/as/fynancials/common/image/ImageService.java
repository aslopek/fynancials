package de.as.fynancials.common.image;

public interface ImageService {

  boolean isPng(byte[] imageBytes);

  byte[] scaleImage(byte[] imageBytes);
}
