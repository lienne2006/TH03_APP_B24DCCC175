package com.example.thuchanhapp_3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thuchanhapp_3.database.NoteDatabaseHelper;
import com.example.thuchanhapp_3.model.Note;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditNoteActivity extends AppCompatActivity {
    private TextInputLayout tilTitle, tilContent;
    private TextInputEditText etTitle, etContent;
    private Button btnSave;
    private ImageButton btnBack;
    private NoteDatabaseHelper databaseHelper;
    private long noteId;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        databaseHelper = new NoteDatabaseHelper(this);
        tilTitle = findViewById(R.id.tilTitle);
        tilContent = findViewById(R.id.tilContent);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        noteId = getIntent().getLongExtra("NOTE_ID", -1);
        currentNote = databaseHelper.getById(noteId);

        if (currentNote == null) {
            Toast.makeText(this, "Ghi chú không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle.setText(currentNote.getTitle());
        etContent.setText(currentNote.getContent());

        btnSave.setOnClickListener(v -> updateNote());
    }

    private void updateNote() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";

        boolean isValid = true;

        if (title.isEmpty()) {
            tilTitle.setError("Tiêu đề không được để trống");
            isValid = false;
        } else if (title.length() > 100) {
            tilTitle.setError("Tiêu đề tối đa 100 ký tự");
            isValid = false;
        } else {
            tilTitle.setError(null);
        }

        if (content.isEmpty()) {
            tilContent.setError("Nội dung không được để trống");
            isValid = false;
        } else {
            tilContent.setError(null);
        }

        if (!isValid) {
            return;
        }

        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setUpdatedAt(System.currentTimeMillis());

        databaseHelper.updateNote(currentNote);
        showSuccessDialog();
    }

    private void showSuccessDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Lưu ghi chú thành công")
                .setPositiveButton("OK", (d, which) -> {
                    Intent intent = new Intent(EditNoteActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        }
    }
}
