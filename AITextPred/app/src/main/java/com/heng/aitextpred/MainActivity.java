package com.heng.aitextpred;
/*
21/12/2019 Heng Initial created APP template.
21/12/2019 Heng Initial Integrated TensorFlow Interface.
21/12/2019 Heng Initial Added ObjectDetector.
*/
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
