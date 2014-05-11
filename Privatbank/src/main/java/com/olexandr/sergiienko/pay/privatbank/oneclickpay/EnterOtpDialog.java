package com.olexandr.sergiienko.pay.privatbank.oneclickpay;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

/**
 * Created by Alex on 11.05.14.
 */
public class EnterOtpDialog extends Dialog {
    public EnterOtpDialog(Context context) {
        super(context);
    }

    public EnterOtpDialog(Context context, int theme) {
        super(context, theme);
    }

    protected EnterOtpDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.enter_otp_text_hint);
        setContentView(input);

    }
}
