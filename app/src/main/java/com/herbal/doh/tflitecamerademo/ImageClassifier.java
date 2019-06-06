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

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import org.tensorflow.lite.Interpreter;

/** Classifies images with Tensorflow Lite. */
public class ImageClassifier {

  private static final long COUNTDOWN_IN_MILLIS = 10000;
  private CountDownTimer countDownTimer;
  private long timeLeftInMillis;
  private static String result = "";

  /** Tag for the {@link Log}. */
  private static final String TAG = "TfLiteCameraDemo";

  /** Name of the model file stored in Assets. */
  private static final String MODEL_PATH = "herbal_graph.tflite";

  /** Name of the label file stored in Assets. */
  private static final String LABEL_PATH = "labels.txt";

  /** Number of results to show in the UI. */
  private static final int RESULTS_TO_SHOW = 1;

  /** Dimensions of inputs. */
  private static final int DIM_BATCH_SIZE = 1;

  private static final int DIM_PIXEL_SIZE = 3;

  static final int DIM_IMG_SIZE_X = 224;
  static final int DIM_IMG_SIZE_Y = 224;

  private static final int IMAGE_MEAN = 128;
  private static final float IMAGE_STD = 128.0f;


  /* Preallocated buffers for storing image data in. */
  private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

  /** An instance of the driver class to run model inference with Tensorflow Lite. */
  private Interpreter tflite;

  /** Labels corresponding to the output of the vision model. */
  private List<String> labelList;

  /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
  private ByteBuffer imgData = null;

  /** An array to hold inference results, to be feed into Tensorflow Lite as outputs. */
  private float[][] labelProbArray = null;
  /** multi-stage low pass filter **/
  private float[][] filterLabelProbArray = null;
  private static final int FILTER_STAGES = 3;
  private static final float FILTER_FACTOR = 0.4f;

