package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.TaskImageAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.TaskModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.ImageFileHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.PermissionHelper;

public class TaskDetailBottomSheet extends BottomSheetDialogFragment {

    private TaskModel task;
    private TaskDatabaseHelper dbHelper;
    private OnTaskUpdatedListener updateListener;
    private String highlightKeyword = "";
    private TaskImageAdapter imageAdapter;
    private RecyclerView rvTaskImages;
    private List<String> taskImages;
    
    // Camera và Gallery launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> galleryPermissionLauncher;
    
    // Biến tạm để lưu URI ảnh từ camera
    private Uri tempCameraImageUri;
    private File tempCameraImageFile;

    public interface OnTaskUpdatedListener {
        void onTaskUpdated();
    }

    public TaskDetailBottomSheet(TaskModel task) {
        this.task = task;
    }

    public void setHighlightKeyword(String keyword) {
        this.highlightKeyword = keyword != null ? keyword : "";
    }

    public void setOnTaskUpdatedListener(OnTaskUpdatedListener listener) {
        this.updateListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLaunchers();
    }

    /**
     * Khởi tạo các ActivityResultLauncher cho Camera và Gallery
     */
    private void initializeLaunchers() {
        // Camera Launcher - Xử lý kết quả chụp ảnh
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Ảnh đã được lưu vào tempCameraImageUri
                    if (tempCameraImageUri != null) {
                        onImageCaptured(tempCameraImageUri);
                    }
                } else {
                    // User hủy hoặc có lỗi
                    Toast.makeText(requireContext(), "Đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                    cleanupTempCameraFile();
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

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                
                // Cần set height của bottom sheet container là MATCH_PARENT để cho phép vuốt lên full screen
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    bottomSheet.setLayoutParams(layoutParams);
                }

                // Set peek height to 50% of screen height for Figure 1
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight(screenHeight / 2);
                behavior.setSkipCollapsed(false); // Enable collapsed state
                
                // Start at peek height (collapsed)
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        View backBtn = getView() != null ? getView().findViewById(R.id.detail_back_button) : null;
                        if (backBtn != null) {
                            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                                backBtn.setVisibility(View.VISIBLE);
                            } else {
                                backBtn.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        // Optional: Fade in the back button based on slideOffset
                        View backBtn = getView() != null ? getView().findViewById(R.id.detail_back_button) : null;
                        if (backBtn != null) {
                            if (slideOffset > 0.8f) {
                                backBtn.setVisibility(View.VISIBLE);
                                backBtn.setAlpha((slideOffset - 0.8f) * 5f);
                            } else if (slideOffset < 0.8f) {
                                backBtn.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = TaskDatabaseHelper.getInstance(requireContext());
        taskImages = new ArrayList<>();

        // Header Views
        TextView textListName = view.findViewById(R.id.detail_list_name);
        ImageView listIcon = view.findViewById(R.id.detail_list_icon);
        TextView listEmoji = view.findViewById(R.id.detail_list_emoji);

        // Content Views
        CheckBox checkbox = view.findViewById(R.id.detail_checkbox);
        TextView textDateTag = view.findViewById(R.id.detail_date_tag);
        EditText editTitle = view.findViewById(R.id.detail_task_title);
        EditText editDescription = view.findViewById(R.id.detail_task_description);

        // Setup RecyclerView for images FIRST (before binding data)
        rvTaskImages = view.findViewById(R.id.rvTaskImages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), 
                LinearLayoutManager.HORIZONTAL, false);
        rvTaskImages.setLayoutManager(layoutManager);
        
        imageAdapter = new TaskImageAdapter();
        imageAdapter.setOnImageActionListener(new TaskImageAdapter.OnImageActionListener() {
            @Override
            public void onImageClick(String imageUri, int position) {
                // TODO: Mở ảnh full screen
                Toast.makeText(requireContext(), "Xem ảnh: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImageDelete(String imageUri, int position) {
                // Xóa khỏi danh sách trước
                if (position >= 0 && position < taskImages.size()) {
                    taskImages.remove(position);
                }
                
                // Sau đó xóa khỏi adapter
                imageAdapter.removeImage(position);
                updateImageVisibility();
                
                // Lưu vào database
                if (task != null) {
                    task.setImageUris(taskImages);
                    dbHelper.updateTaskImages(task.getId(), taskImages);
                }
                
                Toast.makeText(requireContext(), "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
            }
        });
        rvTaskImages.setAdapter(imageAdapter);

        // Bind data
        if (task != null) {
            int colorHighlight = android.graphics.Color.parseColor("#f59e0b");
            editTitle.setText(getHighlightedText(task.getTitle(), highlightKeyword, colorHighlight), TextView.BufferType.EDITABLE);
            if (task.getDescription() != null) {
                editDescription.setText(getHighlightedText(task.getDescription(), highlightKeyword, colorHighlight), TextView.BufferType.EDITABLE);
            }
            checkbox.setChecked(task.isCompleted());

            // Date tag
            if (task.getDateTag() != null && !task.getDateTag().isEmpty()) {
                textDateTag.setText(task.getDateTag() + " ");
                textDateTag.setVisibility(View.VISIBLE);
            } else {
                textDateTag.setVisibility(View.GONE);
            }

            // List Name & Icon
            String listName = dbHelper.getListNameById(task.getListId());
            textListName.setText(listName);

            int iconResId = dbHelper.getListIconResId(requireContext(), task.getListId());
            if (iconResId != 0) {
                listIcon.setImageResource(iconResId);
                listIcon.setVisibility(View.VISIBLE);
                listEmoji.setVisibility(View.GONE);
            } else {
                String emoji = dbHelper.getListEmojiById(task.getListId());
                if (emoji != null && !emoji.isEmpty()) {
                    listEmoji.setText(emoji);
                    listEmoji.setVisibility(View.VISIBLE);
                    listIcon.setVisibility(View.GONE);
                }
            }
            
            // Load existing images
            if (task.getImageUris() != null && !task.getImageUris().isEmpty()) {
                taskImages = new ArrayList<>(task.getImageUris());
                imageAdapter.setImages(taskImages);
                updateImageVisibility();
            }
        }

        // Actions
        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (task != null) {
                task.setCompleted(isChecked);
                dbHelper.updateTaskCompleted(task.getId(), isChecked);
                if (updateListener != null) {
                    updateListener.onTaskUpdated();
                }
            }
        });

        // Save modifications when the dialog is dismissed
        // Since sqlite database for TickTick doesn't have a description column, we only
        // save title.
        
        // Back Button
        ImageView backButton = view.findViewById(R.id.detail_back_button);
        backButton.setOnClickListener(v -> {
            // "về màn hình activity_main" -> Dismiss
            dismiss();
        });

        // Attach button
        ImageButton btnAttach = view.findViewById(R.id.detail_action_attach);
        btnAttach.setOnClickListener(v -> showImageAttachmentOptions());
    }

    private void showImageAttachmentOptions() {
        ImageAttachmentBottomSheet bottomSheet = new ImageAttachmentBottomSheet();
        bottomSheet.setOnImageSourceSelectedListener(new ImageAttachmentBottomSheet.OnImageSourceSelectedListener() {
            @Override
            public void onCameraSelected() {
                openCamera();
            }

            @Override
            public void onGallerySelected() {
                openGallery();
            }
        });
        bottomSheet.show(getParentFragmentManager(), "ImageAttachmentBottomSheet");
    }

    /**
     * Mở Camera để chụp ảnh
     * Tự động kiểm tra và request permission nếu cần
     */
    private void openCamera() {
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
    private void openGallery() {
        // Kiểm tra permission
        if (!PermissionHelper.hasGalleryPermission(requireContext())) {
            // Chưa có permission, request
            galleryPermissionLauncher.launch(PermissionHelper.getGalleryPermissions());
            return;
        }
        
        // Đã có permission, mở gallery
        launchGallery();
    }

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
        tempCameraImageFile = ImageFileHelper.createImageFile(requireContext());
        if (tempCameraImageFile == null) {
            Toast.makeText(requireContext(), 
                "Không thể tạo file ảnh", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Chuyển File thành Uri an toàn qua FileProvider
        tempCameraImageUri = ImageFileHelper.getUriForFile(requireContext(), tempCameraImageFile);
        if (tempCameraImageUri == null) {
            Toast.makeText(requireContext(), 
                "Lỗi FileProvider", 
                Toast.LENGTH_SHORT).show();
            cleanupTempCameraFile();
            return;
        }
        
        // Gửi Uri cho Camera app
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraImageUri);
        
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

    /**
     * Callback khi chụp ảnh thành công
     */
    private void onImageCaptured(Uri imageUri) {
        addImageToTask(imageUri.toString());
        
        // Reset temp variables
        tempCameraImageUri = null;
        tempCameraImageFile = null;
    }
    
    /**
     * Callback khi chọn ảnh từ gallery thành công
     */
    private void onImageSelected(Uri imageUri) {
        addImageToTask(imageUri.toString());
    }
    
    /**
     * Thêm ảnh vào task và cập nhật giao diện
     */
    private void addImageToTask(String imageUri) {
        taskImages.add(imageUri);
        imageAdapter.addImage(imageUri);
        updateImageVisibility();
        
        // Lưu vào database
        if (task != null) {
            task.setImageUris(taskImages);
            dbHelper.updateTaskImages(task.getId(), taskImages);
        }
        
        Toast.makeText(requireContext(), "Đã thêm ảnh", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Dọn dẹp file tạm nếu có lỗi
     */
    private void cleanupTempCameraFile() {
        if (tempCameraImageFile != null && tempCameraImageFile.exists()) {
            tempCameraImageFile.delete();
        }
        tempCameraImageUri = null;
        tempCameraImageFile = null;
    }

    private void updateImageVisibility() {
        if (taskImages.isEmpty()) {
            rvTaskImages.setVisibility(View.GONE);
        } else {
            rvTaskImages.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Dọn dẹp file tạm nếu còn
        cleanupTempCameraFile();
        
        // Save modifications when the dialog is dismissed
        if (task != null) {
            View view = getView();
            if (view != null) {
                EditText editTitle = view.findViewById(R.id.detail_task_title);
                EditText editDescription = view.findViewById(R.id.detail_task_description);
                String newTitle = editTitle.getText().toString().trim();
                String newDescription = editDescription.getText().toString().trim();

                boolean hasChanged = false;
                if (!newTitle.isEmpty() && !newTitle.equals(task.getTitle())) {
                    task.setTitle(newTitle);
                    hasChanged = true;
                }

                String oldDescription = task.getDescription() == null ? "" : task.getDescription();
                if (!newDescription.equals(oldDescription)) {
                    task.setDescription(newDescription);
                    hasChanged = true;
                }

                if (hasChanged) {
                    dbHelper.updateTaskDetails(task.getId(), task.getTitle(), task.getDescription());
                    if (updateListener != null) {
                        updateListener.onTaskUpdated();
                    }
                }
            }
        }
    }

    private CharSequence getHighlightedText(String text, String keyword, int color) {
        if (keyword == null || keyword.isEmpty() || text == null || text.isEmpty()) {
            return text != null ? text : "";
        }

        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        android.text.SpannableString spannable = new android.text.SpannableString(text);

        int start = lowerText.indexOf(lowerKeyword);
        while (start >= 0) {
            int end = start + lowerKeyword.length();
            spannable.setSpan(new android.text.style.ForegroundColorSpan(color), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = lowerText.indexOf(lowerKeyword, end);
        }
        return spannable;
    }
}
