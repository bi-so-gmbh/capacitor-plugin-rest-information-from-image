package com.biso.capacitor.plugins.rest.information.from.image;

import static com.biso.capacitor.plugins.rest.information.from.image.Utils.calculateRectF;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import org.json.JSONObject;

public class CameraOverlay extends SurfaceView implements Callback {

  private final ScannerSettings settings;
  private RectF scanArea;
  public CameraOverlay(Context context, ScannerSettings settings) {
    super(context);
    this.settings = settings;
    setZOrderOnTop(true);
    SurfaceHolder holder = getHolder();
    holder.setFormat(PixelFormat.TRANSPARENT);
    holder.addCallback(this);
  }

  public CameraOverlay(Context context) {
    this(context, new ScannerSettings(new JSONObject()));
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    // intentionally empty
  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    scanArea = calculateRectF(surfaceHolder.getSurfaceFrame().height(),
        surfaceHolder.getSurfaceFrame().width(),
        settings.getDetectorSize(), settings.getAspectRatioF());

    Canvas canvas = surfaceHolder.lockCanvas();
    canvas.drawColor(0, PorterDuff.Mode.CLEAR);

    drawScanArea(canvas);

    surfaceHolder.unlockCanvasAndPost(canvas);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    // intentionally empty
  }

  private void drawScanArea(Canvas canvas) {
    if (settings.isDrawFocusLine()) {
      drawFocusLine(canvas, settings.getFocusLineColor(),
          settings.getFocusLineThickness());
    }

    if (settings.isDrawFocusRect()) {
      drawScanAreaOutline(canvas, settings.getFocusRectColor(),
          settings.getFocusRectBorderThickness(),
          settings.getFocusRectBorderRadius());
    }

    if (settings.isDrawFocusBackground()) {
      drawFocusBackground(canvas, settings.getFocusBackgroundColor(),
          settings.getFocusRectBorderRadius());
    }
  }

  /**
   * Draws a rectangle outline around the scan area
   *
   * @param canvas    The canvas to draw on
   * @param color     String with the color in hexadecimal format
   * @param thickness thickness of the outline
   * @param radius    Corner radius
   */
  private void drawScanAreaOutline(Canvas canvas, String color, int thickness, int radius) {
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(Color.parseColor(color));
    paint.setStrokeWidth(thickness);

    canvas.drawRoundRect(scanArea, radius, radius, paint);
  }

  /**
   * Draws a line through the center of the scan are
   *
   * @param canvas    The canvas to draw on
   * @param color     String with the color in hexadecimal format
   * @param thickness thickness of the line
   */
  private void drawFocusLine(Canvas canvas, String color, int thickness) {
    Paint paint = new Paint();
    paint.setColor(Color.parseColor(color));
    paint.setStrokeWidth(thickness);

    canvas.drawLine(scanArea.left, scanArea.centerY(), scanArea.right, scanArea.centerY(), paint);
  }

  /**
   * Fills out everything but the scan area
   *
   * @param canvas The canvas to draw on
   * @param color  String with the color in hexadecimal format (can contain alpha-channel)
   * @param radius Corner radius
   */
  private void drawFocusBackground(Canvas canvas, String color, int radius) {
    Path path = new Path();
    path.addRoundRect(scanArea, radius,
        radius, Path.Direction.CCW);
    canvas.clipOutPath(path);
    canvas.drawColor(Color.parseColor(color));
  }
}
