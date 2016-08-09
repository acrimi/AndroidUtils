package com.isbx.androidtools.media;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.isbx.androidtools.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by alexs_000 on 5/9/2016.
 */
public class MediaPicker implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MediaPicker.class.getSimpleName();

    private static final int REQUEST_PHOTO_CAMERA = 1000;
    private static final int REQUEST_PHOTO_LIBRARY = 1001;
    private static final int REQUEST_VIDEO = 1002;
    private static final int REQUEST_CAMERA_PERMISSION = 1100;

    private static final String CACHE_FILE_NAME = "mediapicker_pic";

    public enum MediaType {
        PHOTO_CAMERA,
        PHOTO_LIBRARY,
        VIDEO
    }

    private Activity activity;
    private Fragment fragment;

    private boolean cameraEnabled = true;
    private boolean libraryEnabled = true;
    private boolean videoEnabled = true;

    private boolean cameraPermissionPending;
    private boolean videoPermissionPending;

    private Uri fileUri;

    private MediaPickerListener listener;

    public MediaPicker(Activity activity) {
        this.activity = activity;
    }

    public MediaPicker(Fragment fragment) {
        this.fragment = fragment;
    }

    public MediaPicker(Activity activity, MediaPickerListener listener) {
        this(activity);
        setMediaPickerListener(listener);
    }

    public MediaPicker(Fragment fragment, MediaPickerListener listener) {
        this(fragment);
        setMediaPickerListener(listener);
    }

    private Context getContext() {
        if (fragment != null) {
            return fragment.getContext();
        }

        return activity;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_PHOTO_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (listener != null) {
                    listener.onMediaSelected(fileUri, MediaType.PHOTO_CAMERA);
                }
            }
        } else if (requestCode == REQUEST_PHOTO_LIBRARY) {
            if (resultCode == Activity.RESULT_OK) {
                fileUri = data.getData();

                // Create internal cache file to avoid content uri permission issues on >= 6.0
                fileUri = copyUriToCache(fileUri);

                if (listener != null) {
                    listener.onMediaSelected(fileUri, MediaType.PHOTO_LIBRARY);
                }
            }
        } else if (requestCode == REQUEST_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                fileUri = data.getData();
                if (listener != null) {
                    listener.onMediaSelected(fileUri, MediaType.VIDEO);
                }
            }
        }
    }

    private Uri copyUriToCache(Uri source) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = activity.getContentResolver().openInputStream(source);
            if (is != null) {
                File file = new File(activity.getCacheDir(), CACHE_FILE_NAME);
                fos = new FileOutputStream(file);

                byte[] bytes = new byte[2048];
                int bytesRead;
                while((bytesRead = is.read(bytes)) > -1) {
                    fos.write(bytes, 0, bytesRead);
                }

                source = Uri.fromFile(file);
                // Append query param to prevent caching errors with certain image loading libraries
                source = source.buildUpon().appendQueryParameter("t", ""+System.currentTimeMillis()).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return source;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (cameraPermissionPending || videoPermissionPending) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (cameraPermissionPending) {
                        cameraPermissionPending = false;
                        launchCameraIntent();
                    } else if (videoPermissionPending) {
                        videoPermissionPending = false;
                        launchVideoIntent();
                    }
                } else {
                    new AlertDialog.Builder(activity)
                        .setTitle(R.string.permission_denied)
                        .setMessage(R.string.camera_permission_msg)
                        .setPositiveButton(R.string.dialog_button_ok, null)
                        .show();
                }
            }
        }
    }


    public void setPhotoCameraEnabled(boolean enabled) {
        cameraEnabled = enabled;
    }

    public void setPhotoLibraryEnabled(boolean enabled) {
        libraryEnabled = enabled;
    }

    public void setVideoEnabled(boolean enabled) {
        videoEnabled = enabled;
    }


    public void setMediaPickerListener(MediaPickerListener listener) {
        this.listener = listener;
    }


    public void showChooser() {
        int optionLength = (cameraEnabled ? 1 : 0) + (libraryEnabled ? 1 : 0) + (videoEnabled ? 1 : 0);
        if (optionLength == 0) {
            Log.w(TAG, "No media options are enabled!");
            return;
        }

        Context ctx = getContext();
        String[] options = new String[optionLength];
        int index = 0;
        if (cameraEnabled) {
            options[index++] = ctx.getString(R.string.media_picker_option_photo_camera);
        }
        if (libraryEnabled) {
            options[index++] = ctx.getString(R.string.media_picker_option_photo_library);
        }
        if (videoEnabled) {
            options[index] = ctx.getString(R.string.media_picker_option_video);
        }

        new AlertDialog.Builder(ctx)
            .setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            if (cameraEnabled) {
                                launchCamera();
                            } else if (libraryEnabled) {
                                launchLibrary();
                            } else if (videoEnabled) {
                                launchVideo();
                            }
                            break;
                        case 1:
                            if (libraryEnabled && cameraEnabled) {
                                launchLibrary();
                            } else if (videoEnabled) {
                                launchVideo();
                            }
                            break;
                        case 2:
                            if (videoEnabled) {
                                launchVideo();
                            }
                            break;
                    }
                }
            })
            .show();
    }


    private void launchCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasCameraPermissionInManifest()) {
            // Starting in marshmallow, if app declares CAMERA permission but doesn't have it granted,
            // the image capture intent will throw a security exception, so we need to make sure it's granted
            // Apps targeting Marhsmallow with CAMERA permission should pass onRequestPermissionResult()
            // through to MediaPicker
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                cameraPermissionPending = true;
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } else {
            launchCameraIntent();
        }
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = createImageFile(getContext());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_PHOTO_CAMERA);
    }

    private void launchLibrary() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), REQUEST_PHOTO_LIBRARY);
    }

    private void launchVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasCameraPermissionInManifest()) {
            // Starting in marshmallow, if app declares CAMERA permission but doesn't have it granted,
            // the image capture intent will throw a security exception, so we need to make sure it's granted
            // Apps targeting Marhsmallow with CAMERA permission should pass onRequestPermissionResult()
            // through to MediaPicker
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchVideoIntent();
            } else {
                videoPermissionPending = true;
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } else {
            launchVideoIntent();
        }
    }

    private void launchVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO);
        } else {
            new AlertDialog.Builder(getContext())
                .setTitle(R.string.not_supported_title)
                .setMessage(R.string.not_supported_message)
                .setPositiveButton(R.string.dialog_button_ok, null)
                .show();
        }
    }


    private boolean hasCameraPermissionInManifest() {
        final String packageName = activity.getPackageName();
        try {
            final PackageInfo packageInfo = activity.getPackageManager()
                .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            final String[] declaredPermissions = packageInfo.requestedPermissions;
            if (declaredPermissions != null && declaredPermissions.length > 0) {
                for (String p : declaredPermissions) {
                    if (p.equals(Manifest.permission.CAMERA)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void startActivityForResult(Intent intent, int requestCode) {
        cameraPermissionPending = false;
        videoPermissionPending = false;
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private Uri createImageFile(Context context) {
        File storageDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // SD Card Mounted
            storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            // Use internal storage
            storageDir = context.getCacheDir();
        }

        String filename = "pic_" + System.currentTimeMillis();
        File image = new File(storageDir, filename + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        return Uri.fromFile(image);
    }


    public interface MediaPickerListener {
        public void onMediaSelected(Uri fileUri, MediaType mediaType);
    }
}
