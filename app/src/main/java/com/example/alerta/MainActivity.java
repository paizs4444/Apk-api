package com.example.alerta;
//librerias
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.LEFT;
import static android.view.View.VISIBLE;

//inicio de activity
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    //variables para mostrar datos de google
    private TextView NombreText;
    private ListView lv1;
    TextView  direccion;
    private String Nombre;
    private String Correo;
    private String Direccion = "";
    private String latitud = "";
    private String longitud = "";
    //variable para obtener datos de google
    private GoogleApiClient googleApiClient;
    //Variable String
    public String Area = "";
    public String telefono = "";
    public String SetArea = "Chiquimula";
//    variables especiales
String Seleccion= "";
    ImageButton but;
    //array para listview
    ArrayList<String> accidentes = new ArrayList<String>();

//crear activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        cinfiguracion de variables
        but  = (ImageButton) findViewById(R.id.imageButton);
        lv1 =(ListView)findViewById(R.id.ListView1);
        NombreText = (TextView) findViewById(R.id.NombreText);
        direccion = (TextView) findViewById(R.id.DirView);
        lv1.setVisibility(View.INVISIBLE);
        but.setVisibility(View.VISIBLE);
        NombreText.setVisibility(View.VISIBLE);
        direccion.setVisibility(View.VISIBLE);
        //obtener datos de accidentes
        getData();
        //configurar los arreglos para listview
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.listview_item, accidentes);
        lv1.setAdapter(adapter);

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int posicion, long id) {

                Seleccion = new String(lv1.getItemAtPosition(posicion).toString());
                but.setVisibility(View.INVISIBLE);
                NombreText.setVisibility(View.INVISIBLE);
                direccion.setVisibility(View.INVISIBLE);
//                sal.setText(lv1.getItemAtPosition(posicion).toString());
//                Seleccion = sal.toString();
                //enviar datos de la alerta
                enviarAlerta();
            }
        });

        //obtener telefono guardado

        String archivos [] = fileList();

        if (ArchivoExiste(archivos, "Telefono.txt")){
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("Telefono.txt"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                String TelefonoCompleto = "";
                    TelefonoCompleto = linea;
                    br.close();
                    archivo.close();
                    telefono =(TelefonoCompleto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//enlazar variables con metodo de googleapiclient

        //logeo silencioso
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //gps configuracion

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }

    }

    private boolean ArchivoExiste(String archivos [], String NombreArchivo){
        for (int i = 0; i < archivos.length; i++)
            if (NombreArchivo.equals(archivos[i]))
                return true;
        return false;
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
        Toast Toast2 =
                Toast.makeText(getApplicationContext(), "Localización agregada", Toast.LENGTH_SHORT);
        Toast2.setGravity(CENTER , 0, 0);
        Toast2.show();
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }
    //gps obtener la direccion
    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    direccion.setText(DirCalle.getAdminArea());
                    if (DirCalle.getAdminArea() !="Chiquimula")
                    {
                        Area = DirCalle.getAdminArea();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;
        public MainActivity getMainActivity() {
            return mainActivity;
        }
        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }
        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.getLatitude();
            loc.getLongitude();
            String sLatitud = String.valueOf(loc.getLatitude());
            String sLongitud = String.valueOf(loc.getLongitude());
            latitud = sLatitud;
            longitud = sLongitud;
            this.mainActivity.setLocation(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            Toast Toast2 =
                    Toast.makeText(getApplicationContext(), R.string.gps_desactivado, Toast.LENGTH_SHORT);
            Toast2.setGravity(CENTER , 0, 0);
            Toast2.show();
        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            Toast Toast2 =
                    Toast.makeText(getApplicationContext(), R.string.gps_activado, Toast.LENGTH_SHORT);
            Toast2.setGravity(CENTER , 0, 0);
            Toast2.show();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "Localizacion Obtenida");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "Localizacion Fuera de sevicio");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "Localizacion no Obtenida");
                    break;
            }
        }
    }

