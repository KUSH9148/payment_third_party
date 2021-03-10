import 'package:flutter/material.dart';
import 'package:razorpay_flutter/razorpay_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'iHealthPay Payments'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  IHealthPay _ihealthpay;


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Github Razorpay'),
        ),
        body: Center(
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  RaisedButton(onPressed: payByDebit, child: Text('Pay 100 By Debit')),
                  RaisedButton(onPressed: payByCredit, child: Text('Pay 100 By Credit')),
                  RaisedButton(onPressed: payByUpi, child: Text('Pay 100 By UPI')),
                  RaisedButton(onPressed: payByNetbanking, child: Text('Pay 200 By Net banking')),
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
  void payByNetbanking() {
    openCheckout("netbanking", amount: 200);
  }

  void payByUpi() {
    openCheckout("UPI");
  }

  void openCheckout(selectedMethod, {amount = 100}) async {
    var customer = {
      'name': "Shivam Lussote",
      'contact': '7995055011',
      'email': 'sourabhuniyal@iphysicianhub.com',
      'id': '197545',
      'organization_id':'2875'
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
    print("IHP: ======= _handlePaymentSuccess ======");
    print(response);
  }

  void _handlePaymentError(PaymentFailureResponse response) {
    print("IHP: ======= _handlePaymentError ======");
    print(response);
  }

  void _handleExternalWallet(ExternalWalletResponse response) {
    print("IHP: ======= _handleExternalWallet ======");
    print(response);
  }
}
