package com.razorpay.razorpay_flutter.iph;


import org.json.JSONArray;

public class CreateOrderRequest {
    private String amount;
    private String currency;
    private String receipt;
    private boolean payment_capture;
    private JSONArray[]notes;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public boolean isPayment_capture() {
        return payment_capture;
    }

    public void setPayment_capture(boolean payment_capture) {
        this.payment_capture = payment_capture;
    }

    public JSONArray[] getNotes() {
        return notes;
    }

    public void setNotes(JSONArray[] notes) {
        this.notes = notes;
    }
}
