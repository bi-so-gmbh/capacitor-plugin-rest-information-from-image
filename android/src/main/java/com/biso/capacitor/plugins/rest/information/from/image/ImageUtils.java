package com.biso.capacitor.plugins.rest.information.from.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import android.graphics.Matrix;

public class ImageUtils {

  public static Bitmap rgba8888ToBitmap(Image image) {
    Image.Plane[] planes = image.getPlanes();
    ByteBuffer buffer = planes[0].getBuffer();
    int pixelStride = planes[0].getPixelStride();
    int rowStride = planes[0].getRowStride();
    int rowPadding = rowStride - pixelStride * image.getWidth();
    Bitmap bitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride,
        image.getHeight(), Bitmap.Config.ARGB_8888);
    bitmap.copyPixelsFromBuffer(buffer);
    return bitmap;
  }

  public static Bitmap jpegToBitmap(Image image) {
    ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
    byteBuffer.rewind();
    byte[] bytes = new byte[byteBuffer.capacity()];
    byteBuffer.get(bytes);
    byte[] clonedBytes = bytes.clone();

    return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
  }

  public static ByteArrayOutputStream imageToByteArrayOutputStream(Image image, int rotation) {
    Bitmap bitmap = switch (image.getFormat()) {
      case ImageFormat.JPEG -> rotateBitmap(jpegToBitmap(image), rotation);
      case 1 -> rotateBitmap(rgba8888ToBitmap(image), rotation);
      default ->
        throw new IllegalStateException("Image format " + image.getFormat() + " not supported");
    };

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.JPEG, 75, byteArrayOutputStream);

    return byteArrayOutputStream;
  }

  public static String imageToBase64(ByteArrayOutputStream byteArrayOutputStream) {
    return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
  }

  public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
    Matrix mat = new Matrix();
    mat.postRotate(degrees);
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
  }

  public static void saveImageToDisk(ByteArrayOutputStream byteArrayOutputStream, String folder, String fileName) {
    try {
      File folderAsFile = new File(folder);
      folderAsFile.mkdirs();
      File imageAsFile = new File(folderAsFile, fileName);
      OutputStream outputStream = new FileOutputStream(imageAsFile);
      byteArrayOutputStream.writeTo(outputStream);
    } catch (IOException e) {
      Log.e("SaveToDisk", e.getMessage(), e);
    }
  }

  private ImageUtils() {
    throw new IllegalStateException("Utility class");
  }
}
