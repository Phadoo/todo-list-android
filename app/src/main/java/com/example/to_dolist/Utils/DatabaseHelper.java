package com.example.to_dolist.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.to_dolist.Model.ToDoModel;

import java.util.ArrayList;
import java.util.List;

/** @noinspection CallToPrintStackTrace*/
public class DatabaseHelper extends SQLiteOpenHelper {

    // Constants for database configuration
    private static final String DATABASE_NAME = "todo_database";
    private static final int DATABASE_VERSION = 1;

    // Constants for table and column names
    private static final String TABLE_NAME = "todo_table";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TASK = "TASK";
    public static final String COLUMN_STATUS = "STATUS";
    public static final String COLUMN_POSITION = "POSITION";

    // Create table query
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TASK + " TEXT, "
            + COLUMN_STATUS + " INTEGER, "
            + COLUMN_POSITION + " INTEGER)";

    // Index for status column
    private static final String CREATE_STATUS_INDEX = "CREATE INDEX IF NOT EXISTS idx_status ON "
            + TABLE_NAME + " (" + COLUMN_STATUS + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_QUERY); // Create table
        db.execSQL(CREATE_STATUS_INDEX); // Create index for status
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); // Drop old table
        onCreate(db); // Recreate table
    }

    // Insert a new task
    public void insertTask(ToDoModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TASK, model.getTask());
        contentValues.put(COLUMN_STATUS, 0); // Default status is 0 (incomplete)
        contentValues.put(COLUMN_POSITION, 0);
        db.insert(TABLE_NAME, null, contentValues);
    }

    // Update a task
    public void updateTask(int id, String task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TASK, task);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Update task status
    public void updateStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updateIndices(List<ToDoModel> mList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < mList.size(); i++) {
                ToDoModel task = mList.get(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_POSITION, i);
                db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    // Delete a task
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Fetch all tasks
    @SuppressLint("Range")
    public List<ToDoModel> getAllTasks() {
        List<ToDoModel> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_NAME,
                null, // Columns (null means all columns)
                null, // Selection (null means all rows)
                null, // Selection args
                null, // Group by
                null, // Having
                COLUMN_STATUS + " ASC, " + COLUMN_POSITION + " ASC" // Order by status ascending (incomplete first), then ID descending
        )) {
            // Columns (null means all columns)
            // Selection (null means all rows)
            // Selection args
            // Group by
            // Having
            // Order by ID descending (newest first)

            if (cursor.moveToFirst()) {
                do {
                    ToDoModel task = new ToDoModel();
                    task.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    task.setTask(cursor.getString(cursor.getColumnIndex(COLUMN_TASK)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)));

                    // Read the position value
                    if (cursor.getColumnIndex(COLUMN_POSITION) != -1) {
                        task.setPosition(cursor.getInt(cursor.getColumnIndex(COLUMN_POSITION)));
                    }

                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return taskList;
    }
}
