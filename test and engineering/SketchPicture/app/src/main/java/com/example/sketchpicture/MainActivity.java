package com.example.sketchpicture;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView mImageView;
    private EditText mREditView;
    private Button mDisColorBtn;
    private Button mDodgeBtn;
    private Button mGaussBlurBtn;
    private Button mConvertBtn;

    private ProgressDialog mDialog;
    private Bitmap mSourceBitmap;
    private Bitmap mConvertedBitmap;

    private static final int TYPE_DISCOLOR = 0;
    private static final int TYPE_DODGE = 1;
    private static final int TYPE_GAUSSBLUR = 2;
    private static final int TYPE_CONVERT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.sketch_image_view);
        mImageView.setOnClickListener(this);

        mREditView = (EditText) findViewById(R.id.sketch_r_edit_text);

        mDisColorBtn = (Button) findViewById(R.id.sketch_discolor_btn);
        mDisColorBtn.setOnClickListener(this);
        mDodgeBtn = (Button) findViewById(R.id.sketch_dodge_btn);
        mDodgeBtn.setOnClickListener(this);
        mGaussBlurBtn = (Button) findViewById(R.id.sketch_guauss_btn);
        mGaussBlurBtn.setOnClickListener(this);

        mConvertBtn = (Button) findViewById(R.id.sketch_convert_btn);
        mConvertBtn.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConvertedBitmap != null) {
            mConvertedBitmap.recycle();
            mConvertedBitmap = null;
        }
    }

    @Override
    public void onClick(View v) {
        String rStr = mREditView.getText().toString();
        int r = Integer.parseInt(rStr);
        if (v == mConvertBtn) {
            new ConvertTask().execute(new Integer[] { TYPE_CONVERT, r });
        } else if (v == mDisColorBtn) {
            new ConvertTask().execute(new Integer[] { TYPE_DISCOLOR, r });
        } else if (v == mDodgeBtn) {
            new ConvertTask().execute(new Integer[] { TYPE_DODGE, r });
        } else if (v == mGaussBlurBtn) {
            new ConvertTask().execute(new Integer[] { TYPE_GAUSSBLUR, r });
        } else if (v == mImageView) {
            if (mSourceBitmap != null) {
                mImageView.setImageBitmap(mSourceBitmap);
            }
        }
    }

    private class ConvertTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap result) {
            mDialog.dismiss();
            if (result != null) {
                mConvertedBitmap = result;
                mImageView.setImageBitmap(result);
            }

        }

        @Override
        protected void onPreExecute() {
            if (mDialog == null) {
                mDialog = new ProgressDialog(MainActivity.this);
            }
            mDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            int type = params[0];
            int r = params[1];
            if (mSourceBitmap == null) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView
                        .getDrawable();
                mSourceBitmap = bitmapDrawable.getBitmap();
            } else if (mConvertedBitmap != null) {
                mConvertedBitmap.recycle();
                mConvertedBitmap = null;
            }

            Bitmap result = null;
            switch (type) {
                case TYPE_DISCOLOR:
                    result = testSketch.testDiscolor(mSourceBitmap);
                    break;
                case TYPE_DODGE:
                    result = testSketch.testReverseColor(mSourceBitmap);
                    break;
                case TYPE_GAUSSBLUR:
                    result = testSketch.testSingleGaussBlur(mSourceBitmap, r, r / 3);
                    break;
                case TYPE_CONVERT:
                    result = testSketch.testGaussBlur(mSourceBitmap, r, r / 3);
                    break;
            }

            return result;
        }

    }

}
