package com.example.Kalendar.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.Kalendar.HistoryAndStatsActivity;
import com.example.Kalendar.R;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 100;

    private ImageView profileImage;
    private TextView profileName, profileNickname, profileDescription;
    private Button statsButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        profileName = view.findViewById(R.id.profileName);
        profileNickname = view.findViewById(R.id.profileNickname);
        profileDescription = view.findViewById(R.id.profileDescription);
        statsButton = view.findViewById(R.id.statsButton);

        setupListeners();
        return view;
    }

    private void setupListeners() {
        profileImage.setOnClickListener(v -> openGallery());
        profileName.setOnClickListener(v -> editField(profileName));
        profileNickname.setOnClickListener(v -> editField(profileNickname));
        profileDescription.setOnClickListener(v -> editField(profileDescription));
        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HistoryAndStatsActivity.class);
            startActivity(intent);
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void editField(TextView field) {
        EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(field.getText());

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Редактировать")
                .setView(editText)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    field.setText(editText.getText().toString());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
