package com.example.Kalendar.ui.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupView();
        setupListeners();
        observeViewModel();
    }

    protected abstract void setupView();
    protected abstract void setupListeners();
    protected abstract void observeViewModel();
}