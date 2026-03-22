package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;

public class ImageAttachmentBottomSheet extends BottomSheetDialogFragment {

    public interface OnImageSourceSelectedListener {
        void onCameraSelected();
        void onGallerySelected();
    }

    private OnImageSourceSelectedListener listener;

    public void setOnImageSourceSelectedListener(OnImageSourceSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_image_attachment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout btnCamera = view.findViewById(R.id.btnCamera);
        LinearLayout btnGallery = view.findViewById(R.id.btnGallery);

        btnCamera.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCameraSelected();
            }
            dismiss();
        });

        btnGallery.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGallerySelected();
            }
            dismiss();
        });
    }
}
