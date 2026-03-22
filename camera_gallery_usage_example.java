// ============================================================================
// DEMO CODE: Cách sử dụng Camera và Gallery với FileProvider
// File này chỉ để tham khảo, KHÔNG compile trực tiếp
// ============================================================================

package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;

import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.ImageFileHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.PermissionHelper;

/**
 * DEMO: Cách tích hợp Camera và Gallery vào Fragment/Activity
 * 
 * Các bước chính:
 * 1. Khai báo ActivityResultLauncher
 * 2. Khai báo permission launcher
 * 3. Khởi tạo launchers trong onCreate/onAttach
 * 4. Implement methods để mở camera/gallery
 * 5. Xử lý kết quả
 */
public class CameraGalleryUsageExample extends Fragment {

    // ========================================================================
    // BƯỚC 1: Khai báo các launcher và biến
    // ========================================================================
    
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> galleryPermissionLauncher;
    
    private Uri tempImageUri;
    private File tempImageFile;

    // ========================================================================
    // BƯỚC 2: Khởi tạo launchers trong onCreate hoặc onAttach
    // ========================================================================
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Camera Launcher - Xử lý kết quả chụp ảnh
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Ảnh đã được lưu vào tempImageUri
                    if (tempImageUri != null) {
                        onImageCaptured(tempImageUri);
                    }
                } else {
                    // User hủy hoặc có lỗi
                    Toast.makeText(requireContext(), "Đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                    cleanupTempFile();
                }
            }
        );
        
        // Gallery Launcher - Xử lý kết quả chọn ảnh
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        onImageSelected(selectedImageUri);
                    }
                } else {
                    Toast.makeText(requireContext(), "Không chọn ảnh nào", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        // Camera Permission Launcher
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean cameraGranted = permissions.get(android.Manifest.permission.CAMERA);
                if (cameraGranted != null && cameraGranted) {
                    // Permission được cấp, mở camera
                    launchCamera();
                } else {
                    // Permission bị từ chối
                    Toast.makeText(requireContext(), 
                        "Cần quyền Camera để chụp ảnh", 
                        Toast.LENGTH_LONG).show();
                }
            }
        );
        
        // Gallery Permission Launcher
        galleryPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                // Kiểm tra permission phù hợp với phiên bản Android
                boolean granted = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    Boolean mediaGranted = permissions.get(android.Manifest.permission.READ_MEDIA_IMAGES);
                    granted = mediaGranted != null && mediaGranted;
                } else {
                    Boolean storageGranted = permissions.get(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    granted = storageGranted != null && storageGranted;
                }
                
                if (granted) {
                    // Permission được cấp, mở gallery
                    launchGallery();
                } else {
                    // Permission bị từ chối
                    Toast.makeText(requireContext(), 
                        "Cần quyền truy cập ảnh để chọn từ thư viện", 
                        Toast.LENGTH_LONG).show();
                }
            }
        );
    }

    // ========================================================================
    // BƯỚC 3: Public methods để mở Camera/Gallery
    // ========================================================================
    
    /**
     * Mở Camera để chụp ảnh
     * Tự động kiểm tra và request permission nếu cần
     */
    public void openCamera() {
        // Kiểm tra thiết bị có camera không
        if (!PermissionHelper.hasCamera(requireContext())) {
            Toast.makeText(requireContext(), 
                "Thiết bị không có camera", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Kiểm tra permission
        if (!PermissionHelper.hasCameraPermission(requireContext())) {
            // Chưa có permission, request
            cameraPermissionLauncher.launch(PermissionHelper.getCameraPermissions());
            return;
        }
        
        // Đã có permission, mở camera
        launchCamera();
    }
    
    /**
     * Mở Gallery để chọn ảnh
     * Tự động kiểm tra và request permission nếu cần
     */
    public void openGallery() {
        // Kiểm tra permission
        if (!PermissionHelper.hasGalleryPermission(requireContext())) {
            // Chưa có permission, request
            galleryPermissionLauncher.launch(PermissionHelper.getGalleryPermissions());
            return;
        }
        
        // Đã có permission, mở gallery
        launchGallery();
    }

    // ========================================================================
    // BƯỚC 4: Private methods để launch Intent
    // ========================================================================
    
    /**
     * Launch Camera Intent
     */
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Kiểm tra có app Camera không
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) == null) {
            Toast.makeText(requireContext(), 
                "Không tìm thấy ứng dụng Camera", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo file để lưu ảnh
        tempImageFile = ImageFileHelper.createImageFile(requireContext());
        if (tempImageFile == null) {
            Toast.makeText(requireContext(), 
                "Không thể tạo file ảnh", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Chuyển File thành Uri an toàn qua FileProvider
        tempImageUri = ImageFileHelper.getUriForFile(requireContext(), tempImageFile);
        if (tempImageUri == null) {
            Toast.makeText(requireContext(), 
                "Lỗi FileProvider", 
                Toast.LENGTH_SHORT).show();
            cleanupTempFile();
            return;
        }
        
        // Gửi Uri cho Camera app
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
        
        // Launch camera
        cameraLauncher.launch(takePictureIntent);
    }
    
    /**
     * Launch Gallery Intent
     */
    private void launchGallery() {
        Intent pickPhotoIntent = new Intent(
            Intent.ACTION_PICK, 
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        
        // Chỉ chọn ảnh
        pickPhotoIntent.setType("image/*");
        
        // Launch gallery
        galleryLauncher.launch(pickPhotoIntent);
    }

    // ========================================================================
    // BƯỚC 5: Xử lý kết quả
    // ========================================================================
    
    /**
     * Callback khi chụp ảnh thành công
     */
    private void onImageCaptured(Uri imageUri) {
        Toast.makeText(requireContext(), 
            "Đã chụp ảnh: " + imageUri.toString(), 
            Toast.LENGTH_SHORT).show();
        
        // TODO: Thêm ảnh vào RecyclerView
        // TODO: Lưu Uri vào database
        // addImageToTask(imageUri.toString());
        
        // Lưu đường dẫn file thực tế nếu cần
        if (tempImageFile != null) {
            String filePath = tempImageFile.getAbsolutePath();
            // TODO: Lưu filePath vào database nếu cần
        }
        
        // Reset temp variables
        tempImageUri = null;
        tempImageFile = null;
    }
    
    /**
     * Callback khi chọn ảnh từ gallery thành công
     */
    private void onImageSelected(Uri imageUri) {
        Toast.makeText(requireContext(), 
            "Đã chọn ảnh: " + imageUri.toString(), 
            Toast.LENGTH_SHORT).show();
        
        // TODO: Thêm ảnh vào RecyclerView
        // TODO: Lưu Uri vào database
        // addImageToTask(imageUri.toString());
        
        // Lưu ý: Uri từ gallery là content:// URI
        // Không cần copy file, sử dụng Uri trực tiếp
    }
    
    /**
     * Dọn dẹp file tạm nếu có lỗi
     */
    private void cleanupTempFile() {
        if (tempImageFile != null && tempImageFile.exists()) {
            tempImageFile.delete();
        }
        tempImageUri = null;
        tempImageFile = null;
    }

    // ========================================================================
    // BƯỚC 6: Cleanup khi destroy
    // ========================================================================
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dọn dẹp file tạm nếu còn
        cleanupTempFile();
    }

    // ========================================================================
    // BONUS: Xử lý permission bị từ chối vĩnh viễn
    // ========================================================================
    
    /**
     * Kiểm tra xem user có từ chối permission vĩnh viễn không
     * (Chọn "Don't ask again")
     */
    private boolean shouldShowPermissionRationale(String permission) {
        return shouldShowRequestPermissionRationale(permission);
    }
    
    /**
     * Hiển thị dialog giải thích tại sao cần permission
     * và hướng dẫn user vào Settings để cấp quyền
     */
    private void showPermissionRationaleDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cần quyền truy cập")
            .setMessage(message)
            .setPositiveButton("Đi tới Cài đặt", (dialog, which) -> {
                // Mở Settings app
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}

// ============================================================================
// CÁCH SỬ DỤNG TRONG TaskDetailBottomSheet
// ============================================================================

/*
1. Copy các launcher declarations vào TaskDetailBottomSheet
2. Copy các initialization code vào onCreate()
3. Thay đổi showImageAttachmentOptions():

private void showImageAttachmentOptions() {
    ImageAttachmentBottomSheet bottomSheet = new ImageAttachmentBottomSheet();
    bottomSheet.setOnImageSourceSelectedListener(new ImageAttachmentBottomSheet.OnImageSourceSelectedListener() {
        @Override
        public void onCameraSelected() {
            openCamera();  // Gọi method openCamera()
        }

        @Override
        public void onGallerySelected() {
            openGallery();  // Gọi method openGallery()
        }
    });
    bottomSheet.show(getParentFragmentManager(), "ImageAttachmentBottomSheet");
}

4. Implement onImageCaptured() và onImageSelected() để thêm ảnh vào adapter:

private void onImageCaptured(Uri imageUri) {
    addImageToTask(imageUri.toString());
}

private void onImageSelected(Uri imageUri) {
    addImageToTask(imageUri.toString());
}

private void addImageToTask(String imageUri) {
    taskImages.add(imageUri);
    imageAdapter.addImage(imageUri);
    updateImageVisibility();
    
    // Save to database
    if (task != null) {
        task.setImageList(taskImages);
        dbHelper.updateTaskImages(task.getId(), task.getImageUris());
    }
    
    Toast.makeText(requireContext(), "Đã thêm ảnh", Toast.LENGTH_SHORT).show();
}
*/

// ============================================================================
// LƯU Ý QUAN TRỌNG
// ============================================================================

/*
1. LUÔN kiểm tra permission trước khi sử dụng Camera/Gallery
2. Sử dụng FileProvider cho Camera (Android 7.0+)
3. Xử lý trường hợp user từ chối permission
4. Cleanup temp files khi không cần
5. Test trên nhiều phiên bản Android khác nhau
6. Xử lý trường hợp thiết bị không có camera
7. Xử lý trường hợp không có app Camera/Gallery
8. Nén ảnh nếu cần để tiết kiệm dung lượng
9. Giới hạn số lượng ảnh tối đa
10. Backup ảnh khi user gỡ app (nếu cần)
*/
