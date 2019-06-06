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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/** Main {@code Activity} class for the Camera app. */
public class CameraActivity extends Activity {

  final Context context = this;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    if (null == savedInstanceState) {
      getFragmentManager()
          .beginTransaction()
          .replace(R.id.container, Camera2BasicFragment.newInstance())
          .commit();
    }
    dialog();


  }

  public void more(View view){
//    Intent i = new Intent(CameraActivity.this,Manual.class);
//    startActivity(i);

    // custom dialog
    final Dialog dialog = new Dialog(context);
    dialog.setContentView(R.layout.mode);

    // set the custom dialog components - text, image and button
    ImageButton capture = (ImageButton) dialog.findViewById(R.id.capture);
    ImageButton manual = (ImageButton) dialog.findViewById(R.id.manual);
    ImageButton about = (ImageButton) dialog.findViewById(R.id.about);
    ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.dialogButtonOK);
    // if button is clicked, close the custom dialog
    dialogButton.setOnClickListener(v -> dialog.dismiss());
    capture.setOnClickListener(v -> {Intent i = new Intent(CameraActivity.this,Capture2Fragment.class);startActivity(i);});
    manual.setOnClickListener(v -> {Intent i = new  Intent(CameraActivity.this,Manual.class); startActivity(i); });
    about.setOnClickListener(v -> {Intent i = new  Intent(CameraActivity.this,About.class); startActivity(i); });

    dialog.show();




  }
  public void info(View view){
    instruction();
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  public void dialog(){
    boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
    if (isFirstRun){
    // custom dialog
          final Dialog dialog = new Dialog(context);
          dialog.setContentView(R.layout.instruction);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.instruction);
        ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.dialogButtonOK);
        ImageButton dialogButton1 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK1);
        ImageButton dialogButton2 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK2);
        ImageButton dialogButton3 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK3);
        ImageButton dialogButton4 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK4);
        ImageButton dialogButton5 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK5);
        ImageButton dialogBack1 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack1);
        ImageButton dialogBack2 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack2);
        ImageButton dialogBack3 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack3);
        ImageButton dialogBack4 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack4);
        ImageButton dialogBack5 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack5);
        ImageButton close = (ImageButton) dialog.findViewById(R.id.close);

        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(v -> {text.setText(R.string.ins2); dialogButton.setVisibility(View.GONE); dialogButton1.setVisibility(View.VISIBLE); dialogBack1.setVisibility(View.VISIBLE); });
        dialogBack1.setOnClickListener(v -> {text.setText(R.string.ins1);dialogButton.setVisibility(View.VISIBLE); dialogBack1.setVisibility(View.GONE);dialogButton1.setVisibility(View.GONE);});

        dialogButton1.setOnClickListener(v -> {text.setText(R.string.ins3); dialogButton1.setVisibility(View.GONE); dialogButton2.setVisibility(View.VISIBLE); dialogBack2.setVisibility(View.VISIBLE); dialogBack1.setVisibility(View.GONE); });
        dialogBack2.setOnClickListener(v -> {text.setText(R.string.ins2);dialogButton1.setVisibility(View.VISIBLE);dialogButton2.setVisibility(View.GONE);dialogBack2.setVisibility(View.GONE);dialogBack1.setVisibility(View.VISIBLE); });

        dialogButton2.setOnClickListener(v -> {text.setText(R.string.ins4); dialogButton2.setVisibility(View.GONE); dialogButton3.setVisibility(View.VISIBLE); dialogBack3.setVisibility(View.VISIBLE); dialogBack2.setVisibility(View.GONE); });
        dialogBack3.setOnClickListener(v -> {text.setText(R.string.ins3); dialogButton2.setVisibility(View.VISIBLE); dialogButton3.setVisibility(View.GONE); dialogBack3.setVisibility(View.GONE); dialogBack2.setVisibility(View.VISIBLE);});

        dialogButton3.setOnClickListener(v -> {text.setText(R.string.ins5); dialogButton3.setVisibility(View.GONE); dialogButton4.setVisibility(View.VISIBLE); dialogBack4.setVisibility(View.VISIBLE);dialogBack3.setVisibility(View.GONE);});
        dialogBack4.setOnClickListener(v -> {text.setText(R.string.ins4); dialogButton3.setVisibility(View.VISIBLE); dialogButton4.setVisibility(View.GONE); dialogBack4.setVisibility(View.GONE); dialogBack3.setVisibility(View.VISIBLE);});

        dialogButton4.setOnClickListener(v -> {text.setText(R.string.ins6); dialogButton4.setVisibility(View.GONE); dialogButton5.setVisibility(View.VISIBLE); dialogBack5.setVisibility(View.VISIBLE); dialogBack4.setVisibility(View.GONE);});
        dialogBack5.setOnClickListener(v -> {text.setText(R.string.ins5); dialogButton4.setVisibility(View.VISIBLE); dialogButton5.setVisibility(View.GONE); dialogBack5.setVisibility(View.GONE); dialogBack4.setVisibility(View.VISIBLE);});
        dialogButton5.setOnClickListener(v-> {dialog.dismiss();});
        close.setOnClickListener(v -> {dialog.dismiss();});

        dialog.show();


      getSharedPreferences("PREFERENCE", MODE_PRIVATE)
              .edit()
              .putBoolean("isFirstRun", false)
              .apply();
    }

  }

  public void instruction(){
      // custom dialog
      final Dialog dialog = new Dialog(context);
      dialog.setContentView(R.layout.instruction);

      // set the custom dialog components - text, image and button
      TextView text = (TextView) dialog.findViewById(R.id.instruction);
      ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.dialogButtonOK);
      ImageButton dialogButton1 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK1);
      ImageButton dialogButton2 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK2);
      ImageButton dialogButton3 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK3);
      ImageButton dialogButton4 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK4);
      ImageButton dialogButton5 = (ImageButton) dialog.findViewById(R.id.dialogButtonOK5);
      ImageButton dialogBack1 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack1);
      ImageButton dialogBack2 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack2);
      ImageButton dialogBack3 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack3);
      ImageButton dialogBack4 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack4);
      ImageButton dialogBack5 = (ImageButton) dialog.findViewById(R.id.dialogButtonBack5);
      ImageButton close = (ImageButton) dialog.findViewById(R.id.close);

      // if button is clicked, close the custom dialog
      dialogButton.setOnClickListener(v -> {text.setText(R.string.ins2); dialogButton.setVisibility(View.GONE); dialogButton1.setVisibility(View.VISIBLE); dialogBack1.setVisibility(View.VISIBLE); });
      dialogBack1.setOnClickListener(v -> {text.setText(R.string.ins1);dialogButton.setVisibility(View.VISIBLE); dialogBack1.setVisibility(View.GONE);dialogButton1.setVisibility(View.GONE);});

      dialogButton1.setOnClickListener(v -> {text.setText(R.string.ins3); dialogButton1.setVisibility(View.GONE); dialogButton2.setVisibility(View.VISIBLE); dialogBack2.setVisibility(View.VISIBLE); dialogBack1.setVisibility(View.GONE); });
      dialogBack2.setOnClickListener(v -> {text.setText(R.string.ins2);dialogButton1.setVisibility(View.VISIBLE);dialogButton2.setVisibility(View.GONE);dialogBack2.setVisibility(View.GONE);dialogBack1.setVisibility(View.VISIBLE); });

      dialogButton2.setOnClickListener(v -> {text.setText(R.string.ins4); dialogButton2.setVisibility(View.GONE); dialogButton3.setVisibility(View.VISIBLE); dialogBack3.setVisibility(View.VISIBLE); dialogBack2.setVisibility(View.GONE); });
      dialogBack3.setOnClickListener(v -> {text.setText(R.string.ins3); dialogButton2.setVisibility(View.VISIBLE); dialogButton3.setVisibility(View.GONE); dialogBack3.setVisibility(View.GONE); dialogBack2.setVisibility(View.VISIBLE);});

      dialogButton3.setOnClickListener(v -> {text.setText(R.string.ins5); dialogButton3.setVisibility(View.GONE); dialogButton4.setVisibility(View.VISIBLE); dialogBack4.setVisibility(View.VISIBLE);dialogBack3.setVisibility(View.GONE);});
      dialogBack4.setOnClickListener(v -> {text.setText(R.string.ins4); dialogButton3.setVisibility(View.VISIBLE); dialogButton4.setVisibility(View.GONE); dialogBack4.setVisibility(View.GONE); dialogBack3.setVisibility(View.VISIBLE);});

      dialogButton4.setOnClickListener(v -> {text.setText(R.string.ins6); dialogButton4.setVisibility(View.GONE); dialogButton5.setVisibility(View.VISIBLE); dialogBack5.setVisibility(View.VISIBLE); dialogBack4.setVisibility(View.GONE);});
      dialogBack5.setOnClickListener(v -> {text.setText(R.string.ins5); dialogButton4.setVisibility(View.VISIBLE); dialogButton5.setVisibility(View.GONE); dialogBack5.setVisibility(View.GONE); dialogBack4.setVisibility(View.VISIBLE);});
      dialogButton5.setOnClickListener(v-> {dialog.dismiss();});
      close.setOnClickListener(v -> {dialog.dismiss();});

      dialog.show();



  }

  public void onclickbawang(View view){
    Intent i = new Intent(CameraActivity.this,Bawang.class);
    startActivity(i);
  }

  public void onclickakapulko(View view){
    Intent i = new Intent(CameraActivity.this,Akapulko.class);
    startActivity(i);
  }

  public void onclickampalaya(View view){
    Intent i = new Intent(CameraActivity.this,Ampalaya.class);
    startActivity(i);
  }

  public void onclickbayabas(View view){
    Intent i = new Intent(CameraActivity.this,Bayabas.class);
    startActivity(i);
  }

  public void onclicklagundi(View view){
    Intent i = new Intent(CameraActivity.this,Lagundi.class);
    startActivity(i);
  }

  public void onclickniyog_niyogan(View view){
    Intent i = new Intent(CameraActivity.this,NiyogNiyogan.class);
    startActivity(i);
  }

  public void onclicksambong(View view){
    Intent i = new Intent(CameraActivity.this,Sambong.class);
    startActivity(i);
  }

  public void onclicktsaang_gubat(View view){
    Intent i = new Intent(CameraActivity.this,TsaangGubat.class);
    startActivity(i);
  }

  public void onclickulasimang_bato(View view){
    Intent i = new Intent(CameraActivity.this,UlasimangBato.class);
    startActivity(i);
  }

  public void onclickyerba_buena(View view){
    Intent i = new Intent(CameraActivity.this,YerbaBuena.class);
    startActivity(i);
  }


}
