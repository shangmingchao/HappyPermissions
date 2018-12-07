package cn.frank.happypermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to request and check System permissions
 *
 * @author frank
 * @date 2018/12/6
 */

public class HappyPermissions {

    public static boolean checkSelfPermissions(@NonNull Context context, @Size(min = 1) String[] permissions) {
        for (String perm : permissions) {
            if (!checkSelfPermission(context, perm)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkSelfPermission(@NonNull Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
            return checkSelfPermissionForXiaomi(context, permission);
        } else {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private static boolean checkSelfPermissionForXiaomi(Context context, String permission) {
        String permissionToOp = AppOpsManagerCompat.permissionToOp(permission);
        if (permissionToOp == null) {
            return true;
        }
        int noteOp = AppOpsManagerCompat.noteOp(context, permissionToOp, Process.myUid(), context.getPackageName());
        return noteOp == AppOpsManagerCompat.MODE_ALLOWED &&
                PermissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static String[] shouldShowRequestPermissionRationale(@NonNull Activity activity, @Size(min = 1) String[] permissions) {
        List<String> rationaleList = new ArrayList<>();
        for (String perm : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                rationaleList.add(perm);
            }
        }
        return rationaleList.toArray(new String[0]);
    }

    public static void requestPermissions(@NonNull final Activity activity, @NonNull final String[] permissions,
                                          @IntRange(from = 0L) final int permissionsRequestCode) {
        ActivityCompat.requestPermissions(activity, permissions, permissionsRequestCode);
    }

    public static String[] getDeniedPermissions(@NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> deniedList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedList.add(permissions[i]);
            }
        }
        return deniedList.toArray(new String[0]);
    }

    public static String getDefaultRationale(Activity activity, String[] rationalePermissions) {
        SpannableStringBuilder rationale = new SpannableStringBuilder("");
        for (int i = 0; i < rationalePermissions.length; i++) {
            String permission = rationalePermissions[i];
            if (i > 0) {
                rationale.append("\n\n");
            }
            if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_read_contacts)).append("】");
            } else if (Manifest.permission.CAMERA.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_camera)).append("】");
            } else if (Manifest.permission.READ_PHONE_STATE.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_read_phone_state)).append("】");
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_write_external_storage)).append("】");
            }
        }
        return String.format(activity.getString(R.string.rationale_format), rationale);
    }

    public static String getDefaultAppSettingsRationale(Activity activity, String[] deniedPermissions) {
        SpannableStringBuilder rationale = new SpannableStringBuilder("");
        for (int i = 0; i < deniedPermissions.length; i++) {
            String permission = deniedPermissions[i];
            if (i > 0) {
                rationale.append("\n\n");
            }
            if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_read_contacts)).append("】");
            } else if (Manifest.permission.CAMERA.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_camera)).append("】");
            } else if (Manifest.permission.READ_PHONE_STATE.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_read_phone_state)).append("】");
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                rationale.append("【").append(activity.getString(R.string.rationale_write_external_storage)).append("】");
            }
        }
        return String.format(activity.getString(R.string.app_settings_rationale_format), rationale);
    }

    public static void showRationaleDialog(final Activity activity, final int permissionsRequestCode,
                                           final String[] rationalePermissions, final PermissionCallbacks callbacks) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.need_permission))
                .setMessage(callbacks.getRationale(rationalePermissions))
                .setPositiveButton(activity.getString(R.string.next_step), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HappyPermissions.requestPermissions(activity, rationalePermissions, permissionsRequestCode);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callbacks.onPermissionsDenied(permissionsRequestCode, rationalePermissions);
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static void showAppSettingsDialog(final Activity activity, final int permissionsRequestCode,
                                             final int settingsRequestCode, final String[] deniedPermissions,
                                             final PermissionCallbacks callbacks) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.need_permission))
                .setMessage(callbacks.getAppSettingsRationale(deniedPermissions))
                .setPositiveButton(activity.getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", activity.getPackageName(), null));
                        activity.startActivityForResult(intent, settingsRequestCode);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callbacks.onPermissionsDenied(permissionsRequestCode, deniedPermissions);
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static void happyRequestPermissions(Activity activity, int permissionsRequestCode, String[] permissions,
                                               PermissionCallbacks callbacks) {
        if (!HappyPermissions.checkSelfPermissions(activity, permissions)) {
            String[] rationalePermissions = HappyPermissions.shouldShowRequestPermissionRationale(
                    activity, permissions);
            if (rationalePermissions.length > 0) {
                showRationaleDialog(activity, permissionsRequestCode, rationalePermissions, callbacks);
            } else {
                HappyPermissions.requestPermissions(activity, permissions, permissionsRequestCode);
            }
        } else {
            callbacks.onPermissionsGranted(permissionsRequestCode, permissions);
        }
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults,
                                                  Activity activity, int permissionsRequestCode,
                                                  int settingsRequestCode, boolean required,
                                                  PermissionCallbacks callbacks) {
        String[] deniedPermissions = HappyPermissions.getDeniedPermissions(permissions, grantResults);
        if (deniedPermissions.length == 0) {
            callbacks.onPermissionsGranted(permissionsRequestCode, permissions);
        } else if (required) {
            String[] rationalePermissions = HappyPermissions.shouldShowRequestPermissionRationale(activity, permissions);
            if (rationalePermissions.length == 0) {
                showAppSettingsDialog(activity, permissionsRequestCode, settingsRequestCode, deniedPermissions, callbacks);
            } else {
                showRationaleDialog(activity, permissionsRequestCode, rationalePermissions, callbacks);
            }
        } else {
            callbacks.onPermissionsDenied(permissionsRequestCode, deniedPermissions);
        }
    }

    public interface PermissionCallbacks {

        void onPermissionsGranted(int permissionsRequestCode, String[] permissions);

        void onPermissionsDenied(int permissionsRequestCode, String[] permissions);

        String getRationale(String[] rationalePermissions);

        String getAppSettingsRationale(String[] deniedPermissions);
    }
}
