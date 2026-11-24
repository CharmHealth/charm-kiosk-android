package com.charm.ehr.kiosk.common.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.charm.ehr.kiosk.presentation.tour_page.SessionManager

/**
 * Utility for handling camera and gallery permissions and intents.
 */
object MediaUtil {
    const val REQUEST_CAMERA_PERMISSION = 1001
    const val REQUEST_GALLERY_PERMISSION = 1002
    const val REQUEST_WRITE_PERMISSION = 1006

    const val isCameraPermissionDenied = "isCameraPermissionDenied"

    fun requestCameraPermission(
        context: Activity,
        permissionGrantedCallback: (() -> Unit),
        requestPermissionCallback: (() -> Unit),
        permissionDeniedCallBack: (() -> Unit),
    ) {
        checkAndRequestPermission(
            context,
            Manifest.permission.CAMERA,
            isCameraPermissionDenied,
            permissionGrantedCallback,
            requestPermissionCallback,
            permissionDeniedCallBack

        )
    }

    fun checkPermission(context: Context, permission: String): Boolean {

        return (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun checkAndRequestPermission(
        context: Activity,
        permission: String,
        deniedModelUtilPerm: String,
        permissionGrantedCallback: (() -> Unit),
        requestPermissionCallback: (() -> Unit),
        permissionDeniedCallBack: (() -> Unit),
    ) {
        if (!checkPermission(context, permission)) {
            if (getPermissionDenied(context, deniedModelUtilPerm)) {
                permissionDeniedCallBack()
            } else {
                requestPermissionCallback()
            }
        } else {
            //Granted
            permissionGrantedCallback()
        }
    }


    /** Check if write permission is granted (only required pre-Android 10 for external URIs) */
    fun hasWritePermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    /** Check if gallery (read) permission is granted */
    fun hasGalleryPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    /** Request camera permission */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    /** Request gallery (read) permission */
    fun requestGalleryPermission(activity: Activity) {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(perm),
            REQUEST_GALLERY_PERMISSION
        )
    }

    /** Request write permission for external storage (only for API<29) */
    fun requestWritePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_PERMISSION
            )
        }
    }

    /** Create content URI for storing a captured image */
    fun createImageUri(activity: Activity): Uri? {
        return FileUtil.createFileForType(activity, FileUtil.FileType.JPG).let { file ->
            file?.let {
                FileProvider.getUriForFile(
                    activity,
                    activity.applicationContext.packageName + ".provider",
                    file
                )
            }
        }
    }

    /** Build camera intent (caller should use ActivityResultLauncher) */
    fun buildCameraIntent(outputUri: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        }
    }

    /**
     * Returns a ManagedActivityResultLauncher for starting an activity for result (e.g., camera or gallery).
     * When the launched activity returns, it calls the provided onResult callback and sets passcode allowed to true.
     *
     * @param onResult Callback to handle the ActivityResult returned from the launched activity.
     * @param sessionManager SessionManager instance to update passcode state after returning from the activity.
     * @return ManagedActivityResultLauncher to be used for launching intents and receiving results.
     */
    @Composable
    fun rememberActivityResultLauncherWithPasscodeReset(
        onResult: (ActivityResult) -> Unit,
        sessionManager: SessionManager
    ): ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onResult(it)
            Log.d("MediaUtil", "ActivityResult received, resetting passcode allowed")
            // Set passcode allowed to true when returning from camera
            sessionManager.updateIsPasscodeAllowed(true)
        }
    }


    fun launchCamera(
        activity: Activity?,
        cameraLauncher: ActivityResultLauncher<Intent>,
        sessionManager: SessionManager
    ): Uri? {
        if (activity != null) {
            val uri = createImageUri(activity)
            if (uri != null) {
                val intent = buildCameraIntent(uri)
                Log.d("MediaUtil", "Setting passcode allowed to false before launching camera")
                sessionManager.updateIsPasscodeAllowed(false)
                cameraLauncher.launch(intent)
            }
            return uri
        }
        return null
    }

    /** Build single image picker intent */
    fun buildPickSingleImageIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
    }

    /** Build multiple image picker intent */
    fun buildPickMultipleImagesIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }


    private fun setPermissionDenied(context: Activity, deniedPermission: String) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        val deniedModelUtilPerm = getDeniedParam(deniedPermission)
        pref.putBoolean(deniedModelUtilPerm, true)
        pref.commit()
    }

    private fun getDeniedParam(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> {
                isCameraPermissionDenied
            }

            else -> {
                ""
            }
        }
    }

    private fun getPermissionDenied(
        context: Activity,
        deniedModelUtilPerm: String
    ): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getBoolean(deniedModelUtilPerm, false)
    }

    fun permissionDeniedHandler(
        context: Activity,
        permission: String,
        permissionDeniedCallBack: (() -> Unit)
    ) {
        if (shouldShowRequestPermissionRationale(context, permission) && !getPermissionDenied(
                context,
                permission
            )
        ) {
            setPermissionDenied(context, permission)
            permissionDeniedCallBack()
        }
    }

    private fun shouldShowRequestPermissionRationale(
        context: Activity,
        permission: String
    ): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
    }

    fun intentToSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }


}
