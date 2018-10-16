package quynguyen.s3corp.com.ardemo;

import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class ARDemoApplication extends MultiDexApplication {

    // Substitute your Twilio AccountSid and ApiKey details
    public static final String ACCOUNT_SID = "ACb1aa6a1cd560cae3a348efcd1d4049d3";
    public static final String API_KEY_SID = "SKd8cf740824029257076c65116190c797";
    public static final String API_KEY_SECRET = "zF1nS9h1qe34M8LS0rYPs5uEnFNZxhVi";
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
//        else {
//            Timber.plant(new CrashReportingTree());
//        }

//        // Create a VideoGrant
//        final VideoGrant grant = new VideoGrant();
//        grant.setRoom("cool room");
//
//        // Create an Access Token
//        final AccessToken token = new AccessToken.Builder(ACCOUNT_SID, API_KEY_SID, API_KEY_SECRET)
//                .identity("example-user") // Set the Identity of this token
//                .grant(grant) // Grant access to Video
//                .build();
//
//        // Serialize the token as a JWT
//        final String jwt = token.toJWT();
//        System.out.println(jwt);
    }
}
