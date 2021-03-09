package com.razorpay.razorpay_flutter;

import android.app.Activity;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.razorpay.Checkout;
import com.razorpay.CheckoutActivity;
import com.razorpay.ExternalWalletListener;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.razorpay.razorpay_flutter.iph.Charges;
import com.razorpay.razorpay_flutter.iph.IphOrder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RazorpayDelegate implements ActivityResultListener, ExternalWalletListener, PaymentResultWithDataListener {

    private final Activity activity;
    private Result pendingResult;
    private Map<String, Object> pendingReply;
    private Map<String, Object> requestedArguments;
    private final String API_AUTH_KEY = "cnpwX3Rlc3RfVHg3VEs2V1gwdDdaaVE6enNRSTZKREJQRGlicXNpa2pNaE4xS1Jz";
    private final String WEB_SERVICE_URL = "http://192.168.1.20/App_DataService/api/Service";
//    private final String WEB_SERVICE_URL = "http://appdataservice.iphysicianhub.com/api/Service";
    private final String CREATE_ORDER_URL = "https://api.razorpay.com/v1/orders";
    private final String RAZOR_KEY = "rzp_test_Tx7TK6WX0t7ZiQ";

    // Response codes for communicating with plugin
    private static final int CODE_PAYMENT_SUCCESS = 0;
    private static final int CODE_PAYMENT_ERROR = 1;
    private static final int CODE_PAYMENT_EXTERNAL_WALLET = 2;

    // Payment error codes for communicating with plugin
    private static final int NETWORK_ERROR = 0;
    private static final int INVALID_OPTIONS = 1;
    private static final int PAYMENT_CANCELLED = 2;
    private static final int TLS_ERROR = 3;
    private static final int INCOMPATIBLE_PLUGIN = 3;
    private static final int UNKNOWN_ERROR = 100;
    private IphOrder createdOrder;


    public RazorpayDelegate(Activity activity) {
        this.activity = activity;
    }

    void openCheckout(Map<String, Object> arguments, Result result) {
        this.pendingResult = result;

        requestedArguments = arguments;

       //invokeGetChargeDetails();
         activity.runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    invokeGetChargeDetails();
                }
            }
        );

    }

    private void sendReply(Map<String, Object> data) {
        if (pendingResult != null) {
            pendingResult.success(data);
            pendingReply = null;
        } else {
            pendingReply = data;
        }
    }

    public void resync(Result result) {
        result.success(pendingReply);
        pendingReply = null;
    }

    private static int translateRzpPaymentError(int errorCode) {
        switch (errorCode) {
            case Checkout.NETWORK_ERROR:
                return NETWORK_ERROR;
            case Checkout.INVALID_OPTIONS:
                return INVALID_OPTIONS;
            case Checkout.PAYMENT_CANCELED:
                return PAYMENT_CANCELLED;
            case Checkout.TLS_ERROR:
                return TLS_ERROR;
            case Checkout.INCOMPATIBLE_PLUGIN:
                return INCOMPATIBLE_PLUGIN;
            default:
                return UNKNOWN_ERROR;
        }
    }

    @Override
    public void onPaymentError(int code, String message, PaymentData paymentData) {
        Map<String, Object> reply = new HashMap<>();
        reply.put("type", CODE_PAYMENT_ERROR);

        Map<String, Object> data = new HashMap<>();
        data.put("code", translateRzpPaymentError(code));
        data.put("message", message);

        reply.put("data", data);

        sendReply(reply);
    }

    @Override
    public void onPaymentSuccess(String paymentId, PaymentData paymentData) {
        Map<String, Object> reply = new HashMap<>();
        reply.put("type", CODE_PAYMENT_SUCCESS);

        Map<String, Object> data = new HashMap<>();
        data.put("razorpay_payment_id", paymentData.getPaymentId());
        data.put("razorpay_order_id", paymentData.getOrderId());
        data.put("razorpay_signature", paymentData.getSignature());

        reply.put("data", data);
        sendReply(reply);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Checkout.handleActivityResult(activity, requestCode, resultCode, data, this, this);
        return true;
    }

    @Override
    public void onExternalWalletSelected(String walletName, PaymentData paymentData) {
        Map<String, Object> reply = new HashMap<>();
        reply.put("type", CODE_PAYMENT_EXTERNAL_WALLET);

        Map<String, Object> data = new HashMap<>();
        data.put("external_wallet", walletName);
        reply.put("data", data);

        sendReply(reply);
    }

    private String getReceiptId(){
        String receipt = "receipt";
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMMddHHmmss");
            Date date = new Date();
            receipt = receipt+"_"+dateFormat.format(date);
        }catch(Exception e){
            e.printStackTrace();
        }
        return  receipt;
    }

    private void invokeGetChargeDetails() {
        OkHttpClient client = new OkHttpClient();

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        final JsonObject requestData = new JsonObject();
        requestData.addProperty("ParameterName", "[]");
        requestData.addProperty("ParameterValue", "[]");
        requestData.addProperty("WebMethodName", "GetChargeDetails");

        RequestBody body = RequestBody.create(JSON, requestData.toString());

        Request request = new Request.Builder()
                .url(WEB_SERVICE_URL)
                .post(body)
                .build();

        try {
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                    String responseStr = response.body().string();
                    try {
                        JSONObject mainObject = new JSONObject(responseStr);
                        String resStr = mainObject.getString("Result");

                        Gson gson = new Gson();
                        Type token = new TypeToken<Collection<Charges>>() {
                        }.getType();
                        Collection<Charges> objList = gson.fromJson(resStr, token);

                        List<Charges> chargesList = (List<Charges>) objList;
                        String selectedMethod = (requestedArguments.get("selectedMethod")).toString();
                        float amount = Float.parseFloat(requestedArguments.get("amount") + "");
                        float selectedAmount = amount;

                        for(int i=0; i<chargesList.size()-1; i++){
                            Charges c = chargesList.get(i);
                            selectedMethod = selectedMethod.trim();
                            String cardType = c.getCardType().trim();
                            float minAmount = Float.parseFloat(c.getMinAmount()+"");
                            float maxAmount = Float.parseFloat(c.getMaxAmount()+"");
                            float extraCharge = Float.parseFloat(c.getExtraCharges()+"");
                            float razorPayFee = Float.parseFloat(c.getRazorPayFee()+"");

                            if(selectedMethod.equals(cardType) && (amount >= minAmount && amount <= maxAmount)){
                                float amount1 = (selectedAmount * razorPayFee) / 100;
                                selectedAmount = amount1 + extraCharge;
                                break;
                            }else if(selectedMethod.equals(cardType) && amount <= maxAmount){
                                float amount1 = (selectedAmount * razorPayFee) / 100;
                                selectedAmount = amount1 + extraCharge;
                                break;
                            }
                        }
                        amount = amount + selectedAmount;
                        amount = amount * 100; // convert to paise

                        long finalAmount = (long) amount;
                        requestedArguments.put("amount", finalAmount);

                        invokeCreateOrder(finalAmount);

                    }catch (Exception e){
                        Map<String, Object> reply = new HashMap<>();
                        reply.put("type", "ERROR_GetChargeDetails");
                        Map<String, Object> data = new HashMap<>();
                        data.put("description", "failed to get charges details");
                        data.put("error", e.getMessage());
                        reply.put("data", data);
                        sendReply(reply);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    call.cancel();
                    Map<String, Object> reply = new HashMap<>();
                    reply.put("type", "ERROR_GetChargeDetails");
                    Map<String, Object> data = new HashMap<>();
                    data.put("description", "failed to get charges details");
                    data.put("error", e.getMessage());
                    reply.put("data", data);
                    sendReply(reply);
                }
            });
        } catch (Exception e) {
            Map<String, Object> reply = new HashMap<>();
            reply.put("type", "ERROR_GetChargeDetails");
            Map<String, Object> data = new HashMap<>();
            data.put("description", "failed to get charges details");
            data.put("error", e.getMessage());
            reply.put("data", data);
            sendReply(reply);
            e.printStackTrace();
        }

    }

    private void invokeCreateOrder(long finalAmount){
        try {
            String receipt = getReceiptId();
            JsonObject notesData = new JsonObject();
            notesData.addProperty("notes", "note value");
//            String notes = "note value";
            String notes = "{\"notes\": \"note value\"}";
            requestedArguments.put("receipt", receipt);
            requestedArguments.put("notes", notes);

            String currency = "INR";
            boolean payment_capture = true;
//            long finalAmount = Long.parseLong(requestedArguments.get("amount").toString());

            JsonObject requestData = new JsonObject();
            requestData.addProperty("amount", finalAmount);
            requestData.addProperty("currency", currency);
            requestData.addProperty("receipt", receipt);
            requestData.addProperty("payment_capture", payment_capture);
           // requestData.addProperty("notes", notes);

            Map<String, String> map = new HashMap<>();
            map.put("Authorization", API_AUTH_KEY);

            OkHttpClient client = new OkHttpClient();
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, requestData.toString());

            Request request = new Request.Builder()
                    .header("Authorization", "Basic "+API_AUTH_KEY)
                    .url(CREATE_ORDER_URL)
                    .post(body)
                    .build();

            try {
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                        String responseStr = response.body().string();
                        try {
                            System.out.println(responseStr);

                            Gson gson = new Gson();
                            Type token = new TypeToken<IphOrder>() {
                            }.getType();
                            createdOrder = gson.fromJson(responseStr, token);

                            if(createdOrder!= null && createdOrder.getId()!=null && !TextUtils.isEmpty(createdOrder.getId())){
                                openCheckOut();
                            }else{
                                Map<String, Object> reply = new HashMap<>();
                                reply.put("type", "ERROR_CreateOrder");
                                Map<String, Object> data = new HashMap<>();
                                data.put("description", "failed to create order");
                                data.put("error", "");
                                reply.put("data", data);
                                sendReply(reply);
                            }
                        }catch (Exception e){
                            Map<String, Object> reply = new HashMap<>();
                            reply.put("type", "ERROR_CreateOrder");
                            Map<String, Object> data = new HashMap<>();
                            data.put("description", "failed to create order");
                            data.put("error", e.getMessage());
                            reply.put("data", data);
                            sendReply(reply);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                        call.cancel();
                        Map<String, Object> reply = new HashMap<>();
                        reply.put("type", "ERROR_CreateOrder");
                        Map<String, Object> data = new HashMap<>();
                        data.put("description", "failed to create order");
                        data.put("error", e.getMessage());
                        reply.put("data", data);
                        sendReply(reply);
                    }
                });
            } catch (Exception e) {
                Map<String, Object> reply = new HashMap<>();
                reply.put("type", "ERROR_CreateOrder");
                Map<String, Object> data = new HashMap<>();
                data.put("description", "failed to create order");
                data.put("error", e.getMessage());
                reply.put("data", data);
                sendReply(reply);
            }
        }catch (Exception e){
            Map<String, Object> reply = new HashMap<>();
            reply.put("type", "ERROR_CreateOrder");
            Map<String, Object> data = new HashMap<>();
            data.put("description", "failed to create order");
            data.put("error", e.getMessage());
            reply.put("data", data);
            sendReply(reply);
        }
    }

    private void openCheckOut(){
        try {
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("key", RAZOR_KEY);
            paymentData.put("name", "iHealth Pay");
            paymentData.put("prefill", requestedArguments.get("customer_data"));
            paymentData.put("amount", requestedArguments.get("amount"));
            paymentData.put("currency", "INR");
            paymentData.put("theme", "{color: '#4458CB'}");
//            paymentData.put("image", "");
           // paymentData.put("notes", requestedArguments.get("notes"));
            paymentData.put("order_id", createdOrder.getId());
            paymentData.put("receipt", requestedArguments.get("receipt"));


            JSONObject options = new JSONObject(paymentData);
            Intent intent = new Intent(activity, CheckoutActivity.class);
            intent.putExtra("OPTIONS", options.toString());
            intent.putExtra("FRAMEWORK", "flutter");

            activity.startActivityForResult(intent, Checkout.RZP_REQUEST_CODE);
        }catch (Exception e){
            e.printStackTrace();
            Map<String, Object> reply = new HashMap<>();
            reply.put("type", "ERROR_Payments");
            Map<String, Object> data = new HashMap<>();
            data.put("description", "failed to make payments");
            data.put("error", e.getMessage());
            reply.put("data", data);
            sendReply(reply);
        }
    }

    private void savePaymentData(){

    }
}
