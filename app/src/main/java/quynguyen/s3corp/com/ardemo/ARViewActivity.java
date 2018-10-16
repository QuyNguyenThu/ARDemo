package quynguyen.s3corp.com.ardemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;

import timber.log.Timber;

/**
 * can draw line
 */
public class ARViewActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private Snackbar loadingMessageSnackbar = null;

    private ArSceneView arSceneView;
    private ProgressBar progress;
    private ModelRenderable andyRenderable, heartRenderable, cabinRenderable, homeRendarable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ar_view_main);

        arSceneView = findViewById(R.id.ar_scene_view);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
//        if (arSceneView.getSession() == null) {
//            // If the session wasn't created yet, don't resume rendering.
//            // This can happen if ARCore needs to be updated or permissions are not granted yet.
//
//            try {
//                arSceneView.setupSession(new Session(this));
//            } catch (UnavailableArcoreNotInstalledException e) {
//                e.printStackTrace();
//            } catch (UnavailableApkTooOldException e) {
//                e.printStackTrace();
//            } catch (UnavailableSdkTooOldException e) {
//                e.printStackTrace();
//            }
//
//        }


        ModelRenderable.builder()
                .setSource(this, R.raw.heart)
//                .setSource(this, R.raw.heart)
                .build()
                .thenAccept(renderable -> heartRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        ModelRenderable.builder()
                .setSource(this, R.raw.house)
//                .setSource(this, R.raw.heart)
                .build()
                .thenAccept(renderable -> homeRendarable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        ModelRenderable.builder()
                .setSource(this, R.raw.cabin)
//                .setSource(this, R.raw.heart)
                .build()
                .thenAccept(renderable -> cabinRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
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
                            progress.setVisibility(View.GONE);
                            return null;
                        });
        // Set up a tap gesture detector.
        gestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                onSingleTap(e);
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });

        // Set a touch listener on the Scene to listen for taps.
        arSceneView
                .getScene()
                .setOnTouchListener(
                        (HitTestResult hitTestResult, MotionEvent event) -> {
                            // If the solar system hasn't been placed yet, detect a tap and then check to see if
                            // the tap occurred on an ARCore plane to place the solar system.

                            return gestureDetector.onTouchEvent(event);

                        });

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            if (loadingMessageSnackbar == null) {
                                return;
                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                if (plane.getTrackingState() == TrackingState.TRACKING) {
                                    //hideLoadingMessage();
                                }
                            }
                        });
    }
    private Session session;
    @Override
    public void onResume() {
        super.onResume();

        if(session==null)
        {
            //Toast.makeText(HelloSceneformActivity.this,"Session is null",Toast.LENGTH_SHORT).show();
            try {
                session = new Session(/* context= */ this);
            } catch (UnavailableArcoreNotInstalledException e) {
                e.printStackTrace();
            } catch (UnavailableApkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableSdkTooOldException e) {
                e.printStackTrace();
            }

            // Create default config and check if supported.
            Config config = new Config(session);
            if (!session.isSupported(config)) {
                //                showSnackbarMessage("This device does not support AR", true);
            }
            config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            session.configure(config);
        }
        if (arSceneView == null) {
            return;
        } else{
            arSceneView.setupSession(session);
        }




        try {
            arSceneView.resume();

        } catch (CameraNotAvailableException ex) {
//            DemoUtils.displayError(this, "Unable to get camera", ex);
//            finish();
            return;
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        if(session!=null)
        {
            session.pause();
        }
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (arSceneView != null) {
            arSceneView.destroy();
        }
    }


    private void onSingleTap(MotionEvent tap) {
//        if (!hasFinishedLoading) {
//            // We can't do anything yet.
//            return;
//        }
      Timber.d("Single tap");
        Frame frame = arSceneView.getArFrame();
        if (frame != null) {
            tryPlaceSolarSystem(tap, frame);
        }
    }

    private boolean tryPlaceSolarSystem(MotionEvent tap, Frame frame) {
        Timber.d("Single tap "+ tap);
        Timber.d("Single tap "+ frame.getCamera().getTrackingState());

        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            if(frame.hitTest(tap) == null || frame.hitTest(tap).isEmpty()) {
//                frame.getAndroidSensorPose();
//
//                Timber.d("Single tap render");
//                //if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
////                    // Create the Anchor.
////                    Anchor anchor = hit.createAnchor();
////                    AnchorNode anchorNode = new AnchorNode(anchor);
////                    anchorNode.setParent(arSceneView.getScene());
////                    Node solarSystem = createSolarSystem();
////                    anchorNode.addChild(solarSystem);
//
//                // Create the Anchor.
//                Anchor anchor = hit.createAnchor();
//                AnchorNode anchorNode = new AnchorNode(anchor);
//                anchorNode.setParent(arSceneView.getScene());
//                anchorNode.setRenderable(andyRenderable);
//                Timber.d("Single tap render POSE");
//                Pose pose = arSceneView.getArFrame().getCamera().getPose();
//                AnchorNode anchorNode2 = new AnchorNode(arSceneView.getSession().createAnchor(pose));
//                anchorNode2.setParent(arSceneView.getScene());
//                anchorNode2.setRenderable(andyRenderable);
            } else {

                for (HitResult hit : frame.hitTest(tap)) {
                    Trackable trackable = hit.getTrackable();
                    Timber.d("Single tap render " + trackable);
                    //if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
//                    // Create the Anchor.
//                    Anchor anchor = hit.createAnchor();
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arSceneView.getScene());
//                    Node solarSystem = createSolarSystem();
//                    anchorNode.addChild(solarSystem);

                    // Create the Anchor.
//                    Anchor anchor = hit.createAnchor();
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arSceneView.getScene());
//                    anchorNode.setRenderable(andyRenderable);
//
//
                    // Create the transformable andy and add it to the anchor.
//                    TransformableNode andy = new TransformableNode(arSceneView..getTransformationSystem());
//                    andy.setParent(anchorNode);
//                    andy.setRenderable(andyRenderable);
//                    andy.select();

//                    Node node = new Node();
//                    node.setParent(anchorNode);
//                    node.setRenderable(andyRenderable);

//                    return true;
                    //}

                    if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
//                        Plane plane = (Plane) trackable;
//                        // Create the Anchor.
//                    Anchor anchor = hit.createAnchor();
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arSceneView.getScene());
//                    anchorNode.setRenderable(andyRenderable);
                        Timber.d("Single tap render Plane ");
                        // Handle plane hits.
                        break;
                    } else if (trackable instanceof Point) {
                        // Handle point hits
                        Point point = (Point) trackable;
                        Timber.d("Single tap render Point " + point);

                        Pose pose = frame.getCamera().getPose();
                        AnchorNode anchorNode2 = new AnchorNode(arSceneView.getSession().createAnchor(pose));
                        anchorNode2.setParent(arSceneView.getScene());
                        anchorNode2.setRenderable(andyRenderable);

//                        Anchor anchor1 = point.createAnchor(
//                                frame.getCamera().getPose()
//                                        .compose(Pose.makeTranslation(0, 0.02f, -1f))
//                                        .extractTranslation());
//                        AnchorNode anchorNode1 = new AnchorNode(anchor1);
//                        anchorNode1.setParent(arSceneView.getScene());
//                        anchorNode1.setRenderable(andyRenderable);
                        // Create the transformable andy and add it to the anchor.
//                    TransformableNode andy = new TransformableNode(arSceneView..getTransformationSystem());
//                    andy.setParent(anchorNode);
//                    andy.setRenderable(andyRenderable);
//                    andy.select();
//
//                    Node node1 = new Node();
//                    node1.setParent(anchorNode1);
//                    node1.setRenderable(heartRenderable);

                    } else if (trackable instanceof AugmentedImage) {
                        // Handle image hits.
                        AugmentedImage image = (AugmentedImage) trackable;
                    }
                    return true;
                }
            }
        }
        return false;
    }
