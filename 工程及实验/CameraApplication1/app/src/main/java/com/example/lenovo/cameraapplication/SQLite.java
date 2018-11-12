package com.example.lenovo.cameraapplication;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by lenovo on 2018/5/12.
 */

public class SQLite extends SQLiteOpenHelper {
    public static final String Create_Message="create table message("
            +"Userid,"
            +"Userpassword)";
    private Context mcontext;
    public SQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mcontext = context;
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Create_Message);
        Toast.makeText(mcontext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists message");
        onCreate(db);
    }
}
