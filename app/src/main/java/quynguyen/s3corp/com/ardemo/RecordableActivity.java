package quynguyen.s3corp.com.ardemo;

import android.icu.util.Calendar;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Record video uses lib and stream video: TODO
 * Lib: https://github.com/UncorkedStudios/recordablesurfaceview
 */
public class RecordableActivity extends AppCompatActivity
        implements RecordableSurfaceView.RendererCallbacks{
    private RecordableSurfaceView mSurfaceView;
    private Button btnRecord;
    private Session mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordable);

        mSurfaceView = findViewById(R.id.surfaceview);
        btnRecord = findViewById(R.id.btn_record);

        mSurfaceView.setRendererCallbacks(this);
        mSurfaceView.doSetup();
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mSession == null) {
                mSession = new Session(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSurfaceView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mRecordButton.isRecording()) {
//            mRecordButton.setRecording(false);
//        }

        // Note that the order matters - SurfaceView is paused first so that it does not try
        // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
        // still call mSession.update() and get a SessionPausedException.
        if (mSession != null) {
            mSession.pause();
        }
        mSurfaceView.pause();
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_video.mp4";
    }

    private File createVideoOutputFile() {

        File tempFile;


//        File dir = new File(getCacheDir(), "captures");
        File dir = new File(getCacheDir(), "captures");

        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        Calendar c = Calendar.getInstance();

        String filename = "ARCoreDemo" +
                c.get(Calendar.YEAR) + "-" +
                (c.get(Calendar.MONTH) + 1) + "-" +
                c.get(Calendar.DAY_OF_MONTH)
                + "_" +
                c.get(Calendar.HOUR_OF_DAY) +
                c.get(Calendar.MINUTE) +
                c.get(Calendar.SECOND);

//        tempFile = new File(dir, filename + ".mp4");
        tempFile = new File(generateFilename());

        return tempFile;

    }
    private boolean startRecording() {
        boolean startSuccessful = mSurfaceView.startRecording();

        if (startSuccessful) {
            Timber.d("Recording Started");
        } else {
            Toast.makeText(this, "Record failed", Toast.LENGTH_SHORT).show();
            prepareForRecording();
        }
        return startSuccessful;
    }

    private void prepareForRecording() {
        Timber.d( "prepareForRecording: ");
        try {
            File mOutputFile = createVideoOutputFile();
            android.graphics.Point size = new android.graphics.Point();
            getWindowManager().getDefaultDisplay().getRealSize(size);
            mSurfaceView.initRecorder(mOutputFile, size.x, size.y, null, null);

            // on some devices, this will not be on the UI thread when called from onSurfaceCreated
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mRecordButton.setEnabled(true);
//                }
//            });
        } catch (IOException ioex) {
            Timber.d("Couldn't setup recording"+ ioex);
            Timber.d("Error setting up recording");
        }


    }

    @Override
    public void onSurfaceCreated() {
        prepareForRecording();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {

    }

    @Override
    public void onSurfaceDestroyed() {

    }

    @Override
    public void onContextCreated() {

    }

    @Override
    public void onPreDrawFrame() {

    }

    @Override
    public void onDrawFrame() {
//        mRenderer.onDrawFrame(null);


    }
}
