package hcmute.edu.vn.lequanghung_23110110.ticktick.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.CalendarHelper;

public class SettingsBottomSheet extends BottomSheetDialogFragment {

    private SwitchCompat switchCalendarSync;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchCalendarSync = view.findViewById(R.id.settings_switch_calendar_sync);
        CalendarHelper calHelper = CalendarHelper.getInstance(requireContext());

        // Khởi tạo trạng thái ban đầu của switch
        switchCalendarSync.setChecked(calHelper.isSyncEnabled());

        // Lắng nghe thay đổi toggle
        switchCalendarSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            calHelper.setSyncEnabled(isChecked);
            String message = isChecked ? "Đã bật đồng bộ Google Calendar" : "Đã tắt đồng bộ Google Calendar";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }
}
