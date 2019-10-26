package com.example.alerta;

//librerias
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.os.StrictMode;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.view.Gravity.CENTER;

//inicio de activity
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

//    variables para google api
private GoogleApiClient googleApiClient;

private SignInButton signInButtonG;

    private String Nombre;
    private String Correo;

    private String Telefono = "";
//codigo de logeo
public static  final  int SIGN_IN_CODE = 999;

//crear activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButtonG = (SignInButton) findViewById(R.id.signInButton);
        signInButtonG.setSize(SignInButton.SIZE_WIDE);
        signInButtonG.setColorScheme(SignInButton.COLOR_DARK);
        signInButtonG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_CODE);
            }
        });
    }

//iniciar activity creada
    @Override
    protected void onStart() {
        super.onStart();
//        dialogo de terminos
        @SuppressLint("ResourceType") AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this, android.R.color.background_dark);
        dialogo1.setTitle("Importante");
        dialogo1.setMessage("¿ Acepta compartir informacion personal como:\nCorreo\nNombre\nNumero telefonico\nUbicacion ?\n\nAVISO IMPORTANTE:\nEluso de esta aplicacion es unicamente en caso de emergencia.");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                aceptarPoliticas();
            }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                cancelarPoliticas();
            }
        });
        dialogo1.show();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()){
            GoogleSignInResult result = opr.get();
            handleSingInResult2(result);

        }
        else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSingInResult2(googleSignInResult);
                }
            });

        }

    }
//funcion para ir a activity main
    private void handleSingInResult2(GoogleSignInResult result) {
        if (result.isSuccess()){
            goMainScreen();
        }

    }
//si la coneccion falla
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
//verificar el logeo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSingInResult(result);
        }
    }
//obtener datos de google
    private void handleSingInResult(GoogleSignInResult result) {

            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                Nombre = (account.getDisplayName()).toString();
                Correo = (account.getEmail()).toString();

                setTelefono();
            }
            else {
                Toast.makeText(this, R.string.not_log_in, Toast.LENGTH_SHORT).show();
            }
    }
//solicitar numero telefonico
    private void setTelefono() {
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Telefono");
            builder.setMessage("Ingrese su numero de telefono");

// Set up the input
            final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Telefono = input.getText().toString();
                    if (!Telefono.isEmpty()) {
                        try {
                        OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput("Telefono.txt", Activity.MODE_PRIVATE));
                        archivo.write(Telefono);
                        archivo.flush();
                        archivo.close();
                        goMainScreen();
                    }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        setTelefono();
                    }

                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {

                        }
                    });
                    dialog.cancel();
                }
            });

            builder.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//ir a main al obtener daots
    private void goMainScreen() {
        //crear usuario
        setData();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
//funcion al aceptar politicas
    public void aceptarPoliticas() {
        Toast Toast2 =
                Toast.makeText(getApplicationContext(), "Bienveniods a la aplicacion de\nBomberos Voluntarios", Toast.LENGTH_LONG);
        Toast2.setGravity(CENTER , 0, 0);
        Toast2.show();
    }
//funcion al cancelas politicas
    public void cancelarPoliticas() {
        finish();
    }

//    funcion para envio de datos de creacion de usuario
    public void setData(){
        String sql = "http://paizsrest.pythonanywhere.com/personas/";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        HttpURLConnection conn=null;
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(sql);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type","application/json");

            conn.setRequestProperty("Host", "paizsrest.pythonanywhere.com");
            conn.connect();

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("telefonoPer", Telefono);
            jsonParam.put("nombrePer", Nombre);
            jsonParam.put("correoPer", Correo);
            OutputStreamWriter out = new   OutputStreamWriter(conn.getOutputStream());
            out.write(jsonParam.toString());
            out.flush ();
            out.close();

            int HttpResult =conn.getResponseCode();
            if(HttpResult ==HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(),"utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

//                System.out.println(""+sb.toString());
//                Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG);

            }else{
//                System.out.println(conn.getResponseMessage());
//                Toast.makeText(getApplicationContext(), conn.getResponseMessage(), Toast.LENGTH_LONG);
            }
        } catch (MalformedURLException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            if(conn!=null)
                conn.disconnect();
        }
    }

}
