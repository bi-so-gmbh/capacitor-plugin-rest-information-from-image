package com.biso.capacitor.plugins.rest.information.from.image;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture.OnImageCapturedCallback;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageCaptureListener extends OnImageCapturedCallback {

  private static final String LOG_KEY = "ImageCaptureListener";
  public static final String STATUS = "status";
  public static final String RESULT = "result";
  public static final String ERROR = "error";
  private final RestDataListener restDataListener;
  private final HttpRequest httpRequest;

  public ImageCaptureListener(HttpRequest httpRequest, RestDataListener restDataListener) {
    super();
    this.httpRequest = httpRequest;
    this.restDataListener = restDataListener;
  }

  @Override
  public void onCaptureSuccess(@NotNull ImageProxy imageProxy) {
    @SuppressLint("UnsafeOptInUsageError")
    Image image = imageProxy.getImage();
    if (image == null) {
      imageProxy.close();
      return;
    }
    String base64Image = ImageUtils.imageToBase64(image,
        imageProxy.getImageInfo().getRotationDegrees());
    image.close();
    imageProxy.close();

    Intent data = new Intent();
    try {
      JSONObject result = doPOSTRequest(base64Image);
      data.putExtra(RESULT, result.toString());
    } catch (JSONException e) {
      // Shouldn't happen, exception is thrown on duplicate keys. Any important
      // JSONExceptions are caught, only those for error handling can bubble up.
    }

    restDataListener.onRestData(data);
  }

  @Override
  public void onError(@NonNull @NotNull ImageCaptureException exception) {
    Log.e(LOG_KEY, "ImageCaptureException: " + exception.getMessage());
    JSONObject error = new JSONObject();
    try {
      error.put(ERROR, exception.getMessage());
      Intent data = new Intent();
      data.putExtra(RESULT, error.toString());
      restDataListener.onRestData(data);
    } catch (JSONException e) {
      // Shouldn't happen, exception is thrown on duplicate keys and this is an empty object...
    }
    super.onError(exception);
  }

  private JSONObject doPOSTRequest(String base64Image) throws JSONException {
    JSONObject result = new JSONObject();
    try {
      HttpURLConnection httpURLConnection = (HttpURLConnection) httpRequest.getUrl()
          .openConnection();

      httpURLConnection.setRequestMethod("POST");
      httpURLConnection.setDoOutput(true);
      httpURLConnection.setDoInput(true);
      for (Iterator<String> it = httpRequest.getHeaders().keys(); it.hasNext(); ) {
        String key = it.next();
        httpURLConnection.setRequestProperty(key, httpRequest.getHeaders().getString(key));
      }
      JSONObject body = httpRequest.getBody();
      body.put(httpRequest.getBase64Key(), base64Image);
      body.put(httpRequest.getImageTypeKey(), "jpeg");

      OutputStream os = httpURLConnection.getOutputStream();
      os.write(httpRequest.getBody().toString().getBytes(StandardCharsets.UTF_8));
      os.close();

      // read the response
      if (httpURLConnection.getResponseCode() == 200) {
        result = inputStreamToJson(httpURLConnection.getInputStream());
        result.put(STATUS, httpURLConnection.getResponseCode());
      } else {
        result = inputStreamToJson(httpURLConnection.getErrorStream());
      }
      httpURLConnection.disconnect();
    } catch (IOException e) {
      Log.e(LOG_KEY, e.getMessage());
      result.put(ERROR, e.getMessage());
    } catch (JSONException e) {
      result.put(ERROR, "JSONException");
    }
    return result;
  }

  private JSONObject inputStreamToJson(InputStream inputStream) throws IOException, JSONException {
    InputStream in = new BufferedInputStream(inputStream);
    String responseBody;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      responseBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } else {
      responseBody = new BufferedReader(
          new InputStreamReader(in, StandardCharsets.UTF_8))
          .lines()
          .collect(Collectors.joining("\n"));
    }
    JSONObject result = new JSONObject(responseBody);
    in.close();
    return result;
  }
}
