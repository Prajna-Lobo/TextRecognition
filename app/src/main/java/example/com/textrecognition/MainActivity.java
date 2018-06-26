package example.com.textrecognition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView;
    Button sendButton;
    CameraSource cameraSource;
    final int RequestPermissionID = 1001;
    private Button stopButton;
    private Button refreshButton;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode){
           case RequestPermissionID :
               if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                   }
                   try {
                       cameraSource.start(cameraView.getHolder());
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
       }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.textview);
        sendButton = (Button) findViewById(R.id.send_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        refreshButton = (Button) findViewById(R.id.refresh_button);

        textRecognizer();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, textView.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                sendIntent.setPackage("com.whatsapp");
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity","stop : ");
                cameraSource.stop();
                stopButton.setVisibility(View.GONE);   try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                refreshButton.setVisibility(View.VISIBLE);
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity","refresh : ");
                refreshButton.setVisibility(View.GONE);

                textRecognizer();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopButton.setVisibility(View.VISIBLE);


            }
        });
    }
    private void cameraForReconistion(TextRecognizer textRecognizer){
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},RequestPermissionID);
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if(items.size() != 0){
                    sendButton.setVisibility(View.VISIBLE);

                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            for(int i=0;i<items.size();i++){
                                TextBlock item = items.valueAt(i);
                                stringBuilder.append(item.getValue());
                                stringBuilder.append("\n");
                            }
                            textView.setText(stringBuilder.toString());
                        }
                    });

                }
            }
        });
    }

    private void textRecognizer(){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.d("MainActivity", "Detector dependencies are not yet available");
        } else {
            cameraForReconistion(textRecognizer);
        }
    }
}

