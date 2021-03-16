import Flutter
import Razorpay

public class RazorpayDelegate: NSObject, RazorpayPaymentCompletionProtocolWithData, ExternalWalletSelectionProtocol {
    
    static let CODE_PAYMENT_SUCCESS = 0
    static let CODE_PAYMENT_ERROR = 1
    static let CODE_PAYMENT_EXTERNAL_WALLET = 2
    
    static let NETWORK_ERROR = 0
    static let INVALID_OPTIONS = 1
    static let PAYMENT_CANCELLED = 2
    static let TLS_ERROR = 3
    static let INCOMPATIBLE_PLUGIN = 3
    static let UNKNOWN_ERROR = 100
    var API_AUTH_KEY = "";
    var RAZOR_KEY = "";
    
    let WEB_SERVICE_URL = "http://192.168.1.20/App_DataService/api/Service";
    //let WEB_SERVICE_URL = "http://appdataservice.iphysicianhub.com/api/Service";
    //let WEB_SERVICE_URL = "http://stageservices.iphysicianhub.com/api/Service";
    
    
    let CREATE_ORDER_URL = "https://api.razorpay.com/v1/orders";
    
    
    var requestedArguments:Dictionary<String, Any> = [:];
    var customerDetails:Dictionary<String, Any> = [:];
    var initializeDetails:Dictionary<String, String> = [:];
    var order:Dictionary<String, Any> = [:];
    
    
    
    
    
    public func onExternalWalletSelected(_ walletName: String, withPaymentData paymentData: [AnyHashable : Any]?) {
        var response = [String:Any]()
        response["type"] = RazorpayDelegate.CODE_PAYMENT_EXTERNAL_WALLET
        
        var data = [String:Any]()
        data["external_wallet"] = walletName
        response["data"] = data
        
        pendingResult(response as NSDictionary)
    }
    
    private var pendingResult: FlutterResult!
    
    public func onPaymentError(_ code: Int32, description message: String, andData data: [AnyHashable : Any]?) {
        var response = [String:Any]()
        response["type"] = RazorpayDelegate.CODE_PAYMENT_ERROR
        
        var data = [String:Any]()
        data["code"] = RazorpayDelegate.translateRzpPaymentError(errorCode: Int(code))
        data["message"] = message
        
        response["data"] = data
        print("58")
        
        pendingResult(response as NSDictionary)
    }
    
    public func onPaymentSuccess(_ payment_id: String, andData data: [AnyHashable: Any]?) {
        var response = [String:Any]()
        response["type"] = RazorpayDelegate.CODE_PAYMENT_SUCCESS
        //response["data"] = data
        
        var dataReply = Dictionary<String, Any>();
        dataReply.updateValue(data?["razorpay_payment_id"] ?? "", forKey: "ihealthpay_payment_id");
        dataReply.updateValue(data?["razorpay_order_id"] ?? "", forKey: "ihealthpay_order_id");
        dataReply.updateValue(data?["razorpay_signature"] ?? "", forKey: "ihealthpay_signature");
        
        response["data"] = dataReply
        
        print("75")
        
        pendingResult(response as NSDictionary)
    }
    
    public func initialized(options: Dictionary<String, String>, result: @escaping FlutterResult) {
        
        self.pendingResult = result
        self.initializeDetails = options;
        
        
        if(initializeDetails == nil){
            sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide organization_id and account_id");
        }else {
            if ((initializeDetails["account_id"] == nil) && initializeDetails["account_id"] == "") {
                sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide your account id");
            }else if(initializeDetails["organization_id"] == nil && initializeDetails["organization_id"] == ""){
                sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide your organization id");
            }else{
                getIHealthPayDetails();
            }
        }
    }
    
    public func open(options: Dictionary<String, Any>, result: @escaping FlutterResult) {
        
        self.pendingResult = result
        self.requestedArguments = options;
        
        self.RAZOR_KEY = getValue(key: "iHealthKey");
        self.API_AUTH_KEY = getValue(key: "iHealthAuthKey")
        
        customerDetails = Dictionary.init();
        
        
        if(requestedArguments["customer_data"] == nil){
            sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide customer_data");
        }else {
            let customerData:Dictionary<String, String> = requestedArguments["customer_data"] as! Dictionary<String, String>;
            if ((customerData["name"] == nil) && customerData["name"] == "") {
                sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide customer name");
            }else if(customerData["contact"] == nil && customerData["contact"] == ""){
                sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide contact number");
            }else if(customerData["id"] == nil && customerData["id"] == ""){
                sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide customer id");
            }else if(customerData["organization_id"] == nil && customerData["organization_id"] == ""){
                sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Please provide organization id");
            }else{
                customerDetails.updateValue(customerData["name"] ?? "", forKey: "name");
                customerDetails.updateValue(customerData["contact"] ?? "", forKey: "contact");
                customerDetails.updateValue(customerData["email"] ?? "", forKey: "email");
                customerDetails.updateValue(customerData["id"] ?? "", forKey: "customer_id");
                customerDetails.updateValue(customerData["organization_id"] ?? "", forKey: "organization_id");
                
                getCharges();
            }
        }
    
        
        //let key = options["key"] as? String
        
        
    }
    
    
    private func sendReply(code:Any, error:String="", message:String = ""){
        var response = [String:Any]()
        response["type"] = code
        
        var data = [String:Any]()
        data["code"] = code
        data["message"] = message
        data["error"] = error
        
        response["data"] = data
        pendingResult(response as NSDictionary)
    }
    
