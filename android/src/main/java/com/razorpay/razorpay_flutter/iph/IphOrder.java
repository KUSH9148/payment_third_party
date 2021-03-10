package com.razorpay.razorpay_flutter.iph;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

public class IphOrder  implements Serializable {

    private String id;
    private String order_id;
    private String description;
    private long amount;
    private String currency;
    private String receipt;
    private boolean payment_capture;
    private JSONObject notes;
    private boolean authorized;

    private long amount_paid;
    private long amount_due;
    private String status;
    private int attempts;
    private long created_at;
    private String entity;
    private String method;
    private String paymentMethod;
    private boolean international;
    private String refund_status;
    private long amount_refunded;
    private boolean captured;
    private String name;
    private String email;
    private String contact;
    private String fee;
    private long tax;
    private String error_code;
    private String error_description;
//    private NetbankingProvider netbankingProvider;
//    private Wallets wallet;
//    private CardDetails cardDetails;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
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

    public JSONObject getNotes() {
        return notes;
    }

    public void setNotes(JSONObject notes) {
        this.notes = notes;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public long getAmount_paid() {
        return amount_paid;
    }

    public void setAmount_paid(long amount_paid) {
        this.amount_paid = amount_paid;
    }

    public long getAmount_due() {
        return amount_due;
    }

    public void setAmount_due(long amount_due) {
        this.amount_due = amount_due;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isInternational() {
        return international;
    }

    public void setInternational(boolean international) {
        this.international = international;
    }

    public String getRefund_status() {
        return refund_status;
    }

    public void setRefund_status(String refund_status) {
        this.refund_status = refund_status;
    }

    public long getAmount_refunded() {
        return amount_refunded;
    }

    public void setAmount_refunded(long amount_refunded) {
        this.amount_refunded = amount_refunded;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public long getTax() {
        return tax;
    }

    public void setTax(long tax) {
        this.tax = tax;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getError_description() {
        return error_description;
    }

    public void setError_description(String error_description) {
        this.error_description = error_description;
    }

//    public NetbankingProvider getNetbankingProvider() {
//        return netbankingProvider;
//    }
//
//    public void setNetbankingProvider(NetbankingProvider netbankingProvider) {
//        this.netbankingProvider = netbankingProvider;
//    }
//
//    public Wallets getWallet() {
//        return wallet;
//    }
//
//    public void setWallet(Wallets wallet) {
//        this.wallet = wallet;
//    }
//
//    public CardDetails getCardDetails() {
//        return cardDetails;
//    }
//
//    public void setCardDetails(CardDetails cardDetails) {
//        this.cardDetails = cardDetails;
//    }
}
