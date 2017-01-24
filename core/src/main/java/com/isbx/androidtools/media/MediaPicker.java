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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.isbx.androidtools.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides a simple mechanism to prompt the user to provide an audio/video media file.
 *
 * <p>
 * There are three main ways a user can provide a media file to the application:
 * </p>
 *
 * <ul>
 * <li>Taking a photo from the camera app</li>
 * <li>Recording a video from the camera app</li>
 * <li>Selecting a media file from their device's media library</li>
 * </ul>
 *
 * <p>
 * By default, all of these options are enabled, but they can be configured using
 * {@link MediaPicker#setPhotoCameraEnabled(boolean)}, {@link MediaPicker#setVideoEnabled(boolean)},
 * and {@link MediaPicker#setPhotoLibraryEnabled(boolean)} respectively.
 * </p>
 *
 * <p>
 * Once you have configured the MediaPicker instance, a call to {@link MediaPicker#showChooser()}
 * will present a dialog to the user to choose which of the available media sources they would like
 * to use. In order to receive the result from the user action, implement the
 * {@link MediaPickerListener} interface which will be called with a {@link Uri} representing the
 * media the user has selected.
 * </p>
 *
 * <p>
 * MediaPicker defers to the built in camera and library apps to actually retrieve the appropriate
 * media file. MediaPicker handles this logic internally, but in order for it to work you must be
 * sure to call {@link MediaPicker#onActivityResult(int, int, Intent)} from the appropriate method
 * in your fragment or activity ({@link Fragment#onActivityResult(int, int, Intent)},
 * {@link Activity#onActivityResult(int, int, Intent)}. For example, to use MediaPicker from an
 * activity:
 * </p>
 *
 * <pre>
 * <code>&#064;Override
 * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *     super.onActivityResult(requestCode, resultCode, data);
 *     mediaPicker.onActivityResult(requestCode, resultCode, data);
 * }</code>
 * </pre>
 *
 * <p>
 * Similarly, apps that target API 23 and up and also include the
 * {@link android.Manifest.permission#CAMERA} permission in their manifest should pass through
 * calls to {@link Activity#onRequestPermissionsResult(int, String[], int[])}:
 * </p>
 *
 * <pre>
 * <code>&#064;Override
 * protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
 *     super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 *     mediaPicker.onRequestPermissionsResult(requestCode, permissions, grantResults);
 * }</code>
 * </pre>
 *
 * <p>
 * This is due to a quirk beginning in API 23 that requires the user to grant the application access
 * to the camera if the permission is declared in the manifest. As long as you call
 * {@link MediaPicker#onRequestPermissionsResult(int, String[], int[])} from your activity/fragment,
 * MediaPicker will handle the permission checking and requesting automatically. If you are
 * targeting an API below 23, or you do not declare {@link android.Manifest.permission#CAMERA} in
 * your manifest, you do not need to do this.
 * </p>
 */
public class MediaPicker implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MediaPicker.class.getSimpleName();

    private static final int REQUEST_PHOTO_CAMERA = 1000;
    private static final int REQUEST_PHOTO_LIBRARY = 1001;
    private static final int REQUEST_VIDEO = 1002;
    private static final int REQUEST_CAMERA_PERMISSION = 1100;

    private static final String CACHE_FILE_NAME = "mediapicker_pic";

    /**
     * The supported media sources.
     */
    public enum MediaType {
        /**
         * A photo captured from the camera app.
         */
        PHOTO_CAMERA,
        /**
         * A media file selected from the library app.
         */
        PHOTO_LIBRARY,
        /**
         * A video recorded from the camera app.
         */
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

    /**
     * Creates a new MediaPicker tied to the given {@link Activity}. The activity will be used
     * internally to show the picker dialog and to start intents to the device's camera/library
     * apps.
     *
     * @param activity The {@link Activity} to use for this MediaPicker
     */
    public MediaPicker(Activity activity) {
        this.activity = activity;
    }

    /**
     * Creates a new MediaPicker tied to the given {@link Fragment}. The fragment will be used
     * internally to show the picker dialog and to start intents to the device's camera/library
     * apps.
     *
     * @param fragment The {@link Fragment} to use for this MediaPicker
     */
    public MediaPicker(Fragment fragment) {
        this.fragment = fragment;
    }

    /**
     * Creates a new MediaPicker tied to the given {@link Activity}. The activity will be used
     * internally to show the picker dialog and to start intents to the device's camera/library
     * apps.
     *
     * @param activity The {@link Activity} to use for this MediaPicker
     * @param listener A {@link MediaPickerListener} to be notified when the user selects a media
     *                 object
     */
    public MediaPicker(Activity activity, MediaPickerListener listener) {
        this(activity);
        setMediaPickerListener(listener);
    }

    /**
     * Creates a new MediaPicker tied to the given {@link Fragment}. The fragment will be used
     * internally to show the picker dialog and to start intents to the device's camera/library
     * apps.
     *
     * @param fragment The {@link Fragment} to use for this MediaPicker
     * @param listener A {@link MediaPickerListener} to be notified when the user selects a media
     *                 object
     */
    public MediaPicker(Fragment fragment, MediaPickerListener listener) {
        this(fragment);
        setMediaPickerListener(listener);
    }

    /**
     * Return the nearest {@link Context} this MediaPicker is associated with, either
     * {@code activity} or {@link Fragment#getContext()}.
     *
     * @return The {@link Context} for this MediaPicker
     */
    private Context getContext() {
        if (fragment != null) {
            return fragment.getContext();
        }

        return activity;
    }


    /**
     * Handles results from the device's built in camera/media library apps. This should be invoked
     * directly from either {@link Activity#onActivityResult(int, int, Intent)} or
     * {@link Fragment#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The requestCode that was passed in to startActivityForResult()
     * @param resultCode The resultCode from the returning activity, set by
     *                   {@link Activity#setResult(int)}
     * @param data The data {@link Intent} from the returning activity, set by
     *             {@link Activity#setResult(int, Intent)}
     *
     * @see Activity#onActivityResult(int, int, Intent)
     * @see Fragment#onActivityResult(int, int, Intent)
     */
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

    /**
     * Copies a file into the app's private cache directory. This is necessary for certain Uris to
     * circumvent access expiration conditions, for example, navigating to a new activity. Copying
     * the Uri to the cache immediately ensures we will have access to it for as long as the cache
     * file exists.
     *
     * @param source The {@link Uri} to copy
     * @return A {link Uri} pointing to the new cache file
     */
    private Uri copyUriToCache(Uri source) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = getContext().getContentResolver().openInputStream(source);
            if (is != null) {
                File file = new File(getContext().getCacheDir(), CACHE_FILE_NAME+System.currentTimeMillis());
                fos = new FileOutputStream(file);

                byte[] bytes = new byte[2048];
                int bytesRead;
                while((bytesRead = is.read(bytes)) > -1) {
                    fos.write(bytes, 0, bytesRead);
                }

                source = Uri.fromFile(file);
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

    /**
     * <p>
     * Handles results for requesting the {@link android.Manifest.permission#CAMERA} permission.
     * This should be invoked directly from either
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])} or
     * {@link Fragment#onRequestPermissionsResult(int, String[], int[])} if and only if you satisfy
     * <strong>both</strong> of the following conditions:
     * </p>
     *
     * <ul>
     * <li>Your app's {@code targetSdkVersion} is set to 23 or higher</li>
     * <li>You declare {@code <uses-permission android:name="android.permission.CAMERA" />} in your
     * manifest file</li>
     * </ul>
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     * @see Fragment#onRequestPermissionsResult(int, String[], int[])
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (cameraPermissionPending || videoPermissionPending) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (cameraPermissionPending) {
                        cameraPermissionPending = false;
                        launchCameraIntent();
                    } else if (videoPermissionPending) {
                        videoPermissionPending = false;
                        launchVideoIntent();
                    }
                } else {
                    new AlertDialog.Builder(getContext())
                        .setTitle(R.string.permission_denied)
                        .setMessage(R.string.camera_permission_msg)
                        .setPositiveButton(R.string.dialog_button_ok, null)
                        .show();
                }
            }
        }
    }


    /**
     * Sets whether or not the media picker dialog will have an option to take a picture.
     *
     * @param enabled {@code true} if option to take a photo should be available, {@code false}
     *                otherwise
     *
     * @see MediaPicker#showChooser()
     */
    public void setPhotoCameraEnabled(boolean enabled) {
        cameraEnabled = enabled;
    }

    /**
     * Sets whether or not the media picker dialog will have an option to choose from the media
     * library.
     *
     * @param enabled {@code true} if option to choose from the media library should be available,
     *                {@code false} otherwise
     *
     * @see MediaPicker#showChooser()
     */
    public void setPhotoLibraryEnabled(boolean enabled) {
        libraryEnabled = enabled;
    }

    /**
     * Sets whether or not the media picker dialog will have an option to record a video.
     *
     * @param enabled {@code true} if option to record a video should be available, {@code false}
     *                otherwise
     *
     * @see MediaPicker#showChooser()
     */
    public void setVideoEnabled(boolean enabled) {
        videoEnabled = enabled;
    }


    /**
     * Sets the listener object to be notified when the user selects a media object
     *
     * @param listener The {@link MediaPickerListener} to be notified of the user's media selection
     */
    public void setMediaPickerListener(MediaPickerListener listener) {
        this.listener = listener;
    }


    /**
     * Presents a simple alert dialog to the user with options to either take a photo, record a
     * video, or choose a media file from their library, depending on which sources are enabled for
     * this MediaPicker. Once the user makes a selection, they will be taken to the appropriate app
     * (camera or media library), where they can perform the necessary action. The result of this
     * action will be passed to this MediaPicker's {@link MediaPickerListener}.
     *
     * @see MediaPicker#setPhotoCameraEnabled(boolean)
     * @see MediaPicker#setPhotoLibraryEnabled(boolean)
     * @see MediaPicker#setVideoEnabled(boolean)
     */
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


    /**
     * Launches the camera app if the app meets the requisite permission criteria. Otherwise,
     * prompts the user to grant the {@link android.Manifest.permission#CAMERA} permission.
     *
     * @see MediaPicker#launchCameraIntent()
     */
    private void launchCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasCameraPermissionInManifest()) {
            // Starting in marshmallow, if app declares CAMERA permission but doesn't have it granted,
            // the image capture intent will throw a security exception, so we need to make sure it's granted
            // Apps targeting Marhsmallow with CAMERA permission should pass onRequestPermissionResult()
            // through to MediaPicker
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                cameraPermissionPending = true;
                String[] permissions = new String[]{Manifest.permission.CAMERA};
                if (fragment != null) {
                    fragment.requestPermissions(permissions, REQUEST_CAMERA_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(activity, permissions, REQUEST_CAMERA_PERMISSION);
                }
            }
        } else {
            launchCameraIntent();
        }
    }

    /**
     * Fires an intent to launch the built in camera app
     */
    private void launchCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = createImageFile(getContext());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_PHOTO_CAMERA);
    }

    /**
     * Fires an intent to launch the built in media library app
     */
    private void launchLibrary() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), REQUEST_PHOTO_LIBRARY);
    }

    /**
     * Launches the camera app in video mode if the app meets the requisite permission criteria.
     * Otherwise, prompts the user to grant the {@link android.Manifest.permission#CAMERA}
     * permission.
     *
     * @see MediaPicker#launchVideoIntent()
     */
    private void launchVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasCameraPermissionInManifest()) {
            // Starting in marshmallow, if app declares CAMERA permission but doesn't have it granted,
            // the image capture intent will throw a security exception, so we need to make sure it's granted
            // Apps targeting Marhsmallow with CAMERA permission should pass onRequestPermissionResult()
            // through to MediaPicker
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchVideoIntent();
            } else {
                videoPermissionPending = true;
                String[] permissions = new String[]{Manifest.permission.CAMERA};
                if (fragment != null) {
                    fragment.requestPermissions(permissions, REQUEST_CAMERA_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(activity, permissions, REQUEST_CAMERA_PERMISSION);
                }
            }
        } else {
            launchVideoIntent();
        }
    }

    /**
     * Fires an intent to launch the built in camera app in video mode
     */
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


    /**
     * Utility method to check if the app declares the {@link android.Manifest.permission#CAMERA}
     * permission in its manifest.
     *
     * @return {@code true} if the manifest declares the permission, {@code false} otherwise
     */
    private boolean hasCameraPermissionInManifest() {
        final String packageName = getContext().getPackageName();
        try {
            final PackageInfo packageInfo = getContext().getPackageManager()
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


    /**
     * Uses either the {@code fragment} or {@code activity} associated with this MediaPicker in that
     * order to start the given {@link Intent} with the given requestCode.
     *
     * @param intent The {@link Intent} to start
     * @param requestCode The requestCode to start {@code intent} with
     *
     * @see Activity#startActivityForResult(Intent, int)
     * @see Fragment#startActivityForResult(Intent, int)
     */
    private void startActivityForResult(Intent intent, int requestCode) {
        cameraPermissionPending = false;
        videoPermissionPending = false;
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Get a reference to a temporary file to use as the destination when capturing a new photo
     * with {@link MediaPicker#launchCameraIntent()}. The preferred location will be the app's
     * pictures directory in the external storage, with a fallback to the private cache directory if
     * external storage is not available.
     *
     * @param context A {@link Context} object to use to obtain the app's directory locations
     * @return A {@link Uri} representing the file location to use
     */
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
        return FileProvider.getUriForFile(context, context.getPackageName() + ".androidtools.fileprovider", image);
    }


    /**
     * Listener interface to receive media selection events
     *
     * @see MediaPicker#setMediaPickerListener(MediaPickerListener)
     */
    public interface MediaPickerListener {
        /**
         * This method will be invoked each time a user selects a media file as a result of
         * {@link MediaPicker#showChooser()}.
         *
         * @param fileUri A {@link Uri} representing the media object selected by the user
         * @param mediaType A {@link MediaType} indicating which source this media object came from.
         *                  Can be one of {@link MediaType#PHOTO_CAMERA},
         *                  {@link MediaType#PHOTO_LIBRARY}, or {@link MediaType#VIDEO}.
         */
        void onMediaSelected(Uri fileUri, MediaType mediaType);
    }
}
