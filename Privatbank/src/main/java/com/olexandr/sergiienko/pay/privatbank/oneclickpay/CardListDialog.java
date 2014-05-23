package com.olexandr.sergiienko.pay.privatbank.oneclickpay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ua.privatbank.payoneclicklib.Pay;
import ua.privatbank.payoneclicklib.model.Card;

/**
 * Created by Alex on 11.05.14.
 */
public class CardListDialog extends Dialog implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener, CompoundButton.OnCheckedChangeListener {
    private OneClickPayIntegrator integrator;
    private ListView mList;
    private Button mBtnAddCard;
    private ProgressBar mProgress;
    private TextView mText;
    private View mTextProgressBar;
    private CheckBox mCBUseDefault;
    private OneClickPayIntegrator.CompleteListener mCompleteListener;

    public CardListDialog(Context context) {
        super(context);
    }

    public CardListDialog(Context context, int theme) {
        super(context, theme);
    }

    protected CardListDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setIntegrator(OneClickPayIntegrator integrator) {
        this.integrator = integrator;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_card_list);
        mList = (ListView) findViewById(android.R.id.list);
        mList.setOnItemClickListener(CardListDialog.this);
        mList.setOnItemLongClickListener(CardListDialog.this);
        mBtnAddCard = (Button) findViewById(R.id.addCard);
        mBtnAddCard.setOnClickListener(this);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        mTextProgressBar = findViewById(R.id.text_progress);
        mText = (TextView) findViewById(R.id.empty_list_text);
        mCBUseDefault = (CheckBox) findViewById(R.id.useDefaultCard);
        mCBUseDefault.setOnCheckedChangeListener(this);
        setTitle(R.string.cards_list_title);
        getCardList();
    }

    public void getCardList() {
        try {
            if (integrator != null && integrator.getPay() != null) {
                mProgress.setVisibility(View.VISIBLE);
                mList.setVisibility(View.INVISIBLE);
                mText.setVisibility(View.VISIBLE);
                mText.setText(R.string.please_wait);
                integrator.getPay().getCards(new Pay.CardListCallBack() {
                    @Override
                    public void onGetCardListSuccess(List<Card> cardList) {
                        mProgress.setVisibility(View.GONE);
                        mList.setVisibility(View.VISIBLE);
                        if (cardList == null || cardList.size() == 0) {
                            mText.setText(R.string.err_card_list_is_empty);
                            return;
                        } else {
                            mText.setVisibility(View.GONE);
                        }
                        // Set up the input
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                getContext(),
                                android.R.layout.select_dialog_singlechoice);
                        for (Card card : cardList) {
                            arrayAdapter.add(card.getCard_id());
                        }
                        mList.setAdapter(arrayAdapter);
                    }

                    @Override
                    public void onGetCardListFailed() {
                /*  ваш код обработки ошибок при получении списка карт */
                        mProgress.setVisibility(View.GONE);
                        if ("err_link_device_phone_not_exist".equals(integrator.getApi().getLastServerFailCode())) {
                            mText.setText(R.string.err_link_device_phone_not_exist);
                        } else {
                            mText.setText(R.string.err_internet);
                        }

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delCard(final String cardId) {
        try {
            integrator.getPay().delCard(cardId, new Pay.DelCardCallBack() {
                @Override
                public void onDelCardSuccess() {
                    Toast.makeText(getContext(), R.string.operation_success, Toast.LENGTH_LONG).show();
                    getCardList();
                    /*  ваш код обработки успешного удаления карты*/
                }

                @Override
                public void onDelCardFailed() {
                        /*  ваш код обработки ошибок при удалении карты*/
                    Toast.makeText(getContext(), R.string.operation_error, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDefCard(final String cardId) {
        try {
            integrator.getPay().setDefaultCard(cardId, new Pay.SetDefaultCardCallBack() {
                @Override
                public void onSetDefCardSuccess() {
                    Toast.makeText(getContext(), R.string.operation_success, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSetDefCardFailed() {
                    Toast.makeText(getContext(), R.string.operation_error, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCard(final String phone) {
        try {
            integrator.getPay().addCard(phone, new Pay.AddCardCallBack() {

                @Override
                public void onAddCardSuccess() {
               /*  ваш код обработки успешного добавления карты*/
                    SharedPreferences pref = getContext().getSharedPreferences("Private ONE CLICK", Context.MODE_PRIVATE);
                    pref.edit().putString(OneClickPayIntegrator.class.getPackage().getName() + ".PhoneNumber", phone).commit();
                }

                @Override
                public void onReceiveOtpSend(final Pay.OtpCheckListener listener) {
                        /* будет вызван когда отп пароль будет отправлен на указанный телефон*/

                    if (integrator != null) {
                        integrator.enterOtpDialog(listener, new Pay.OtpCallBack() {
                            @Override
                            public void onOtpSuccess() {
                                Toast.makeText(getContext(), R.string.operation_success, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onOtpFailed() {
                                Toast.makeText(getContext(), R.string.incorrect_code, Toast.LENGTH_LONG).show();
                                integrator.enterOtpDialog(listener, this);
                            }
                        });
                    }
                }

                @Override
                public void onAddCardFailed() {
                    Toast.makeText(getContext(), R.string.operation_error, Toast.LENGTH_LONG).show();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mCompleteListener != null) {
            mCompleteListener.onComplete((String) adapterView.getItemAtPosition(i));
            dismiss();
        }
    }

    public void setCompleteListener(OneClickPayIntegrator.CompleteListener completeListener) {
        this.mCompleteListener = completeListener;
    }

    @Override
    public void onClick(View view) {
        if (integrator != null) {
            addCard(integrator.getPhone());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choose_operation);
        final String cardId = (String) adapterView.getItemAtPosition(i);
        builder.setMessage(cardId);
        // Set up the buttons
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delCard(cardId);
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.set_default, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setDefCard(cardId);
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        SharedPreferences pref = getContext().getSharedPreferences("Private ONE CLICK", Context.MODE_PRIVATE);
        pref.edit().putBoolean(OneClickPayIntegrator.class.getPackage().getName() + ".PayByDefaultCard", b).commit();
    }
}
