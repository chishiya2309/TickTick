package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

/**
 * Helper class để kiểm tra và quản lý permissions cho Camera và Storage
 */
public class PermissionHelper {

    // Permission request codes
    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_GALLERY_PERMISSION = 101;

    /**
     * Kiểm tra quyền Camera
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Kiểm tra quyền đọc ảnh từ thư viện
     * Xử lý khác nhau cho Android 13+ (READ_MEDIA_IMAGES) và các phiên bản cũ hơn (READ_EXTERNAL_STORAGE)
     */
    public static boolean hasGalleryPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 và thấp hơn
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Lấy danh sách permissions cần request cho Camera
     */
    public static String[] getCameraPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    /**
     * Lấy danh sách permissions cần request cho Gallery
     * Trả về permission phù hợp với phiên bản Android
     */
    public static String[] getGalleryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            // Android 12 và thấp hơn
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    /**
     * Kiểm tra xem thiết bị có camera không
     */
    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
}
