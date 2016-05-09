package com.kongapay.example.kongapaysdksample;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.kongapay.android.AuthorizationResult;
import com.kongapay.android.Config;
import com.kongapay.android.KongaPay;
import com.kongapay.android.PaymentResult;

public class SampleActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ONE_OFF_PAYMENT = 1000;
    private static final int REQUEST_CODE_KONGAPAY_PRE_APPROVED = 1001;

    private static final String CLIENT_ID = "kongaPayApp";
    private static final String CLIENT_KEY = "Kpay4pp$ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();

        setupView();

        //create config object
        Config config = new Config.Builder(CLIENT_ID, CLIENT_KEY)
                .setEnvironment(Config.ENVIRONMENT_SANDBOX)
                .build();

        //initialize KongaPay SDK
        KongaPay.initialize(getApplicationContext(), config);

    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.tool_bar_title));
        }
    }

    private void setupView() {
        Button oneOffPaymentButton = (Button)findViewById(R.id.button_pay_one_off);
        Button recurrentPaymentButton = (Button)findViewById(R.id.button_pay_recurrent);

        oneOffPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOneOffPaymentButtonClicked();
            }
        });

        recurrentPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecurrentPaymentButtonClicked();
            }
        });

    }

    /**
     * handles click for the one off payment button
     */
    private void onOneOffPaymentButtonClicked() {
        String transactionId = generateUniqueTransactionId();
        String amount = "20000";

        Bundle bundle = new Bundle();
        bundle.putString(KongaPay.ARG_AMOUNT, amount);
        bundle.putString(KongaPay.ARG_TRANSACTION_ID, transactionId);

        KongaPay.startPayment(this, bundle, REQUEST_CODE_ONE_OFF_PAYMENT);
    }

    /**
     * handles click event for the recurrent payment button
     */
    private void onRecurrentPaymentButtonClicked() {
        KongaPay.startPreApprovedPayment(this, REQUEST_CODE_KONGAPAY_PRE_APPROVED);
    }

    /**
     * function to generate uniques transaction ID for this transaction
     */
    private String generateUniqueTransactionId() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.hasExtra(KongaPay.EXTRA_RESULT)) {
            switch (requestCode) {
                case REQUEST_CODE_ONE_OFF_PAYMENT:
                    PaymentResult result = data.getParcelableExtra(KongaPay.EXTRA_RESULT);
                    showToast(String.format("Payment Reference:  %s\n",result.getPaymentReference()));
                    break;
                case REQUEST_CODE_KONGAPAY_PRE_APPROVED:
                    AuthorizationResult oauthResult = data.getParcelableExtra(KongaPay.EXTRA_RESULT);

                    String token = oauthResult.getToken();

                    showToast(String.format("Authorization token: %s\n", token));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String message) {
        Toast.makeText(SampleActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
