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
    String base64Image = ImageUtils.imageToBase64(image, imageProxy.getImageInfo().getRotationDegrees());
    image.close();
    imageProxy.close();

    JSONObject result = doPOSTRequest(base64Image);

    Intent data = new Intent();
    data.putExtra("RESULT", result.toString());
    restDataListener.onRestData(data);
  }

  @Override
  public void onError(@NonNull @NotNull ImageCaptureException exception) {
    System.out.println("Image capture error");
    super.onError(exception);
  }

  private JSONObject doPOSTRequest(String base64Image) {
    JSONObject result = new JSONObject();
    try {
      HttpURLConnection httpURLConnection = (HttpURLConnection) httpRequest.getUrl().openConnection();

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
        result.put("status", httpURLConnection.getResponseCode());
      } else {
        result = inputStreamToJson(httpURLConnection.getErrorStream());
      }
      httpURLConnection.disconnect();
    } catch (IOException e) {
      Log.e("ImageAnalyzer - POST Request", e.getMessage());
      try {
        result.put("error", e);
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
    } catch (JSONException e) {
      try {
        result.put("error", "JSONException");
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
    }
    return result;
  }

  private JSONObject inputStreamToJson(InputStream inputStream) throws IOException, JSONException {
    InputStream in = new BufferedInputStream(inputStream);
    String responseBody = null;
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
