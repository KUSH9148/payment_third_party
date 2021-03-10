package com.razorpay.razorpay_flutter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.razorpay.Checkout;
import com.razorpay.CheckoutActivity;
import com.razorpay.ExternalWalletListener;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.razorpay.razorpay_flutter.iph.Charges;
import com.razorpay.razorpay_flutter.iph.IphOrder;

import org.json.JSONException;
import org.json.JSONObject;

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
import okhttp3.Response;

public class RazorpayDelegate implements ActivityResultListener, ExternalWalletListener, PaymentResultWithDataListener {

    private final Activity activity;
    private Result pendingResult;
    private Map<String, Object> pendingReply;
    private Map<String, Object> requestedArguments;
    private final String API_AUTH_KEYGN = "cnpwX3Rlc3RfVHg3VEs2V1gwdDdaaVE6enNRSTZKREJQRGlicXNpa2pNaE4xS1Jz";
    private final String API_AUTH_KEY = "cnpwX3Rlc3RfZkVXS0puV3RmSFZuYkk6RG1yMnpZc2Vkdmp3V0hWSHh1T3dCelNS";
    private final String WEB_SERVICE_URL = "http://192.168.1.20/App_DataService/api/Service";
    //    private final String WEB_SERVICE_URL = "http://appdataservice.iphysicianhub.com/api/Service";
    private final String CREATE_ORDER_URL = "https://api.razorpay.com/v1/orders";
    private final String RAZOR_KEYGN = "rzp_test_Tx7TK6WX0t7ZiQ";
    private final String RAZOR_KEY = "rzp_test_fEWKJnWtfHVnbI";

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
    private Map<String, String> customerDetails;


    public RazorpayDelegate(Activity activity) {
        this.activity = activity;
    }

