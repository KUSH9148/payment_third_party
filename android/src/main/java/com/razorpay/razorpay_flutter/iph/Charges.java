package com.razorpay.razorpay_flutter.iph;

import java.io.Serializable;

public class Charges  implements Serializable {

    private String MinAmount;
    private String MaxAmount;
    private String CardType;
    private String RazorPayFee;
    private String GSTPercentage;
    private String IPHCharges;
    private String ExtraCharges;
    private String TeleHealthOfficeFee;

    public String getMinAmount() {
        return MinAmount;
    }

    public void setMinAmount(String minAmount) {
        MinAmount = minAmount;
    }

    public String getMaxAmount() {
        return MaxAmount;
    }

    public void setMaxAmount(String maxAmount) {
        MaxAmount = maxAmount;
    }

    public String getCardType() {
        return CardType;
    }

    public void setCardType(String cardType) {
        CardType = cardType;
    }

    public String getRazorPayFee() {
        return RazorPayFee;
    }

    public void setRazorPayFee(String razorPayFee) {
        RazorPayFee = razorPayFee;
    }

    public String getGSTPercentage() {
        return GSTPercentage;
    }

    public void setGSTPercentage(String GSTPercentage) {
        this.GSTPercentage = GSTPercentage;
    }

    public String getIPHCharges() {
        return IPHCharges;
    }

    public void setIPHCharges(String IPHCharges) {
        this.IPHCharges = IPHCharges;
    }

    public String getExtraCharges() {
        return ExtraCharges;
    }

    public void setExtraCharges(String extraCharges) {
        ExtraCharges = extraCharges;
    }

    public String getTeleHealthOfficeFee() {
        return TeleHealthOfficeFee;
    }

    public void setTeleHealthOfficeFee(String teleHealthOfficeFee) {
        TeleHealthOfficeFee = teleHealthOfficeFee;
    }
}
