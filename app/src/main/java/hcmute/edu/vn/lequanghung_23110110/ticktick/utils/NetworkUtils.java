package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

public final class NetworkUtils {

    private NetworkUtils() {
    }

    /** Check xem thiết bị có đang kết nối WiFi không */
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

    /** Check xem thiết bị có kết nối mạng bất kỳ (WiFi, Mobile, Ethernet) không */
    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        Network active = cm.getActiveNetwork();
        if (active == null) {
            return false;
        }

        NetworkCapabilities caps = cm.getNetworkCapabilities(active);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
