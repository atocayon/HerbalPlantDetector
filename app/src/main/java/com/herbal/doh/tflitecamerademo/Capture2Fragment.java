package com.herbal.doh.tflitecamerademo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Capture2Fragment extends Activity {

    private static final String MODEL_PATH = "herbal_graph.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private String herbname = null, DataProb = null;
    Button bawang,akapulko,ampalaya,bayabas,lagundi,niyog_niyogan,sambong,tsaang_gubat,ulasimang_bato,yerba_buena;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_capture2_fragment );
        cameraView = findViewById( R.id.cameraView );
        imageViewResult = findViewById( R.id.imageViewResult );
        textViewResult = findViewById( R.id.textViewResult );
        textViewResult.setMovementMethod( new ScrollingMovementMethod() );
        Integer max = textViewResult.getMaxHeight();
        textViewResult.setHeight(max);

        bawang = (Button) findViewById(R.id.bawang);
        akapulko = (Button) findViewById(R.id.akapulko);
        ampalaya = (Button) findViewById(R.id.ampalaya);
        bayabas = (Button) findViewById(R.id.bayabas);
        lagundi = (Button) findViewById(R.id.lagundi);
        niyog_niyogan = (Button) findViewById(R.id.niyog_niyogan);
        sambong = (Button) findViewById(R.id.sambong);
        tsaang_gubat = (Button) findViewById(R.id.tsaang_gubat);
        ulasimang_bato = (Button) findViewById(R.id.ulasimang_bato);
        yerba_buena = (Button) findViewById(R.id.yerba_buena);


        btnDetectObject = findViewById( R.id.btnDetectObject );

        cameraView.addCameraKitListener( new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap( bitmap, INPUT_SIZE, INPUT_SIZE, false );

                imageViewResult.setImageBitmap( bitmap );

                final List<Classifier.Recognition> results = classifier.recognizeImage( bitmap );

                String[] herbalInfo = results.toString().replace( "[", "" ).replace( "]", "" ).split( " " );
                herbname = herbalInfo[0];

               if(herbname.equals("bawang")) {

                   textViewResult.setText("Herbal Name: Bawang (Allium sativum)\nConfidence Outcome: " + herbalInfo[1] + "\n\nBasic Info.:\nThis herbal plant is popularly known as `Garlic`, it helps reduce cholesterol in the blood and hence, helps control blood pressure.");
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
               else if (herbname.equals("akapulko")) {
                   textViewResult.setText("Herbal Name: Akapulko (Cassia alata)\nConfidence Outcome: " + herbalInfo[1] + "\n\nBasic Info.:\nThis herbal plant is popularly known as `Bayabas-bayabasan` in tagalog and `Ringworm bush` in English, this herbal medicine is used to treat ringworms and skin fungal infections.");
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
               else if (herbname.equals("ampalaya")) {
                    textViewResult.setText("Herbal Name: Ampalaya (Momordica charantia)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant is popularly known as “bitter gourd” or “bitter melon” in English, it is used as a treatment for diabetes (diabetes mellitus), for the non-insulin dependent patients.");
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
               else if(herbname.equals("bayabas")) {
                   textViewResult.setText("Herbal Name: Bayabas (Psidium guajava)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant is popularly known as “Guava” in English, it is used primarily as an antiseptic  to disinfect wounds, Also, it can be used as a mouth wash to treat tooth decay and gum infection.");
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
               else if(herbname.equals("lagundi")) {
                    textViewResult.setText("Herbal Name: Lagundi (Vitex negundo)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant is popularly known as “5-leaved chaste tree” in English, The main uses of this herbal plant is for treating coughs and asthma.");
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
               else if(herbname.equals("niyog_niyogan")) {
                    textViewResult.setText("Herbal Name: Niyog-niyogan (Quisqualis indica L.)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant has a characteristics of a vine and popularly known as “Chinese honey suckle” in English, this herbal plant is effective in eliminating intestinal worms, particularly the Ascaris (large roundworm) and Trichina (pork worm). Only the dried matured seeds are medicinal, crack and ingest the dried seeds two hours after eating (5 to 7 seeds for children & 8 to 10 seeds for adults). If one dose does not eliminate the intestinal worms, wait for a week before repeating the dose.");
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
               else if(herbname.equals("sambong")) {
                   textViewResult.setText("Herbal Name: Sambong (Blumea balsamifera)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant is popularly known as `Blumea camphora` in English. It is used as a diuretic  (increased production of urine) that helps in the excretion (eliminating) of urinary stones. It can also be used as an edema (medical term for swelling).");
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
               else if (herbname.equals("tsaang_gubat")) {
                    textViewResult.setText("Herbal Name: Tsaang Gubat (Ehretia microphylla Lam.)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant is used like a tea, and this is effective in treating intestinal motility (abnormal intestinal contractions), and this is also used as a mouth wash since the leaves of this herbal plant has high fluoride content.");
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
               else if(herbname.equals("ulasimang_bato")) {
                   textViewResult.setText("Herbal Name: Ulasimang Bato|Pancit-pancitan (Peperomia pellucida)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant is effective in fighting arthritis and gout. The leaves can be eaten fresh (about a cupful) as salad or can be drink like a tea. For the decoction, boil a cup of clean chopped leaves in 2 cups of water. Boil for 15 to 20 minutes. Strain, let cool and drink a cup after meals (3 times day).");
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
               else if(herbname.equals("yerba_buena")) {
                   textViewResult.setText("Herbal Name: Yerba Buena (Clinopodium douglasii)\nConfidence outcome: " + herbalInfo[1] + "\n\nBasic Info. :\nThis herbal plant is commonly known as `Peppermint` in English, this plant has a characteristic of a vine and it is used as an analgesic (painkiller) to relive body aches and pain. Also, it can be taken internally as a decoction or externally by pounding the leaves and applying the extract directly on the affected area.");
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
               }
               else {
                   textViewResult.setText("No Herbal Plant has been captured!");
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

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        } );

        initTensorFlowAndLoadModel();
    }

    public void Detect(View v){
        cameraView.captureImage();
    }

    public void info(View view){
        dialog();
    }

    public void dialog(){
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
        ImageButton close = (ImageButton) dialog.findViewById(R.id.close);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(v -> {text.setText(R.string.ins2); dialogButton.setVisibility(View.GONE); dialogButton1.setVisibility(View.VISIBLE);  });
        dialogButton1.setOnClickListener(v -> {text.setText(R.string.ins3); dialogButton1.setVisibility(View.GONE); dialogButton2.setVisibility(View.VISIBLE); });
        dialogButton2.setOnClickListener(v -> {text.setText(R.string.ins4); dialogButton2.setVisibility(View.GONE); dialogButton3.setVisibility(View.VISIBLE);});
        dialogButton3.setOnClickListener(v -> {text.setText(R.string.ins5); dialogButton3.setVisibility(View.GONE); dialogButton4.setVisibility(View.VISIBLE);});
        dialogButton4.setOnClickListener(v -> {text.setText(R.string.ins6); dialogButton4.setVisibility(View.GONE); dialogButton5.setVisibility(View.VISIBLE);});
        dialogButton5.setOnClickListener(v-> {dialog.dismiss();});
        close.setOnClickListener(v -> {dialog.dismiss();});

        dialog.show();
    }

    public void more(View view){
//    Intent i = new Intent(CameraActivity.this,Manual.class);
//    startActivity(i);

        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.mode2);

        // set the custom dialog components - text, image and button
        ImageButton capture = (ImageButton) dialog.findViewById(R.id.capture);
        ImageButton manual = (ImageButton) dialog.findViewById(R.id.manual);
        ImageButton about = (ImageButton) dialog.findViewById(R.id.about);
        ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(v -> dialog.dismiss());
        capture.setOnClickListener(v -> {Intent i = new Intent(Capture2Fragment.this,CameraActivity.class);startActivity(i);});
        manual.setOnClickListener(v -> {Intent i = new  Intent(Capture2Fragment.this,Manual.class); startActivity(i); });
        about.setOnClickListener(v -> {Intent i = new  Intent(Capture2Fragment.this,About.class); startActivity(i); });

        dialog.show();




    }

    public void onclickbawang(View view){
        Intent i = new Intent(Capture2Fragment.this,Bawang.class);
        startActivity(i);
    }

    public void onclickakapulko(View view){
        Intent i = new Intent(Capture2Fragment.this,Akapulko.class);
        startActivity(i);
    }

    public void onclickampalaya(View view){
        Intent i = new Intent(Capture2Fragment.this,Ampalaya.class);
        startActivity(i);
    }

    public void onclickbayabas(View view){
        Intent i = new Intent(Capture2Fragment.this,Bayabas.class);
        startActivity(i);
    }

    public void onclicklagundi(View view){
        Intent i = new Intent(Capture2Fragment.this,Lagundi.class);
        startActivity(i);
    }

    public void onclickniyog_niyogan(View view){
        Intent i = new Intent(Capture2Fragment.this,NiyogNiyogan.class);
        startActivity(i);
    }

    public void onclicksambong(View view){
        Intent i = new Intent(Capture2Fragment.this,Sambong.class);
        startActivity(i);
    }

    public void onclicktsaang_gubat(View view){
        Intent i = new Intent(Capture2Fragment.this,TsaangGubat.class);
        startActivity(i);
    }

    public void onclickulasimang_bato(View view){
        Intent i = new Intent(Capture2Fragment.this,UlasimangBato.class);
        startActivity(i);
    }

    public void onclickyerba_buena(View view){
        Intent i = new Intent(Capture2Fragment.this,YerbaBuena.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute( new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        } );
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute( new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT );
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException( "Error initializing TensorFlow!", e );
                }
            }
        } );
    }

    private void makeButtonVisible() {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility( View.VISIBLE );
            }
        } );
    }
}
