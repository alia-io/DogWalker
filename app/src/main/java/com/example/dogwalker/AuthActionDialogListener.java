package com.example.dogwalker;

public interface AuthActionDialogListener {
    void onVerifyEmailAttempt(AuthActionFragment fragment, String email, String password);
    void onResetPasswordAttempt(AuthActionFragment fragment, String email);
}
