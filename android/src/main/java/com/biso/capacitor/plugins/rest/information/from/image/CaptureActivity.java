package com.biso.capacitor.plugins.rest.information.from.image;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.Preview.SurfaceProvider;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import com.biso.capacitor.plugins.rest.information.from.image.databinding.CaptureActivityBinding;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity {

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private Camera camera;

  private ImageCapture imageCapture;
  private ProcessCameraProvider cameraProvider;
  private Preview preview;
  private boolean readyToTakePicture = true;
  private ScannerSettings scannerSettings;

  private final ActivityResultLauncher<String> requestPermissionLauncher =
    registerForActivityResult(new RequestPermission(), isGranted -> {
      if (Boolean.TRUE.equals(isGranted)) {
        startCamera();
      } else {
        finishWithError(ErrorMessages.NO_CAMERA_PERMISSION);
      }
    });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CaptureActivityBinding binding = CaptureActivityBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
          || cameraManager.getCameraIdList().length == 0) {
        finishWithError(ErrorMessages.NO_CAMERA);
      }
    } catch (CameraAccessException e) {
      finishWithError(ErrorMessages.NO_CAMERA);
    }

    HttpRequest httpRequest;
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      scannerSettings = getIntent().getParcelableExtra(Keys.SETTINGS, ScannerSettings.class);
      httpRequest = getIntent().getParcelableExtra(Keys.REQUEST, HttpRequest.class);
    } else {
      // noinspection deprecation
      scannerSettings = getIntent().getParcelableExtra(Keys.SETTINGS); // NOSONAR
      // noinspection deprecation
      httpRequest = getIntent().getParcelableExtra(Keys.REQUEST); // NOSONAR
    }

    CameraOverlay cameraOverlay = new CameraOverlay(this, scannerSettings);

    binding.topLayout.addView(cameraOverlay);
    if (checkSelfPermission(permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
      startCamera();
    } else {
      requestPermissionLauncher.launch(permission.CAMERA);
    }

    ProgressBar progressBar = new ProgressBar(CaptureActivity.this);
    progressBar.setIndeterminate(true);
    progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); // NOSONAR
    progressBar.setVisibility(View.GONE);

    binding.getRoot().addView(progressBar);

    binding.topLayout.setOnClickListener(v -> {
      if (readyToTakePicture) {
        v.performHapticFeedback(MotionEvent.AXIS_TOUCH_MINOR);
        readyToTakePicture = false;
        progressBar.setIndeterminateTintList(
            ColorStateList.valueOf(Color.parseColor(scannerSettings.getLoadingCircleColor())));
        // original size is 100dp so lets scale it to match the setting
        float scale = scannerSettings.getLoadingCircleSize() / 100f;
        progressBar.setScaleX(scale);
        progressBar.setScaleY(scale);
        progressBar.setVisibility(View.VISIBLE);
        imageCapture.takePicture(executor,
            new ImageCaptureListener(httpRequest, this::finishWithSuccess, scannerSettings));
        cameraProvider.unbind(preview);
      }
    });

    binding.torchButton.setOnClickListener(v -> {
      LiveData<Integer> flashState = camera.getCameraInfo().getTorchState();
      if (flashState.getValue() != null) {
        v.performHapticFeedback(MotionEvent.AXIS_TOUCH_MINOR);
        boolean state = flashState.getValue() == 1;
        binding.torchButton.setBackgroundResource(
            !state ? R.drawable.torch_active : R.drawable.torch_inactive);
        camera.getCameraControl().enableTorch(!state);
      }
    });
  }

  private void finishWithError(String errorMessage) {
    Intent result = new Intent();
    result.putExtra(Keys.ERROR, errorMessage);
    setResult(CommonStatusCodes.ERROR, result);
    finish();
  }

  private void finishWithSuccess(Intent data) {
    setResult(CommonStatusCodes.SUCCESS, data);
    finish();
  }

  void startCamera() {
    PreviewView previewView = findViewById(R.id.previewView);
    previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

    previewView.setScaleX(1F);
    previewView.setScaleY(1F);

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(
        this);
    cameraProviderFuture.addListener(() -> {
      try {
        cameraProvider = cameraProviderFuture.get();
        CaptureActivity.this.bindPreview(cameraProvider, previewView.getSurfaceProvider());
      } catch (ExecutionException | InterruptedException e) { // NOSONAR
        // No errors need to be handled for this Future.
        // This should never be reached.
        Log.e("RestInformationPlugin - Capture Activity", "Couldn't start camera");
      }
    }, ContextCompat.getMainExecutor(this));
  }

  /**
   * Binding to camera
   */
  private void bindPreview(ProcessCameraProvider cameraProvider, SurfaceProvider surfaceProvider) {
    preview = new Preview.Builder().build();

    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(
      CameraSelector.LENS_FACING_BACK).build();

    preview.setSurfaceProvider(surfaceProvider);

    ResolutionSelector resolutionSelector = new ResolutionSelector.Builder().setAspectRatioStrategy(
        new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
      .setResolutionStrategy(new ResolutionStrategy(
        new Size(scannerSettings.getImageWidth(), scannerSettings.getImageHeight()),
        ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)).build();

    imageCapture = new ImageCapture.Builder()
      .setResolutionSelector(resolutionSelector).build();
    camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
  }
}
