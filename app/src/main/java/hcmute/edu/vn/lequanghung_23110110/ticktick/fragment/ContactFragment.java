package hcmute.edu.vn.lequanghung_23110110.ticktick.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.adapter.ContactAdapter;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.dialog.DatePickerBottomSheet;
import hcmute.edu.vn.lequanghung_23110110.ticktick.model.ContactModel;
import hcmute.edu.vn.lequanghung_23110110.ticktick.provider.SyncedContactContract;

public class ContactFragment extends Fragment {

    private EditText etSearch;
    private ImageView btnClear;
    private RecyclerView recyclerView;
    private View emptyState;
    private ContactAdapter adapter;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    if (!entry.getValue()) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    syncContactsFromPhone();
                } else {
                    handlePermissionDenied();
                }
            });

    public ContactFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        etSearch = view.findViewById(R.id.et_contact_search);
        btnClear = view.findViewById(R.id.btn_clear_contact_search);
        recyclerView = view.findViewById(R.id.recyclerViewContacts);
        emptyState = view.findViewById(R.id.contact_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSearch();
        checkAndRequestPermissions();

        return view;
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClear.setOnClickListener(v -> etSearch.setText(""));
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            syncContactsFromPhone();
        } else {
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void handlePermissionDenied() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) ||
            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
            
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cần cấp quyền")
                    .setMessage("Ứng dụng cần quyền truy cập danh bạ và gọi điện để thực hiện chức năng này.")
                    .setPositiveButton("Thử lại", (dialog, which) -> checkAndRequestPermissions())
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Quyền bị từ chối")
                    .setMessage("Bạn đã từ chối quyền vĩnh viễn. Vui lòng vào Cài đặt để cấp quyền cho ứng dụng.")
                    .setPositiveButton("Cài đặt", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

    private void syncContactsFromPhone() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 1. Xóa toàn bộ dữ liệu cũ trong Custom Provider
            requireContext().getContentResolver().delete(SyncedContactContract.ContactEntry.CONTENT_URI, null, null);

            Cursor cursor = null;
            try {
                // 2. Đọc danh bạ gốc
                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String[] projection = {
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };

                cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);

                if (cursor != null) {
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    while (cursor.moveToNext()) {
                        String name = cursor.getString(nameIndex);
                        String number = cursor.getString(numberIndex);

                        // 3. Chèn vào Custom Provider
                        ContentValues values = new ContentValues();
                        values.put(SyncedContactContract.ContactEntry.COLUMN_NAME, name);
                        values.put(SyncedContactContract.ContactEntry.COLUMN_PHONE, number);
                        requireContext().getContentResolver().insert(SyncedContactContract.ContactEntry.CONTENT_URI, values);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }

            // 4. Hoàn tất, tải lại dữ liệu từ Custom Provider lên UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadContactsFromCustomProvider);
            }
        });
    }

    private void loadContactsFromCustomProvider() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<ContactModel> contactList = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = requireContext().getContentResolver().query(
                        SyncedContactContract.ContactEntry.CONTENT_URI,
                        null, null, null, 
                        SyncedContactContract.ContactEntry.COLUMN_NAME + " ASC"
                );

                if (cursor != null) {
                    int idIndex = cursor.getColumnIndex(SyncedContactContract.ContactEntry._ID);
                    int nameIndex = cursor.getColumnIndex(SyncedContactContract.ContactEntry.COLUMN_NAME);
                    int phoneIndex = cursor.getColumnIndex(SyncedContactContract.ContactEntry.COLUMN_PHONE);

                    while (cursor.moveToNext()) {
                        String id = String.valueOf(cursor.getLong(idIndex));
                        String name = cursor.getString(nameIndex);
                        String phone = cursor.getString(phoneIndex);
                        contactList.add(new ContactModel(id, name, phone, null));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (contactList.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        adapter = new ContactAdapter(contactList, new ContactAdapter.OnContactActionListener() {
                            @Override
                            public void onCallClick(ContactModel contact) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
                                startActivity(intent);
                            }

                            @Override
                            public void onSmsClick(ContactModel contact) {
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("smsto:" + contact.getPhoneNumber()));
                                startActivity(intent);
                            }

                            @Override
                            public void onAddTaskClick(ContactModel contact) {
                                showAddTaskBottomSheet(contact.getName());
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private void showAddTaskBottomSheet(String contactName) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_add_task, null);
        bottomSheet.setContentView(sheetView);

        EditText inputTitle = sheetView.findViewById(R.id.input_task_title);
        EditText inputDescription = sheetView.findViewById(R.id.input_task_description);
        View dateChipContainer = sheetView.findViewById(R.id.date_chip_container);
        TextView dateChipText = sheetView.findViewById(R.id.date_chip_text);
        View actionDate = sheetView.findViewById(R.id.action_date);

        inputTitle.setText("Liên hệ với " + contactName);
        inputTitle.setSelection(inputTitle.getText().length());

        final String[] selectedDateTag = { "" };
        final long[] selectedDateMillis = { -1 };
        final int[] selectedHour = { -1 };
        final int[] selectedMinute = { -1 };
        final List<String>[] selectedReminders = new List[]{ new ArrayList<>() };

        Runnable openDatePicker = () -> {
            DatePickerBottomSheet datePicker = new DatePickerBottomSheet();
            datePicker.setOnDateSelectedListener((dateTag, dateMillis, reminders) -> {
                selectedDateTag[0] = dateTag;
                selectedDateMillis[0] = dateMillis;
                selectedReminders[0] = reminders;
                dateChipText.setText(dateTag);
                dateChipContainer.setVisibility(View.VISIBLE);
                actionDate.setVisibility(View.GONE);
                dateChipText.setTextColor(getDateChipColor(dateTag));
            });
            datePicker.setOnDateClearedListener(() -> {
                selectedDateTag[0] = ""; selectedDateMillis[0] = -1;
                selectedHour[0] = -1; selectedMinute[0] = -1;
                selectedReminders[0] = new ArrayList<>();
                dateChipContainer.setVisibility(View.GONE);
                actionDate.setVisibility(View.VISIBLE);
            });
            datePicker.setOnTimeSelectedListener((h, m) -> { selectedHour[0] = h; selectedMinute[0] = m; });
            if (selectedDateMillis[0] > 0) datePicker.setPreSelectedDate(selectedDateMillis[0]);
            if (selectedHour[0] >= 0) datePicker.setPreSelectedTime(selectedHour[0], selectedMinute[0]);
            datePicker.show(getChildFragmentManager(), "date_picker");
        };

        actionDate.setOnClickListener(v -> openDatePicker.run());
        dateChipContainer.setOnClickListener(v -> openDatePicker.run());

        sheetView.findViewById(R.id.btn_submit_task).setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                inputTitle.setError("Nhập tiêu đề task");
                return;
            }

            long finalDueDate = selectedDateMillis[0];
            if (finalDueDate > 0 && selectedHour[0] >= 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(finalDueDate);
                cal.set(Calendar.HOUR_OF_DAY, selectedHour[0]);
                cal.set(Calendar.MINUTE, selectedMinute[0]);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                finalDueDate = cal.getTimeInMillis();
            }
            
            TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(getContext());
            dbHelper.insertTask(title, inputDescription.getText().toString().trim(), 1, selectedDateTag[0], finalDueDate, selectedReminders[0]);
            
            bottomSheet.dismiss();
            Toast.makeText(getContext(), "Đã thêm công việc liên hệ", Toast.LENGTH_SHORT).show();
        });

        bottomSheet.show();
    }

    private int getDateChipColor(String dateTag) {
        switch (dateTag) {
            case "Hôm nay": return Color.parseColor("#4C6FE0");
            case "Ngày mai": return Color.parseColor("#FFA726");
            case "Thứ Hai tới": return Color.parseColor("#42A5F5");
            case "Đến cuối ngày": return Color.parseColor("#66BB6A");
            default: return Color.parseColor("#B0B0B0");
        }
    }
}
