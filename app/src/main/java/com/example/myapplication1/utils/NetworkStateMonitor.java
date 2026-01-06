package com.example.myapplication1.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.HashSet;
import java.util.Set;

/**
 * Network State Monitor - Tracks connectivity status for offline mode
 * Provides real-time network status updates throughout the app
 */
public class NetworkStateMonitor {

    private static NetworkStateMonitor instance;
    private final Context context;
    private final ConnectivityManager connectivityManager;

    private final MutableLiveData<NetworkState> networkState = new MutableLiveData<>();
    private final Set<NetworkStateListener> listeners = new HashSet<>();

    private ConnectivityManager.NetworkCallback networkCallback;
    private BroadcastReceiver networkReceiver;

    // Network state enum
    public enum NetworkState {
        CONNECTED_WIFI,
        CONNECTED_MOBILE,
        CONNECTED_OTHER,
        DISCONNECTED
    }

    public interface NetworkStateListener {
        void onNetworkStateChanged(NetworkState state, boolean isConnected);
    }

    private NetworkStateMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Set initial state
        networkState.setValue(getCurrentNetworkState());

        // Register for network changes
        registerNetworkCallback();
    }

    public static synchronized NetworkStateMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkStateMonitor(context);
        }
        return instance;
    }

    /**
     * Get current network state
     */
    public NetworkState getCurrentNetworkState() {
        if (connectivityManager == null) {
            return NetworkState.DISCONNECTED;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return NetworkState.DISCONNECTED;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return NetworkState.DISCONNECTED;
            }

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return NetworkState.CONNECTED_WIFI;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return NetworkState.CONNECTED_MOBILE;
            } else if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return NetworkState.CONNECTED_OTHER;
            }
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                int type = networkInfo.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    return NetworkState.CONNECTED_WIFI;
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    return NetworkState.CONNECTED_MOBILE;
                } else {
                    return NetworkState.CONNECTED_OTHER;
                }
            }
        }

        return NetworkState.DISCONNECTED;
    }

    /**
     * Check if device is currently connected
     */
    public boolean isConnected() {
        NetworkState state = getCurrentNetworkState();
        return state != NetworkState.DISCONNECTED;
    }

    /**
     * Check if connected to WiFi
     */
    public boolean isWifiConnected() {
        return getCurrentNetworkState() == NetworkState.CONNECTED_WIFI;
    }

    /**
     * Check if connected to mobile data
     */
    public boolean isMobileConnected() {
        return getCurrentNetworkState() == NetworkState.CONNECTED_MOBILE;
    }

    /**
     * Get LiveData for observing network state changes
     */
    public LiveData<NetworkState> getNetworkStateLiveData() {
        return networkState;
    }

    /**
     * Add a listener for network state changes
     */
    public void addListener(NetworkStateListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     */
    public void removeListener(NetworkStateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Register network callback for real-time updates
     */
    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    updateNetworkState();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    updateNetworkState();
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network,
                                                  @NonNull NetworkCapabilities capabilities) {
                    updateNetworkState();
                }
            };

            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();

            connectivityManager.registerNetworkCallback(request, networkCallback);
        } else {
            // Fallback for older devices
            networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateNetworkState();
                }
            };

            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(networkReceiver, filter);
        }
    }

    /**
     * Update network state and notify listeners
     */
    private void updateNetworkState() {
        NetworkState newState = getCurrentNetworkState();
        networkState.postValue(newState);

        boolean isConnected = newState != NetworkState.DISCONNECTED;

        for (NetworkStateListener listener : listeners) {
            listener.onNetworkStateChanged(newState, isConnected);
        }
    }

    /**
     * Get human-readable network status
     */
    public String getNetworkStatusString() {
        NetworkState state = getCurrentNetworkState();
        switch (state) {
            case CONNECTED_WIFI:
                return "📶 Connecté (WiFi)";
            case CONNECTED_MOBILE:
                return "📱 Connecté (Mobile)";
            case CONNECTED_OTHER:
                return "🌐 Connecté";
            case DISCONNECTED:
            default:
                return "❌ Hors ligne";
        }
    }

    /**
     * Get network icon
     */
    public String getNetworkIcon() {
        NetworkState state = getCurrentNetworkState();
        switch (state) {
            case CONNECTED_WIFI:
                return "📶";
            case CONNECTED_MOBILE:
                return "📱";
            case CONNECTED_OTHER:
                return "🌐";
            case DISCONNECTED:
            default:
                return "❌";
        }
    }

    /**
     * Unregister callbacks (call when app is destroyed)
     */
    public void unregister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                // Ignore if already unregistered
            }
        } else if (networkReceiver != null) {
            try {
                context.unregisterReceiver(networkReceiver);
            } catch (Exception e) {
                // Ignore if already unregistered
            }
        }
    }
}
