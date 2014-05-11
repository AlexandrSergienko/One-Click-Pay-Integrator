package com.olexandr.sergiienko.pay.privatbank.oneclickpay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;

import ua.privatbank.framework.api.Api;
import ua.privatbank.framework.api.Message;
import ua.privatbank.payoneclicklib.Pay;
import ua.privatbank.payoneclicklib.model.Card;
import ua.privatbank.payoneclicklib.model.PayData;

/**
 * Created by Alex on 09.05.14.
 */
public class OneClickPayIntegrator {

    private static final String TAG = OneClickPayIntegrator.class.getSimpleName();
    private Context mContext;
    private Pay mPay;
    private String mPhone;
    private ua.privatbank.payoneclicklib.Api mApi;
    //    private Pay.OtpCheckListener mOtpCheckListener;
    private Pay.OtpCallBack mOtpCallback = new Pay.OtpCallBack() {
        @Override
        public void onOtpSuccess() {

        }

        @Override
        public void onOtpFailed() {

        }
    };

    public OneClickPayIntegrator(Context context, String merchantId) {
        mContext = context;
        initPaymentSystem(context, merchantId);
    }

    public Pay getPay() {
        return mPay;
    }

    public String getPhone() {
        return mPhone;
    }

    public ua.privatbank.payoneclicklib.Api getApi() {
        return mApi;
    }

    private void initPaymentSystem(Context context, String merchantId) {
        mPay = new Pay(context, new Api.ApiEventListener<ua.privatbank.payoneclicklib.Api>() {

            @Override
            public void onApiStartRequest() {
                  /* ваш код обработки начала отправки очереди запросов*/
                Log.d(TAG, "onApiStartRequest()");
            }

            @Override
            public void onApiFinishRequest() {
                 /* ваш код обработки завершения отправки очереди запросов */
                Log.d(TAG, "onApiFinishRequest()");
            }

            @Override
            public void onApiError(ua.privatbank.payoneclicklib.Api api, Message.ErrorCode code) {
                mApi = api;
                if (!TextUtils.isEmpty(mApi.getLastServerFailCode())
                        && !(Pay.FieldException.phone_is_null.equals(mApi.getLastServerFailCode()) ||
                        "err_wrong_phone".equals(mApi.getLastServerFailCode()) ||
                        "err_link_device_phone_not_exist".equals(mApi.getLastServerFailCode()))) {
                    Toast.makeText(mContext, getErrorText(api.getLastServerFailCode()), Toast.LENGTH_LONG).show();
                }
                Log.e(TAG, "Error on Api." + code.name() + " lastError = " + api.getLastServerFailCode());
            }
        }, merchantId);
    }

