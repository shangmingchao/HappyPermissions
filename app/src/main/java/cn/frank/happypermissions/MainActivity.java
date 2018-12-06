package cn.frank.happypermissions;

import android.Manifest;
import android.annotation.SuppressLint;
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

public class MainActivity extends AppCompatActivity {

    private static final int RC_CONTACTS = 0;
    private static final int RC_CAMERA = 1;
    private static final int RC_DEVICES_AND_FILES = 2;
    private static final int RC_SETTINGS_CONTACTS = 20000;
    private static final int RC_SETTINGS_CAMERA = 20001;
    private static final int RC_SETTINGS_DEVICES_AND_FILES = 20002;

    private static final String[] PERMISSION_CONTACTS = new String[]{Manifest.permission.READ_CONTACTS};
    private static final String[] PERMISSION_CAMERA = new String[]{Manifest.permission.CAMERA};
    private static final String[] PERMISSION_DEVICES_AND_FILES = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private HappyPermissions.PermissionCallbacks mContactsPermissionCallbacks;
    private HappyPermissions.PermissionCallbacks mCameraPermissionCallbacks;
    private HappyPermissions.PermissionCallbacks mDevicesAndFilesPermissionCallbacks;

    private TextView mDescTextView;
    private SaveFilesTask mSaveFilesTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button contactsButton = findViewById(R.id.contacts);
        Button cameraButton = findViewById(R.id.camera);
        Button devicesAndFilesButton = findViewById(R.id.devices_and_files);
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
                HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CONTACTS,
                        PERMISSION_CONTACTS, mContactsPermissionCallbacks);
            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CAMERA,
                        PERMISSION_CAMERA, mCameraPermissionCallbacks);
            }
        });
        devicesAndFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HappyPermissions.happyRequestPermissions(MainActivity.this, RC_DEVICES_AND_FILES,
                        PERMISSION_DEVICES_AND_FILES, mDevicesAndFilesPermissionCallbacks);
            }
        });
    }

    private void showContacts() {
        startActivity(ContactsActivity.getIntent(MainActivity.this));
    }

    private void showCamera() {
        startActivity(CameraActivity.getIntent(MainActivity.this));
    }

    @SuppressLint("MissingPermission")
    private void getDeviceIdAndWriteToFiles() {
        TelephonyManager telephonyManager = (TelephonyManager) MainActivity.this.getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        final String deviceId = telephonyManager.getDeviceId();
        mSaveFilesTask = new SaveFilesTask(mDescTextView);
        mSaveFilesTask.execute(deviceId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CONTACTS) {
            HappyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    MainActivity.this, RC_CONTACTS, RC_SETTINGS_CONTACTS, true, mContactsPermissionCallbacks);
        } else if (requestCode == RC_CAMERA) {
            HappyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    MainActivity.this, RC_CAMERA, RC_SETTINGS_CAMERA, false, mCameraPermissionCallbacks);
        } else if (requestCode == RC_DEVICES_AND_FILES) {
            HappyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    MainActivity.this, RC_DEVICES_AND_FILES, RC_SETTINGS_DEVICES_AND_FILES, true, mDevicesAndFilesPermissionCallbacks);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SETTINGS_CONTACTS) {
            HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CONTACTS,
                    PERMISSION_CONTACTS, mContactsPermissionCallbacks);
        } else if (requestCode == RC_SETTINGS_CAMERA) {
            HappyPermissions.happyRequestPermissions(MainActivity.this, RC_CAMERA,
                    PERMISSION_CAMERA, mCameraPermissionCallbacks);
        } else if (requestCode == RC_SETTINGS_DEVICES_AND_FILES) {
            HappyPermissions.happyRequestPermissions(MainActivity.this, RC_DEVICES_AND_FILES,
                    PERMISSION_DEVICES_AND_FILES, mDevicesAndFilesPermissionCallbacks);
        }
    }

    static class SaveFilesTask extends AsyncTask<String, Void, String> {

        private WeakReference<TextView> mDescWeakReference;

        public SaveFilesTask(TextView descTextView) {
            mDescWeakReference = new WeakReference<>(descTextView);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileOutputStream fileOutputStream = null;
            OutputStreamWriter outputStreamWriter = null;
            try {
                File cacheDirectory = new File(Environment.getExternalStorageDirectory() + File.separator);
                if (cacheDirectory.mkdirs() || (cacheDirectory.exists() && cacheDirectory.isDirectory())) {
                    File cacheFile = new File(cacheDirectory, "deviceId.txt");
                    fileOutputStream = new FileOutputStream(cacheFile);
                    outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                    String cacheString = strings[0];
                    if (!TextUtils.isEmpty(cacheString)) {
                        outputStreamWriter.write(cacheString);
                    }
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
            return strings[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView descTextView = mDescWeakReference.get();
            if (descTextView != null) {
                descTextView.setText(result);
            }
        }
    }
}
