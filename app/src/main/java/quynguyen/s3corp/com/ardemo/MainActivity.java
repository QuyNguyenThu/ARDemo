package quynguyen.s3corp.com.ardemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    Button btnARFragment,btnARView;
    Button btnARPixel,btnARRecordable;
    TextView tvMessage;
    // Set to true ensures requestInstall() triggers installation if necessary.
    private boolean mUserRequestedInstall = true;
    private Session mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        btnARFragment = (Button) findViewById(R.id.btn_ar_fragment);
        btnARView = (Button) findViewById(R.id.btn_ar_view);
        btnARPixel = (Button) findViewById(R.id.btn_ar_image);
        btnARRecordable = (Button) findViewById(R.id.btn_ar_video);
        tvMessage = (TextView) findViewById(R.id.tv_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        btnARFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Open AR Fragment", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goToARFragmentActivity();

            }


        });
        btnARView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Open AR View", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goToARViewActivity();
            }
        });

        btnARPixel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Open AR Fragment", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goToARPixelActivity();

            }


        });

        btnARRecordable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Open AR Fragment", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goToARRecordtActivity();

            }


        });

        checkARSupport();
    }

    private void goToARFragmentActivity() {
        Intent intent = new Intent(this, ARFragmentActivity.class);
        startActivity(intent);
    }
    private void goToARViewActivity() {
        Intent intent = new Intent(this, ARViewActivity.class);
        startActivity(intent);
    }
    private void goToARPixelActivity() {
        Intent intent = new Intent(this, PixelCopyAtivity.class);
        startActivity(intent);
    }
    private void goToARRecordtActivity() {
        Intent intent = new Intent(this, RecordableActivity.class);
        startActivity(intent);
    }
    private void checkARSupport() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkARSupport();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            tvMessage.setText("Yeah! It's supported AR");
            btnARFragment.setEnabled(true);
            btnARView.setEnabled(true);
            // indicator on the button.
        } else { // Unsupported or unknown.
            tvMessage.setText("Ooops! It isn't supported AR");
            btnARFragment.setEnabled(false);
            btnARView.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ARCore requires camera permission to operate.
        if (!CameraPermissionHelper.hasPermissions(this)) {
            CameraPermissionHelper.requestPermissions(this);
            return;
        }

        // Make sure ARCore is installed and up to date.
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success, create the AR session.
                        mSession = new Session(this);
                        break;
                    case INSTALL_REQUESTED:
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedInstall = false;
                        return;
                }
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
            return;
        } catch (Exception e1) {  // Current catch statements.
            e1.printStackTrace();
            return;  // mSession is still null.
        }
    }

    public Session getSession(){
        return mSession;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!CameraPermissionHelper.hasPermissions(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    public void bindFragment(Class fragmentClass, Bundle args, String tag) {
        Fragment fragment;
        if (fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                if(args != null){
                    fragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment, tag)
                .addToBackStack(fragment.getClass().getSimpleName()).commit();
                Timber.d("bindFragment fragmentClass : " + fragmentClass.getSimpleName());

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
