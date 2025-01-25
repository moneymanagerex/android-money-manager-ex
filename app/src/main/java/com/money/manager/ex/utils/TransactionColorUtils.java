package com.money.manager.ex.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.servicelayer.InfoService;

public class TransactionColorUtils {
    private TransactionColorUtils mTransactionColorUtils;
    private Context mContext;
    private InfoService mInfoService;
    private int mCurColor;
    private AlertDialog mDialog;

    public interface OnColorSelect {
        void onColorSelect(int color);
    }

    public TransactionColorUtils(Context context) {
        if (mTransactionColorUtils == null) {
//            mTransactionColorUtils = new TransactionColorUtils();
            mContext = context;
            mInfoService = new InfoService(mContext);
        }
    }

    public TransactionColorUtils resetControl() {
        mCurColor = -1;
        return mTransactionColorUtils;
    }

    public int getSelectedColor() { return mCurColor; }

    private void setMDialog(AlertDialog a) {
        this.mDialog = a;
    }
    private AlertDialog getMDialog() {
        return mDialog;
    };

    public void initColorControls(TextView colorTextView, int color, OnColorSelect onColorSelect) {
        mCurColor = color;
        if (mCurColor <= 0) {
            colorTextView.setHint( mContext.getString(R.string.empty_color_message));
        } else {
            colorTextView.setHint("");
            colorTextView.setBackgroundColor(mInfoService.getColorNumberFromInfoKey(mCurColor));
        }

        colorTextView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.empty_color_message);
            LinearLayout mainLayout       = new LinearLayout(mContext);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            InfoService info = new InfoService(mContext);
            for( int i = 0; i<= 7; i++ ) {
                LinearLayout layout       = new LinearLayout(mContext);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setGravity(Gravity.CENTER);
                Button rb = new Button(mContext);
                rb.setGravity(Gravity.CENTER);
                if (i == 0 ) {
                    rb.setTag(-1);
                    rb.setText("No Color");
                } else {
                    rb.setTag(i);
                    rb.setText(String.format("Color %d", i));
                    layout.setBackgroundColor(info.getColorNumberFromInfoKey(i));
                }
                rb.setOnClickListener(v1 -> {
                    mCurColor = ((int)v1.getTag());
                    colorTextView.setBackgroundColor(info.getColorNumberFromInfoKey(mCurColor));
                    colorTextView.setTag(mCurColor);
                    getMDialog().dismiss();
                    if (onColorSelect != null) onColorSelect.onColorSelect(mCurColor);
                } );
                layout.addView(rb);
                layout.setMinimumHeight(50);
                mainLayout.addView(layout);
            }
            builder.setView(mainLayout);
            AlertDialog dialog = builder.create();
            setMDialog(dialog);
            dialog.show();
        });

    }

}
