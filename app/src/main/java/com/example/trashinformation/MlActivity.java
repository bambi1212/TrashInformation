package com.example.trashinformation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.common.NormalizationOptions;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;


import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MlActivity extends AppCompatActivity {


    private TextView outputTextView;
    private TextView failOrNot; // just for check
    private ImageLabeler labeler;
    private InputImage imageToProcces;

    private TextToSpeech mTTS;

    private ImageView inputImageGalaryView;

    private int REQUEST_PICK_IMAGE =1000;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml);
        defineLocalModelAndViews();


        inputImageGalaryView = findViewById(R.id.inputImageViewGalaryi);

        labeler.process(imageToProcces)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                          @Override
                                          public void onSuccess(List<ImageLabel> labels) {
                                              failOrNot.setText("Success :)");


                                              // Task completed successfully
                                              // ...
                        /*
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            outputTextView.setText("text:"+text);}}


                         */
                                              if (labels.size() > 0) {
                                                  StringBuilder builder = new StringBuilder();
                                                  for (ImageLabel label : labels) {
                                                      builder.append(label.getText())
                                                              .append(" ; ")
                                                              //.append(label.getConfidence()) //how ,utch the ai think its true from 0 to 1
                                                              .append("\n");

                                                  }
                                                  outputTextView.setText(builder.toString());
                                              } else {
                                                  outputTextView.setText("could not classified");

                                              }

                                          }
                                      }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        e.printStackTrace();
                        failOrNot.setText("fail :(");

                    }

                });


        Button mButtonSpeak= findViewById(R.id.mButtonSpeak);
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) { //enable play button
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.UK);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "language not supported");
                    } else {
                        mButtonSpeak.setEnabled(true);
                    }

                }
            }
        });
        checkSdkGalary();

    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(imageHelperActivity.class.getSimpleName(),"grant result for"+ permissions[0] +"is granted"+ grantResults[0]);
    }

     */


    public void speak(View view) {



        mTTS.setPitch(0.5f);
        mTTS.setSpeechRate(0.5f);
        mTTS.speak(outputTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);


    }



    public void defineLocalModelAndViews(){
        outputTextView = findViewById(R.id.textViewOutput);
        failOrNot = findViewById(R.id.onsucces);
        failOrNot.setText("not seccses or fail");

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("WasteClassificationModel.tflite")


                        // or .setAbsoluteFilePath(absolute file path to model file)
                        // or .setUri(URI to model file)
                        .build();

        CustomImageLabelerOptions customImageLabelerOptions =
                new CustomImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.5f)
                        .setMaxResultCount(5)
                        .build();
         labeler = ImageLabeling.getClient(customImageLabelerOptions);

        // Get a reference to the drawable resource
        Resources resources = getResources();
        int drawableId = R.drawable.juice_image;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, drawableId);




        /*
        NormalizationOptions Normalioptions =
                new NormalizationOptions.Builder()
                        .setMin(0) // Assuming pixel values are in the range [0, 255]
                        .setMax(255)
                        .setNormalized(-1.0f, 1.0f) // Normalize pixel values to [-1, 1]
                        .build();

         */
        // Create an InputImage object from the Bitmap only this can be proccesed
         imageToProcces = InputImage.fromBitmap(bitmap, 0);

        //check if got image
        ImageView imageViewXml = findViewById(R.id.imageView);
        imageViewXml.setImageBitmap(imageToProcces.getBitmapInternal());
        //end check
    }


    public void logout(View v) {

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MlActivity.this, LoginActivity.class));
            finish(); //so he cant press back button and come to here from login

        }
        public void checkSdkGalary(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission not granted, request it
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(MlActivity.class.getSimpleName(),"grant result for"+ permissions[0] +"is granted"+ grantResults[0]);
    }

    public void onPickImage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }
    public void onStartCamera(View view){

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
            if(requestCode == REQUEST_PICK_IMAGE){
                Uri uri = data.getData();
                Bitmap bitmap = loadFromUri(uri);
                inputImageGalaryView.setImageBitmap(bitmap);



                ImageView imageViewXml = findViewById(R.id.inputImageViewGalaryi);
                imageViewXml.setImageBitmap(bitmap);

                //ImageView imageViewXml = findViewById(R.id.inputImageViewGalaryi);
                //imageViewXml.setImageBitmap(bitmap);


                //runClassification(bitmap);
            }
    }

    private Bitmap loadFromUri(Uri uri){
        Bitmap bitmap = null;

        try{
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }
            else{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }


        } catch (IOException e){
            e.printStackTrace();
        }

        return bitmap;
    }





    }
