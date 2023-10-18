package com.biso.capacitor.plugins.rest.information.from.image;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.MediaPlayer;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.common.api.CommonStatusCodes;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import org.json.JSONException;

@CapacitorPlugin(name = "RestInformation")
public class RestInformationPlugin extends Plugin {
  public static final String LOG_TAG = "RestInformationPlugin";
  private ScannerSettings scannerSettings;
  private HttpRequest httpRequest;
  private MediaPlayer mediaPlayer;
  private Vibrator vibrator;
  Context context;

  @Override
  public void load() {
    super.load();
    context = this.getActivity().getApplicationContext();
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      VibratorManager vibratorManager = (VibratorManager) context.getSystemService(
          Context.VIBRATOR_MANAGER_SERVICE);
      vibrator = vibratorManager.getDefaultVibrator();
    } else {
      // noinspection deprecation
      vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE); // NOSONAR
    }
    mediaPlayer = new MediaPlayer();

    try (AssetFileDescriptor descriptor = context.getAssets().openFd("beep.mp3")) {
      mediaPlayer.setAudioAttributes(
          new Builder()
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .setUsage(AudioAttributes.USAGE_NOTIFICATION)
              .build()
      );
      mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(),
          descriptor.getLength());
      mediaPlayer.prepare();
    } catch (IOException e) {
      Log.e(LOG_TAG, "Something went wrong trying to play 'beep.mp3'");
    }
  }

  @PluginMethod
  public void scan(PluginCall call) {
    Intent intent = new Intent(context, CaptureActivity.class);

    JSObject scanCall = call.getData();
    if (Objects.isNull(scanCall) || !scanCall.has(Keys.REQUEST)) {
      call.reject(ErrorMessages.REQUIRED_DATA_MISSING);
    }

    JSObject scanSettings = scanCall.getJSObject(Keys.SETTINGS);
    if (scanSettings == null) {
      scanSettings = new JSObject();
    }
    scannerSettings = new ScannerSettings(scanSettings);

    try {
      JSObject scanRequest = scanCall.getJSObject(Keys.REQUEST);
      if (scanRequest != null && scanRequest.length() > 0) {
        httpRequest = new HttpRequest(scanRequest);
      } else {
        call.reject(ErrorMessages.REQUEST_INVALID);
      }
    } catch (MalformedURLException e) {
      Log.e(LOG_TAG, e.getMessage());
      call.reject(ErrorMessages.INVALID_URL);
    }
    intent.putExtra(Keys.SETTINGS, scannerSettings);
    intent.putExtra(Keys.REQUEST, httpRequest);
    startActivityForResult(call, intent, "onScanResult");
  }

  @ActivityCallback
  private void onScanResult(PluginCall call, ActivityResult result) throws JSONException {
    if (call == null) {
      return;
    }
    Intent data = result.getData();
    // finishWithSuccess used (ImageCaptureListener via RestDataListener)
    if (result.getResultCode() == CommonStatusCodes.SUCCESS) {
      if (data == null) {
        call.reject(ErrorMessages.CANCELLED);
        return;
      }
      JSObject scanResult = new JSObject(data.getStringExtra(Keys.RESULT));

      if (scanResult.has(Keys.STATUS) && scanResult.getInt(Keys.STATUS) == 200) {
        call.resolve(scanResult);
      } else {
        if (scanResult.has(Keys.ERROR)) {
          if (scanResult.has(Keys.STATUS)) {
            call.reject(scanResult.getString(Keys.ERROR), String.valueOf(scanResult.getInt(Keys.STATUS)));
          } else {
            call.reject(scanResult.getString(Keys.ERROR));
          }
        }
      }

      if (scannerSettings.isBeepOnSuccess()) {
        mediaPlayer.start();
      }

      if (scannerSettings.isVibrateOnSuccess()) {
        int duration = 200;
        vibrator.vibrate(
            VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
      }
      call.resolve(scanResult);
    }
    // finishWithError used
    else {
      String err = ErrorMessages.UNKNOWN_ERROR;
      if (data != null && (data.hasExtra(Keys.ERROR))) {
          err = data.getStringExtra(Keys.ERROR);
      }
      call.reject(err);
    }
  }
}
