package quynguyen.s3corp.com.ardemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.ar.core.Config;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import quynguyen.s3corp.com.ardemo.pojo.Stroke;
import timber.log.Timber;

/**
 * Snapshot screen and stream: Can snap image by PixelCopy but lag.
 * can draw line and place Andy object on ARSceneView
 * refer: https://codelabs.developers.google.com/codelabs/sceneform-intro/index.html?index=..%2F..%2Fio2018#14
 */
public class PixelCopyAtivity extends AppCompatActivity
        implements Scene.OnUpdateListener, Scene.OnPeekTouchListener{

    private ArFragment arFragment;
    private ArSceneView arSceneView;
    private ProgressBar progress;
    private ImageView btnClearAll, btnDrawLine, btnAndy;

    private ModelRenderable andyRenderable;
    private ViewRenderable closeControlsRenderable;
    private static final float DRAW_DISTANCE = 0.10f;
    private static final Color WHITE = new Color(android.graphics.Color.WHITE);
    private static final Color RED = new Color(android.graphics.Color.RED);
    private static final Color GREEN = new Color(android.graphics.Color.GREEN);
    private static final Color BLUE = new Color(android.graphics.Color.BLUE);
    private static final Color BLACK = new Color(android.graphics.Color.BLACK);

    private ArFragment fragment;
    private AnchorNode anchorNode;
    private AnchorNode anchorNodeAndy;
    private final ArrayList<Stroke> strokes = new ArrayList<>();
    private Material material;
    private Stroke currentStroke;

    private Session session;
    private boolean isDrawingLine, isDrawingAndy;
    private ModelRenderable houseRenderable;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pixel_copy_ativity);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        arSceneView = arFragment.getArSceneView();
//        arSceneView = findViewById(R.id.ar_scene_view);
        MaterialFactory.makeOpaqueWithColor(this, RED)
                .thenAccept(material1 -> material = material1.makeCopy())
                .exceptionally(
                        throwable -> {
                            //displayError(throwable);
                            throw new CompletionException(throwable);
                        });

//        arSceneView.getPlaneRenderer().setEnabled(false);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arSceneView.getScene().addOnUpdateListener(this);
        arSceneView.getScene().addOnPeekTouchListener(this);

        progress = findViewById(R.id.progress);
        btnClearAll = findViewById(R.id.clearButton);
        btnDrawLine = findViewById(R.id.redCircle);
        btnAndy = findViewById(R.id.andyButton);
        isDrawingLine = true;
        btnClearAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (Stroke stroke : strokes) {
                            stroke.clear();
                        }
                        strokes.clear();
                    }
                });
        btnDrawLine.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isDrawingLine = true;
                        isDrawingAndy = false;
                    }
                });
        btnAndy.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isDrawingAndy = true;
                        isDrawingLine =false;
//                        if (strokes.size() < 1) {
//                            return;
//                        }
//                        int lastIndex = strokes.size() - 1;
//                        strokes.get(lastIndex).clear();
//                        strokes.remove(lastIndex);
                    }
                });
