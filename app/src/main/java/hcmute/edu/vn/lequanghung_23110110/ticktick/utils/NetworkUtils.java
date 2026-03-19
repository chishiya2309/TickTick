package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean isOnWifi(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        Network active = cm.getActiveNetwork();
        if (active == null) {
            return false;
        }

        NetworkCapabilities caps = cm.getNetworkCapabilities(active);
        return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
}

