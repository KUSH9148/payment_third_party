import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'package:razorpay_flutter/razorpay_flutter.dart';

import 'package:fluttertoast/fluttertoast.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const platform = const MethodChannel("razorpay_flutter");

  IHealthPay _ihealthpay;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('IHealthPay Sample App'),
        ),
        body: Center(
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                RaisedButton(onPressed: payByDebit, child: Text('Pay 100 By Debit')),
                  RaisedButton(onPressed: payByCredit, child: Text('Pay 100 By Credit')),
                  RaisedButton(onPressed: payByUpi, child: Text('Pay 100 By UPI')),
                  RaisedButton(onPressed: payByCredit5, child: Text('Pay 50000 By Credit'))
            ])),
      ),
    );
  }

  @override
  void initState() {
    super.initState();
    _ihealthpay = IHealthPay();
    _ihealthpay.on(IHealthPay.EVENT_PAYMENT_SUCCESS, _handlePaymentSuccess);
    _ihealthpay.on(IHealthPay.EVENT_PAYMENT_ERROR, _handlePaymentError);
    _ihealthpay.on(IHealthPay.EVENT_EXTERNAL_WALLET, _handleExternalWallet);

    initIHealthPay();
  }

  @override
  void dispose() {
    super.dispose();
    _ihealthpay.clear();
  }

  void payByDebit() {
   openCheckout("Debit card");
  }

  void payByCredit() {
    openCheckout("Credit card");
  }
  void payByCredit5() {
    openCheckout("Credit card", amount: 5000);
  }

  void payByUpi() {
    openCheckout("UPI");
  }

  String getRandomId(){
    int randomId = 1;
    var rng = new Random();
   // for (var i = 0; i < 10; i++) {
      randomId = rng.nextInt(10000);
   // }
    return randomId.toString();
  }

  void initIHealthPay() async {
    var options = {
      'account_id': "acc_01245",
      'organization_id':'2883'
    };
    try {
      _ihealthpay.init(options);
    } catch (e) {
      debugPrint(e);
    }
  }

  void openCheckout(selectedMethod, {amount = 100}) async {
    String customerId = getRandomId();
    print("CID:"+customerId);
    var customer = {
      'name': "Ravi P",
      'contact': '7995055011',
      'email': 'sourabhuniyal@iphysicianhub.com',
      'id': customerId,
      'organization_id':'2883'
    };
    var options = {
      'key': 'iph_gujarat_pharmacy',
      'amount': amount,
      'customer_data': customer,
      'selectedMethod': selectedMethod
    };

    try {
      _ihealthpay.open(options);
    } catch (e) {
      debugPrint(e);
    }
  }

  void _handlePaymentSuccess(PaymentSuccessResponse response) {
    Fluttertoast.showToast(
        msg: "SUCCESS: " + response.paymentId, timeInSecForIos: 4);
  }

  void _handlePaymentError(PaymentFailureResponse response) {
    Fluttertoast.showToast(
        msg: "ERROR: " + response.code.toString() + " - " + response.message,
        timeInSecForIos: 4);
  }

  void _handleExternalWallet(ExternalWalletResponse response) {
    Fluttertoast.showToast(
        msg: "EXTERNAL_WALLET: " + response.walletName, timeInSecForIos: 4);
  }
}
