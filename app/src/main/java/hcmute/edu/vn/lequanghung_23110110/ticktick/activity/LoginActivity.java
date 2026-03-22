package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.database.TaskDatabaseHelper;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.NetworkUtils;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SyncManager;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private FirebaseAuth firebaseAuth;
    private CredentialManager credentialManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.getSessionType() != SessionManager.SessionType.NONE) {
            navigateToMain();
            return;
        }

        findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.btn_guest_mode).setOnClickListener(v -> continueAsGuest());
    }

    private void signInWithGoogle() {
        requestGoogleCredential();
    }

    private void requestGoogleCredential() {
        String webClientId = resolveWebClientId();
        if (webClientId == null) {
            Log.e(TAG, "Missing default_web_client_id. Check google-services.json OAuth client config.");
            Toast.makeText(this, "Cau hinh Google Sign-In chua dung. Vui long cap nhat Firebase config.", Toast.LENGTH_LONG).show();
            return;
        }

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                // Always show account chooser (all Google accounts on the device)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            Log.d(TAG, "User cancelled Google account selector");
                            return;
                        }

                        if (e instanceof NoCredentialException) {
                            Log.w(TAG, "No Google credential available", e);
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Khong tim thay tai khoan Google tren thiet bi. Ban co the them tai khoan hoac tiep tuc Guest.",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        Log.e(TAG, "Get credential failed", e);
                        Toast.makeText(LoginActivity.this, "Dang nhap Google that bai", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private String resolveWebClientId() {
        int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (resId == 0) {
            return null;
        }

        String value = getString(resId);
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("YOUR_")) {
            return null;
        }

        return trimmed;
    }

    private void handleSignIn(Credential credential) {
        if (!(credential instanceof CustomCredential)) {
            Toast.makeText(this, "Loại credential không hỗ trợ", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomCredential customCredential = (CustomCredential) credential;
        if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
            Toast.makeText(this, "Credential type không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        GoogleIdTokenCredential googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(customCredential.getData());

        String idToken = googleIdTokenCredential.getIdToken();
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(firebaseCredential).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = firebaseAuth.getCurrentUser().getUid();
            String email = firebaseAuth.getCurrentUser().getEmail();
            String displayName = firebaseAuth.getCurrentUser().getDisplayName();
            String photoUrl = firebaseAuth.getCurrentUser().getPhotoUrl() != null
                    ? firebaseAuth.getCurrentUser().getPhotoUrl().toString()
                    : null;

            // Phase 5: Check if there's guest data to migrate
            TaskDatabaseHelper dbHelper = TaskDatabaseHelper.getInstance(this);
            int guestDataCount = dbHelper.countGuestData();

            if (guestDataCount > 0) {
                showGuestMergeDialog(uid, email, displayName, photoUrl, dbHelper, guestDataCount);
            } else {
                completeLogin(uid, email, displayName, photoUrl);
            }
        });
    }

    private void showGuestMergeDialog(String uid, String email, String displayName,
                                       String photoUrl, TaskDatabaseHelper dbHelper, int guestDataCount) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Hợp nhất dữ liệu")
                .setMessage("Bạn có " + guestDataCount + " mục dữ liệu từ chế độ Khách. "
                        + "Bạn có muốn chuyển chúng vào tài khoản " + (email != null ? email : "Google") + " không?")
                .setPositiveButton("Có, hợp nhất", (dialog, which) -> {
                    dbHelper.migrateGuestDataToUser(uid);
                    Log.i(TAG, "Migrated " + guestDataCount + " guest items to user: " + uid);
                    completeLogin(uid, email, displayName, photoUrl);
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    Log.d(TAG, "User declined guest data migration");
                    completeLogin(uid, email, displayName, photoUrl);
                })
                .setCancelable(false)
                .show();
    }

    private void completeLogin(String uid, String email, String displayName, String photoUrl) {
        sessionManager.setUserSession(uid, email, displayName, photoUrl);

        if (NetworkUtils.isConnected(this)) {
            SyncManager.syncNow(this, "login_success_network");
        } else {
            Log.d(TAG, "Skip immediate cloud sync because there is no active network connection");
        }

        SyncManager.schedulePeriodic(this);
        navigateToMain();
    }

    private void continueAsGuest() {
        sessionManager.setGuestSession();
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
