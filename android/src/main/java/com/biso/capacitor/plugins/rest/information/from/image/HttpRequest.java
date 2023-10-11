package com.biso.capacitor.plugins.rest.information.from.image;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequest implements Parcelable {

  private static final String LOG_KEY = "HttpRequest";
  private final URL url;
  private final JSONObject headers;
  private final JSONObject body;
  private final String base64Key;
  private final String imageTypeKey;

  public HttpRequest(JSObject request) throws MalformedURLException {
    url = new URL(request.getString("url"));
    body = request.getJSObject("body");
    headers = request.getJSObject("headers");
    base64Key = request.getString("base64Key", "fileBase64");
    imageTypeKey = request.getString("imageTypeKey", "imageType");
  }

  public URL getUrl() {
    return url;
  }

  public JSONObject getHeaders() {
    return headers;
  }

  public JSONObject getBody() {
    return body;
  }

  public String getBase64Key() {
    return base64Key;
  }

  public String getImageTypeKey() {
    return imageTypeKey;
  }

  @Override
  public int describeContents() {
    return hashCode();
  }

  @Override
  public int hashCode() {
    int result = 1;
    int prime = 13;
    result += prime * url.toString().hashCode();
    result += prime * headers.hashCode();
    result += prime * body.hashCode();
    result += prime * base64Key.hashCode();
    result += prime * imageTypeKey.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HttpRequest that = (HttpRequest) o;
    return that.getUrl().toString().equals(this.getUrl().toString())
        && that.getBody().equals(this.getBody())
        && that.getHeaders().equals(this.getHeaders())
        && that.getBase64Key().equals(this.getBase64Key())
        && that.getImageTypeKey().equals(this.getImageTypeKey());
  }

  @Override
  @NonNull
  public String toString() {
    return "(" + url.toString() + ", " + base64Key + ", " + imageTypeKey + ", " + headers.toString()
        + ", " + body.toString() + ")";
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeString(headers.toString());
    dest.writeString(url.toString());
    dest.writeString(body.toString());
    dest.writeString(base64Key);
    dest.writeString(imageTypeKey);
  }

  protected HttpRequest(Parcel in) {
    try {
      headers = new JSONObject(in.readString());
      url = new URL(in.readString());
      body = new JSONObject(in.readString());
      base64Key = in.readString();
      imageTypeKey = in.readString();
    } catch (MalformedURLException | JSONException e) {
      Log.e(LOG_KEY, "The impossible happened, a valid httpRequest object wasn't valid anymore after parceling");
      throw new IllegalArgumentException("Unable to un-parcel");
    }
  }

  public static final Creator<HttpRequest> CREATOR = new Creator<>() {
    @Override
    public HttpRequest createFromParcel(Parcel source) {
      return new HttpRequest(source);
    }

    @Override
    public HttpRequest[] newArray(int size) {
      return new HttpRequest[size];
    }
  };
}
