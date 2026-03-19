package hcmute.edu.vn.lequanghung_23110110.ticktick.activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
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
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import hcmute.edu.vn.lequanghung_23110110.ticktick.R;
import hcmute.edu.vn.lequanghung_23110110.ticktick.utils.SessionManager;

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
            sessionManager.setUserSession(uid, email, displayName, photoUrl);
            navigateToMain();
        });
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

    // Gọi khi logout để đổi account sạch hơn
    private void clearCredentialStateOnLogout() {
        credentialManager.clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        Log.d(TAG, "Credential state cleared");
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.w(TAG, "Clear credential state failed", e);
                    }

                }
        );
    }
}
