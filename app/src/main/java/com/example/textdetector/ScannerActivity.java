package com.example.textdetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    private ImageView captureIV;
    private TextView resultTV ;
    private Button snapBtn, detectBtn;
    private Bitmap imageBitmap ;
    static final int REQUEST_IMG_CAPTURE = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        captureIV = findViewById(R.id.idVICaptureImage);
        resultTV = findViewById(R.id.idTVDetectedText) ;
        snapBtn = findViewById(R.id.idBtnSnap) ;
        detectBtn = findViewById(R.id.idBtnDetect) ;

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText() ;
            }
        });


        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    captureImage();
                }
                else {
                    requestPermission();
                }

            }
        });

    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA_SERVICE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED ;
    }

    private void requestPermission() {
        int PERMISSION_CODE = 200 ;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA_SERVICE}, PERMISSION_CODE);
    }

    private void captureImage() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMG_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraPermission  = grantResults[0] == PackageManager.PERMISSION_GRANTED ;

            if (true) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_LONG).show();
                captureImage();
            }
            else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMG_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras() ;
            imageBitmap = (Bitmap) extras.get("data") ;
            captureIV.setImageBitmap(imageBitmap);
        }
    }

    private void detectText() {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(@NonNull Text text) {
                        StringBuilder result = new StringBuilder();
                        for (Text.TextBlock block : text.getTextBlocks()) {
                            String blockText = block.getText();
                            Point[] blockCornerPoint = block.getCornerPoints();
                            Rect block_frame = block.getBoundingBox() ;
                            for (Text.Line line : block.getLines()) {
                                String getLineText = line.getText() ;
                                Point[] lineCornerPoints = line.getCornerPoints() ;
                                Rect lineRect = line.getBoundingBox();

                                System.out.println("ELEMENT TEXT: ");
                                for (Text.Element element : line.getElements()) {
                                    String elementText = element.getText() ;
                                    result.append(elementText);
                                    System.out.println("---------------------\n "+elementText+" \n----------------------");

                                }

                            }
                            System.out.println("---------------------\n "+result+" \n----------------------");
                            resultTV.setText(blockText);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScannerActivity.this, "Failed to Detect Text "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}