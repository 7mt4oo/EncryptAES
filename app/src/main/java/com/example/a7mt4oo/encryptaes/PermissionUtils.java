package com.example.a7mt4oo.encryptaes;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;


public class PermissionUtils {
public static final int REQUEST_CODE_EXTERNAL_STORAGE = 103;


    public static final String[] REQUEST_EXTERNAL_STORAGE = new String[]{"android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE"};


    private Activity activity;
    private Fragment fragment;

    private OnPermissionResponse onPermissionResponse;

    public PermissionUtils(Activity activity) {
        this.activity = activity;
        onPermissionResponse = (OnPermissionResponse) activity;
    }

    public PermissionUtils(Fragment fragment) {
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        onPermissionResponse = (OnPermissionResponse) fragment;
    }

    /**
     * @param permissions string list of permission you want to ask for
     * @param requestCode int code for requesting permission
     */
    public void requestPermissions(String[] permissions, int requestCode) {
        if (checkPermission(permissions)) {
            if (onPermissionResponse != null) {
                onPermissionResponse.onPermissionGranted(requestCode);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (fragment != null)
                    fragment.requestPermissions(permissions, requestCode);
                else
                    ((Activity) activity).requestPermissions(permissions, requestCode);
            }
        }

    }

    /**
     * @param permissions string list of permission you want to ask for
     * @return boolean  returns true if permission already granted
     */
    public boolean checkPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        } else {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != 0) {
                    return false;
                }
            }
            return true;
        }
    }


    public interface OnPermissionResponse {
        void onPermissionGranted(int requestCode);

        void onPermissionDenied(int requestCode);
    }

    /**
     * @param requestCode  requestCode received from onRequestPermissionsResult()
     * @param permissions  permissions received from onRequestPermissionsResult()
     * @param grantResults grantResults received from onRequestPermissionsResult()
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.v("Tag","onRequestPermissionsResult");
        if (verifyPermissionResults(grantResults)) {

            if (onPermissionResponse != null) {
                onPermissionResponse.onPermissionGranted(requestCode);
            }
        } else {
            if (onPermissionResponse != null) {
                onPermissionResponse.onPermissionDenied(requestCode);
            }
        }
    }

    public boolean verifyPermissionResults(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}