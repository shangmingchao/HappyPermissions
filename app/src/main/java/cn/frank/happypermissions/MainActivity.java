package cn.frank.happypermissions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_CONTACTS = 0;
    private static final int RC_CAMERA = 1;
    private static final int RC_DEVICES_AND_FILES = 2;
    private static final int RC_SETTINGS_CONTACTS = 20000;
    private static final int RC_SETTINGS_CAMERA = 20001;
    private static final int RC_SETTINGS_DEVICES_AND_FILES = 20002;

    private static final String[] PERMISSION_CONTACTS = new String[]{Manifest.permission.READ_CONTACTS};
    private static final String[] PERMISSION_CAMERA = new String[]{Manifest.permission.CAMERA};
    private static final String[] PERMISSION_DEVICES_AND_FILES = new String[]{
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private HappyPermissions.PermissionCallbacks mContactsPermissionCallbacks;
    private HappyPermissions.PermissionCallbacks mCameraPermissionCallbacks;
    private HappyPermissions.PermissionCallbacks mDevicesAndFilesPermissionCallbacks;

    private TextView mDescTextView;
    private SaveFilesTask mSaveFilesTask;
    private List<Dialog> mDialogList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button contactsButton = findViewById(R.id.contacts);
        Button cameraButton = findViewById(R.id.camera);
        Button devicesAndFilesButton = findViewById(R.id.devices_and_files);
        Button locationButton = findViewById(R.id.location);
        mDescTextView = findViewById(R.id.desc);
        mContactsPermissionCallbacks = new HappyPermissionCallbacks(MainActivity.this) {
            @Override
            public void onPermissionsGranted(int permissionsRequestCode, String[] permissions) {
                showContacts();
            }
        };
        mCameraPermissionCallbacks = new HappyPermissionCallbacks(MainActivity.this) {
            @Override
            public void onPermissionsGranted(int permissionsRequestCode, String[] permissions) {
                showCamera();
            }
        };
        mDevicesAndFilesPermissionCallbacks = new HappyPermissionCallbacks(MainActivity.this) {
            @Override
            public void onPermissionsGranted(int permissionsRequestCode, String[] permissions) {
                getDeviceIdAndWriteToFiles();
            }
        };
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog(HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CONTACTS,
                        PERMISSION_CONTACTS, mContactsPermissionCallbacks));
            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog(HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CAMERA,
                        PERMISSION_CAMERA, mCameraPermissionCallbacks));
            }
        });
        devicesAndFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog(HappyPermissions.happyRequestPermissions(MainActivity.this, RC_DEVICES_AND_FILES,
                        PERMISSION_DEVICES_AND_FILES, mDevicesAndFilesPermissionCallbacks));
            }
        });
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LocationActivity.getIntent(MainActivity.this));
            }
        });
    }

    private void showContacts() {
        startActivity(ContactsActivity.getIntent(MainActivity.this));
    }

    private void showCamera() {
        startActivity(CameraActivity.getIntent(MainActivity.this));
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void getDeviceIdAndWriteToFiles() {
        TelephonyManager telephonyManager = (TelephonyManager) MainActivity.this
                .getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        final String deviceId = telephonyManager.getDeviceId();
        mDescTextView.setText(String.format(getString(R.string.device_id_desc_format), deviceId));
        mSaveFilesTask = new SaveFilesTask(mDescTextView);
        mSaveFilesTask.execute(deviceId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CONTACTS) {
            addDialog(HappyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    MainActivity.this, RC_CONTACTS, RC_SETTINGS_CONTACTS,
                    true, mContactsPermissionCallbacks));
        } else if (requestCode == RC_CAMERA) {
            addDialog(HappyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    MainActivity.this, RC_CAMERA, RC_SETTINGS_CAMERA,
                    false, mCameraPermissionCallbacks));
        } else if (requestCode == RC_DEVICES_AND_FILES) {
            addDialog(HappyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    MainActivity.this, RC_DEVICES_AND_FILES, RC_SETTINGS_DEVICES_AND_FILES,
                    true, mDevicesAndFilesPermissionCallbacks));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SETTINGS_CONTACTS) {
            addDialog(HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CONTACTS,
                    PERMISSION_CONTACTS, mContactsPermissionCallbacks));
        } else if (requestCode == RC_SETTINGS_CAMERA) {
            addDialog(HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CAMERA,
                    PERMISSION_CAMERA, mCameraPermissionCallbacks));
        } else if (requestCode == RC_SETTINGS_DEVICES_AND_FILES) {
            addDialog(HappyPermissions.happyRequestPermissions(MainActivity.this, RC_DEVICES_AND_FILES,
                    PERMISSION_DEVICES_AND_FILES, mDevicesAndFilesPermissionCallbacks));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeAllDialog();
        if (mSaveFilesTask != null) {
            mSaveFilesTask.cancel(true);
        }
    }

    private void addDialog(Dialog dialog) {
        if (dialog != null) {
            mDialogList.add(dialog);
        }
    }

    private void removeAllDialog() {
        for (Dialog dialog : mDialogList) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        mDialogList.clear();
    }

    static class SaveFilesTask extends AsyncTask<String, Void, String> {

        private WeakReference<TextView> mDescWeakReference;
        private final String cacheDir = Environment.getExternalStorageDirectory() + File.separator;

        public SaveFilesTask(TextView descTextView) {
            mDescWeakReference = new WeakReference<>(descTextView);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileOutputStream fileOutputStream = null;
            OutputStreamWriter outputStreamWriter = null;
            String cacheString = null;
            try {
                Thread.sleep(2000);
                File cacheDirectory = new File(cacheDir);
                boolean initCacheDir = cacheDirectory.mkdirs() ||
                        (cacheDirectory.exists() && cacheDirectory.isDirectory());
                if (initCacheDir) {
                    File cacheFile = new File(cacheDirectory, "deviceId.txt");
                    fileOutputStream = new FileOutputStream(cacheFile);
                    outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                    cacheString = strings[0];
                    if (!TextUtils.isEmpty(cacheString)) {
                        outputStreamWriter.write(cacheString);
                    }
                    return cacheFile.getAbsolutePath();
                }
            } catch (Exception cacheException) {
                cacheException.printStackTrace();
            } finally {
                if (outputStreamWriter != null) {
                    try {
                        outputStreamWriter.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView descTextView = mDescWeakReference.get();
            if (descTextView != null) {
                if (!TextUtils.isEmpty(result)) {
                    descTextView.setText(String.format(descTextView.getContext()
                                    .getResources().getString(R.string.save_files_succeed),
                            result));
                } else {
                    descTextView.setText(descTextView.getContext()
                            .getResources().getString(R.string.save_files_failed));
                }
            }
        }
    }
}