    public func resync(result: @escaping FlutterResult) {
        result(nil)
    }
    
    static func translateRzpPaymentError(errorCode: Int) -> Int {
        switch (errorCode) {
        case 0:
            return NETWORK_ERROR
        case 1:
            return INVALID_OPTIONS
        case 2:
            return PAYMENT_CANCELLED
        default:
            return UNKNOWN_ERROR
        }
    }
    
    private func getCharges(){
        //declare parameter as a dictionary which contains string as key and value combination.
        let parameters = ["ParameterName": "[]", "ParameterValue": "[]", "WebMethodName": "GetChargeDetails"]
        

        //create the url with NSURL
        let url = URL(string: WEB_SERVICE_URL)!

        //create the session object
        let session = URLSession.shared

        //now create the Request object using the url object
        var request = URLRequest(url: url)
        request.httpMethod = "POST" //set http method as POST

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: parameters, options: .prettyPrinted) // pass dictionary to data object and set it as request body
        } catch let error {
            self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to get charges");
            //print(error.localizedDescription)
            //completion(nil, error)
        }

        //HTTP Headers
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        //request.addValue("application/json", forHTTPHeaderField: "Accept")

        //create dataTask using the session object to send data to the server
        let task = session.dataTask(with: request, completionHandler: { data, response, error in

            guard error == nil else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to get charges");
               // completion(nil, error)
                return
            }

            guard let data = data else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to get charges");
               // completion(nil, NSError(domain: "dataNilError", code: -100001, userInfo: nil))
                return
            }

            do {
                //create json object from data
                guard let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? Dictionary<String, Any> else {
                   // completion(nil, NSError(domain: "invalidJSONTypeError", code: -100009, userInfo: nil))
                    self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to get charges");
                    return
                }
                print("192")
                self.calculateCharges(responseStr: data,charges: json["Result"] as! NSArray);

               // completion(json, nil)
            } catch let error {
                //print(error.localizedDescription)
               // completion(nil, error)
            }
        })

        task.resume()
    }
    
    private func calculateCharges(responseStr: Data, charges: NSArray){
        
        let selectedMethod:String = requestedArguments["selectedMethod"] as! String;
        var amount:Double = requestedArguments["amount"] as! Double;
        var selectedAmount:Double = 0.0;
        requestedArguments.updateValue(amount , forKey: "actual_amount");
        
        for obj in charges {
            if let charge = obj as? NSDictionary {
                
                // Now reference the data you need using:
                let CardType = ((charge.value(forKey: "CardType") ?? "" ) as AnyObject).trimmingCharacters(in: .whitespacesAndNewlines)
                let MinAmount:Double = charge.value(forKey: "MinAmount") as! Double
                let MaxAmount:Double = charge.value(forKey: "MaxAmount") as! Double
                let ExtraCharges:Double = charge.value(forKey: "ExtraCharges") as! Double
                let RazorPayFee:Double = charge.value(forKey: "RazorPayFee") as! Double
                
                
                if ((selectedMethod ) == CardType && (amount >= MinAmount && amount <= MaxAmount)) {
                    let amount1 = (amount * RazorPayFee) / 100;
                    selectedAmount = amount1 + ExtraCharges;
                    break;
                } else if ((selectedMethod ) == CardType && amount <= MaxAmount) {
                    let amount1 = (amount * RazorPayFee) / 100;
                    selectedAmount = amount1 + ExtraCharges;
                    break;
                }
            }
        }
        amount = amount + selectedAmount;
        requestedArguments.updateValue(amount, forKey:"final_amount_rs");
        amount = amount * 100; // convert to paise

        requestedArguments.updateValue(amount, forKey: "amount");
        
        
        invokeCreateOrder()
    }
    
    func invokeCreateOrder(){
        print(requestedArguments["amount"] as! Double)
        
        
        let receipt = self.getReceiptId();
        var notestValue = Dictionary<String, String>();
        notestValue.updateValue("note value", forKey: "notes");
        
        requestedArguments.updateValue(notestValue, forKey: "notes");
        
        var orderRequest = Dictionary<String, Any>();
        orderRequest.updateValue(requestedArguments["amount"] ?? "0", forKey: "amount");
        orderRequest.updateValue("INR", forKey: "currency");
        orderRequest.updateValue(true, forKey: "payment_capture");
        orderRequest.updateValue(receipt, forKey: "receipt");
        orderRequest.updateValue(requestedArguments["amount"] ?? "", forKey: "amount");
        orderRequest.updateValue(notestValue, forKey: "notes");
        

        //create the url with NSURL
        let url = URL(string: CREATE_ORDER_URL)!

        //create the session object
        let session = URLSession.shared

        //now create the Request object using the url object
        var request = URLRequest(url: url)
        request.httpMethod = "POST" //set http method as POST
        request.addValue("Basic "+API_AUTH_KEY, forHTTPHeaderField: "Authorization");

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: orderRequest, options: .prettyPrinted) // pass dictionary to data object and set it as request body
        } catch let error {
            self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to create order");
            //print(error.localizedDescription)
            //completion(nil, error)
        }

        //HTTP Headers
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        //request.addValue("application/json", forHTTPHeaderField: "Accept")

        //create dataTask using the session object to send data to the server
        let task = session.dataTask(with: request, completionHandler: { data, response, error in

            guard error == nil else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to create order");
               // completion(nil, error)
                return
            }

            guard let data = data else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to create order");
               // completion(nil, NSError(domain: "dataNilError", code: -100001, userInfo: nil))
                return
            }

            do {
                //create json object from data
                guard let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? Dictionary<String, Any> else {
                   // completion(nil, NSError(domain: "invalidJSONTypeError", code: -100009, userInfo: nil))
                    self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to create order");
                    return
                }
                print("310")
                
                self.bindCreateOrderDetails(order: json);

               // completion(json, nil)
            } catch let error {
                //print(error.localizedDescription)
               // completion(nil, error)
            }
        })

        task.resume()
    }
    
    func bindCreateOrderDetails(order: Dictionary<String, Any>){
        self.order = order;
        if(order == nil || order["error"] != nil){
            self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to create order");
        }else{
            
            self.invokeSaveRazorPaydetails();
        }
    }
    
    func invokeSaveRazorPaydetails(){
        //declare parameter as a dictionary which contains string as key and value combination.
        let parameterNames = ["Mobile", "Name", "iHealthPayOrganizationID", "CustomerUniqueID", "Amount", "EmailID", "OrderID"]
        let parameterValue = [customerDetails["contact"], customerDetails["name"], customerDetails["organization_id"], customerDetails["customer_id"], requestedArguments["final_amount_rs"], customerDetails["email"], self.order["id"]]
        
        var saveRequest = Dictionary<String, Any>();
        saveRequest.updateValue(parameterNames, forKey: "ParameterName");
        saveRequest.updateValue(parameterValue, forKey: "ParameterValue");
        saveRequest.updateValue("SaveRazorPaydetails", forKey: "WebMethodName");

        //create the url with NSURL
        let url = URL(string: WEB_SERVICE_URL)!

        //create the session object
        let session = URLSession.shared

        //now create the Request object using the url object
        var request = URLRequest(url: url)
        request.httpMethod = "POST" //set http method as POST

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: saveRequest, options: .prettyPrinted) // pass dictionary to data object and set it as request body
        } catch let error {
            self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to get charges");
            //print(error.localizedDescription)
            //completion(nil, error)
        }

        //HTTP Headers
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        //request.addValue("application/json", forHTTPHeaderField: "Accept")

        //create dataTask using the session object to send data to the server
        let task = session.dataTask(with: request, completionHandler: { data, response, error in

            guard error == nil else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to save payment details");
               // completion(nil, error)
                return
            }

            guard let data = data else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to save payment details");
               // completion(nil, NSError(domain: "dataNilError", code: -100001, userInfo: nil))
                return
            }

            do {
                //create json object from data
                guard let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? Dictionary<String, Any> else {
                   // completion(nil, NSError(domain: "invalidJSONTypeError", code: -100009, userInfo: nil))
                    self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to gsave payment details");
                    return
                }
                print("391")
                print(json)
                self.onPaymentDetailsSaved();

               // completion(json, nil)
            } catch let error {
                //print(error.localizedDescription)
               // completion(nil, error)
            }
        })

        task.resume()
        
    }
    private func onPaymentDetailsSaved(){
        DispatchQueue.main.async {
            self.openCheckout();
        }
    }
    private func openCheckout(){
        
        var theme = Dictionary<String, Any>();
        theme.updateValue("#4458CB", forKey: "color");

        var paymentData = Dictionary<String, Any>();
        paymentData.updateValue(RAZOR_KEY, forKey: "key");
        paymentData.updateValue("iHealth Pay", forKey: "name");
        paymentData.updateValue(requestedArguments["customer_data"] ?? [], forKey: "prefill");
        paymentData.updateValue(requestedArguments["amount"] ?? 0, forKey: "amount");
        paymentData.updateValue("INR", forKey: "currency");
        paymentData.updateValue(theme, forKey: "theme");
        paymentData.updateValue(requestedArguments["notes"] ?? 0, forKey: "notes");
        paymentData.updateValue(order["id"] ?? 0, forKey: "order_id");
        paymentData.updateValue(requestedArguments["amount"] ?? 0, forKey: "receipt");
        
        let value = requestedArguments["actual_amount"]

        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        guard let gspcAmount = formatter.string(for: value) else { return}
        
        paymentData.updateValue("GSPC Payment;" +  gspcAmount, forKey: "description");
    
        print("436")
        
        let razorpay = RazorpayCheckout.initWithKey(self.RAZOR_KEY , andDelegateWithData: self);
        razorpay.setExternalWalletSelectionDelegate(self);
        var options = paymentData;
        options["integration"] = "flutter"
        options["FRAMEWORK"] = "flutter"
        razorpay.open(options)
    }
    
    func getReceiptId() -> String{
        let date : Date = Date()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyyMMMddHHmmss"
        let todaysDate = dateFormatter.string(from: date)
        return "receipt_i"+todaysDate;
    }
    
    
    func getIHealthPayDetails(){
        //declare parameter as a dictionary which contains string as key and value combination.
        let parameterNames = ["AccountID", "iHealthPayOrganizationID"]
        let parameterValue = [initializeDetails["account_id"], initializeDetails["organization_id"]]
        
        var saveRequest = Dictionary<String, Any>();
        saveRequest.updateValue(parameterNames, forKey: "ParameterName");
        saveRequest.updateValue(parameterValue, forKey: "ParameterValue");
        saveRequest.updateValue("GetIhealthpayCredentials", forKey: "WebMethodName");

        //create the url with NSURL
        let url = URL(string: WEB_SERVICE_URL)!

        //create the session object
        let session = URLSession.shared

        //now create the Request object using the url object
        var request = URLRequest(url: url)
        request.httpMethod = "POST" //set http method as POST

        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: saveRequest, options: .prettyPrinted) // pass dictionary to data object and set it as request body
        } catch let error {
            self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to get charges");
            //print(error.localizedDescription)
            //completion(nil, error)
        }

        //HTTP Headers
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        //request.addValue("application/json", forHTTPHeaderField: "Accept")

        //create dataTask using the session object to send data to the server
        let task = session.dataTask(with: request, completionHandler: { data, response, error in

            guard error == nil else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to initialize ihealth pay");
               // completion(nil, error)
                return
            }

            guard let data = data else {
                self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to initialize ihealth pay");
               // completion(nil, NSError(domain: "dataNilError", code: -100001, userInfo: nil))
                return
            }

            do {
                //create json object from data
                guard let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? Dictionary<String, Any> else {
                   // completion(nil, NSError(domain: "invalidJSONTypeError", code: -100009, userInfo: nil))
                    self.sendReply(code:RazorpayDelegate.CODE_PAYMENT_ERROR, error: "Failed to initialize ihealth pay");
                    return
                }
                print("527")
                self.saveIhealthPayDetails(ihaDetails: json["Result"] as! NSDictionary);

               // completion(json, nil)
            } catch let error {
                //print(error.localizedDescription)
               // completion(nil, error)
            }
        })

        task.resume()
        
    }
    
    private func saveIhealthPayDetails(ihaDetails: NSDictionary){
        
        let preferences = UserDefaults.standard

        preferences.set(ihaDetails["Key"], forKey: "iHealthKey")
        preferences.set((ihaDetails["Auth_Key"] as! String), forKey: "iHealthAuthKey")

        //  Save to disk
        let didSave = preferences.synchronize()

        if !didSave {
            //  Couldn't save (I've never seen this happen in real world testing)
        }
    }
    private func getValue(key: String) -> String{
        let preferences = UserDefaults.standard
        var value = "";
        if preferences.object(forKey: key) == nil {
            //  Doesn't exist
        } else {
            value = preferences.string(forKey: key) ?? ""
        }
        return value;
    }
}
