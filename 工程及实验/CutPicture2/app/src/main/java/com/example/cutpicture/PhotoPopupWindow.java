package com.example.cutpicture;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

/**
 * Created by Lister on 2017-06-12.
 * PopupWindow 工具类
 */

public class PhotoPopupWindow extends PopupWindow{
    private View mView;
    private Context mContext;
    private View.OnClickListener mSelectListener;
    private View.OnClickListener mCaptureListener;

    public PhotoPopupWindow(Context context,
                            View.OnClickListener mSelectListener, View.OnClickListener mCaptureListener) {
        super(context);
        this.mContext = context;
        this.mSelectListener = mSelectListener;
        this.mCaptureListener = mCaptureListener;
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater)mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //要动态载入的界面
        mView = inflater.inflate(R.layout.pop_item,null);
        Button btn_camera = (Button)mView.findViewById(R.id.icon_btn_camera);
        Button btn_select = (Button)mView.findViewById(R.id.icon_btn_select);
        Button btn_cancel = (Button)mView.findViewById(R.id.icon_btn_cancel);

        btn_camera.setOnClickListener(mCaptureListener);
        btn_select.setOnClickListener(mSelectListener);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //导入布局
        this.setContentView(mView);
        //设置动画效果
        this.setAnimationStyle(R.style.popwindow_anim_style);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        //设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);
        //单击弹出窗外，关闭弹出窗
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mView.findViewById(R.id.ll_pop).getTop();
                int y = (int)event.getY();
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(y < height){
                        dismiss();
                    }
                }

                return true;
            }
        });


    }

}
