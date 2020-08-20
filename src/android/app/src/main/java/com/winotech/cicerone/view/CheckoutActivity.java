package com.winotech.cicerone.view;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.GeneralController;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.model.Event;
import com.winotech.cicerone.model.Subscribed;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;


public class CheckoutActivity extends Activity {

    // puntatore al controller delle attività
    private static transient GeneralController generalController;

    // oggetti grafici
    private TextView myNameTextView;
    private TextView myGroupDimensionTextView;
    private TextView amountTextView;

    // puntatore all'iscrizione
    private Subscribed subscribed;

    // costo totale da pagare
    float amount;

    // definizione  delle stringhe per il salvataggio del pagamento
    String globetrotterUsername;
    String eventID;

    // tag della stringa di debug
    private static final String TAG = "paymentExample";

    // definizione della configurazione dell'environment
    // imposta ENVIRONMENT_PRODUCTION per transazioni reali,
    // ENVIRONMENT_SANDBOX per usare il sandbox di PayPal e
    // ENVIRONMENT_NO_NETWORK per effettuare tutto in locale
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX; //ENVIRONMENT_NO_NETWORK;

    // credenziali per l'id del cliente (esse sono diverse tra account live e sandbox)
    private static final String CONFIG_CLIENT_ID = "AZvr8dGFULJabdCfbONbFDNT3ki0XdjWMT8ttQBX8wl6TI8GIemZXlgjxBsiBIdLn9y1ehtBRgo011nO";

    // costanti per il costrutto switch-case
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int REQUEST_CODE_PROFILE_SHARING = 3;

    // configurazione dell'oggetto PayPal
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            // I successivi sono usati solo per l'activity PayPalFuturePaymentActivity.
            .merchantName("Example Merchant")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        // definizione del puntatore ai controller
        generalController = GeneralController.getInstance();

        // inizializzazione degli oggetti grafici
        myNameTextView = this.findViewById(R.id.my_email_textView);
        myGroupDimensionTextView = this.findViewById(R.id.cicerone_email_textView);
        amountTextView = this.findViewById(R.id.amount_textView);

        // acquisizione dell'oggetto
        globetrotterUsername = getIntent().getExtras().getString("GLOBETROTTER_USER");
        eventID = getIntent().getExtras().getString("EVENT_ID");
        String cost = getIntent().getExtras().getString("COST");
        String groupDimension = getIntent().getExtras().getString("GROUP_DIMENSION");
        String firstName = getIntent().getExtras().getString("FIRST_NAME");
        String lastName = getIntent().getExtras().getString("LAST_NAME");

        // xosto totale da pagare
        amount = Integer.parseInt(groupDimension) * Float.parseFloat(cost);

        // definizione del nome
        String name = firstName + " " + lastName;
        myNameTextView.setText(name);

        // definizione della dimensione del gruppo
        myGroupDimensionTextView.setText(groupDimension);

        // definizione del costo totale
        String amountString = cost + "€ x " + groupDimension + " = " +
                amount + "€";
        amountTextView.setText(amountString);
    }


    public void onBuyPressed(View pressed) {

        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }


    private PayPalPayment getThingToBuy(String paymentIntent) {

        // definizione del costo da passare
        BigDecimal numericCost = BigDecimal.valueOf(amount);
        String causal = "Payment for Cicerone's app event";

        return new PayPalPayment(numericCost, "EUR", causal, paymentIntent);
    }


    protected void displayResultText(String result) {

        ((TextView)findViewById(R.id.txtResult)).setText("Result : " + result);
        Toast.makeText(
                getApplicationContext(),
                result, Toast.LENGTH_LONG)
                .show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_PAYMENT) {

            if (resultCode == Activity.RESULT_OK) {

                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if (confirm != null) {

                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        displayResultText(getResources().getString(R.string.payment_successfully));

                        generalController.savePayment(globetrotterUsername, eventID);

                        finish();


                    } catch (JSONException e) {

                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                Log.i(TAG, "The user canceled.");

            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {

                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }

        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {

            if (resultCode == Activity.RESULT_OK) {

                PayPalAuthorization auth = data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {

                    try {

                        Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        displayResultText("Future Payment code received from PayPal");

                    } catch (JSONException e) {

                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                Log.i("FuturePaymentExample", "The user canceled.");

            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {

                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }

        } else if (requestCode == REQUEST_CODE_PROFILE_SHARING) {

            if (resultCode == Activity.RESULT_OK) {

                PayPalAuthorization auth = data.getParcelableExtra(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION);

                if (auth != null) {

                    try {

                        Log.i("ProfileSharingExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("ProfileSharingExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        displayResultText("Profile Sharing code received from PayPal");

                    } catch (JSONException e) {

                        Log.e("ProfileSharingExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {

                Log.i("ProfileSharingExample", "The user canceled.");

            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {

                Log.i(
                        "ProfileSharingExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }
    }


    private void sendAuthorizationToServer(PayPalAuthorization authorization) {

        /**
         * TODO: Send the authorization response to your server, where it can
         * exchange the authorization code for OAuth access and refresh tokens.
         *
         * Your server must then store these tokens, so that your server code
         * can execute payments for this user in the future.
         *
         * A more complete example that includes the required app-server to
         * PayPal-server integration is available from
         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
         */

    }


    @Override
    public void onDestroy() {

        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}