    public String getErrorText(String error) {
        try {
            Field f = R.string.class.getField(error);
            Class<?> t = f.getType();
            if (t == int.class) {
                String str = mContext.getString(f.getInt(null));
                return str;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return error;
    }

    public void pay(final PayData payData) {
        if (payData != null && !TextUtils.isEmpty(payData.getAmount())
                && !TextUtils.isEmpty(payData.getDescription())
                && !TextUtils.isEmpty(payData.getCcy())) {
            payData.setCardId(null);
            if (!hasPhone(payData)) return;
            if (!hasCard(payData)) return;

            try {
                mPay.pay(payData, new Pay.PaymentCallBack() {
                    @Override
                    public void onPaymentSuccess() {
                          /* ваш код обработки успешных платежей без  проверки  отп  */
                        Log.d(TAG, "onPaymentSuccess()");
                        SharedPreferences pref = mContext.getSharedPreferences("Private ONE CLICK", Context.MODE_PRIVATE);
                        pref.edit().putString(OneClickPayIntegrator.class.getPackage().getName() + ".PhoneNumber", payData.getPhone()).commit();
                    }

                    @Override
                    public void onReceiveOtpSend(Pay.OtpCheckListener otpListener) {
               /* будет вызван когда отп пароль будет отправлен на указанный телефон*/
                        Log.d(TAG, "onReceiveOtpSend()");
                        enterOtpDialog(otpListener, mOtpCallback);
                    }

                    @Override
                    public void onPaymentFailed() {
                /* ваш код обработки ошибок при осуществлении платежа*/
                        Log.d(TAG, "onPaymentFailed()");
                        if (mApi.getLastServerFailCode().equals(Pay.FieldException.phone_is_null) ||
                                "err_wrong_phone".equals(mApi.getLastServerFailCode())) {
                            enterPhoneDialog(new CompleteListener() {
                                @Override
                                public void onComplete(String phone) {
                                    payData.setPhone(phone);
                                    pay(payData);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }
                    }

                    @Override
                    public void onPaymentProcessing() {
              /*  ваш код обработки ошибки когда платеж находится в обработке */
                        Log.d(TAG, "onPaymentProcessing()");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error to pay.", e);
            }
        }
    }

    private boolean hasPhone(final PayData payData) {
        if (TextUtils.isEmpty(payData.getPhone())) {
            SharedPreferences pref = mContext.getSharedPreferences("Private ONE CLICK", Context.MODE_PRIVATE);
            payData.setPhone(pref.getString(OneClickPayIntegrator.class.getPackage().getName() + ".PhoneNumber", null));
        }
        if (TextUtils.isEmpty(payData.getPhone())) {
            enterPhoneDialog(new CompleteListener() {
                @Override
                public void onComplete(String phone) {
                    payData.setPhone(phone);
                    mPhone = phone;
                    pay(payData);
                }

                @Override
                public void onCancel() {

                }
            });
            return false;
        }
        return true;
    }

    private boolean hasCard(final PayData payData) {
        if (TextUtils.isEmpty(payData.getCardId())) {
            final CardListDialog dialog = new CardListDialog(mContext);
            dialog.setIntegrator(this);
            dialog.setCompleteListener(new CompleteListener() {
                @Override
                public void onComplete(String data) {
                    payData.setCardId(data);
                    pay(payData);
                }

                @Override
                public void onCancel() {

                }
            });
            SharedPreferences pref = mContext.getSharedPreferences("Private ONE CLICK", Context.MODE_PRIVATE);
            if (pref.getBoolean(OneClickPayIntegrator.class.getPackage().getName() + ".PayByDefaultCard", false)) {
                try {
                    mPay.getCards(new Pay.CardListCallBack() {
                        @Override
                        public void onGetCardListSuccess(List<Card> cards) {
                            if (cards != null && cards.size() > 0) {
                                for (Card card : cards) {
                                    if (card.getDefault_card()) {
                                        payData.setCardId(card.getCard_id());
                                        pay(payData);
                                        break;
                                    }
                                }
                            } else {
                                dialog.show();
                            }
                        }

                        @Override
                        public void onGetCardListFailed() {
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                dialog.show();
            }
            return false;
        }
        return true;
    }

    public void showHistory() {
        TransactionsDialog dialog = new TransactionsDialog(mContext);
        dialog.setIntegrator(this);
        dialog.show();
    }

    public void chooseCard(CompleteListener completeListener) {
        CardListDialog dialog = new CardListDialog(mContext);
        dialog.setIntegrator(this);
        dialog.setCompleteListener(completeListener);
        dialog.show();
    }

    public void enterOtpDialog(final Pay.OtpCheckListener listener, final Pay.OtpCallBack otpCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.enter_otp_title);
        // Set up the input
        final EditText input = new EditText(mContext);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.enter_otp_text_hint);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String otp = input.getText().toString();
                listener.onOtpCheck(otp, mPhone, otpCallback);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void enterPhoneDialog(final CompleteListener callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.enter_phone_title);
        // Set up the input
        final EditText input = new EditText(mContext);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint(R.string.enter_phone_text_hint);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String phone = input.getText().toString();
                callback.onComplete(phone);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onCancel();
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public enum Currency {UAH, USD}

    public interface CompleteListener {
        void onComplete(String data);

        void onCancel();
    }


}
