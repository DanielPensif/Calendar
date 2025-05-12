package com.example.Kalendar.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Kalendar.AuthActivity;
import com.example.Kalendar.HistoryAndStatsActivity;
import com.example.Kalendar.R;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.UserEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE = 100;

    private ImageView profileImage;
    private TextView profileName;
    private TextView profileNickname;
    private TextView profileDescription;
    private TextView statsButton;
    private ImageView btnLogout;

    private UserEntity currentUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage       = view.findViewById(R.id.profileImage);
        profileName        = view.findViewById(R.id.profileName);
        profileNickname    = view.findViewById(R.id.profileNickname);
        profileDescription = view.findViewById(R.id.profileDescription);
        statsButton        = view.findViewById(R.id.statsButton);
        btnLogout          = view.findViewById(R.id.btnLogout);

        setupListeners();
        loadUserData();
        return view;
    }

    private void setupListeners() {
        profileImage.setOnClickListener(v -> openGallery());
        profileName.setOnClickListener(v -> editField(profileName));
        profileDescription.setOnClickListener(v -> editField(profileDescription));
        statsButton.setOnClickListener(v -> startActivity(new Intent(getContext(), HistoryAndStatsActivity.class)));
        btnLogout.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Да", (dialog, which) -> logout())
                .setNegativeButton("Отмена", null)
                .show());
    }

    private void loadUserData() {
        new Thread(() -> {
            int userId = SessionManager.getLoggedInUserId(requireContext());
            currentUser = AppDatabase.getDatabase(requireContext()).userDao().getById(userId);
            requireActivity().runOnUiThread(() -> {
                if (currentUser == null) return;

                String name = (currentUser.name != null && !currentUser.name.isEmpty())
                        ? currentUser.name : "Имя";
                profileName.setText(name);

                profileNickname.setText("@" + currentUser.username);

                String desc = (currentUser.description != null && !currentUser.description.isEmpty())
                        ? currentUser.description : "Описание профиля";
                profileDescription.setText(desc);

                if (currentUser.photoUri != null && !currentUser.photoUri.isEmpty()) {
                    File file = new File(currentUser.photoUri);
                    if (file.exists()) {
                        Glide.with(this)
                                .load(file)
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.ic_camera_profile)
                                .into(profileImage);
                    }
                }
            });
        }).start();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            String localPath = copyImageToInternalStorage(uri);
            if (localPath == null) return;

            Glide.with(this)
                    .load(new File(localPath))
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_camera_profile)
                    .into(profileImage);

            new Thread(() -> {
                if (currentUser != null) {
                    currentUser.photoUri = localPath;
                    AppDatabase.getDatabase(requireContext()).userDao().update(currentUser);
                }
            }).start();
        }
    }

    private String copyImageToInternalStorage(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            String filename = "profile_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), filename);

            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                return file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void editField(TextView field) {
        EditText ed = new EditText(requireContext());
        ed.setInputType(InputType.TYPE_CLASS_TEXT);
        ed.setText(field.getText());
        new AlertDialog.Builder(requireContext())
                .setTitle("Редактировать")
                .setView(ed)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String value = ed.getText().toString().trim();
                    field.setText(value);
                    saveField(field, value);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void saveField(TextView field, String value) {
        new Thread(() -> {
            if (currentUser == null) return;
            if (field == profileName) currentUser.name = value;
            else if (field == profileDescription) currentUser.description = value;
            AppDatabase.getDatabase(requireContext()).userDao().update(currentUser);
        }).start();
    }

    private void logout() {
        SessionManager.clear(requireContext());
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}