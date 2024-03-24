package com.example.trashinformation;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.common.NormalizationOptions;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;


import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MlActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView outputTextView;
    private TextView failOrNot;
    private ImageLabeler labeler;
    private InputImage imageToProcess;
    private TextToSpeech textToSpeech;
    private ImageView inputImageGalleryView;
    private int REQUEST_PICK_IMAGE = 1000;

    private int REQUEST_CAPTURE_IMAGE = 1001;


    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml);
        initializeComponents();
        mAuth = FirebaseAuth.getInstance();
        checkSdkGallery();
    }



    private void initializeComponents() {
        outputTextView = findViewById(R.id.textViewOutput);
        failOrNot = findViewById(R.id.onsuccess);
        failOrNot.setText("not success or fail");

        LocalModel localModel = new LocalModel.Builder()
                .setAssetFilePath("WasteClassificationModel.tflite")
                .build();

        CustomImageLabelerOptions customImageLabelerOptions =
                new CustomImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.5f)
                        .setMaxResultCount(5)
                        .build();
        labeler = ImageLabeling.getClient(customImageLabelerOptions);

        Resources resources = getResources();
        int drawableId = R.drawable.juice_image;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, drawableId);
        imageToProcess = InputImage.fromBitmap(bitmap, 0);
        runClassification(bitmap);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });


        inputImageGalleryView = findViewById(R.id.image_trash);
        inputImageGalleryView.setImageBitmap(imageToProcess.getBitmapInternal());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menumenu,menu);
        return true;

        }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.score) {
            movetoscoreboard();
        }else if(id == R.id.log_out){
            logout();
        }




        return true;
    }



    private void checkSdkGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    public void runClassification(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        labeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        failOrNot.setText("Success :)");

                        if (labels.size() > 0) {
                            StringBuilder builder = new StringBuilder();
                            for (ImageLabel label : labels) {
                                builder.append(label.getText())
                                        .append(" ; ")
                                        .append("\n");
                            }
                            outputTextView.setText(builder.toString());
                        } else {
                            outputTextView.setText("could not classified");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        failOrNot.setText("fail :(");
                        incrementUserScore(); //need to be in on succses
                    }
                });
    }

    public void speak(View view) {
        textToSpeech.setPitch(0.5f); //quality of sound
        textToSpeech.setSpeechRate(0.5f); //speed of sound
        textToSpeech.speak(outputTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MlActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(MlActivity.class.getSimpleName(),"grant result for"+ permissions[0] +"is granted"+ grantResults[0]);
    }

    public void onPickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    public void onStartCamera(View view) {
        //create a file to share with camera
        photoFile = createPhotoFile();
        Uri fileUri = FileProvider.getUriForFile(this, "com.iago.fileprovider",photoFile);

        //create an intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT , fileUri);

        //start activity for result
        startActivityForResult(intent,REQUEST_CAPTURE_IMAGE);
    }

    private File createPhotoFile(){
        File photoFileDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"Trash_Pictures");
        if(!photoFileDir.exists()){
            photoFileDir.mkdir();
        }
        String FileName =  new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        File file = new File(photoFileDir.getPath() + File.separator + FileName);
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)  {
            if( requestCode == REQUEST_PICK_IMAGE){
            Uri uri = data.getData();
            Bitmap bitmap = loadFromUri(uri);
            inputImageGalleryView.setImageBitmap(bitmap);
            runClassification(bitmap);
        }else if(requestCode == REQUEST_CAPTURE_IMAGE){
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                inputImageGalleryView.setImageBitmap(rotatedBitmap);
                runClassification(bitmap);
        }
        }
    }

    private Bitmap loadFromUri(Uri uri) {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void movetoscoreboard() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MlActivity.this, ScoreBoard.class));

    }
    public void incrementUserScore() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference("users/" + FirebaseAuth.getInstance().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    user.incrementScore();
                    // Update the score in the database
                    userRef.setValue(user);
                } else {
                    // Handle the case where the user's data doesn't exist
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }
}
