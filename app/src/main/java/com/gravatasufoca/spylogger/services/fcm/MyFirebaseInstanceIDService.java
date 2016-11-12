package com.gravatasufoca.spylogger.services.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by bruno on 05/11/16.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
// eaEcDENKTHM:APA91bFmZdeDAWA3ri4wABGKqrOJ4MdXmx2fa8F5NfO_2Xznu0Q5rzBtJeoBuJtiE3_KWfe2n_1ZC37XD80yIQ5ZFZ3VRPN3vMPyw7VwABdZx8SzakIGKhJjOfkXq3uk-GElaPpVIsWk
    private static final String TAG = "FIREBASE TOKEN" ;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {

    }
}
