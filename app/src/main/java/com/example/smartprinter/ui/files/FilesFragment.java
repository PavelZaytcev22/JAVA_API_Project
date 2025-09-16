package com.example.smartprinter.ui.files;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;


import androidx.annotation.Nullable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.smartprinter.databinding.FragmentFilesBinding;


public class FilesFragment extends Fragment {

    private FragmentFilesBinding binding;
    private Uri fileUri;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    fileUri = result.getData().getData();
                    if (fileUri != null) {
                        showPreview(fileUri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentFilesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnChooseFile.setOnClickListener(v -> openFileChooser());

        return root;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // можно ограничить "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Выберите файл"));
    }

    private void showPreview(Uri uri) {
        String type = requireContext().getContentResolver().getType(uri);

        if (type != null && type.startsWith("image/")) {
            binding.previewImage.setImageURI(uri);
        } else {
            Toast.makeText(getContext(), "Предпросмотр доступен только для изображений", Toast.LENGTH_SHORT).show();
        }
    }
}


