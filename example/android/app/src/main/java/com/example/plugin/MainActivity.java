package com.example.plugin;

import android.os.Bundle;
import com.biso.capacitor.plugins.rest.information.from.image.RestInformationPlugin;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    registerPlugin(RestInformationPlugin.class);
    super.onCreate(savedInstanceState);
  }
}
