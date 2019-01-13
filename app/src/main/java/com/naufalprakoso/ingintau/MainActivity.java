package com.naufalprakoso.ingintau;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button chooseImage;
    ImageView image;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(MainActivity.this);
        chooseImage = findViewById(R.id.bt_choose_image);
        image = findViewById(R.id.iv_image);
        text = findViewById(R.id.tv_text);
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChooseImage();
            }
        });
    }

    public void setChooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (null != selectedImageUri) image.setImageURI(selectedImageUri);
                runTextRecognition(bitmap);
            }
        }
    }

    private void runTextRecognition(Bitmap mSelectedImage) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(
                new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText texts) {
                        processTextRecognitionResult(texts);
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.Block> blocks = texts.getBlocks();
        if (blocks.size() == 0) {
            text.setText("No text found!");
            return;
        }
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) sb.append(elements.get(k).getText()).append(" ");
                sb.append("\n");
            }
            sb.append("\n");
        }

        String[] dataSet = {"sabeb", "nich", "kuy", "afgan", "nego",
                "cmiiw", "oot", "ily", "idk", "fyi", "takis", "sabi",
                "sa ae", "yxgq"};
        String[] resultSet = {"bebas", "nih", "yuk", "sadis", "nawar",
                "correct me if im wrong", "out of topic", "i like you", "i dont know",
                "for your information", "sikat", "bisa", "bisa aja", "ya kali enggak"};

        String result = sb.toString();

        for (int i = 0; i < dataSet.length; i++){
            if (containsIgnoreCase(result, dataSet[i])){
                int idxStart = result.toLowerCase().indexOf(dataSet[i].toLowerCase());
//                Log.d("InfoConvert", result);
//                Log.d("InfoConvert", resultSet[i]);
//                Log.d("InfoConvert", dataSet[i]);
//                Log.d("InfoConvert", idxStart + "");
//                Log.d("InfoConvert", (idxStart + dataSet[i].length()) + "");
                sb.replace(idxStart, (idxStart + dataSet[i].length()), resultSet[i]);
            }
        }

        text.setText(sb.toString());
    }

    private static boolean containsIgnoreCase(String str, String searchStr) {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }
}

