package com.biso.capacitor.plugins.rest.information.from.image;

import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.LOADING_CIRCLE_COLOR;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.LOADING_CIRCLE_SIZE;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.IMAGE_HEIGHT;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.IMAGE_WIDTH;
import static com.biso.capacitor.plugins.rest.information.from.image.Utils.getAspectRatioFromString;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.BEEP_ON_SUCCESS;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.DETECTOR_ASPECT_RATIO;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.DETECTOR_SIZE;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.DRAW_FOCUS_BACKGROUND;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.DRAW_FOCUS_LINE;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.DRAW_FOCUS_RECT;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.FOCUS_BACKGROUND_COLOR;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.FOCUS_LINE_COLOR;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.FOCUS_LINE_THICKNESS;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.FOCUS_RECT_BORDER_RADIUS;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.FOCUS_RECT_BORDER_THICKNESS;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.FOCUS_RECT_COLOR;
import static com.biso.capacitor.plugins.rest.information.from.image.ScannerSettings.Settings.VIBRATE_ON_SUCCESS;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import org.json.JSONObject;

public class ScannerSettings implements Parcelable {

  private String aspectRatio = "1:1";
  private float aspectRatioF = 1;
  private double detectorSize = 0.5;
  private boolean drawFocusRect = true;
  private String focusRectColor = "#FFFFFF";
  private int focusRectBorderRadius = 100;
  private int focusRectBorderThickness = 2;
  private boolean drawFocusLine = true;
  private String focusLineColor = "#FFFFFF";
  private int focusLineThickness = 1;
  private boolean drawFocusBackground = true;
  private String focusBackgroundColor = "#CCFFFFFF";
  private boolean beepOnSuccess = false;
  private boolean vibrateOnSuccess = false;
  private String loadingCircleColor = "#FF00FF";
  private int loadingCircleSize = 20;
  private int imageWidth = 720;
  private int imageHeight = 1280;

  public ScannerSettings(JSONObject settings) {
    Iterator<String> keys = settings.keys();

    while (keys.hasNext()) {
      String key = keys.next();

      Optional<Settings> setting = Settings.get(key);
      if (setting.isPresent()) {
        switch (setting.get()) {
          case DETECTOR_ASPECT_RATIO:
            aspectRatio = settings.optString(DETECTOR_ASPECT_RATIO.value(), getAspectRatio());
            aspectRatioF = getAspectRatioFromString(aspectRatio);
            break;
          case DETECTOR_SIZE:
            double temp = settings.optDouble(DETECTOR_SIZE.value(), getDetectorSize());
            if (!(temp < 0 || temp > 1)) {
              detectorSize = temp;
            }
            break;
          case DRAW_FOCUS_RECT:
            drawFocusRect = settings.optBoolean(DRAW_FOCUS_RECT.value(), isDrawFocusRect());
            break;
          case FOCUS_RECT_COLOR:
            focusRectColor = settings.optString(FOCUS_RECT_COLOR.value(), getFocusRectColor());
            break;
          case FOCUS_RECT_BORDER_RADIUS:
            focusRectBorderRadius = settings.optInt(FOCUS_RECT_BORDER_RADIUS.value(),
                getFocusRectBorderRadius());
            break;
          case FOCUS_RECT_BORDER_THICKNESS:
            focusRectBorderThickness = settings.optInt(FOCUS_RECT_BORDER_THICKNESS.value(),
                getFocusRectBorderThickness());
            break;
          case DRAW_FOCUS_LINE:
            drawFocusLine = settings.optBoolean(DRAW_FOCUS_LINE.value(), isDrawFocusLine());
            break;
          case FOCUS_LINE_COLOR:
            focusLineColor = settings.optString(FOCUS_LINE_COLOR.value(), getFocusLineColor());
            break;
          case FOCUS_LINE_THICKNESS:
            focusLineThickness = settings.optInt(FOCUS_LINE_THICKNESS.value(),
                getFocusLineThickness());
            break;
          case DRAW_FOCUS_BACKGROUND:
            drawFocusBackground = settings.optBoolean(DRAW_FOCUS_BACKGROUND.value(),
                isDrawFocusBackground());
            break;
          case FOCUS_BACKGROUND_COLOR:
            focusBackgroundColor = settings.optString(FOCUS_BACKGROUND_COLOR.value(),
                getFocusBackgroundColor());
            break;
          case BEEP_ON_SUCCESS:
            beepOnSuccess = settings.optBoolean(BEEP_ON_SUCCESS.value(), isBeepOnSuccess());
            break;
          case VIBRATE_ON_SUCCESS:
            vibrateOnSuccess = settings.optBoolean(VIBRATE_ON_SUCCESS.value(),
                isVibrateOnSuccess());
            break;
          case LOADING_CIRCLE_COLOR:
            loadingCircleColor = settings.optString(LOADING_CIRCLE_COLOR.value(),
                getLoadingCircleColor());
            break;
          case LOADING_CIRCLE_SIZE:
            loadingCircleSize = settings.optInt(LOADING_CIRCLE_SIZE.value(), getLoadingCircleSize());
            break;
          case IMAGE_WIDTH:
            imageWidth = settings.optInt(IMAGE_WIDTH.value(), getImageWidth());
            break;
          case IMAGE_HEIGHT:
            imageHeight = settings.optInt(IMAGE_HEIGHT.value(), getImageHeight());
            break;
          default:
            Log.e("SETTINGS", "No known setting for " + key);
            break;
        }
      } else {
        Log.e("SETTINGS", "No known setting for " + key);
      }
    }
  }

