package com.incomrecycle.prms.rvm.gui.activity.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import com.incomrecycle.prms.rvm.R;
import it.sauronsoftware.ftp4j.FTPCodes;

public class MyDialog extends Dialog {

    public static class Builder {
        private View contentView;
        private Context context;
        private String message;
        private OnClickListener negativeButtonClickListener;
        private String negativeButtonText;
        private OnClickListener positiveButtonClickListener;
        private String positiveButtonText;
        private String title;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = (String) this.context.getText(message);
            return this;
        }

        public Builder setTitle(int title) {
            this.title = (String) this.context.getText(title);
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText, OnClickListener listener) {
            this.positiveButtonText = (String) this.context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText, OnClickListener listener) {
            this.negativeButtonText = (String) this.context.getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public MyDialog create(Bitmap bm) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
            final MyDialog dialog = new MyDialog(this.context, R.style.MyDialog);
            View layout = inflater.inflate(R.layout.clear_pingnum_dialog, null);
            dialog.setContentView(layout);
            Window dialogWindow = dialog.getWindow();
            LayoutParams lp = dialogWindow.getAttributes();
            lp.width = FTPCodes.SYNTAX_ERROR;
            lp.height = 280;
            dialogWindow.setAttributes(lp);
            ((ImageView) layout.findViewById(R.id.alarm_iv_erweima)).setImageBitmap(bm);
            if (this.positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(this.positiveButtonText);
                if (this.positiveButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Builder.this.positiveButtonClickListener.onClick(dialog, -1);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.positiveButton).setVisibility(8);
            }
            if (this.negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(this.negativeButtonText);
                if (this.negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Builder.this.negativeButtonClickListener.onClick(dialog, -2);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.negativeButton).setVisibility(8);
            }
            dialog.setContentView(layout);
            return dialog;
        }
    }

    public MyDialog(Context context) {
        super(context);
    }

    public MyDialog(Context context, int theme) {
        super(context, theme);
    }
}
