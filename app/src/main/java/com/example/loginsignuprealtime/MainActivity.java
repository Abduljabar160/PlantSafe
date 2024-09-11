package com.example.loginsignuprealtime;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.loginsignuprealtime.R;
import com.example.loginsignuprealtime.ml.ModelUnquant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.schema.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    TextView result, confidence;
    ImageView imageView;
    Button picture;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch camera if we have permission
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
    }

    public void classifyImage(Bitmap image) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val & 0xFF)*(1.f/255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"\uD835\uDDE3\uD835\uDDF6\uD835\uDDFB\uD835\uDDF8 \uD835\uDDD7\uD835\uDDEE\uD835\uDDF9\uD835\uDDFA\uD835\uDDEE\uD835\uDE01\uD835\uDDF6\uD835\uDDEE\uD835\uDDFB : \uD835\uDDE3\uD835\uDDFC\uD835\uDDF6\uD835\uDE00\uD835\uDDFC\uD835\uDDFB\uD835\uDDFC\uD835\uDE02\uD835\uDE00 - is a houseplant with pink-speckled green leaves. It is poisonous because its sap contains calcium oxalate crystals, which can cause irritation and swelling if ingested or if it comes into contact with the skin.\n \n"+
                    "\uD835\uDDD9\uD835\uDDF6\uD835\uDDFF\uD835\uDE00\uD835\uDE01 \uD835\uDDD4\uD835\uDDF6\uD835\uDDF1 \uD835\uDDE7\uD835\uDDFF\uD835\uDDF2\uD835\uDDEE\uD835\uDE01\uD835\uDDFA\uD835\uDDF2\uD835\uDDFB\uD835\uDE01 \n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFED. Wash the affected area with soap and water.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEE. Apply a soothing lotion or cream to reduce irritation.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEF. If itching persists, consult a doctor.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFF0. Avoid touching eyes or sensitive areas after contact." , "\uD835\uDDD6\uD835\uDDFC\uD835\uDDFF\uD835\uDDFB\uD835\uDDFD\uD835\uDDF9\uD835\uDDEE\uD835\uDDFB\uD835\uDE01 : \uD835\uDDE3\uD835\uDDFC\uD835\uDDF6\uD835\uDE00\uD835\uDDFC\uD835\uDDFB\uD835\uDDFC\uD835\uDE02\uD835\uDE00 - also known as Dracaena fragrans, is toxic to pets and humans. Its leaves contain saponins, which can cause vomiting, drooling, and loss of appetite if ingested. Keep it out of reach of children and pets to avoid poisoning incidents.\n \n" +
                    "\uD835\uDDD9\uD835\uDDF6\uD835\uDDFF\uD835\uDE00\uD835\uDE01 \uD835\uDDD4\uD835\uDDF6\uD835\uDDF1 \uD835\uDDE7\uD835\uDDFF\uD835\uDDF2\uD835\uDDEE\uD835\uDE01\uD835\uDDFA\uD835\uDDF2\uD835\uDDFB\uD835\uDE01 \n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFED. Move away: Get the person away from the corn plant to prevent further exposure.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEE. Wash affected area: Rinse the infected area with soap and water immediately.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEF. Remove any plant parts: Carefully remove any plant parts stuck in the skin.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFF0. Apply antiseptic: Put an antiseptic or antibacterial ointment on the wound.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFF1. Cover the wound: Use a clean bandage to cover the wound to prevent infection.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFF2. Seek medical help: If the infection seems severe or if there's any doubt, seek medical attention promptly." , "\uD835\uDDD6\uD835\uDDF5\uD835\uDDF6\uD835\uDDFB\uD835\uDDF2\uD835\uDE00\uD835\uDDF2 \uD835\uDDD8\uD835\uDE03\uD835\uDDF2\uD835\uDDFF\uD835\uDDF4\uD835\uDDFF\uD835\uDDF2\uD835\uDDF2\uD835\uDDFB : \uD835\uDDE3\uD835\uDDFC\uD835\uDDF6\uD835\uDE00\uD835\uDDFC\uD835\uDDFB\uD835\uDDFC\uD835\uDE02\uD835\uDE00 - is a popular indoor plant with colorful, patterned leaves. It is poisonous because it contains calcium oxalate crystals, which can cause irritation and swelling if ingested or if they come into contact with skin\n \n" +
                    "\uD835\uDDD9\uD835\uDDF6\uD835\uDDFF\uD835\uDE00\uD835\uDE01 \uD835\uDDD4\uD835\uDDF6\uD835\uDDF1 \uD835\uDDE7\uD835\uDDFF\uD835\uDDF2\uD835\uDDEE\uD835\uDE01\uD835\uDDFA\uD835\uDDF2\uD835\uDDFB\uD835\uDE01 \n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFED. Stay Calm: Keep the person calm and still.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEE. Rinse Mouth: Have them rinse their mouth with water.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEF. Don't Induce Vomiting: Do not make them vomit.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFF0. Give Water or Milk: If conscious, give small sips of water or milk.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFF1. Seek Medical Help: Contact poison control or go to the emergency room.", "\uD835\uDDE3\uD835\uDE02\uD835\uDDFF\uD835\uDDFD\uD835\uDDF9\uD835\uDDF2 \uD835\uDDDB\uD835\uDDF2\uD835\uDDEE\uD835\uDDFF\uD835\uDE01\uD835\uDE00 : \uD835\uDDE3\uD835\uDDFC\uD835\uDDF6\uD835\uDE00\uD835\uDDFC\uD835\uDDFB\uD835\uDDFC\uD835\uDE02\uD835\uDE00 -  is a houseplant with pink-speckled green leaves. It is poisonous because its sap contains calcium oxalate crystals, which can cause irritation and swelling if ingested or if it comes into contact with the skin.\n \n" +
                    "\uD835\uDDD9\uD835\uDDF6\uD835\uDDFF\uD835\uDE00\uD835\uDE01 \uD835\uDDD4\uD835\uDDF6\uD835\uDDF1 \uD835\uDDE7\uD835\uDDFF\uD835\uDDF2\uD835\uDDEE\uD835\uDE01\uD835\uDDFA\uD835\uDDF2\uD835\uDDFB\uD835\uDE01 \n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFED. Wash the affected area with soap and water.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEE. Apply a soothing lotion or cream to reduce irritation.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFEF. If itching persists, consult a doctor.\n\n" +
                    "\uD835\uDDE6\uD835\uDE01\uD835\uDDF2\uD835\uDDFD \uD835\uDFF0. Avoid touching eyes or sensitive areas after contact.", "Unknown - \uD835\uDDE7\uD835\uDDDB\uD835\uDDD8 \uD835\uDDD4\uD835\uDDE3\uD835\uDDE3 \uD835\uDDD6\uD835\uDDD4\uD835\uDDE1'\uD835\uDDE7 \uD835\uDDD7\uD835\uDDD8\uD835\uDDE7\uD835\uDDD8\uD835\uDDD6\uD835\uDDE7 \uD835\uDDE7\uD835\uDDDB\uD835\uDDD8 \uD835\uDDE3\uD835\uDDDF\uD835\uDDD4\uD835\uDDE1\uD835\uDDE7" };

            result.setText(classes[maxPos]);

            String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }

            confidence.setText("Tamag, Vigan, Ilocos Sur ");

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(),image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image,dimension,dimension);
            imageView.setImageBitmap(image);

            image = Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
            classifyImage(image);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}