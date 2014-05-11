package com.olexandr.sergiienko.pay.privatbank.oneclickpay;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ua.privatbank.payoneclicklib.Pay;
import ua.privatbank.payoneclicklib.model.Transaction;

/**
 * Created by Alex on 11.05.14.
 */
public class TransactionsDialog extends Dialog {
    private OneClickPayIntegrator integrator;
    private ListView mList;
    private ProgressBar mProgress;
    private TextView mText;
    private View mTextProgressBar;
    private OneClickPayIntegrator.CompleteListener mCompleteListener;

    public TransactionsDialog(Context context) {
        super(context);
    }

    public TransactionsDialog(Context context, int theme) {
        super(context, theme);
    }

    protected TransactionsDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setIntegrator(OneClickPayIntegrator integrator) {
        this.integrator = integrator;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_transactions_list);
        mList = (ListView) findViewById(android.R.id.list);
        mList.setClickable(false);
        mList.setLongClickable(false);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        mTextProgressBar = findViewById(R.id.text_progress);
        mText = (TextView) findViewById(R.id.empty_list_text);
        setTitle(R.string.transactions);
        getTransactions();
    }

    public void getTransactions() {
        try {
            if (integrator != null && integrator.getPay() != null) {
                mProgress.setVisibility(View.VISIBLE);
                mList.setVisibility(View.INVISIBLE);
                mText.setVisibility(View.VISIBLE);
                mText.setText(R.string.please_wait);
                integrator.getPay().getHistory(new Pay.GetHistoryCallBack() {
                    @Override
                    public void onGetHistorySuccess(List<Transaction> transactions) {
                        mProgress.setVisibility(View.GONE);
                        mList.setVisibility(View.VISIBLE);
               /*  ваш код обработки полученого списка карт*/
                        if (transactions == null || transactions.size() == 0) {
                            mText.setText(R.string.err_transactions_is_empty);
                            return;
                        } else {
                            mText.setVisibility(View.GONE);
                        }
                        // Set up the input
                        String[] from = {"name", "purpose"};
                        int[] to = {android.R.id.text1, android.R.id.text2};
                        List<Map<String, String>> items = new LinkedList<Map<String, String>>();
                        for (Transaction transaction : transactions) {
                            Map<String, String> item = new HashMap<String, String>();
                            item.put("name", transaction.getDescription() + "\t " + transaction.getAmount());
                            item.put("purpose", transaction.getDate() + "\t status:" + transaction.getDtStatus());
                            items.add(item);
                        }
                        final SimpleAdapter arrayAdapter = new SimpleAdapter(
                                getContext(),
                                items,
                                android.R.layout.simple_list_item_2, from, to);
                        mList.setAdapter(arrayAdapter);
                    }

                    @Override
                    public void onGetHistoryFailed() {
                /*  ваш код обработки ошибок при получении списка карт */
                        mProgress.setVisibility(View.GONE);
                        mText.setText(R.string.err_internet);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
