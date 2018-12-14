package cn.frank.happypermissions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LocationFragment extends Fragment implements LocationListener {

    private HappyPermissions.PermissionCallbacks mLocationPermissionCallbacks;
    private TextView mLocationTextView;
    private LocationManager mLocationManager;
    private PermissionListener mListener;

    public LocationFragment() {

    }

    public static LocationFragment newInstance() {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof PermissionListener) {
            mListener = (PermissionListener) getParentFragment();
        } else if (context instanceof PermissionListener) {
            mListener = (PermissionListener) context;
        } else {
            throw new RuntimeException(getParentFragment() + " or " + context
                    + " must implement PermissionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location, container, false);
        mLocationTextView = rootView.findViewById(R.id.location);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLocationPermissionCallbacks = new HappyPermissionCallbacks(getActivity()) {
            @Override
            public void onPermissionsGranted(int permissionsRequestCode, String[] permissions) {
                showLocation();
            }
        };
        if (mListener != null) {
            mListener.requestLocationPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void showLocation() {
        Activity hostActivity = getActivity();
        if (hostActivity == null) {
            return;
        }
        mLocationManager = (LocationManager) hostActivity.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastLocation != null) {
                mLocationTextView.setText(String.format(getString(R.string.network_location_last), lastLocation.toString()));
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } else {
            mLocationTextView.setText(getString(R.string.network_location_is_not_available));
        }
    }

    public HappyPermissions.PermissionCallbacks getLocationPermissionCallbacks() {
        return mLocationPermissionCallbacks;
    }

    @Override
    public void onDestroyView() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
        super.onDestroyView();
    }

    @Override
    public void onLocationChanged(Location location) {
        String updateDesc = mLocationTextView.getText() + "\n" +
                String.format(getString(R.string.network_location_updated), location.toString());
        mLocationTextView.setText(updateDesc);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    interface PermissionListener {
        void requestLocationPermissions();
    }
}
