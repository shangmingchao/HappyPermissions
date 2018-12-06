package cn.frank.happypermissions;

import android.app.Activity;

/**
 * Callbacks for the request of System permissions
 *
 * @author frank
 * @date 2018/12/6
 */
public class HappyPermissionCallbacks implements HappyPermissions.PermissionCallbacks {

    private Activity activity;

    public HappyPermissionCallbacks(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onPermissionsGranted(int permissionsRequestCode, String[] permissions) {

    }

    @Override
    public void onPermissionsDenied(int permissionsRequestCode, String[] permissions) {

    }

    @Override
    public String getRationale(String[] rationalePermissions) {
        return HappyPermissions.getDefaultRationale(activity, rationalePermissions);
    }

    @Override
    public String getAppSettingsRationale(String[] deniedPermissions) {
        return HappyPermissions.getDefaultAppSettingsRationale(activity, deniedPermissions);
    }
}
