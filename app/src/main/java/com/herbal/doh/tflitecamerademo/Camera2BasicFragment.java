/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.herbal.doh.tflitecamerademo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.herbal.doh.tflitecamerademo.ImageClassifier;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Basic fragments for the Camera. */
public class Camera2BasicFragment extends Fragment
        implements FragmentCompat.OnRequestPermissionsResultCallback {




  Button bawang,akapulko,ampalaya,bayabas,lagundi,niyog_niyogan,sambong,tsaang_gubat,ulasimang_bato,yerba_buena;

  /** Tag for the {@link Log}. */
  private static final String TAG = "TfLiteCameraDemo";

  private static final String FRAGMENT_DIALOG = "dialog";

  private static final String HANDLE_THREAD_NAME = "CameraBackground";

  private static final int PERMISSIONS_REQUEST_CODE = 1;

  private final Object lock = new Object();
  private boolean runClassifier = false;
  private boolean checkedPermissions = false;
  private TextView textView;
  private com.herbal.doh.tflitecamerademo.ImageClassifier classifier;

  /** Max preview width that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_WIDTH = 1920;

  /** Max preview height that is guaranteed by Camera2 API */
  private static final int MAX_PREVIEW_HEIGHT = 1080;

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
   * TextureView}.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener =
          new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
              openCamera(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
              configureTransform(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
              return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }
          };

  /** ID of the current {@link CameraDevice}. */
  private String cameraId;

  /** An {@link AutoFitTextureView} for camera preview. */
  private AutoFitTextureView textureView;

  /** A {@link CameraCaptureSession } for camera preview. */
  private CameraCaptureSession captureSession;

  /** A reference to the opened {@link CameraDevice}. */
  private CameraDevice cameraDevice;

  /** The {@link android.util.Size} of camera preview. */
  private Size previewSize;

  /** {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state. */
  private final CameraDevice.StateCallback stateCallback =
          new CameraDevice.StateCallback() {

            @Override
            public void onOpened(@NonNull CameraDevice currentCameraDevice) {
              // This method is called when the camera is opened.  We start camera preview here.
              cameraOpenCloseLock.release();
              cameraDevice = currentCameraDevice;
              createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
              cameraOpenCloseLock.release();
              currentCameraDevice.close();
              cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
              cameraOpenCloseLock.release();
              currentCameraDevice.close();
              cameraDevice = null;
              Activity activity = getActivity();
              if (null != activity) {
                activity.finish();
              }
            }
          };

  /** An additional thread for running tasks that shouldn't block the UI. */
  private HandlerThread backgroundThread;

  /** A {@link Handler} for running tasks in the background. */
  private Handler backgroundHandler;

  /** An {@link ImageReader} that handles image capture. */
  private ImageReader imageReader;

  /** {@link CaptureRequest.Builder} for the camera preview */
  private CaptureRequest.Builder previewRequestBuilder;

  /** {@link CaptureRequest} generated by {@link #previewRequestBuilder} */
  private CaptureRequest previewRequest;

  /** A {@link Semaphore} to prevent the app from exiting before closing the camera. */
  private Semaphore cameraOpenCloseLock = new Semaphore(1);

  /** A {@link CameraCaptureSession.CaptureCallback} that handles events related to capture. */
  private CameraCaptureSession.CaptureCallback captureCallback =
          new CameraCaptureSession.CaptureCallback() {

            @Override
            public void onCaptureProgressed(
                    @NonNull CameraCaptureSession session,
                    @NonNull CaptureRequest request,
                    @NonNull CaptureResult partialResult) {
            }

            @Override
            public void onCaptureCompleted(
                    @NonNull CameraCaptureSession session,
                    @NonNull CaptureRequest request,
                    @NonNull TotalCaptureResult result) {
            }
          };

  /**
   * Shows a {@link Toast} on the UI thread for the classification results.
   *
   * @param text The message to show
   */
  private void showToast(final String text) {
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(
              new Runnable() {
                @Override
                public void run() {


                  if (text.equals("bawang95")){
                    textView.setText("Herbal Name: Bawang (Allium sativum)\nConfidence Outcome: 95%\n\nBasic Info.:\nThis herbal plant is popularly known as `Garlic`, it helps reduce cholesterol in the blood and hence, helps control blood pressure. ");
                    bawang.setVisibility(View.VISIBLE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);


                  }else if(text.equals("bawang90")){
                    textView.setText("Herbal Name: Bawang (Allium sativum)\nConfidence Outcome: 90%\n\nBasic Info.:\nThis herbal plant is popularly known as `Garlic`, it helps reduce cholesterol in the blood and hence, helps control blood pressure. ");
                    bawang.setVisibility(View.VISIBLE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("bawang85")){
                    textView.setText("Herbal Name: Bawang (Allium sativum)\nConfidence Outcome: 85%\n\nBasic Info.:\nThis herbal plant is popularly known as `Garlic`, it helps reduce cholesterol in the blood and hence, helps control blood pressure. ");
                    bawang.setVisibility(View.VISIBLE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("bawang80")){
                    textView.setText("Herbal Name: Bawang (Allium sativum)\nConfidence Outcome: 80%\n\nBasic Info.:\nThis herbal plant is popularly known as `Garlic`, it helps reduce cholesterol in the blood and hence, helps control blood pressure. ");
                    bawang.setVisibility(View.VISIBLE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("bawang75")){
                    textView.setText("Herbal Name: Bawang (Allium sativum)\nConfidence Outcome: 75%\n\nBasic Info.:\nThis herbal plant is popularly known as `Garlic`, it helps reduce cholesterol in the blood and hence, helps control blood pressure. ");
                    bawang.setVisibility(View.VISIBLE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("bawang70")){
                    textView.setText("Herbal Name: Bawang (Allium sativum)\nConfidence Outcome: 70%\n\nBasic Info.:\nThis herbal plant is popularly known as `Garlic`, it helps reduce cholesterol in the blood and hence, helps control blood pressure. ");
                    bawang.setVisibility(View.VISIBLE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if(text.equals("akapulko95")){
                    textView.setText("Herbal Name: Akapulko (Cassia alata)\nConfidence Outcome: 95%\n\nBasic Info.:\nThis herbal plant is popularly known as `Bayabas-bayabasan` in tagalog and `Ringworm bush` in English, this herbal medicine is used to treat ringworms and skin fungal infections. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.VISIBLE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("akapulko90")){
                    textView.setText("Herbal Name: Akapulko (Cassia alata)\nConfidence Outcome: 90%\n\nBasic Info.:\nThis herbal plant is popularly known as `Bayabas-bayabasan` in tagalog and `Ringworm bush` in English, this herbal medicine is used to treat ringworms and skin fungal infections. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.VISIBLE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("akapulko85")){
                    textView.setText("Herbal Name: Akapulko (Cassia alata)\nConfidence Outcome: 85%\n\nBasic Info.:\nThis herbal plant is popularly known as `Bayabas-bayabasan` in tagalog and `Ringworm bush` in English, this herbal medicine is used to treat ringworms and skin fungal infections. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.VISIBLE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("akapulko80")){
                    textView.setText("Herbal Name: Akapulko (Cassia alata)\nConfidence Outcome: 80%\n\nBasic Info.:\nThis herbal plant is popularly known as `Bayabas-bayabasan` in tagalog and `Ringworm bush` in English, this herbal medicine is used to treat ringworms and skin fungal infections. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.VISIBLE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("akapulko75")){
                    textView.setText("Herbal Name: Akapulko (Cassia alata)\nConfidence Outcome: 75%\n\nBasic Info.:\nThis herbal plant is popularly known as `Bayabas-bayabasan` in tagalog and `Ringworm bush` in English, this herbal medicine is used to treat ringworms and skin fungal infections. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.VISIBLE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if(text.equals("akapulko70")){
                    textView.setText("Herbal Name: Akapulko (Cassia alata)\nConfidence Outcome: 70%\n\nBasic Info.:\nThis herbal plant is popularly known as `Bayabas-bayabasan` in tagalog and `Ringworm bush` in English, this herbal medicine is used to treat ringworms and skin fungal infections. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.VISIBLE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("ampalaya95")){
                    textView.setText("Herbal Name: Ampalaya (Momordica charantia)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant is popularly known as “bitter gourd” or “bitter melon” in English, it is used as a treatment for diabetes (diabetes mellitus), for the non-insulin dependent patients. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.VISIBLE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ampalaya90")){
                    textView.setText("Herbal Name: Ampalaya (Momordica charantia)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant is popularly known as “bitter gourd” or “bitter melon” in English, it is used as a treatment for diabetes (diabetes mellitus), for the non-insulin dependent patients. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.VISIBLE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ampalaya85")){
                    textView.setText("Herbal Name: Ampalaya (Momordica charantia)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant is popularly known as “bitter gourd” or “bitter melon” in English, it is used as a treatment for diabetes (diabetes mellitus), for the non-insulin dependent patients. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.VISIBLE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ampalaya80")){
                    textView.setText("Herbal Name: Ampalaya (Momordica charantia)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant is popularly known as “bitter gourd” or “bitter melon” in English, it is used as a treatment for diabetes (diabetes mellitus), for the non-insulin dependent patients. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.VISIBLE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ampalaya75")){
                    textView.setText("Herbal Name: Ampalaya (Momordica charantia)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant is popularly known as “bitter gourd” or “bitter melon” in English, it is used as a treatment for diabetes (diabetes mellitus), for the non-insulin dependent patients. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.VISIBLE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ampalaya70")){
                    textView.setText("Herbal Name: Ampalaya (Momordica charantia)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant is popularly known as “bitter gourd” or “bitter melon” in English, it is used as a treatment of diabetes (diabetes mellitus), for the non-insulin dependent patients. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.VISIBLE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("bayabas95")){
                    textView.setText("Herbal Name: Bayabas (Psidium guajava)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant is popularly known as “Guava” in English, it is used primarily as an antiseptic  to disinfect wounds, Also, it can be used as a mouth wash to treat tooth decay and gum infection. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.VISIBLE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("bayabas90")){
                    textView.setText("Herbal Name: Bayabas (Psidium guajava)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant is popularly known as “Guava” in English, it is used primarily as an antiseptic  to disinfect wounds, Also, it can be used as a mouth wash to treat tooth decay and gum infection. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.VISIBLE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("bayabas85")){
                    textView.setText("Herbal Name: Bayabas (Psidium guajava)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant is popularly known as “Guava” in English, it is used primarily as an antiseptic  to disinfect wounds, Also, it can be used as a mouth wash to treat tooth decay and gum infection. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.VISIBLE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("bayabas80")){
                    textView.setText("Herbal Name: Bayabas (Psidium guajava)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant is popularly known as “Guava” in English, it is used primarily as an antiseptic  to disinfect wounds, Also, it can be used as a mouth wash to treat tooth decay and gum infection. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.VISIBLE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("bayabas75")){
                    textView.setText("Herbal Name: Bayabas (Psidium guajava)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant is popularly known as “Guava” in English, it is used primarily as an antiseptic  to disinfect wounds, Also, it can be used as a mouth wash to treat tooth decay and gum infection. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.VISIBLE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("bayabas70")){
                    textView.setText("Herbal Name: Bayabas (Psidium guajava)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant is popularly known as “Guava” in English, it is used primarily as an antiseptic  to disinfect wounds, Also, it can be used as a mouth wash to treat tooth decay and gum infection. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.VISIBLE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("lagundi95")){
                    textView.setText("Herbal Name: Lagundi (Vitex negundo)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant is popularly known as “5-leaved chaste tree” in English, The main uses of this herbal plant is for treating coughs and asthma. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.VISIBLE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  } else if (text.equals("lagundi90")){
                    textView.setText("Herbal Name: Lagundi (Vitex negundo)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant is popularly known as “5-leaved chaste tree” in English, The main uses of this herbal plant is for treating coughs and asthma. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.VISIBLE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  } else if (text.equals("lagundi85")){
                    textView.setText("Herbal Name: Lagundi (Vitex negundo)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant is popularly known as “5-leaved chaste tree” in English, The main uses of this herbal plant is for treating coughs and asthma. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.VISIBLE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  } else if (text.equals("lagundi80")){
                    textView.setText("Herbal Name: Lagundi (Vitex negundo)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant is popularly known as “5-leaved chaste tree” in English, The main uses of this herbal plant is for treating coughs and asthma. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.VISIBLE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  } else if (text.equals("lagundi75")){
                    textView.setText("Herbal Name: Lagundi (Vitex negundo)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant is popularly known as “5-leaved chaste tree” in English, The main uses of this herbal plant is for treating coughs and asthma. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.VISIBLE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  } else if (text.equals("lagundi70")){
                    textView.setText("Herbal Name: Lagundi (Vitex negundo)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant is popularly known as “5-leaved chaste tree” in English, The main uses of this herbal plant is for treating coughs and asthma. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.VISIBLE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("niyog_niyogan95")){
                    textView.setText("Herbal Name: Niyog - Niyogan (Quisqualis indica L.)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant has a characteristics of a vine and popularly known as “Chinese honey suckle” in English, this herbal plant is effective in eliminating intestinal worms, particularly the Ascaris (large roundworm) and Trichina (pork worm). Only the dried matured seeds are medicinal, crack and ingest the dried seeds two hours after eating (5 to 7 seeds for children & 8 to 10 seeds for adults). If one dose does not eliminate the intestinal worms, wait for a week before repeating the dose. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.VISIBLE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);


                  }else if (text.equals("niyog_niyogan90")){
                    textView.setText("Herbal Name: Niyog - Niyogan (Quisqualis indica L.)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant has a characteristics of a vine and popularly known as “Chinese honey suckle” in English, this herbal plant is effective in eliminating intestinal worms, particularly the Ascaris (large roundworm) and Trichina (pork worm). Only the dried matured seeds are medicinal, crack and ingest the dried seeds two hours after eating (5 to 7 seeds for children & 8 to 10 seeds for adults). If one dose does not eliminate the intestinal worms, wait for a week before repeating the dose. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.VISIBLE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);


                  }else if (text.equals("niyog_niyogan85")){
                    textView.setText("Herbal Name: Niyog - Niyogan (Quisqualis indica L.)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant has a characteristics of a vine and popularly known as “Chinese honey suckle” in English, this herbal plant is effective in eliminating intestinal worms, particularly the Ascaris (large roundworm) and Trichina (pork worm). Only the dried matured seeds are medicinal, crack and ingest the dried seeds two hours after eating (5 to 7 seeds for children & 8 to 10 seeds for adults). If one dose does not eliminate the intestinal worms, wait for a week before repeating the dose. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.VISIBLE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("niyog_niyogan80")){
                    textView.setText("Herbal Name: Niyog - Niyogan (Quisqualis indica L.)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant has a characteristics of a vine and popularly known as “Chinese honey suckle” in English, this herbal plant is effective in eliminating intestinal worms, particularly the Ascaris (large roundworm) and Trichina (pork worm). Only the dried matured seeds are medicinal, crack and ingest the dried seeds two hours after eating (5 to 7 seeds for children & 8 to 10 seeds for adults). If one dose does not eliminate the intestinal worms, wait for a week before repeating the dose. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.VISIBLE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("niyog_niyogan75")){
                    textView.setText("Herbal Name: Niyog - Niyogan (Quisqualis indica L.)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant has a characteristics of a vine and popularly known as “Chinese honey suckle” in English, this herbal plant is effective in eliminating intestinal worms, particularly the Ascaris (large roundworm) and Trichina (pork worm). Only the dried matured seeds are medicinal, crack and ingest the dried seeds two hours after eating (5 to 7 seeds for children & 8 to 10 seeds for adults). If one dose does not eliminate the intestinal worms, wait for a week before repeating the dose. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.VISIBLE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("niyog_niyogan70")){
                    textView.setText("Herbal Name: Niyog - Niyogan (Quisqualis indica L.)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant has a characteristics of a vine and popularly known as “Chinese honey suckle” in English, this herbal plant is effective in eliminating intestinal worms, particularly the Ascaris (large roundworm) and Trichina (pork worm). Only the dried matured seeds are medicinal, crack and ingest the dried seeds two hours after eating (5 to 7 seeds for children & 8 to 10 seeds for adults). If one dose does not eliminate the intestinal worms, wait for a week before repeating the dose. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.VISIBLE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("sambong95")){
                    textView.setText("Herbal Name: Sambong (Blumea balsamifera)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant is popularly known as `Blumea camphora` in English. It is used as a diuretic  (increased production of urine) that helps in the excretion (eliminating) of urinary stones. It can also be used as an edema (medical term for swelling). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.VISIBLE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("sambong90")){
                    textView.setText("Herbal Name: Sambong (Blumea balsamifera)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant is popularly known as `Blumea camphora` in English. It is used as a diuretic  (increased production of urine) that helps in the excretion (eliminating) of urinary stones. It can also be used as an edema (medical term for swelling). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.VISIBLE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("sambong85")){
                    textView.setText("Herbal Name: Sambong (Blumea balsamifera)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant is popularly known as `Blumea camphora` in English. It is used as a diuretic  (increased production of urine) that helps in the excretion (eliminating) of urinary stones. It can also be used as an edema (medical term for swelling). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.VISIBLE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("sambong80")){
                    textView.setText("Herbal Name: Sambong (Blumea balsamifera)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant is popularly known as `Blumea camphora` in English. It is used as a diuretic  (increased production of urine) that helps in the excretion (eliminating) of urinary stones. It can also be used as an edema (medical term for swelling). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.VISIBLE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("sambong75")){
                    textView.setText("Herbal Name: Sambong (Blumea balsamifera)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant is popularly known as `Blumea camphora` in English. It is used as a diuretic  (increased production of urine) that helps in the excretion (eliminating) of urinary stones. It can also be used as an edema (medical term for swelling). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.VISIBLE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("sambong70")){
                    textView.setText("Herbal Name: Sambong (Blumea balsamifera)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant is popularly known as `Blumea camphora` in English. It is used as a diuretic  (increased production of urine) that helps in the excretion (eliminating) of urinary stones. It can also be used as an edema (medical term for swelling). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.VISIBLE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("tsaang_gubat95")){
                    textView.setText("Herbal Name: Tsaang Gubat (Ehretia microphylla Lam.)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant is used like a tea, and this is effective in treating intestinal motility (abnormal intestinal contractions), and this is also used as a mouth wash since the leaves of this herbal plant has high fluoride content. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.VISIBLE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("tsaang_gubat90")){
                    textView.setText("Herbal Name: Tsaang Gubat (Ehretia microphylla Lam.)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant is used like a tea, and this is effective in treating intestinal motility (abnormal intestinal contractions), and this is also used as a mouth wash since the leaves of this herbal plant has high fluoride content. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.VISIBLE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("tsaang_gubat85")){
                    textView.setText("Herbal Name: Tsaang Gubat (Ehretia microphylla Lam.)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant is used like a tea, and this is effective in treating intestinal motility (abnormal intestinal contractions), and this is also used as a mouth wash since the leaves of this herbal plant has high fluoride content. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.VISIBLE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("tsaang_gubat80")){
                    textView.setText("Herbal Name: Tsaang Gubat (Ehretia microphylla Lam.)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant is used like a tea, and this is effective in treating intestinal motility (abnormal intestinal contractions), and this is also used as a mouth wash since the leaves of this herbal plant has high fluoride content. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.VISIBLE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("tsaang_gubat75")){
                    textView.setText("Herbal Name: Tsaang Gubat (Ehretia microphylla Lam.)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant is used like a tea, and this is effective in treating intestinal motility (abnormal intestinal contractions), and this is also used as a mouth wash since the leaves of this herbal plant has high fluoride content. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.VISIBLE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("tsaang_gubat70")){
                    textView.setText("Herbal Name: Tsaang Gubat (Ehretia microphylla Lam.)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant is used like a tea, and this is effective in treating intestinal motility (abnormal intestinal contractions), and this is also used as a mouth wash since the leaves of this herbal plant has high fluoride content. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.VISIBLE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("ulasimang_bato95")){
                    textView.setText("Herbal Name: Ulasimang Bato|Pancit-pancitan (Peperomia pellucida)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant is effective in fighting arthritis and gout. The leaves can be eaten fresh (about a cupful) as salad or can be drink like a tea. For the decoction, boil a cup of clean chopped leaves in 2 cups of water. Boil for 15 to 20 minutes. Strain, let cool and drink a cup after meals (3 times day). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.VISIBLE);
                    yerba_buena.setVisibility(View.GONE);

                  }
                  else if (text.equals("ulasimang_bato90")){
                    textView.setText("Herbal Name: Ulasimang Bato|Pancit-pancitan (Peperomia pellucida)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant is effective in fighting arthritis and gout. The leaves can be eaten fresh (about a cupful) as salad or can be drink like a tea. For the decoction, boil a cup of clean chopped leaves in 2 cups of water. Boil for 15 to 20 minutes. Strain, let cool and drink a cup after meals (3 times day). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.VISIBLE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ulasimang_bato85")){
                    textView.setText("Herbal Name: Ulasimang Bato|Pancit-pancitan (Peperomia pellucida)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant is effective in fighting arthritis and gout. The leaves can be eaten fresh (about a cupful) as salad or can be drink like a tea. For the decoction, boil a cup of clean chopped leaves in 2 cups of water. Boil for 15 to 20 minutes. Strain, let cool and drink a cup after meals (3 times day). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.VISIBLE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ulasimang_bato80")){
                    textView.setText("Herbal Name: Ulasimang Bato|Pancit-pancitan (Peperomia pellucida)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant is effective in fighting arthritis and gout. The leaves can be eaten fresh (about a cupful) as salad or can be drink like a tea. For the decoction, boil a cup of clean chopped leaves in 2 cups of water. Boil for 15 to 20 minutes. Strain, let cool and drink a cup after meals (3 times day). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.VISIBLE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ulasimang_bato75")){
                    textView.setText("Herbal Name: Ulasimang Bato|Pancit-pancitan (Peperomia pellucida)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant is effective in fighting arthritis and gout. The leaves can be eaten fresh (about a cupful) as salad or can be drink like a tea. For the decoction, boil a cup of clean chopped leaves in 2 cups of water. Boil for 15 to 20 minutes. Strain, let cool and drink a cup after meals (3 times day). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.VISIBLE);
                    yerba_buena.setVisibility(View.GONE);

                  }else if (text.equals("ulasimang_bato70")){
                    textView.setText("Herbal Name: Ulasimang Bato|Pancit-pancitan (Peperomia pellucida)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant is effective in fighting arthritis and gout. The leaves can be eaten fresh (about a cupful) as salad or can be drink like a tea. For the decoction, boil a cup of clean chopped leaves in 2 cups of water. Boil for 15 to 20 minutes. Strain, let cool and drink a cup after meals (3 times day). ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.VISIBLE);
                    yerba_buena.setVisibility(View.GONE);

                  }

                  else if (text.equals("yerba_buena95")){
                    textView.setText("Herbal Name: Yerba Buena (Clinopodium douglasii)\nConfidence Outcome: 95%\n\nBasic Info. :\nThis herbal plant is commonly known as `Peppermint` in English, this plant has a characteristic of a vine and it is used as an analgesic (painkiller) to relive body aches and pain. Also, it can be taken internally as a decoction or externally by pounding the leaves and applying the extract directly on the affected area. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.VISIBLE);

                  }else if (text.equals("yerba_buena90")){
                    textView.setText("Herbal Name: Yerba Buena (Clinopodium douglasii)\nConfidence Outcome: 90%\n\nBasic Info. :\nThis herbal plant is commonly known as `Peppermint` in English, this plant has a characteristic of a vine and it is used as an analgesic (painkiller) to relive body aches and pain. Also, it can be taken internally as a decoction or externally by pounding the leaves and applying the extract directly on the affected area. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.VISIBLE);

                  }else if (text.equals("yerba_buena85")){
                    textView.setText("Herbal Name: Yerba Buena (Clinopodium douglasii)\nConfidence Outcome: 85%\n\nBasic Info. :\nThis herbal plant is commonly known as `Peppermint` in English, this plant has a characteristic of a vine and it is used as an analgesic (painkiller) to relive body aches and pain. Also, it can be taken internally as a decoction or externally by pounding the leaves and applying the extract directly on the affected area. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.VISIBLE);

                  }else if (text.equals("yerba_buena80")){
                    textView.setText("Herbal Name: Yerba Buena (Clinopodium douglasii)\nConfidence Outcome: 80%\n\nBasic Info. :\nThis herbal plant is commonly known as `Peppermint` in English, this plant has a characteristic of a vine and it is used as an analgesic (painkiller) to relive body aches and pain. Also, it can be taken internally as a decoction or externally by pounding the leaves and applying the extract directly on the affected area. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.VISIBLE);

                  }else if (text.equals("yerba_buena75")){
                    textView.setText("Herbal Name: Yerba Buena (Clinopodium douglasii)\nConfidence Outcome: 75%\n\nBasic Info. :\nThis herbal plant is commonly known as `Peppermint` in English, this plant has a characteristic of a vine and it is used as an analgesic (painkiller) to relive body aches and pain. Also, it can be taken internally as a decoction or externally by pounding the leaves and applying the extract directly on the affected area. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.VISIBLE);

                  }else if (text.equals("yerba_buena70")){
                    textView.setText("Herbal Name: Yerba Buena (Clinopodium douglasii)\nConfidence Outcome: 70%\n\nBasic Info. :\nThis herbal plant is commonly known as `Peppermint` in English, this plant has a characteristic of a vine and it is used as an analgesic (painkiller) to relive body aches and pain. Also, it can be taken internally as a decoction or externally by pounding the leaves and applying the extract directly on the affected area. ");
                    bawang.setVisibility(View.GONE);
                    akapulko.setVisibility(View.GONE);
                    ampalaya.setVisibility(View.GONE);
                    bayabas.setVisibility(View.GONE);
                    lagundi.setVisibility(View.GONE);
                    niyog_niyogan.setVisibility(View.GONE);
                    sambong.setVisibility(View.GONE);
                    tsaang_gubat.setVisibility(View.GONE);
                    ulasimang_bato.setVisibility(View.GONE);
                    yerba_buena.setVisibility(View.VISIBLE);

                  }else{
                        textView.setText(text);
                        bawang.setVisibility(View.GONE);
                        akapulko.setVisibility(View.GONE);
                        ampalaya.setVisibility(View.GONE);
                        bayabas.setVisibility(View.GONE);
                        lagundi.setVisibility(View.GONE);
                        niyog_niyogan.setVisibility(View.GONE);
                        sambong.setVisibility(View.GONE);
                        tsaang_gubat.setVisibility(View.GONE);
                        ulasimang_bato.setVisibility(View.GONE);
                        yerba_buena.setVisibility(View.GONE);

                  }



                }
              });
    }
  }




  /**
   * Resizes image.
   *
   * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
   * resulting in gorgeous previews but the storage of garbage capture data.
   *
   * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that is
   * at least as large as the respective texture view size, and that is at most as large as the
   * respective max size, and whose aspect ratio matches with the specified value. If such size
   * doesn't exist, choose the largest one that is at most as large as the respective max size, and
   * whose aspect ratio matches with the specified value.
   *
   * @param choices The list of sizes that the camera supports for the intended output class
   * @param textureViewWidth The width of the texture view relative to sensor coordinate
   * @param textureViewHeight The height of the texture view relative to sensor coordinate
   * @param maxWidth The maximum width that can be chosen
   * @param maxHeight The maximum height that can be chosen
   * @param aspectRatio The aspect ratio
   * @return The optimal {@code Size}, or an arbitrary one if none were big enough
   */
  private static Size chooseOptimalSize(
          Size[] choices,
          int textureViewWidth,
          int textureViewHeight,
          int maxWidth,
          int maxHeight,
          Size aspectRatio) {

    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth
              && option.getHeight() <= maxHeight
              && option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }

    // Pick the smallest of those big enough. If there is no one big enough, pick the
    // largest of those not big enough.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  public static Camera2BasicFragment newInstance() {
    return new Camera2BasicFragment();
  }

  /** Layout the preview and buttons. */
  @Override
  public View onCreateView(
          LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
  }

  /** Connect the buttons to their event handler. */
  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    textView = (TextView) view.findViewById(R.id.text);
    bawang = (Button) view.findViewById(R.id.bawang);
    akapulko = (Button) view.findViewById(R.id.akapulko);
    ampalaya = (Button) view.findViewById(R.id.ampalaya);
    bayabas = (Button) view.findViewById(R.id.bayabas);
    lagundi = (Button) view.findViewById(R.id.lagundi);
    niyog_niyogan = (Button) view.findViewById(R.id.niyog_niyogan);
    sambong = (Button) view.findViewById(R.id.sambong);
    tsaang_gubat = (Button) view.findViewById(R.id.tsaang_gubat);
    ulasimang_bato = (Button) view.findViewById(R.id.ulasimang_bato);
    yerba_buena = (Button) view.findViewById(R.id.yerba_buena);

  }


  /** Load the model and labels. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    try {
      classifier = new com.herbal.doh.tflitecamerademo.ImageClassifier(getActivity());
    } catch (IOException e) {
      Log.e(TAG, "Failed to initialize an image classifier.");
    }
    startBackgroundThread();
  }

  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();

    // When the screen is turned off and turned back on, the SurfaceTexture is already
    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
    // a camera and start preview from here (otherwise, we wait until the surface is ready in
    // the SurfaceTextureListener).
    if (textureView.isAvailable()) {
      openCamera(textureView.getWidth(), textureView.getHeight());
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
  }

  @Override
  public void onPause() {
    closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    classifier.close();
    super.onDestroy();
  }

  /**
   * Sets up member variables related to camera.
   *
   * @param width The width of available size for camera preview
   * @param height The height of available size for camera preview
   */
  private void setUpCameraOutputs(int width, int height) {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      for (String cameraId : manager.getCameraIdList()) {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        StreamConfigurationMap map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
          continue;
        }

        // // For still image captures, we use the largest available size.
        Size largest =
                Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
        imageReader =
                ImageReader.newInstance(
                        largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/ 2);

        // Find out if we need to swap dimension to get the preview size relative to sensor
        // coordinate.
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        // noinspection ConstantConditions
        /* Orientation of the camera sensor */
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        boolean swappedDimensions = false;
        switch (displayRotation) {
          case Surface.ROTATION_0:
          case Surface.ROTATION_180:
            if (sensorOrientation == 90 || sensorOrientation == 270) {
              swappedDimensions = true;
            }
            break;
          case Surface.ROTATION_90:
          case Surface.ROTATION_270:
            if (sensorOrientation == 0 || sensorOrientation == 180) {
              swappedDimensions = true;
            }
            break;
          default:
            Log.e(TAG, "Display rotation is invalid: " + displayRotation);
        }

        Point displaySize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        if (swappedDimensions) {
          rotatedPreviewWidth = height;
          rotatedPreviewHeight = width;
          maxPreviewWidth = displaySize.y;
          maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
          maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
          maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }

        previewSize =
                chooseOptimalSize(
                        map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth,
                        rotatedPreviewHeight,
                        maxPreviewWidth,
                        maxPreviewHeight,
                        largest);

        // We fit the aspect ratio of TextureView to the size of preview we picked.
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
          textureView.setAspectRatio(previewSize.getWidth(), previewSize.getWidth());
        } else {
          textureView.setAspectRatio(previewSize.getHeight(), previewSize.getHeight());
        }

        this.cameraId = cameraId;
        return;
      }
    } catch (CameraAccessException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      ErrorDialog.newInstance(getString(R.string.camera_error))
              .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }
  }

  private String[] getRequiredPermissions() {
    Activity activity = getActivity();
    try {
      PackageInfo info =
              activity
                      .getPackageManager()
                      .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  /** Opens the camera specified by {@link Camera2BasicFragment#cameraId}. */
  private void openCamera(int width, int height) {
    if (!checkedPermissions && !allPermissionsGranted()) {
      FragmentCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
      return;
    } else {
      checkedPermissions = true;
    }
    setUpCameraOutputs(width, height);
    configureTransform(width, height);
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }
      if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return;
      }
      manager.openCamera(cameraId, stateCallback, backgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (ContextCompat.checkSelfPermission(getActivity(), permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /** Closes the current {@link CameraDevice}. */
  private void closeCamera() {
    try {
      cameraOpenCloseLock.acquire();
      if (null != captureSession) {
        captureSession.close();
        captureSession = null;
      }
      if (null != cameraDevice) {
        cameraDevice.close();
        cameraDevice = null;
      }
      if (null != imageReader) {
        imageReader.close();
        imageReader = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      cameraOpenCloseLock.release();
    }
  }

  /** Starts a background thread and its {@link Handler}. */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
    synchronized (lock) {
      runClassifier = true;
    }
    backgroundHandler.post(periodicClassify);
  }

  /** Stops the background thread and its {@link Handler}. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
      synchronized (lock) {
        runClassifier = false;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Takes photos and classify them periodically. */
  private Runnable periodicClassify =
      new Runnable() {
        @Override
        public void run() {
          synchronized (lock) {
            if (runClassifier) {
              classifyFrame();
            }
          }
          backgroundHandler.post(periodicClassify);
        }
      };

  /** Creates a new {@link CameraCaptureSession} for camera preview. */
  private void createCameraPreviewSession() {
    try {
      SurfaceTexture texture = textureView.getSurfaceTexture();
      assert texture != null;

      // We configure the size of default buffer to be the size of camera preview we want.
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

      // This is the output Surface we need to start preview.
      Surface surface = new Surface(texture);

      // We set up a CaptureRequest.Builder with the output Surface.
      previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      previewRequestBuilder.addTarget(surface);

      // Here, we create a CameraCaptureSession for camera preview.
      cameraDevice.createCaptureSession(
          Arrays.asList(surface),
          new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // The camera is already closed
              if (null == cameraDevice) {
                return;
              }

              // When the session is ready, we start displaying the preview.
              captureSession = cameraCaptureSession;
              try {
                // Auto focus should be continuous for camera preview.
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Finally, we start displaying the camera preview.
                previewRequest = previewRequestBuilder.build();
                captureSession.setRepeatingRequest(
                    previewRequest, captureCallback, backgroundHandler);
              } catch (CameraAccessException e) {
                e.printStackTrace();
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              showToast("Failed");
            }
          },
          null);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * Configures the necessary {@link android.graphics.Matrix} transformation to `textureView`. This
   * method should be called after the camera preview size is determined in setUpCameraOutputs and
   * also the size of `textureView` is fixed.
   *
   * @param viewWidth The width of `textureView`
   * @param viewHeight The height of `textureView`
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == textureView || null == previewSize || null == activity) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale =
          Math.max(
              (float) viewHeight / previewSize.getHeight(),
              (float) viewWidth / previewSize.getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180, centerX, centerY);
    }
    textureView.setTransform(matrix);
  }

  /** Classifies a frame from the preview stream. */
  private void classifyFrame() {
    if (classifier == null || getActivity() == null || cameraDevice == null) {
      showToast("         Uninitialized Classifier or invalid context.");
      return;
    }
    Bitmap bitmap =
        textureView.getBitmap(com.herbal.doh.tflitecamerademo.ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
    String textToShow = classifier.classifyFrame(bitmap);
    bitmap.recycle();
    showToast(textToShow);
  }

  /** Compares two {@code Size}s based on their areas. */
  private static class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  }

  /** Shows an error message dialog. */
  public static class ErrorDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static ErrorDialog newInstance(String message) {
      ErrorDialog dialog = new ErrorDialog();
      Bundle args = new Bundle();
      args.putString(ARG_MESSAGE, message);
      dialog.setArguments(args);
      return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Activity activity = getActivity();
      return new AlertDialog.Builder(activity)
          .setMessage(getArguments().getString(ARG_MESSAGE))
          .setPositiveButton(
              android.R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  activity.finish();
                }
              })
          .create();
    }
  }
}