//        ModelRenderable.builder()
//                .setSource(this, R.raw.andy)
////                .setSource(this, R.raw.heart)
//                .build()
//                .thenAccept(renderable -> andyRenderable = renderable)
//                .exceptionally(
//                        throwable -> {
//                            Toast toast =
//                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
//                            progress.setVisibility(View.GONE);
//                            return null;
//                        });

        CompletableFuture<ModelRenderable> andyRenderStage =
                ModelRenderable.builder().setSource(this, Uri.parse("andy.sfb")).build();
        // Build a renderable from a 2D View.
        CompletableFuture<ViewRenderable> closeControlsStage =
                ViewRenderable.builder().setView(this, R.layout.view_button_close).build();

        CompletableFuture.allOf(
                andyRenderStage,
                closeControlsStage)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                throwable.printStackTrace();
                                progress.setVisibility(View.GONE);
                                return null;
                            }
                            try {
                                andyRenderable = andyRenderStage.get();
                                closeControlsRenderable = closeControlsStage.get();
                            } catch (InterruptedException | ExecutionException ex) {
                                //DemoUtils.displayError(this, "Unable to load renderable", ex);
                                Timber.d( "Unable to load closeControlsRenderable  renderable");
                                Toast.makeText(this, "TODO: handle exception " + ex, Toast.LENGTH_LONG)
                                        .show();
                                ex.printStackTrace();
                            }
                            progress.setVisibility(View.GONE);
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
//                .setSource(this, R.raw.heart)
                .build()
                .thenAccept(renderable -> houseRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load  houseRenderable renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            progress.setVisibility(View.GONE);
                            return null;
                        });


    }
    Boolean isPlacedAndy = false;

    @Override
    public void onPeekTouch(HitTestResult hitTestResult, MotionEvent tap) {
        int action = tap.getAction();
        Camera camera = arSceneView.getScene().getCamera();
        Ray ray = camera.screenPointToRay(tap.getX(), tap.getY());
        Vector3 drawPoint = ray.getPoint(DRAW_DISTANCE);
        if(isDrawingLine) {
            Timber.d("Choose place isDrawingLine");
            if (action == MotionEvent.ACTION_DOWN) {
                if (anchorNode == null) {
                    com.google.ar.core.Camera coreCamera = arSceneView.getArFrame().getCamera();
                    if (coreCamera.getTrackingState() != TrackingState.TRACKING) {
                        return;
                    }
                    Pose pose = coreCamera.getPose();
                    anchorNode = new AnchorNode(arSceneView.getSession().createAnchor(pose));
                    anchorNode.setParent(arSceneView.getScene());
                }
                currentStroke = new Stroke(anchorNode, material);
                strokes.add(currentStroke);
                currentStroke.add(drawPoint);
            } else if (action == MotionEvent.ACTION_MOVE && currentStroke != null) {
                currentStroke.add(drawPoint);
            } else if (action == MotionEvent.ACTION_UP) {
//                Ray rayAndy = camera.screenPointToRay(tap.getX(), tap.getY());
//
//                Vector3 drawPointAndy = ray.getPoint(DRAW_DISTANCE);
//                com.google.ar.core.Camera coreCamera = arSceneView.getArFrame().getCamera();
////                Timber.d("Choose place Andy UP "+ coreCamera.getTrackingState() );
////                Pose pose = coreCamera.getPose();
////                if (coreCamera.getTrackingState() != TrackingState.TRACKING) {
////                    return;
////                }
//                Timber.d("Choose place Andy UP "+andyRenderable);
//                Timber.d("Choose place Andy UP "+houseRenderable);
//                Pose pose = Pose.makeTranslation(drawPointAndy.x, drawPointAndy.y, drawPointAndy.z);
//                AnchorNode anchorNodeAndy = new AnchorNode(arSceneView.getSession().createAnchor(pose));
//                anchorNodeAndy.setParent(arSceneView.getScene());
//                anchorNodeAndy.setRenderable(houseRenderable);
            }
        } else if(isDrawingAndy){
            Timber.d("Choose place Andy");
            if (action == MotionEvent.ACTION_UP) {
                if(!isPlacedAndy) {
                    Ray rayAndy = camera.screenPointToRay(tap.getX(), tap.getY());

                    Vector3 drawPointAndy = ray.getPoint(DRAW_DISTANCE);
                    com.google.ar.core.Camera coreCamera = arSceneView.getArFrame().getCamera();
//                Timber.d("Choose place Andy UP "+ coreCamera.getTrackingState() );
//                Pose pose = coreCamera.getPose();
//                if (coreCamera.getTrackingState() != TrackingState.TRACKING) {
//                    return;
//                }
                    Timber.d("Choose place Andy UP UP1: " + houseRenderable);
                    Timber.d("Choose place Andy UP UP2: " + andyRenderable);
                    Timber.d("Choose place Andy UP UP2: " + closeControlsRenderable);
                    Pose pose = Pose.makeTranslation(drawPointAndy.x, drawPointAndy.y, drawPointAndy.z).extractTranslation();
//                Pose pose = Pose.makeTranslation(drawPointAndy.x, drawPointAndy.y, -1f).extractTranslation().;
//                AnchorNode anchorNodeAndy = new AnchorNode(arSceneView.getSession().createAnchor(pose));
                    AnchorNode anchorNodeAndy = new AnchorNode(arSceneView.getSession().createAnchor(coreCamera.getPose()));
//                anchorNodeAndy.setLocalPosition(drawPointAndy);
                    anchorNodeAndy.setParent(arSceneView.getScene());
//                anchorNodeAndy.setRenderable(andyRenderable);

                    TransformationSystem transformationSystem = arFragment.getTransformationSystem();
                    TransformableNode andy = new TransformableNode(transformationSystem);
//                    andy.setParent(anchorNodeAndy);
                    andy.setRenderable(andyRenderable);
                    andy.select();
                    anchorNodeAndy.setOnTapListener((final HitTestResult hitTestResult1, MotionEvent motionEvent) -> {
                        // Hit test point will be where the ray from the screen point intersects the model.

                        // Create a sphere and attach it to the point.
                        Color color = new Color(.8f, 0, 0);

                        // Note: you can make one material and one sphere and reuse them.
                        MaterialFactory.makeOpaqueWithColor(this, color)
                                .thenAccept(material -> {
                                    // The sphere is in local coordinate space, so make the center 0,0,0
                                    Renderable sphere = ShapeFactory.makeSphere(0.05f, Vector3.zero(),
                                            material);

                                    Node indicatorModel = new Node();
                                    indicatorModel.setParent(hitTestResult1.getNode());
                                    indicatorModel.setWorldPosition(hitTestResult1.getPoint());
                                    indicatorModel.setRenderable(sphere);
                                });
                    });
                    isPlacedAndy = true;
                }
            }

        } else{
            Timber.d("Choose nothing");
        }
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        com.google.ar.core.Camera camera = arSceneView.getArFrame().getCamera();
        if (camera.getTrackingState() == TrackingState.TRACKING) {
//            fragment.getPlaneDiscoveryController().hide();
            final String filename = generateFilename();
            ArSceneView view = arSceneView;

            // Create a bitmap the size of the scene view.
            final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                    Bitmap.Config.ARGB_8888);

            // Create a handler thread to offload the processing of the image.
            final HandlerThread handlerThread = new HandlerThread("PixelCopier");
            handlerThread.start();
            // Make the request to copy.
            PixelCopy.request(view, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    try {
                        saveBitmapToDisk(bitmap, filename);
                    } catch (IOException e) {
                        Toast toast = Toast.makeText(PixelCopyAtivity.this, e.toString(),
                                Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }
//                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
//                            "Photo saved", Snackbar.LENGTH_LONG);
//                    snackbar.setAction("Open in Photos", v -> {
//                        File photoFile = new File(filename);
//
//                        Uri photoURI = FileProvider.getUriForFile(PixelCopyAtivity.this,
//                                PixelCopyAtivity.this.getPackageName() + ".ar.codelab.name.provider",
//                                photoFile);
//                        Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
//                        intent.setDataAndType(photoURI, "image/*");
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        startActivity(intent);
//
//                    });
//                    snackbar.show();
                } else {
                    Toast toast = Toast.makeText(PixelCopyAtivity.this,
                            "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                    toast.show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        }
    }
    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }


}
