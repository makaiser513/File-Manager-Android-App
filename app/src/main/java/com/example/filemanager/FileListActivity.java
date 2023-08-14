package com.example.filemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class FileListActivity extends AppCompatActivity {
    public static File copiedFile = null;
    public static File movedFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        checkPermissions();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        TextView noFilesText = findViewById(R.id.nofiles_textview);

        String path = getIntent().getStringExtra("path");
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        if(filesAndFolders==null || filesAndFolders.length ==0){
            noFilesText.setVisibility(View.VISIBLE);
            return;
        }

        noFilesText.setVisibility(View.INVISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter(this, filesAndFolders));

        FloatingActionButton createFolderFab = findViewById(R.id.create_folder_fab);
        createFolderFab.setOnClickListener(v -> showCreateFolderDialog());

        FloatingActionButton pasteFileFab = findViewById(R.id.paste_file_fab);
        pasteFileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteFile();
            }
        });


    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                recreate();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Folder");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.new_folder_dialog, (ViewGroup) getWindow().getDecorView(), false);
        final EditText input = viewInflated.findViewById(R.id.new_folder_name);
        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String folderName = input.getText().toString();
            createNewFolder(folderName);
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void createNewFolder(String folderName) {
        String path = getIntent().getStringExtra("path");
        File newFolder = new File(path, folderName);
        if (!newFolder.exists()) {
            boolean success = newFolder.mkdir();
            if (success) {
                Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show();
                recreate();
            } else {
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void pasteFile() {
        if (copiedFile == null && movedFile == null) {
            Toast.makeText(this, "No file or folder to paste", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentPath = getIntent().getStringExtra("path");
        File destinationFolder = new File(currentPath);
        File destination;

        if (copiedFile != null) {
            destination = new File(destinationFolder, copiedFile.getName());
            try {
                FileUtil.copy(copiedFile, destination);
                Toast.makeText(this, "Pasted successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Failed to paste", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
        }

        if (movedFile != null) {
            destination = new File(destinationFolder, movedFile.getName());
            try {
                FileUtil.move(movedFile, destination);
                Toast.makeText(this, "Moved successfully", Toast.LENGTH_SHORT).show();
                movedFile = null; // Clear the movedFile variable after a successful move
            } catch (IOException e) {
                Toast.makeText(this, "Failed to move", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
        }

        // Refresh the RecyclerView
        recreate();
    }

}