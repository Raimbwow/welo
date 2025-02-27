package com.example.welo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * PermissionActivity class
 * It will request the permissions and switch to MainActivity.
 */
public class PermissionActivity extends AppCompatActivity {
  private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;



  /**
   * onCreate method
   * It will request the permissions and switch to MainActivity.
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    System.gc();
    Runtime.getRuntime().gc();

    String[] permissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    System.out.println("----------- Permission done -----------");
    requestPermissionsIfNecessary(permissions);

    while (true) {
      if (checkPermissions(permissions)) {
        break;
      }
    }
    switchToMainActivity();
  }

  /**
   * switchToMainActivity method
   * It will switch to MainActivity.
   */
  private void switchToMainActivity() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  /**
   * onRequestPermissionsResult method
   * It will request the permissions if necessary.
   * @param requestCode int The request code
   * @param permissions String[] The permissions
   * @param grantResults int[] The results of the permissions
   */
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    ArrayList<String> permissionsToRequest = new ArrayList<>(
            Arrays.asList(permissions).subList(0, grantResults.length));

    if (!permissionsToRequest.isEmpty()) {
      ActivityCompat.requestPermissions(
              this,
              permissionsToRequest.toArray(new String[0]),
              REQUEST_PERMISSIONS_REQUEST_CODE);
    }
  }

  /**
   * requestPermissionsIfNecessary method
   * It will request the permissions if necessary.
   * @param permissions String[] The permissions to request
   */
  private void requestPermissionsIfNecessary(String[] permissions) {
    ArrayList<String> permissionsToRequest = new ArrayList<>();
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission)
              != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequest.add(permission);
      }
    }

    if (!permissionsToRequest.isEmpty()) {
      ActivityCompat.requestPermissions(
              this,
              permissionsToRequest.toArray(new String[0]),
              REQUEST_PERMISSIONS_REQUEST_CODE);
    }
  }

  /**
   * checkPermissions method
   * It will check the permissions.
   * @param permissions String[] The permissions to check
   * @return boolean true if the permissions are granted, false otherwise
   */
  private boolean checkPermissions(String[] permissions) {
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission)
              != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }
}