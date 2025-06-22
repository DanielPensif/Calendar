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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Kalendar.AuthActivity;
import com.example.Kalendar.HistoryAndStatsActivity;
import com.example.Kalendar.R;
import com.example.Kalendar.SettingsActivity;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.viewmodel.ProfileViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private ImageView profileImage, btnLogout, settingsButton;
    private TextView profileName, profileNickname, profileDescription, statsButton;
    private ProfileViewModel vm;
    private UserEntity currentUser;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri!=null) onImagePicked(uri);
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_profile, container, false);
        profileImage       = v.findViewById(R.id.profileImage);
        profileName        = v.findViewById(R.id.profileName);
        profileNickname    = v.findViewById(R.id.profileNickname);
        profileDescription = v.findViewById(R.id.profileDescription);
        statsButton        = v.findViewById(R.id.statsButton);
        btnLogout          = v.findViewById(R.id.btnLogout);
        settingsButton     = v.findViewById(R.id.settingsButton);

        vm = new ViewModelProvider(this).get(ProfileViewModel.class);
        vm.user.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            currentUser = user;
            profileName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Имя");
            profileNickname.setText("@" + user.getUsername());
            profileDescription.setText(user.getDescription() != null && !user.getDescription().isEmpty()
                    ? user.getDescription() : "Описание профиля");
            if (user.getPhotoUri() != null && !user.getPhotoUri().isEmpty()) {
                File f = new File(user.getPhotoUri());
                if (f.exists()) {
                    Glide.with(this)
                            .load(f)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_camera_profile)
                            .into(profileImage);
                }
            }
        });

        int uid = SessionManager.getLoggedInUserId(requireContext());
        vm.load(uid);

        setupListeners();
        return v;
    }

    private void setupListeners() {
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });
        profileName.setOnClickListener(v -> editField(profileName, "Имя", vm::saveName));
        profileDescription.setOnClickListener(v -> editField(profileDescription, "Описание", vm::saveDescription));
        statsButton.setOnClickListener(v ->
                startActivity(new Intent(getContext(), HistoryAndStatsActivity.class))
        );
        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SettingsActivity.class))
        );
        btnLogout.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены?")
                .setPositiveButton("Да", (d,w) -> {
                    SessionManager.clear(requireContext());
                    startActivity(new Intent(getActivity(), AuthActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .setNegativeButton("Отмена", null)
                .show());
    }

    private void editField(TextView field, String title, java.util.function.BiConsumer<String, UserEntity> saver) {
        EditText ed = new EditText(requireContext());
        ed.setInputType(InputType.TYPE_CLASS_TEXT);
        ed.setText(field.getText());
        new AlertDialog.Builder(requireContext())
                .setTitle("Редактировать " + title)
                .setView(ed)
                .setPositiveButton("Сохранить", (d,w) -> {
                    String val = ed.getText().toString().trim();
                    field.setText(val);
                    if (currentUser != null) saver.accept(val, currentUser);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void onImagePicked(Uri uri) {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
            String fn = "profile_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fn);
            try (OutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[4096];
                int r;
                while ((r = in.read(buf))>0) out.write(buf,0,r);
            }
            Glide.with(this)
                    .load(file)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_camera_profile)
                    .into(profileImage);

            if (currentUser != null) vm.savePhotoUri(file.getAbsolutePath(), currentUser);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}