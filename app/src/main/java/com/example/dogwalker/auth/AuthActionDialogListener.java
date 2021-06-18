package com.example.dogwalker.auth;

public interface AuthActionDialogListener {
    void onVerifyEmailAttempt(AuthActionFragment fragment, String email, String password);
    void onResetPasswordAttempt(AuthActionFragment fragment, String email);
}
