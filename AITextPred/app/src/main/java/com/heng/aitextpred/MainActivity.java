package com.heng.aitextpred;

/*
21/12/2019 Heng Initial created APP template.
21/12/2019 Heng Initial Integrated TensorFlow Interface.
21/12/2019 Heng Initial Added ObjectDetector.
4/1/2020   Heng Initial added with camera.
4/1/2020   Heng Initial added with Image Classification
*/

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.Bundle;


import android.graphics.Bitmap;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progress;
    private static final String MODEL_PATH = "optimized_graph.lite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "output_labels.txt";
    private static final int INPUT_SIZE = 224;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnCaptureObject;
    private ImageView imageViewResult;
    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);
        imageViewResult = findViewById(R.id.imageViewResult);
        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnCaptureObject = findViewById(R.id.btnCaptureObject);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                String test = results.toString();
                String s=test.substring(1,4);
                textViewResult.setText(results.toString());
              /*  if(s.equals("man") || s.equals("ram")|| s.equals("dur")){
                       textViewResult.setText(results.toString());
                }
                else{
                   textViewResult.setText("Sorry, this image not yet includes on our DataBase");
                }*/
                progress.dismiss();
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });



        btnCaptureObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
                showLoadingDialog();
            }
        });

        initTensorFlowAndLoadModel();
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnCaptureObject.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showLoadingDialog() {

        if (progress == null) {
            progress = new ProgressDialog(this);
            progress.setTitle("Image Classification");
            progress.setMessage("Checking....");
        }
        progress.show();
    }

    public void dismissLoadingDialog() {

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    public class ObjectDetector {
        private String labelFilename;
        private String modelFilename;
        private List<String> labels = new ArrayList<>();
        private AssetManager assetManager;
        private TensorFlowInferenceInterface inferenceInterface;

        public ObjectDetector(String labelFileName, String modelFileName, AssetManager assetManager) {
            this.labelFilename = labelFileName;
            this.modelFilename = modelFileName;
            this.assetManager = assetManager;
        }

        public void load() throws IOException {
            InputStream labelsInput = assetManager.open(labelFilename);
            BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();
            if (inferenceInterface != null) {
                inferenceInterface.close();
            }
            inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        }
    }

}

