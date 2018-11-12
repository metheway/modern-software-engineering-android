package com.example.lenovo.cameraapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private SQLite dbHelper;
    private boolean repeat = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = new SQLite(this, "User.db", null, 2);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);

        mPasswordView = (EditText) findViewById(R.id.register_password);

        Button register_button = (Button)findViewById(R.id.register_register);

        register_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                register();

            }
        });
    }
    private void register(){
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        if (email.length()<=20){
            if(password.length()<=20){
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.query("Message", null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        // 遍历Cursor对象，取出数据并打印
                        String sql_user = cursor.getString(cursor.getColumnIndex("Userid"));
                        Log.d("userid", "judgeture: "+sql_user);
                        if(email.equals(sql_user)) {
                               repeat =true;
                        }
                    } while (cursor.moveToNext());
                }
                if(repeat==true){
                    Toast.makeText(this,"用户名重复！",Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues values = new ContentValues();
                // 开始组装第一条数据
                values.put("Userid", email);
                values.put("Userpassword", password);
                db.insert("Message", null, values); // 插入第一条数据
                values.clear();
                Intent return_sign_in = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(return_sign_in);
                finish();
            }
        }
    }

}
