package uoit.ca.attendme;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class WelcomeScreen extends AppCompatActivity {

    TextView errorMsgBox;
    KeyguardManager keyguardManager;
    FingerprintManager fingerprintManager;
    KeyStore keyStore;
    KeyGenerator keyGenerator;
    String KEY = "key";
    Cipher cipher;
    Button continueToAttendence;
    Boolean authenticationSuccess = false;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        dbHelper = new DBHelper(this,null, null, 1);
        errorMsgBox = (TextView) findViewById(R.id.errorBox);
        continueToAttendence=(Button) findViewById(R.id.continueBtn);
        continueToAttendence.setEnabled(false);
        FingerprintManager.CryptoObject cryptoObject;

        //Source and reference for Fingerprint operations: https://www.androidauthority.com/how-to-add-fingerprint-authentication-to-your-android-app-747304/

        //Verify that Mobile's SDK version is above API 23; that permission to use fingerprint is granted;
        //and that a fingerprint is actually registered on the device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);


            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                errorMsgBox.setText("Your device doesn't support fingerprint authentication");
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                errorMsgBox.setText("Please enable the fingerprint permission");
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                errorMsgBox.setText("No fingerprint configured. Please register at least one fingerprint in your device's Settings");
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                errorMsgBox.setText("Please enable lockscreen security in your device's Settings");
            }
            else
            {
                try
                {
                    generateKeystore();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if(initializeCipher())
            {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                FingerprintHandler fingerprintHandler= new FingerprintHandler(this);
                fingerprintHandler.startAuthentication(fingerprintManager,cryptoObject);
            }
        }
    }

    //Method to access Android's keystore and generate one
    private void generateKeystore()
    {
        try
        {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
            keyStore.load(null);
            //Build key generator using parameters that specify the operation the key will be used for, and that the user must verify their identity using fingerprint everytime
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY, KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            keyGenerator.generateKey();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Method to initialize cipher
    public boolean initializeCipher()
    {
        try
        {
            //Get cipher instance using properties already stated in key generator
            cipher=Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES+"/"+KeyProperties.BLOCK_MODE_CBC+"/"+KeyProperties.ENCRYPTION_PADDING_PKCS7);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY,null);
            cipher.init(Cipher.ENCRYPT_MODE,key);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {


        private CancellationSignal cancellationSignal;
        private Context context;

        public FingerprintHandler(Context mContext) {
            context = mContext;
        }


        public void startAuthentication(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject)
        {
            cancellationSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString)
        {
            errorMsgBox.setText(errString);
        }

        @Override
        public void onAuthenticationFailed()
        {
            errorMsgBox.setText("Fingerprint does not match registered print!");
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString)
        {
            errorMsgBox.setText(helpString);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result)
        {
            errorMsgBox.setText("");
            continueToAttendence.setEnabled(true);
        }

    }

    public void onContinue(View v)
    {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean alreadyAttended=dbHelper.findAttendance(date);
        if(alreadyAttended)
        {
            errorMsgBox.setText("You have already attended!");
            return;
        }
        Intent goToLocationAcitiivty = new Intent(this, LocationActivity.class);
        startActivity(goToLocationAcitiivty);
    }

}