//inicio de activity creada
    @Override
    protected void onStart() {
        super.onStart();
        //verificar logeo
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()){
            GoogleSignInResult result = opr.get();
            handleSingInResult(result);

        }
        else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSingInResult(googleSignInResult);
                }
            });

        }

    }
    //mostrar datos
    private void handleSingInResult(GoogleSignInResult result) {
    if (result.isSuccess()){
       GoogleSignInAccount account = result.getSignInAccount();
        NombreText.setText(account.getDisplayName());
        Nombre = (account.getDisplayName());
        Correo = (account.getEmail());
    }
    //regresar a pantalla de logeo
    else {
        goLogInScreen();
    }
    }
    //metodo para ir a pantalla delogeo
    private void goLogInScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    //deslogear
    public void logOut(View view) {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()){
                    goLogInScreen();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.not_log_out, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

//    funcion de imagebutton
    public void PushBottonAlerta(View view) throws IOException {
        //verifica el area
        if (Area.equals(SetArea)) {
//            getData();
            but.setVisibility(View.INVISIBLE);
            NombreText.setVisibility(View.INVISIBLE);
            direccion.setVisibility(View.INVISIBLE);
            lv1.setVisibility(View.VISIBLE);



//            Toast Toast2 =
//                    Toast.makeText(getApplicationContext(), "Area permitida", Toast.LENGTH_LONG);
//            Toast2.setGravity(CENTER, 0, 0);
//            Toast2.show();
        }//si no obtiene datos de gps
        else if (Area.isEmpty()) {
            Toast Toast2 =
                    Toast.makeText(getApplicationContext(), "No se obtuvo la localizacion\nintente nuevamente", Toast.LENGTH_LONG);
            Toast2.setGravity(CENTER, 0, 0);
            Toast2.show();
        } //si no esta en el area permitida
        else {
            Toast Toast2 =
                    Toast.makeText(getApplicationContext(), "Area no permitida \n Solo se permite del area de\nCHIQUIMULA ", Toast.LENGTH_LONG);
            Toast2.setGravity(CENTER, 0, 0);
            Toast2.show();
        }
    }

//    envio de datos accidente
public void enviarAlerta(){
    @SuppressLint("ResourceType") AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
    dialogo1.setTitle("ENVIAR ALERTA");
    dialogo1.setMessage("ACCIDENTE "+Seleccion.toUpperCase());
    dialogo1.setCancelable(false);
    dialogo1.setPositiveButton("ENVIAR", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogo1, int id) {

            lv1.setVisibility(View.INVISIBLE);
            but.setVisibility(View.VISIBLE);
            NombreText.setVisibility(View.VISIBLE);
            direccion.setVisibility(View.VISIBLE);
            setData();
            Toast Toast2 =
                    Toast.makeText(getApplicationContext(), "DATOS ENVIADOS", Toast.LENGTH_LONG);
            Toast2.setGravity(CENTER , 0, 0);
            Toast2.show();

        }
    });
    dialogo1.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogo1, int id) {

            lv1.setVisibility(View.INVISIBLE);
            but.setVisibility(View.VISIBLE);
            NombreText.setVisibility(View.VISIBLE);
            direccion.setVisibility(View.VISIBLE);

            Toast Toast2 =
                    Toast.makeText(getApplicationContext(), "ENVIO CANCELADO", Toast.LENGTH_LONG);
            Toast2.setGravity(CENTER , 0, 0);
            Toast2.show();

//            finish();
        }
    });
    dialogo1.show();

}

//enviar datos de alerta

    public void setData(){
        String sql = "http://paizsrest.pythonanywhere.com/alertas/";

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
            jsonParam.put("telefonoPer", telefono);
            jsonParam.put("nombrePer", Nombre);
            jsonParam.put("correoPer", Correo);
            jsonParam.put("tipoAccidente", Seleccion);
            jsonParam.put("latitud", latitud);
            jsonParam.put("longitud", longitud);
            jsonParam.put("ubicacion", Area);
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

//obtener listado de accidentes
public void getData(){
    String sql = "http://paizsrest.pythonanywhere.com/accidente/";

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    URL url = null;
    HttpURLConnection conn;

    try {
        url = new URL(sql);
        conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        conn.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String inputLine;

        StringBuffer response = new StringBuffer();

        String json = "";

        while((inputLine = in.readLine()) != null){
            response.append(inputLine);
        }

        json = response.toString();

        JSONArray jsonArr = null;

        jsonArr = new JSONArray(json);
        String mensaje = "";
        for(int i = 0;i<jsonArr.length();i++){
            JSONObject jsonObject = jsonArr.getJSONObject(i);
            mensaje = jsonObject.optString("Accidente");
//                accidentes[i] = mensaje.toString();
            accidentes.add(mensaje);
        }
//            sal.setText(mensaje);
    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (JSONException e) {
        e.printStackTrace();
    }
}


  //mostrar errores
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
