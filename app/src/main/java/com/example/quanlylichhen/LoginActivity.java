package com.example.quanlylichhen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Màn hình 1: đăng nhập.
 *
 * <p>Tên đăng nhập và mật khẩu được kiểm tra bằng cách truy vấn bảng {@code users}
 * (không cố định trong code của Activity). Đăng nhập đúng thì mở {@link MainActivity}
 * và gọi {@code finish()} để nút Back không quay lại màn hình này.</p>
 */
public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Nền màn hình đăng nhập là màu sáng nên icon thanh trạng thái phải màu tối.
        UiUtils.applyEdgeToEdge(this, true);
        applyWindowInsets();

        dbHelper = new DatabaseHelper(this);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        tvError = findViewById(R.id.tvError);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> checkLogin());

        // Người dùng sửa lại nội dung thì ẩn thông báo lỗi cũ đi.
        clearErrorWhenTyping(edtUsername);
        clearErrorWhenTyping(edtPassword);
    }

    /**
     * Chừa chỗ cho thanh trạng thái và thanh điều hướng để nội dung không bị che
     * khi ứng dụng vẽ tràn viền.
     */
    private void applyWindowInsets() {
        View root = findViewById(R.id.rootLogin);
        final int paddingTop = root.getPaddingTop();
        final int paddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(view.getPaddingLeft(), paddingTop + bars.top,
                    view.getPaddingRight(), paddingBottom + bars.bottom);
            return windowInsets;
        });
    }

    /**
     * Kiểm tra dữ liệu nhập rồi đăng nhập.
     * Bỏ trống trường nào thì báo lỗi trường đó và không truy vấn cơ sở dữ liệu.
     */
    private void checkLogin() {
        String username = textOf(edtUsername);
        String password = textOf(edtPassword);

        hideError();

        if (username.isEmpty()) {
            showError(getString(R.string.error_username_empty));
            edtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError(getString(R.string.error_password_empty));
            edtPassword.requestFocus();
            return;
        }

        if (dbHelper.checkLogin(username, password)) {
            hideKeyboard();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // để Back không quay lại màn hình đăng nhập
        } else {
            // Xóa mật khẩu trước rồi mới báo lỗi: nếu làm ngược lại thì TextWatcher
            // của ô mật khẩu sẽ ẩn mất thông báo vừa hiện.
            edtPassword.setText("");
            edtPassword.requestFocus();
            showError(getString(R.string.error_login_failed));
        }
    }

    private String textOf(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void clearErrorWhenTyping(TextInputEditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (tvError.getVisibility() == View.VISIBLE) {
                    hideError();
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focused = getCurrentFocus();
        if (imm != null && focused != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