    void openCheckout(Map<String, Object> arguments, Result result) {
        this.pendingResult = result;

        requestedArguments = arguments;

        customerDetails = new HashMap<>();

        try {
            Map<String, Object> reply = new HashMap<>();
            reply.put("type", CODE_PAYMENT_ERROR);
            Map<String, Object> dataReply = new HashMap<>();

            if(requestedArguments.get("customer_data") == null){
                dataReply.put("message", "Please provide customer_data");
                reply.put("data", dataReply);
                sendReply(reply);
            }else {
                Map<String, String> customerData = (Map<String, String>) requestedArguments.get("customer_data");
                if (!customerData.containsKey("name") || TextUtils.isEmpty(customerData.get("name"))) {
                    dataReply.put("message", "Please provide customer name");
                    reply.put("data", dataReply);
                    sendReply(reply);
                }else if (!customerData.containsKey("contact") || TextUtils.isEmpty(customerData.get("contact"))) {
                    dataReply.put("message", "Please provide customer contact number");
                    reply.put("data", dataReply);
                    sendReply(reply);
                }else if (!customerData.containsKey("id") || TextUtils.isEmpty(customerData.get("id"))) {
                    dataReply.put("message", "Please provide customer id");
                    reply.put("data", dataReply);
                    sendReply(reply);
                }else if (!customerData.containsKey("organization_id") || TextUtils.isEmpty(customerData.get("organization_id"))) {
                    dataReply.put("message", "Please provide organization id");
                    reply.put("data", dataReply);
                    sendReply(reply);
                }else{
                    String CustomerUniqueID = customerData.get("id");
                    String iHealthPayOrganizationID = customerData.get("organization_id");
                    customerDetails.put("name", customerData.get("name"));
                    customerDetails.put("contact", customerData.get("contact"));
                    customerDetails.put("email", customerData.get("email"));
                    customerDetails.put("customer_id", CustomerUniqueID);
                    customerDetails.put("organization_id", iHealthPayOrganizationID);
                    GetChargesHandler getChargesHandler =  new GetChargesHandler();
                    getChargesHandler.execute();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void openCheckOut(){
        try {
            Map<String, String> theme = new HashMap<>();
            theme.put("color", "#4458CB");

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("key", RAZOR_KEY);
            paymentData.put("name", "iHealth Pay");
            paymentData.put("prefill", requestedArguments.get("customer_data"));
            paymentData.put("amount", requestedArguments.get("amount"));
            paymentData.put("currency", "INR");
            paymentData.put("theme", theme);
//            paymentData.put("image", "");
            paymentData.put("notes", requestedArguments.get("notes"));
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
            reply.put("type", CODE_PAYMENT_ERROR);
            Map<String, Object> data = new HashMap<>();
            data.put("description", "failed to make payments");
            data.put("message", e.getMessage());
            reply.put("data", data);
            sendReply(reply);
        }
    }

    public class GetChargesHandler extends AsyncTask<Void, Void, Void> {

        OkHttpClient client = new OkHttpClient();
        Map<String, Object> reply;
        String responseBody;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
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

                Response response = client.newCall(request).execute();
                responseBody = response.body().string();
            } catch (Exception e) {
                reply = new HashMap<>();
                reply.put("type", CODE_PAYMENT_ERROR);
                Map<String, Object> data = new HashMap<>();
                data.put("description", "failed to get charges details");
                data.put("message", e.getMessage());
                reply.put("data", data);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(reply != null){
                sendReply(reply);
            }else {
               calculateCharges(responseBody);
            }
        }
    }

    public void calculateCharges(String responseStr){
        long finalAmount = 0;
        Map<String, Object> reply;
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

            finalAmount = (long) amount;
            requestedArguments.put("amount", finalAmount);


            CreateOrderHandler createOrderHandler = new CreateOrderHandler(finalAmount);
            createOrderHandler.execute();

        }catch (Exception e){
            reply = new HashMap<>();
            reply.put("type", CODE_PAYMENT_ERROR);
            Map<String, Object> data = new HashMap<>();
            data.put("description", "failed to get charges details");
            data.put("message", e.getMessage());
            reply.put("data", data);
            e.printStackTrace();
            sendReply(reply);
        }
    }

    public class CreateOrderHandler extends AsyncTask<Void, Void, Void> {
        OkHttpClient client = new OkHttpClient();
        long finalAmount = 0;
        Map<String, Object> reply;
        String response;

        CreateOrderHandler(Long finalAmount){
            this.finalAmount = finalAmount;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String receipt = getReceiptId();
//                JsonObject notesData = new JsonObject();
//                notesData.addProperty("notes", "note value");
    //            String notes = "note value";
//                String notes = "{\"notes\": \"note value\"}";
                Map<String, Object> notesData = new HashMap<>();
                notesData.put("notes", "note value");

                requestedArguments.put("receipt", receipt);
                requestedArguments.put("notes", notesData);

                String currency = "INR";
                boolean payment_capture = true;
    //            long finalAmount = Long.parseLong(requestedArguments.get("amount").toString());



                Map<String, Object> requestData = new HashMap<>();
                requestData.put("amount", finalAmount);
                requestData.put("currency", currency);
                requestData.put("receipt", receipt);
                requestData.put("payment_capture", payment_capture);
                requestData.put("notes", notesData);

                String data = new GsonBuilder().create().toJson(requestData);

                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, data);

                Request request = new Request.Builder()
                        .header("Authorization", "Basic "+API_AUTH_KEY)
                        .url(CREATE_ORDER_URL)
                        .post(body)
                        .build();

                Response responseBody = client.newCall(request).execute();
                response = responseBody.body().string();
            }catch (Exception e){
                reply = new HashMap<>();
                reply.put("type", CODE_PAYMENT_ERROR);
                Map<String, Object> data = new HashMap<>();
                data.put("description", "failed to create order");
                data.put("message", e.getMessage());
                reply.put("data", data);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(reply != null){
                sendReply(reply);
            }else{
                bindCreateOrderDetails(response);
            }
        }
    }

    public void bindCreateOrderDetails(String responseStr){
        Map<String, Object> reply;
        try {
            Gson gson = new Gson();
            Type token = new TypeToken<IphOrder>() { }.getType();
            createdOrder = gson.fromJson(responseStr, token);
            if(createdOrder!= null && createdOrder.getId()!=null && !TextUtils.isEmpty(createdOrder.getId())){
//               UpdateOrderIDinQuickModePayments updateOrderIDinQuickModePayments = new UpdateOrderIDinQuickModePayments();
//               updateOrderIDinQuickModePayments.execute();
                SaveRazorPaydetails saveRazorPaydetails = new SaveRazorPaydetails();
                saveRazorPaydetails.execute();
                //openCheckOut();
            }else{
                reply = new HashMap<>();
                reply.put("type", CODE_PAYMENT_ERROR);
                Map<String, Object> data = new HashMap<>();
                data.put("message", "failed to create order");
                reply.put("data", data);
                sendReply(reply);
            }
        }catch (Exception e){
            reply = new HashMap<>();
            reply.put("type", CODE_PAYMENT_ERROR);
            Map<String, Object> data = new HashMap<>();
            data.put("message", "failed to create order 455");
            data.put("error", e.getMessage());
            reply.put("data", data);
            sendReply(reply);
        }
    }

    public class UpdateOrderIDinQuickModePayments extends AsyncTask<Void, Void, Void> {
        OkHttpClient client = new OkHttpClient();
        Map<String, Object> reply;
        String response;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String []ParameterName = new String[3];
                ParameterName[0] = "iHealthPayOrganizationID";
                ParameterName[1] = "CustomerUniqueID";
                ParameterName[2] = "OrderID";
                String []ParameterValue = new String[3];
                ParameterValue[0] = customerDetails.get("organization_id");
                ParameterValue[1] = customerDetails.get("customer_id");
                ParameterValue[2] = createdOrder.getId();

                Map<String, Object> requestData = new HashMap<>();
                requestData.put("ParameterName", ParameterName);
                requestData.put("ParameterValue", ParameterValue);
                requestData.put("WebMethodName", "UpdateOrderIDinQuickModePayments");

                String data = new GsonBuilder().create().toJson(requestData);

                RequestBody body = RequestBody.create(JSON, data);
                Request request = new Request.Builder()
                        .url(WEB_SERVICE_URL)
                        .addHeader("content-type", "application/json; charset=utf-8")
                        .post(body)
                        .build();

                Response responseBody = client.newCall(request).execute();
                response = responseBody.body().string();
            } catch (Exception e) {
                reply = new HashMap<>();
                reply.put("type", CODE_PAYMENT_ERROR);
                Map<String, Object> data = new HashMap<>();
                data.put("message", "failed UpdateOrderIDinQuickModePayments");
                data.put("error", e.getMessage());
                reply.put("data", data);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(reply != null){
                sendReply(reply);
            }else {
                openCheckOut();
            }
        }
    }


