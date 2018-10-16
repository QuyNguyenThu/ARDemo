package quynguyen.s3corp.com.ardemo;

import android.graphics.Point;
import android.media.MediaRecorder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Place Andy object on Plane
 */
public class ARFragmentActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);


        arFragment.getArSceneView().getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
                                                                       @Override
                                                                       public void onUpdate(FrameTime frameTime) {
                                                                           arFragment.onUpdate(frameTime);
                                                                           onUpdateScreen();
                                                                           Timber.d("FrameTime: " +frameTime.toString());

                                                                       }
                                                                   });
//        arFragment.setOnTapArPlaneListener(
//                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
//                    if (andyRenderable == null) {
//                        return;
//                    }
//
//                    // Create the Anchor.
//                    Anchor anchor = hitResult.createAnchor();
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//                    // Create the transformable andy and add it to the anchor.
//                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//                    andy.setParent(anchorNode);
//                    andy.setRenderable(andyRenderable);
//                    andy.select();
//                });


        // Set the onclick lister for our button
        // Change this string to point to the .sfb file of your choice :)
//        floatingActionButton.setOnClickListener { addObject(Uri.parse("NOVELO_EARTH.sfb")) }
//
//
        // hiding the plane discovery
//        arFragment.getPlaneDiscoveryController().hide();
//        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
//                .setSource(this, R.raw.heart)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
//        arFragment.onPeekTouch();

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {

                    Timber.d("Hitresul: " + hitResult);
                    Timber.d("Plane: " + plane);
                    Timber.d("motionEvent: " + motionEvent);
                    if (andyRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                    andy.select();
                });

        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile("/Images/");
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoSize(arFragment.getArSceneView().getWidth(), arFragment.getArSceneView().getHeight());
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoEncodingBitRate(10000000);
//        mediaRecorder.setInputSurface(arFragment.getArSceneView().getHolder().getSurface());//
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Boolean isTracking = false;
    private Boolean isHitting = false;
    // Updates the tracking state
    private void onUpdateScreen() {
        updateTracking();
        // Check if the devices gaze is hitting a plane detected by ARCore
        Timber.d("getView: " +arFragment.getArSceneView().getScene().getView());

        if (isTracking) {
            Boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                //TODO
            }
        }

    }

    // Simply returns the center of the screen
    private Point getScreenCenter() {
        View view = findViewById(android.R.id.content);
        return new Point(view.getWidth() / 2, view.getHeight() / 2);
    }

    // Performs frame.HitTest and returns if a hit is detected
    private Boolean updateHitTest()  {
        Frame frame = arFragment.getArSceneView().getArFrame();
        Point point = getScreenCenter();
        List<HitResult> hits = new ArrayList<>();
        Boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(point.x, point.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    // Makes use of ARCore's camera state and returns true if the tracking state has changed
    private Boolean updateTracking() {
        Frame frame = arFragment.getArSceneView().getArFrame();
        Timber.d("Frame: " +frame);
        Timber.d("Frame1: " + frame.toString());
        Timber.d("Frame12: " + new Gson().toJson(frame.getCamera().getPose()));
        try {
//            Timber.d("Frame123: " + new Gson().toJson(frame.getImageMetadata()));
//            Timber.d("Frame123: " + new Gson().toJson(frame.getCamera()));
            Timber.d("Frame1234: " + new Gson().toJson(frame.getAndroidSensorPose()));
            Timber.d("Frame12345: " + new Gson().toJson(frame.getUpdatedAnchors()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Boolean wasTracking = isTracking;
        isTracking = frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }
}
