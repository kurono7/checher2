package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends AppCompatActivity {
    private EditText loginMail;
    private EditText loginPassword;
    private Button loginBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginMail = findViewById(R.id.loginMail);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP();

                if (connectionHTTP.isNetworkAvailable(LoginActivity.this)) {
                    connectionHTTP.sendAutentification("", loginMail.getText().toString(), encryptText(loginPassword.getText().toString()),"",createTransactionID());
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Create a Handler instance on the main thread
                    final Handler handler = new Handler();
                    // Create and start a new Thread
                    new Thread(new Runnable() {
                        int time = 0;
                        public void run() {
                            try {
                                for (time = 0; time < ConnectionHTTP.WAIT && !connectionHTTP.isFinishProcess(); time += 100) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        Toast.makeText(LoginActivity.this, "Inténtalo más tarde", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                            } // Just catch the InterruptedException

                            // Now we use the Handler to post back to the main thread
                            handler.post(new Runnable() {
                                public void run() {
                                    if (time >= connectionHTTP.WAIT) {
                                        Toast.makeText(LoginActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                                    } else if (connectionHTTP.getStatusResponse() >= 300) {
                                        Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String token = "";
                                        try {
                                            JSONObject respon = new JSONObject(connectionHTTP.getResponse());
                                            token = respon.getString("token");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString("token",token);
                                        editor.apply();

                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    }
                                    // Set the View's visibility back on the main UI Thread
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }

    // Encrypts string and encode in Base64
    public static String encryptText(String plainText) {
        String encryptedString = "";
        try {
            // ---- Use specified 3DES key and IV from other source --------------
            byte[] plaintext = plainText.getBytes();//input

            byte[] tdesKeyData = "JCm6Xx4TnA94K8A8SJCAjXTUzE3DnYBtJCm6".getBytes("UTF-8");
            byte[] myIV = "CAjXTUzXx4TnA94K8A8SJE3DnYBt".getBytes("UTF-8");
            SecretKeySpec myKey = new SecretKeySpec(tdesKeyData,0,128/8, "DESede");
            IvParameterSpec ivspec = new IvParameterSpec(myIV,0,128/16);
            Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS7Padding");
            c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
            byte[] cipherText = c3des.doFinal(plaintext);
            encryptedString = Base64.encodeToString(cipherText, Base64.DEFAULT);
            encryptedString = encryptedString.substring(0,encryptedString.length()-1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encryptedString;
    }


    public String createTransactionID(){
        return UUID.randomUUID().toString();
    }
}
