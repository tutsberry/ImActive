package com.tutsberry.imactive;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // LogCat
    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext;
    private GoogleApiClient mGApiClient;
    private BroadcastReceiver receiver;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the textview
        textView = (TextView) findViewById(R.id.msg);
        textView.setMovementMethod(new ScrollingMovementMethod());

        //Set the context
        mContext = this;

        //Check Google Play Service Available
        if(isPlayServiceAvailable()) {
            mGApiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            //Connect to Google API
            mGApiClient.connect();
        }else{
            Toast.makeText(mContext, "Google Play Service not Available", Toast.LENGTH_LONG).show();
        }

        //Broadcast receiver
        receiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Add current time
                Calendar rightNow = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
                String strDate = sdf.format(rightNow.getTime());;
                String v =  strDate + " " +
                        intent.getStringExtra("activity") + " " +
                        "Confidence : " + intent.getExtras().getInt("confidence") + "\n";

                v = textView.getText() + v;
                textView.setText(v);
            }
        };

        //Filter the Intent and register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("ImActive");
        registerReceiver(receiver, filter);
    }

    //Check for Google play services available on device
    private boolean isPlayServiceAvailable() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Intent i = new Intent(this, ActivityRecognitionIntentService.class);
        PendingIntent mActivityRecongPendingIntent = PendingIntent
                .getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d(TAG, "connected to ActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGApiClient, 0, mActivityRecongPendingIntent);

        //Update the TextView
        textView.setText("Connected to Google Play Services \nWaiting for Active Recognition... \n");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Suspended to ActivityRecognition");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Not connected to ActivityRecognition");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Disconnect and detach the receiver
        mGApiClient.disconnect();
        unregisterReceiver(receiver);
    }
}
