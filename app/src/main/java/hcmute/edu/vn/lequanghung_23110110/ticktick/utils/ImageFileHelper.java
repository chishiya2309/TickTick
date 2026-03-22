package hcmute.edu.vn.lequanghung_23110110.ticktick.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class để quản lý file ảnh cho Task
 */
public class ImageFileHelper {

    private static final String IMAGE_PREFIX = "TASK_";
    private static final String IMAGE_EXTENSION = ".jpg";
    private static final String FILE_PROVIDER_AUTHORITY = "hcmute.edu.vn.lequanghung_23110110.ticktick.fileprovider";

    /**
     * Tạo file ảnh tạm thời trong thư mục Pictures
     * Sử dụng cho Camera để lưu ảnh chụp
     * 
     * @param context Context của ứng dụng
     * @return File object hoặc null nếu có lỗi
     */
    public static File createImageFile(Context context) {
        // Tạo tên file với timestamp để tránh trùng lặp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = IMAGE_PREFIX + timeStamp + "_";
        
        // Lấy thư mục Pictures trong external files directory
        // Đường dẫn: /storage/emulated/0/Android/data/[package]/files/Pictures/
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        try {
            // Tạo file tạm thời
            return File.createTempFile(
                imageFileName,      // prefix
                IMAGE_EXTENSION,    // suffix
                storageDir          // directory
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Chuyển đổi File thành Uri sử dụng FileProvider
     * Cần thiết cho Android 7.0+ để chia sẻ file an toàn
     * 
     * @param context Context của ứng dụng
     * @param file File cần chuyển đổi
     * @return Uri của file hoặc null nếu có lỗi
     */
    public static Uri getUriForFile(Context context, File file) {
        if (file == null) {
            return null;
        }
        
        try {
            return FileProvider.getUriForFile(
                context,
                FILE_PROVIDER_AUTHORITY,
                file
            );
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xóa file ảnh
     * 
     * @param filePath Đường dẫn đến file cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public static boolean deleteImageFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Kiểm tra xem file có tồn tại không
     * 
     * @param filePath Đường dẫn đến file
     * @return true nếu file tồn tại, false nếu không
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * Lấy kích thước file theo MB
     * 
     * @param filePath Đường dẫn đến file
     * @return Kích thước file theo MB
     */
    public static double getFileSizeMB(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return 0;
        }
        
        File file = new File(filePath);
        if (file.exists()) {
            long sizeInBytes = file.length();
            return sizeInBytes / (1024.0 * 1024.0);
        }
        
        return 0;
    }

    /**
     * Lấy thư mục Pictures của ứng dụng
     * 
     * @param context Context của ứng dụng
     * @return File object của thư mục Pictures
     */
    public static File getPicturesDirectory(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }
}