  private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
      new PriorityQueue<>(
          RESULTS_TO_SHOW,
          new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
              return (o1.getValue()).compareTo(o2.getValue());
            }
          });

  /** Initializes an {@code ImageClassifier}. */
  ImageClassifier(Activity activity) throws IOException {
    tflite = new Interpreter(loadModelFile(activity));
    labelList = loadLabelList(activity);
    imgData =
        ByteBuffer.allocateDirect(
            4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
    imgData.order(ByteOrder.nativeOrder());
    labelProbArray = new float[1][labelList.size()];
    filterLabelProbArray = new float[FILTER_STAGES][labelList.size()];
    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
  }

  /** Classifies a frame from the preview stream. */
  String classifyFrame(Bitmap bitmap) {
    if (tflite == null) {
      Log.e(TAG, "Image classifier has not been initialized; Skipped.");
      return "Uninitialized Classifier.";
    }
    convertBitmapToByteBuffer(bitmap);
    // Here's where the magic happens!!!
    long startTime = SystemClock.uptimeMillis();
    tflite.run(imgData, labelProbArray);
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime));

    // smooth the results
    applyFilter();

    // print the results Long.toString(endTime - startTime) + "ms" +
    String textToShow = printTopKLabels();
    textToShow = textToShow;
    return textToShow;
  }

  void applyFilter(){
    int num_labels =  labelList.size();

    // Low pass filter `labelProbArray` into the first stage of the filter.
    for(int j=0; j<num_labels; ++j){
      filterLabelProbArray[0][j] += FILTER_FACTOR*(labelProbArray[0][j] -
                                                   filterLabelProbArray[0][j]);
    }
    // Low pass filter each stage into the next.
    for (int i=1; i<FILTER_STAGES; ++i){
      for(int j=0; j<num_labels; ++j){
        filterLabelProbArray[i][j] += FILTER_FACTOR*(
                filterLabelProbArray[i-1][j] -
                filterLabelProbArray[i][j]);

      }
    }

    // Copy the last stage filter output back to `labelProbArray`.
    for(int j=0; j<num_labels; ++j){
      labelProbArray[0][j] = filterLabelProbArray[FILTER_STAGES-1][j];
    }
  }

  /** Closes tflite to release resources. */
  public void close() {
    tflite.close();
    tflite = null;
  }

  /** Reads label list from Assets. */
  private List<String> loadLabelList(Activity activity) throws IOException {
    List<String> labelList = new ArrayList<String>();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(activity.getAssets().open(LABEL_PATH)));
    String line;
    while ((line = reader.readLine()) != null) {
      labelList.add(line);
    }
    reader.close();
    return labelList;
  }

  /** Memory-map the model file in Assets. */
  private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
    AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  /** Writes Image data into a {@code ByteBuffer}. */
  private void convertBitmapToByteBuffer(Bitmap bitmap) {
    if (imgData == null) {
      return;
    }
    imgData.rewind();
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Convert the image to floating point.
    int pixel = 0;
    long startTime = SystemClock.uptimeMillis();
    for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
      for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
        final int val = intValues[pixel++];
        imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
        imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
        imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
      }
    }
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
  }

  /** Prints top-K labels, to be shown in UI as the results. */
  private String printTopKLabels() {
    for (int i = 0; i < labelList.size(); ++i) {
      sortedLabels.add(
          new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
      if (sortedLabels.size() > RESULTS_TO_SHOW) {
        sortedLabels.poll();
      }
    }
    String textToShow = "";
    final int size = sortedLabels.size();

      for (int i = 0; i < size; ++i) {
        Map.Entry<String, Float> label = sortedLabels.poll();
        DecimalFormat df = new DecimalFormat("#%");

        if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("bawang")) {
          textToShow = "bawang95";
        } else if (label.getValue() > 0.90 && label.getValue() < 0.95 && label.getKey().equals("bawang")) {
          textToShow = "bawang90";
        } else if (label.getValue() > 0.85 && label.getValue() < 0.90 && label.getKey().equals("bawang")) {
          textToShow = "bawang85";
        } else if (label.getValue() > 0.80 && label.getValue() < 0.85 && label.getKey().equals("bawang")) {
          textToShow = "bawang80";
        } else if (label.getValue() > 0.75 && label.getValue() < 0.80 && label.getKey().equals("bawang")) {
          textToShow = "bawang75";
        } else if (label.getValue() > 0.70 && label.getValue() < 0.75 && label.getKey().equals("bawang")) {
          textToShow = "bawang70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("akapulko")) {
          textToShow = "akapulko95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("akapulko")) {
          textToShow = "akapulko90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("akapulko")) {
          textToShow = "akapulko85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("akapulko")) {
          textToShow = "akapulko80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("akapulko")) {
          textToShow = "akapulko75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("akapulko")) {
          textToShow = "akapulko70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("ampalaya")) {
          textToShow = "ampalaya95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("ampalaya")) {
          textToShow = "ampalaya90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("ampalaya")) {
          textToShow = "ampalaya85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("ampalaya")) {
          textToShow = "ampalaya80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("ampalaya")) {
          textToShow = "ampalaya75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("ampalaya")) {
          textToShow = "ampalaya70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("bayabas")) {
          textToShow = "bayabas95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("bayabas")) {
          textToShow = "bayabas90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("bayabas")) {
          textToShow = "bayabas85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("bayabas")) {
          textToShow = "bayabas80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("bayabas")) {
          textToShow = "bayabas75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("bayabas")) {
          textToShow = "bayabas70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("lagundi")) {
          textToShow = "lagundi95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("lagundi")) {
          textToShow = "lagundi90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("lagundi")) {
          textToShow = "lagundi85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("lagundi")) {
          textToShow = "lagundi80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("lagundi")) {
          textToShow = "lagundi75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("lagundi")) {
          textToShow = "lagundi70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("niyog_niyogan")) {
          textToShow = "niyog_niyogan95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("niyog_niyogan")) {
          textToShow = "niyog_niyogan90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("niyog_niyogan")) {
          textToShow = "niyog_niyogan85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("niyog_niyogan")) {
          textToShow = "niyog_niyogan80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("niyog_niyogan")) {
          textToShow = "niyog_niyogan75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("niyog_niyogan")) {
          textToShow = "niyog_niyogan70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("sambong")) {
          textToShow = "sambong95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("sambong")) {
          textToShow = "sambong90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("sambong")) {
          textToShow = "sambong85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("sambong")) {
          textToShow = "sambong80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("sambong")) {
          textToShow = "sambong75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("sambong")) {
          textToShow = "sambong70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("tsaang_gubat")) {
          textToShow = "tsaang_gubat95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("tsaang_gubat")) {
          textToShow = "tsaang_gubat90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("tsaang_gubat")) {
          textToShow = "tsaang_gubat85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("tsaang_gubat")) {
          textToShow = "tsaang_gubat80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("tsaang_gubat")) {
          textToShow = "tsaang_gubat75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("tsaang_gubat")) {
          textToShow = "tsaang_gubat70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("ulasimang_bato")) {
          textToShow = "ulasimang_bato95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("ulasimang_bato")) {
          textToShow = "ulasimang_bato90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("ulasimang_bato")) {
          textToShow = "ulasimang_bato85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("ulasimang_bato")) {
          textToShow = "ulasimang_bato80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("ulasimang_bato")) {
          textToShow = "ulasimang_bato75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("ulasimang_bato")) {
          textToShow = "ulasimang_bato70";
        } else if (label.getValue() >= 0.95 && label.getValue() < 1.0 && label.getKey().equals("yerba_buena")) {
          textToShow = "yerba_buena95";
        } else if (label.getValue() >= 0.90 && label.getValue() < 0.95 && label.getKey().equals("yerba_buena")) {
          textToShow = "yerba_buena90";
        } else if (label.getValue() >= 0.85 && label.getValue() < 0.90 && label.getKey().equals("yerba_buena")) {
          textToShow = "yerba_buena85";
        } else if (label.getValue() >= 0.80 && label.getValue() < 0.85 && label.getKey().equals("yerba_buena")) {
          textToShow = "yerba_buena80";
        } else if (label.getValue() >= 0.75 && label.getValue() < 0.80 && label.getKey().equals("yerba_buena")) {
          textToShow = "yerba_buena75";
        } else if (label.getValue() >= 0.70 && label.getValue() < 0.75 && label.getKey().equals("yerba_buena")) {
          textToShow = "yerba_buena70";
        } else if (label.getValue() >= 0.70 && label.getKey().equals("not_herbal")) {
          textToShow = "I think this object is not a herbal plant,  please try another object...";
        } else {
          timeLeftInMillis = COUNTDOWN_IN_MILLIS;
          textToShow = startCountDown();
        }//end of if-else-if condition
      } //end of for loop
    return textToShow;
  }

  //Countdown Timer
  private String startCountDown(){

    countDownTimer = new CountDownTimer(timeLeftInMillis, 500) {
      @Override
      public void onTick(long millisUntilFinished) {
        timeLeftInMillis = millisUntilFinished;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%01d",seconds);
        result = "Recalling learned data, please wait...";

      }

      @Override
      public void onFinish() {
        timeLeftInMillis = 0;
        countDownTimer.cancel();
        result = "Sorry, I can't clearly recognize the object, try to change the angle...";

      }
    }.start();
    return result;
  }


}
