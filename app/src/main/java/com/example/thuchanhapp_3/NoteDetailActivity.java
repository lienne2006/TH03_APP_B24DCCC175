package com.example.thuchanhapp_3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thuchanhapp_3.database.NoteDatabaseHelper;
import com.example.thuchanhapp_3.model.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvContent, tvCreatedAt, tvUpdatedAt;
    private Button btnEdit, btnDelete;
    private ImageButton btnBack;
    private NoteDatabaseHelper databaseHelper;
    private long noteId;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        databaseHelper = new NoteDatabaseHelper(this);
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvUpdatedAt = findViewById(R.id.tvUpdatedAt);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        noteId = getIntent().getLongExtra("NOTE_ID", -1);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(NoteDetailActivity.this, EditNoteActivity.class);
            intent.putExtra("NOTE_ID", noteId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNoteDetail();
    }

    private void loadNoteDetail() {
        currentNote = databaseHelper.getById(noteId);
        if (currentNote == null) {
            Toast.makeText(this, "Ghi chú không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(currentNote.getTitle());
        tvContent.setText(currentNote.getContent());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvCreatedAt.setText("Tạo lúc: " + sdf.format(new Date(currentNote.getCreatedAt())));
        tvUpdatedAt.setText("Cập nhật: " + sdf.format(new Date(currentNote.getUpdatedAt())));
    }

    private void confirmDelete() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Xóa ghi chú")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chú này không?")
                .setPositiveButton("Xóa", (d, which) -> {
                    databaseHelper.deleteNote(noteId);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        }
    }
}
