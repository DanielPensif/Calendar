package com.example.Kalendar.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class BiometricHelper {
    private final BiometricPrompt prompt;
    private final BiometricPrompt.PromptInfo info;

    public BiometricHelper(@NonNull AppCompatActivity act) {
        Executor exe = ContextCompat.getMainExecutor(act);
        prompt = new BiometricPrompt(act, exe,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {
                        onSuccess.run();
                    }
                });
        info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Подтвердите вход")
                .setSubtitle("Используйте отпечаток или код устройства")
                .setDeviceCredentialAllowed(true)
                .build();
    }

    private Runnable onSuccess;
    public void authenticate(Runnable onSuccess) {
        this.onSuccess = onSuccess;
        prompt.authenticate(info);
    }
}