//
//    private Node createSolarSystem() {
//        Node base = new Node();
//
//        Node sun = new Node();
//        sun.setParent(base);
//        sun.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));
//
//        Node sunVisual = new Node();
//        sunVisual.setParent(sun);
//        sunVisual.setRenderable(sunRenderable);
//        sunVisual.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
//
//        Node solarControls = new Node();
//        solarControls.setParent(sun);
//        solarControls.setRenderable(solarControlsRenderable);
//        solarControls.setLocalPosition(new Vector3(0.0f, 0.25f, 0.0f));
//
//        View solarControlsView = solarControlsRenderable.getView();
//        SeekBar orbitSpeedBar = solarControlsView.findViewById(R.id.orbitSpeedBar);
//        orbitSpeedBar.setProgress((int) (solarSettings.getOrbitSpeedMultiplier() * 10.0f));
//        orbitSpeedBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        float ratio = (float) progress / (float) orbitSpeedBar.getMax();
//                        solarSettings.setOrbitSpeedMultiplier(ratio * 10.0f);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {}
//                });
//
//        SeekBar rotationSpeedBar = solarControlsView.findViewById(R.id.rotationSpeedBar);
//        rotationSpeedBar.setProgress((int) (solarSettings.getRotationSpeedMultiplier() * 10.0f));
//        rotationSpeedBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        float ratio = (float) progress / (float) rotationSpeedBar.getMax();
//                        solarSettings.setRotationSpeedMultiplier(ratio * 10.0f);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {}
//                });
//
//        // Toggle the solar controls on and off by tapping the sun.
//        sunVisual.setOnTapListener(
//                (hitTestResult, motionEvent) -> solarControls.setEnabled(!solarControls.isEnabled()));
//
//        createPlanet("Mercury", sun, 0.4f, 47f, mercuryRenderable, 0.019f);
//
//        createPlanet("Venus", sun, 0.7f, 35f, venusRenderable, 0.0475f);
//
//        Node earth = createPlanet("Earth", sun, 1.0f, 29f, earthRenderable, 0.05f);
//
//        createPlanet("Moon", earth, 0.15f, 100f, lunaRenderable, 0.018f);
//
//        createPlanet("Mars", sun, 1.5f, 24f, marsRenderable, 0.0265f);
//
//        createPlanet("Jupiter", sun, 2.2f, 13f, jupiterRenderable, 0.16f);
//
//        createPlanet("Saturn", sun, 3.5f, 9f, saturnRenderable, 0.1325f);
//
//        createPlanet("Uranus", sun, 5.2f, 7f, uranusRenderable, 0.1f);
//
//        createPlanet("Neptune", sun, 6.1f, 5f, neptuneRenderable, 0.074f);
//
//        return base;
//    }
//
//    private Node createPlanet(
//            String name,
//            Node parent,
//            float auFromParent,
//            float orbitDegreesPerSecond,
//            ModelRenderable renderable,
//            float planetScale) {
//        // Orbit is a rotating node with no renderable positioned at the sun.
//        // The planet is positioned relative to the orbit so that it appears to rotate around the sun.
//        // This is done instead of making the sun rotate so each planet can orbit at its own speed.
//        RotatingNode orbit = new RotatingNode(solarSettings, true);
//        orbit.setDegreesPerSecond(orbitDegreesPerSecond);
//        orbit.setParent(parent);
//
//        // Create the planet and position it relative to the sun.
//        Planet planet = new Planet(this, name, planetScale, renderable, solarSettings);
//        planet.setParent(orbit);
//        planet.setLocalPosition(new Vector3(auFromParent * AU_TO_METERS, 0.0f, 0.0f));
//
//        return planet;
//    }
}
