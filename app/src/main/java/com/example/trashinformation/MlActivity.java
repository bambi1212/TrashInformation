package com.example.trashinformation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;

import android.graphics.ImageDecoder;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MlActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView outputTextView;
    private ImageLabeler imageLabeler;
    private TextToSpeech textToSpeech;
    private ImageView inputImageView;

    private File photoFile;
    private static final int REQUEST_PICK_IMAGE = 1000;
    private static final int REQUEST_CAPTURE_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml);
        initializeComponents();
        mAuth = FirebaseAuth.getInstance();
        checkSdkGallery();
    }

    private void initializeComponents() {
        startCountdownTimer();
        outputTextView = findViewById(R.id.textViewOutput);
        inputImageView = findViewById(R.id.image_trash);

        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menumenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.score) {
            movetoScoreboard();
        } else if (id == R.id.log_out) {
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
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        imageLabeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(@NonNull List<ImageLabel> imageLabels) {
                if (imageLabels.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    for (ImageLabel label : imageLabels) {
                        builder.append(label.getText()).append(" ; ").append(label.getConfidence()).append("\n");
                    }
                    outputTextView.setText(builder.toString());
                    suggestDisposalMethod(builder.toString());
                } else {
                    outputTextView.setText("Could not classify.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void suggestDisposalMethod(String labels) {
        if (labels.contains("plastic")) {
            outputTextView.setText("Dispose of in the orange bin.");
        } else if (labels.contains("glass")) {
            outputTextView.setText("Dispose of in the purple bin.");
        } else if (labels.contains("paper")) {
            outputTextView.setText("Dispose of in the blue bin.");
        } else {
            outputTextView.setText("Dispose of in the orange bin.");
        }
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
        Log.d(MlActivity.class.getSimpleName(), "Grant result for " + permissions[0] + " is granted: " + grantResults[0]);
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


    private File createPhotoFile() {
        File photoFileDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Trash_Pictures");
        if (!photoFileDir.exists()) {
            photoFileDir.mkdir();
        }
        String FileName = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        return new File(photoFileDir.getPath() + File.separator + FileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                Uri uri = data.getData();
                Bitmap bitmap = loadFromUri(uri);
                inputImageView.setImageBitmap(bitmap);
                runClassification(bitmap);
            } else if (requestCode == REQUEST_CAPTURE_IMAGE) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Bitmap rotatedBitmap = rotateBitmap(bitmap, 90); // Rotate by 90 degrees
                inputImageView.setImageBitmap(rotatedBitmap);
                runClassification(rotatedBitmap);
            }
        }
    }
    public Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap loadFromUri(Uri uri) {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void movetoScoreboard() {
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


    private void startCountdownTimer() {

        TextView TextTimer = findViewById(R.id.timer);

        new CountDownTimer(43200000, 1000) {
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / 3600000;
                long minutes = (millisUntilFinished % 3600000) / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                TextTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            public void onFinish() {
                TextTimer.setText("Time's up! You can earn points!");
            }
        }.start();
    }


}