  public String getAspectRatio() {
    return aspectRatio;
  }

  public float getAspectRatioF() {
    return aspectRatioF;
  }

  public double getDetectorSize() {
    return detectorSize;
  }

  public boolean isDrawFocusRect() {
    return drawFocusRect;
  }

  public String getFocusRectColor() {
    return focusRectColor;
  }

  public int getFocusRectBorderRadius() {
    return focusRectBorderRadius;
  }

  public int getFocusRectBorderThickness() {
    return focusRectBorderThickness;
  }

  public boolean isDrawFocusLine() {
    return drawFocusLine;
  }

  public String getFocusLineColor() {
    return focusLineColor;
  }

  public int getFocusLineThickness() {
    return focusLineThickness;
  }

  public boolean isDrawFocusBackground() {
    return drawFocusBackground;
  }

  public String getFocusBackgroundColor() {
    return focusBackgroundColor;
  }

  public boolean isBeepOnSuccess() {
    return beepOnSuccess;
  }

  public boolean isVibrateOnSuccess() {
    return vibrateOnSuccess;
  }

  public String getLoadingCircleColor() {
    return loadingCircleColor;
  }

  public int getLoadingCircleSize() {
    return loadingCircleSize;
  }

  public int getImageWidth() {
    return imageWidth;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScannerSettings that = (ScannerSettings) o;
    return Float.compare(that.getAspectRatioF(), getAspectRatioF()) == 0
        && Double.compare(that.getDetectorSize(), getDetectorSize()) == 0
        && isDrawFocusRect() == that.isDrawFocusRect()
        && getFocusRectBorderRadius() == that.getFocusRectBorderRadius()
        && getFocusRectBorderThickness() == that.getFocusRectBorderThickness()
        && isDrawFocusLine() == that.isDrawFocusLine()
        && getFocusLineThickness() == that.getFocusLineThickness()
        && isDrawFocusBackground() == that.isDrawFocusBackground()
        && isBeepOnSuccess() == that.isBeepOnSuccess()
        && isVibrateOnSuccess() == that.isVibrateOnSuccess()
        && getAspectRatio().equals(that.getAspectRatio()) && getFocusRectColor().equals(
        that.getFocusRectColor())
        && getFocusLineColor().equals(that.getFocusLineColor()) && getFocusBackgroundColor().equals(
        that.getFocusBackgroundColor())
        && getLoadingCircleColor().equals(that.getLoadingCircleColor())
        && getLoadingCircleSize() == that.getLoadingCircleSize()
        && getImageWidth() == that.getImageWidth()
        && getImageHeight() == that.getImageHeight();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getAspectRatio(),
        getAspectRatioF(),
        getDetectorSize(),
        isDrawFocusRect(),
        getFocusRectColor(),
        getFocusRectBorderRadius(),
        getFocusRectBorderThickness(),
        isDrawFocusLine(),
        getFocusLineColor(),
        getFocusLineThickness(),
        isDrawFocusBackground(),
        getFocusBackgroundColor(),
        isBeepOnSuccess(),
        isVibrateOnSuccess(),
        getLoadingCircleColor(),
        getLoadingCircleSize(),
        getImageWidth(),
        getImageHeight()
    );
  }

  @Override
  public int describeContents() {
    return hashCode();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.getAspectRatio());
    dest.writeFloat(this.getAspectRatioF());
    dest.writeDouble(this.getDetectorSize());
    dest.writeByte(this.isDrawFocusRect() ? (byte) 1 : (byte) 0);
    dest.writeString(this.getFocusRectColor());
    dest.writeInt(this.getFocusRectBorderRadius());
    dest.writeInt(this.getFocusRectBorderThickness());
    dest.writeByte(this.isDrawFocusLine() ? (byte) 1 : (byte) 0);
    dest.writeString(this.getFocusLineColor());
    dest.writeInt(this.getFocusLineThickness());
    dest.writeByte(this.isDrawFocusBackground() ? (byte) 1 : (byte) 0);
    dest.writeString(this.getFocusBackgroundColor());
    dest.writeByte(this.isBeepOnSuccess() ? (byte) 1 : (byte) 0);
    dest.writeByte(this.isVibrateOnSuccess() ? (byte) 1 : (byte) 0);
    dest.writeString(this.getLoadingCircleColor());
    dest.writeInt(this.getLoadingCircleSize());
    dest.writeInt(this.getImageWidth());
    dest.writeInt(this.getImageHeight());
  }

  protected ScannerSettings(Parcel in) {
    this.aspectRatio = in.readString();
    this.aspectRatioF = in.readFloat();
    this.detectorSize = in.readDouble();
    this.drawFocusRect = in.readByte() != 0;
    this.focusRectColor = in.readString();
    this.focusRectBorderRadius = in.readInt();
    this.focusRectBorderThickness = in.readInt();
    this.drawFocusLine = in.readByte() != 0;
    this.focusLineColor = in.readString();
    this.focusLineThickness = in.readInt();
    this.drawFocusBackground = in.readByte() != 0;
    this.focusBackgroundColor = in.readString();
    this.beepOnSuccess = in.readByte() != 0;
    this.vibrateOnSuccess = in.readByte() != 0;
    this.loadingCircleColor = in.readString();
    this.loadingCircleSize = in.readInt();
    this.imageWidth = in.readInt();
    this.imageHeight = in.readInt();
  }

  public static final Creator<ScannerSettings> CREATOR = new Creator<>() {
    @Override
    public ScannerSettings createFromParcel(Parcel source) {
      return new ScannerSettings(source);
    }

    @Override
    public ScannerSettings[] newArray(int size) {
      return new ScannerSettings[size];
    }
  };

  public enum Settings {

    BARCODE_FORMATS("barcodeFormats"),
    DETECTOR_ASPECT_RATIO("detectorAspectRatio"),
    DETECTOR_SIZE("detectorSize"),
    ROTATE_CAMERA("rotateCamera"),
    DRAW_FOCUS_RECT("drawFocusRect"),
    FOCUS_RECT_COLOR("focusRectColor"),
    FOCUS_RECT_BORDER_RADIUS("focusRectBorderRadius"),
    FOCUS_RECT_BORDER_THICKNESS("focusRectBorderThickness"),
    DRAW_FOCUS_LINE("drawFocusLine"),
    FOCUS_LINE_COLOR("focusLineColor"),
    FOCUS_LINE_THICKNESS("focusLineThickness"),
    DRAW_FOCUS_BACKGROUND("drawFocusBackground"),
    FOCUS_BACKGROUND_COLOR("focusBackgroundColor"),
    BEEP_ON_SUCCESS("beepOnSuccess"),
    VIBRATE_ON_SUCCESS("vibrateOnSuccess"),
    LOADING_CIRCLE_COLOR("loadingCircleColor"),
    LOADING_CIRCLE_SIZE("loadingCircleSize"),
    IMAGE_WIDTH("imageWidth"),
    IMAGE_HEIGHT("imageHeight");

    private final String option;

    Settings(String option) {
      this.option = option;
    }

    public String value() {
      return option;
    }

    public static Optional<Settings> get(String option) {
      return Arrays.stream(Settings.values())
          .filter(o -> o.option.equals(option))
          .findFirst();
    }
  }
}
