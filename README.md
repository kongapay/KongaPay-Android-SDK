# KongaPay Android SDK Documentation

The KongaPay Android SDK is designed for use in Android. This SDK is useful for
developers to accept payments on their native Android applications. This SDK is written
in Java. The `minSdkVersion` that KongaPay Android SDK supports is 10.

**What's new?**  
Please check the [CHANGELOG](CHANGELOG.md)
for what's new in version [`1.0.0`](CHANGELOG.md#v1.0.0).

## How to import the KongaPay Android SDK

You can get the SDK via maven repository simply by adding the following lines to your
application's `build.gradle` file.


## How to use the KongaPay Android SDK

### 1. Setup your AndroidManifest.xml

The first step in using KongaPay Android SDK is to setup your AndroidManifest.xml file.
#### a. Add internet & access network state permissions
Add the following lines to your AndroidManifest.xml file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

#### b. Add KongaPayActivity
The SDK requires you to add the KongaPayActivity & theme within your `<application></application>` tag
in the AndroidManifest.xml file. Add the KongaPayActivity and your manifest file should like:

```xml
<manifest xlmns:android...>
    <application>
        ...
        <!-- other activities -->
        <activity>...</activity>

        <!--- KongaPayActivity and theme -->
        <activity
            android:name="com.kongapay.android.activities.KongaPayActivity"
            android:theme="@style/KongaPaySDK"/>
    </application>
</manifest>
```

### 2. Build a Configuration.

It is possible to use the SDK in Sandbox mode or a Live mode, hence the need for configuration.
The SDK provides a `Config.Builder()` class for which you can build your configuration.
This step requires you to add your `CLIENT ID` (which is also your `MERCHANT ID`) and
`CLIENT KEY` you will be provided with on registering as a merchant.
A typical Config build for sandbox will look like:

```java
Config config = new Config.Builder(CLIENT_ID, CLIENT_KEY)
                .setEnvironment(Config.ENVIRONMENT_SANDBOX)
                .build();
```
while a typical live configuration looks like
```java
Config config = new Config.Builder(CLIENT_ID, CLIENT_KEY)
                .setEnvironment(Config.ENVIRONMENT_LIVE)
                .build();
```


### 3. Initialize the SDK.

The first step in using the KongaPay Android SDK is to initialize the SDK. To initalize,
you call the method `KongaPay.initliaze()` method. This method takes two parameters:

1. The first parameter is a context (typically an activity).
2. The second parameter is the config object built in step 2 above.

Initialization is typically done this way:
```java
    KongaPay.initialize(getApplicationContext(), config);
```

### 4. Using the SDK for Payment.

There are typically two ways of using the SDK:

  * To do a one-off payment.
  * To allow a user authorize for pre-approved payments.


#### a. One-off payments
In order to use the SDK for a one-off payment, the KongaPay SDK has a method `KongaPay.startPayment()`.
This method takes in 3 parameters:  
1. `fragment` or `activity` - The first parameter is an instance of the current fragment or activity  
2. `bundle` - a [Bundle](http://developer.android.com/reference/android/os/Bundle.html) that contains String values of details for the payment. The bundle must contain `KongaPay.ARG_AMOUNT` and `KongaPay.ARG_TRANSACTION_ID` as keys. The valuen for `KongaPay.ARG_AMOUNT` is the amount to be paid, and the value for `KongaPay.ARG_TRANSACTION_ID` is transaction ID and must be unique every time the SDK is called.  
3. `request code` an integer value for a request code, which will be used to receive
the response when payment is completed.  

An example of this is as shown below:

```java
//generate unique transaction id
String transactionID = /** call function to generate unique transaction ID **/

//build bundle
Bundle bundle = new Bundle();
bundle.putString(KongaPay.ARG_AMOUNT, mEditAmount.getText().toString().trim());
bundle.putString(KongaPay.ARG_TRANSACTION_ID, transactionID);

//start payment
KongaPay.startPayment(MainActivity.this, bundle, REQUEST_KONGAPAY);
```

#### b. Pre-approved payments
A pre-approved payment is one in which merchants can charge a user without the user
explicitly initiating such payments provided the user has authorized such transactions
for a particular merchant.

To do this, the KongaPay SDK has a method `KongaPay.startPreApprovedPayment()`.
Using this method will launch the KongaPay SDK and prompt the user to login and authorize
the merchant to carry out transactions on behalf of the user.

This method requires 2 parameters:  
1. `fragment` or `activity` - The first parameter is an instance of the current fragment or activity.  
2. `request code` - an integer value for the request, which will be used to retrieve the response when payment is completed.  

An example of this is as shown below:

```java
KongaPay.startPreApprovedPayment(MainActivity.this, REQUEST_KONGAPAY_PRE_APPROVED);
```

### 5. Handling Results
Handling results is done in the `onActivityResult()` method of your fragment or your activity.
The type of response you get depends on the kind of payment used. The response is retrieved via a [Parcelable](http://developer.android.com/reference/android/os/Parcelable.html) extra with key `KongaPay.EXTRA_RESULT` from the intent returned in the `onActivityResult` method.

#### a. One-off payments
For the one-off payments, the result is an object of `PaymentResult`.
With this PaymentResult object, you can retrieve the transaction id, payment reference, payment status.
A typical implementation of this is as shown below:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_KONGAPAY) {
        if (resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(KongaPay.EXTRA_RESULT)) {
                PaymentResult result = data.getParcelableExtra(KongaPay.EXTRA_RESULT);

                //use payment result
                String paymentReference = result.getPaymentReference();
                String transactionId = result.getTransactionId();
                String paymentStatus = result.getPaymentStatus();
            }
        } else if (resultCode == RESULT_CANCELED) {
            //user cancelled
        }
    }
    super.onActivityResult(requestCode, resultCode, data);
}
```
where `REQUEST_KONGAPAY` is the request code used in KongaPay.startPayment() method in step 4a.

#### b. Pre-approved payments
For pre-approved payments, the result is an object of `AuthorizationResult`. With this `AuthorizationResult` object, you can retrieve a token, with which you will use to make future transactions on behalf of the user.
A typical implementation of this is as shown below:

```java

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_PRE_APPROVED) {
        if (resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(KongaPay.EXTRA_RESULT)) {
                AuthorizationResult result = data.getParcelableExtra(KongaPay.EXTRA_RESULT);

                //use authorization result
                String token = result.getToken();

                //send token to your own server for future pre-approved transactions.
            }
        } else if(resultCode == RESULT_CANCELED) {
            //user cancelled

        }
    }
    super.onActivityResult(requestCode, resultCode, data);
}
```

###  6. Making payments using KongaPay payment for future pre-approved payments.
See [this guide](https://github.com/kongapay/KongaPay-Android-SDK/blob/master/preapproved_server_side.md),
for information on how to make pre-approved payments.


## Contact
For more inquiries and technical challenges or questions, please send an email to
developers@kongapay.com
