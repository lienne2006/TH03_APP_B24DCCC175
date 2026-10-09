package com.example.thuchanhapp_3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuchanhapp_3.adapter.NoteAdapter;
import com.example.thuchanhapp_3.database.NoteDatabaseHelper;
import com.example.thuchanhapp_3.model.Note;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private EditText etSearch;
    private FloatingActionButton fabAdd;
    private MaterialButton btnFilterDate;
    private NoteDatabaseHelper databaseHelper;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private Long selectedDateStartTime = null;
    private Long selectedDateEndTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new NoteDatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        fabAdd = findViewById(R.id.fabAdd);
        btnFilterDate = findViewById(R.id.btnFilterDate);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadNotes();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

        btnFilterDate.setOnClickListener(v -> showDatePickerDialog());
        btnFilterDate.setOnLongClickListener(v -> {
            selectedDateStartTime = null;
            selectedDateEndTime = null;
            btnFilterDate.setText("Lọc ngày");
            Toast.makeText(this, "Đã xóa bộ lọc ngày", Toast.LENGTH_SHORT).show();
            applyFilters();
            return true;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFilters();
    }

    private void loadNotes() {
        if (selectedDateStartTime != null && selectedDateEndTime != null) {
            noteList = databaseHelper.getNotesByDateRange(selectedDateStartTime, selectedDateEndTime);
        } else {
            noteList = databaseHelper.getAllNotes();
        }
        updateUI(noteList);
    }

    private void applyFilters() {
        String keyword = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        List<Note> list;

        if (selectedDateStartTime != null && selectedDateEndTime != null) {
            list = databaseHelper.getNotesByDateRange(selectedDateStartTime, selectedDateEndTime);
            if (!keyword.isEmpty()) {
                list.removeIf(note -> !note.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        && !note.getContent().toLowerCase().contains(keyword.toLowerCase()));
            }
        } else {
            if (keyword.isEmpty()) {
                list = databaseHelper.getAllNotes();
            } else {
                list = databaseHelper.searchNotes(keyword);
            }
        }
        updateUI(list);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar startCal = Calendar.getInstance();
            startCal.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            selectedDateStartTime = startCal.getTimeInMillis();

            Calendar endCal = Calendar.getInstance();
            endCal.set(selectedYear, selectedMonth, selectedDay, 23, 59, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            selectedDateEndTime = endCal.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            btnFilterDate.setText(sdf.format(new Date(selectedDateStartTime)));

            applyFilters();
        }, year, month, day);

        datePickerDialog.show();
    }

    private void updateUI(List<Note> list) {
        if (noteAdapter == null) {
            noteAdapter = new NoteAdapter(list, this);
            recyclerView.setAdapter(noteAdapter);
        } else {
            noteAdapter.setNotes(list);
        }

        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
        intent.putExtra("NOTE_ID", note.getId());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(Note note) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Xóa ghi chú")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chú này không?")
                .setPositiveButton("Xóa", (d, which) -> {
                    databaseHelper.deleteNote(note.getId());
                    applyFilters();
                })
                .setNegativeButton("Hủy", null)
                .show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        }
    }
}
