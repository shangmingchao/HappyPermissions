package cn.frank.happypermissions;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class LocationActivity extends AppCompatActivity implements LocationFragment.PermissionListener {

    private static final int RC_LOCATION = 0;
    private static final int RC_SETTINGS_LOCATION = 20000;

    private static final String[] PERMISSION_LOCATION = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, LocationActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = LocationFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_LOCATION) {
            HappyPermissions.PermissionCallbacks locationPermissionCallbacks = getLocationPermissionCallbacks();
            if (locationPermissionCallbacks != null) {
                HappyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                        LocationActivity.this, RC_LOCATION, RC_SETTINGS_LOCATION, true, locationPermissionCallbacks);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SETTINGS_LOCATION) {
            HappyPermissions.PermissionCallbacks locationPermissionCallbacks = getLocationPermissionCallbacks();
            if (locationPermissionCallbacks != null) {
                HappyPermissions.happyRequestPermissions(LocationActivity.this, RC_LOCATION,
                        PERMISSION_LOCATION, locationPermissionCallbacks);
            }
        }
    }

    @Override
    public void requestLocationPermissions() {
        HappyPermissions.PermissionCallbacks locationPermissionCallbacks = getLocationPermissionCallbacks();
        if (locationPermissionCallbacks != null) {
            HappyPermissions.happyRequestPermissions(LocationActivity.this, RC_LOCATION,
                    PERMISSION_LOCATION, locationPermissionCallbacks);
        }
    }

    private HappyPermissions.PermissionCallbacks getLocationPermissionCallbacks() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof LocationFragment) {
            return ((LocationFragment) fragment).getLocationPermissionCallbacks();
        } else {
            return null;
        }
    }
}
