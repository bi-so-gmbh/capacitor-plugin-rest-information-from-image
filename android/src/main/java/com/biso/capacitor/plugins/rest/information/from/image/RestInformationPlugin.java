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

  public static final String SETTINGS = "settings";
  public static final String REQUEST = "request";
  private ScannerSettings scannerSettings;
  private Request request;
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
      Log.e("MLKitBarcodeScanner", "Something went wrong trying to play 'beep.mp3'");
    }
  }

  @PluginMethod
  public void scan(PluginCall call) {
    Intent intent = new Intent(context, CaptureActivity.class);

    JSObject scanCall = call.getData();
    if (Objects.isNull(scanCall)) {
      scanCall = new JSObject();
    }
    JSObject scanSettings = new JSObject();
    JSObject scanRequest = new JSObject();
    try {
      scanSettings = scanCall.getJSObject(SETTINGS, new JSObject());
      scanRequest = scanCall.getJSObject(REQUEST, new JSObject());
    } catch (JSONException e) {
      Log.e("RestInformationPlugin", "something is wrong with the call, check the parameters");
    }
    scannerSettings = new ScannerSettings(scanSettings);
    try {
      request = new Request(scanRequest);
    } catch (MalformedURLException e) {
      call.reject("INVALID_URL");
    }
    intent.putExtra(SETTINGS, scannerSettings);
    intent.putExtra(REQUEST, request);
    startActivityForResult(call, intent, "onScanResult");
  }

  @ActivityCallback
  private void onScanResult(PluginCall call, ActivityResult result) throws JSONException {
    if (call == null) {
      return;
    }
    Intent data = result.getData();
    if (result.getResultCode() == CommonStatusCodes.SUCCESS) {
      if (data == null) {
        call.reject("CANCELED");
        return;
      }
      JSObject scanResult = new JSObject(data.getStringExtra("RESULT"));
      System.out.println(scanResult);
      if (scanResult.has("status") && scanResult.getInt("status") == 200) {
        System.out.println("done, back to js");
        call.resolve(scanResult);
      } else {
        if (scanResult.has("error")) {
          if (scanResult.has("status")) {
            call.reject(scanResult.getString("error"), String.valueOf(scanResult.getInt("status")));
          } else {
            call.reject(scanResult.getString("error"));
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
    } else {
      String err = "UNKNOWN_ERROR";
      if (data != null) {
        err = result.getData().getStringExtra("error");
      }
      call.reject(err);
    }
  }
}