    public class SaveRazorPaydetails extends AsyncTask<Void, Void, Void> {
        OkHttpClient client = new OkHttpClient();
        Map<String, Object> reply;
        String response;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String []ParameterName = new String[7];
                ParameterName[0] = "Mobile";
                ParameterName[1] = "Name";
                ParameterName[2] = "iHealthPayOrganizationID";
                ParameterName[3] = "CustomerUniqueID";
                ParameterName[4] = "Amount";
                ParameterName[5] = "EmailID";
                ParameterName[6] = "OrderID";
                String []ParameterValue = new String[7];
                ParameterValue[0] = customerDetails.get("name");
                ParameterValue[1] = customerDetails.get("contact");
                ParameterValue[2] = customerDetails.get("organization_id");
                ParameterValue[3] = customerDetails.get("customer_id");
                ParameterValue[4] = requestedArguments.get("amount").toString();
                ParameterValue[5] = customerDetails.get("email");
                ParameterValue[6] = createdOrder.getId();

                Map<String, Object> requestData = new HashMap<>();
                requestData.put("ParameterName", ParameterName);
                requestData.put("ParameterValue", ParameterValue);
                requestData.put("WebMethodName", "SaveRazorPaydetails");

                String data = new GsonBuilder().create().toJson(requestData);

                RequestBody body = RequestBody.create(JSON, data);
                Request request = new Request.Builder()
                        .url(WEB_SERVICE_URL)
                        .addHeader("content-type", "application/json; charset=utf-8")
                        .post(body)
                        .build();

                Response responseBody = client.newCall(request).execute();
                response = responseBody.body().string();
                Log.e("IPH 557", response);
            } catch (Exception e) {
                reply = new HashMap<>();
                reply.put("type", CODE_PAYMENT_ERROR);
                Map<String, Object> data = new HashMap<>();
                data.put("message", "failed save customer");
                data.put("error", e.getMessage());
                reply.put("data", data);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(reply != null){
                sendReply(reply);
            }else {
                openCheckOut();
            }
        }
    }
}
