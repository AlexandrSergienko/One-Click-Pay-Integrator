package com.olexandr.sergiienko.pay;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import com.olexandr.sergiienko.pay.privatbank.oneclickpay.OneClickPayIntegrator;

import ua.privatbank.payoneclicklib.model.PayData;

public class MainActivity extends Activity {
    private static final String MERCHANT_ID = "";
    private static final String DESCRIPTION = "Мойка %s (%s)";
    private OneClickPayIntegrator mIntegrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntegrator = new OneClickPayIntegrator(this, MERCHANT_ID);
        mIntegrator.showHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        PayData payData = new PayData();
        payData.setCcy(OneClickPayIntegrator.Currency.UAH.name());
        payData.setDescription(String.format(DESCRIPTION, "alex", "test"));
        payData.setAmount("0.01");
        mIntegrator.pay(payData);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
