package group.omarks.xeuxenroll;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Student_info extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag myTag;
    private Button retourner;
    private String qrCode;
    boolean writeMode;
    public static ImageView imageView;
    ProgressDialog pd;
    TextView matricule;
    TextView nom;
    TextView date_naiss;
    TextView filiere;
    TextView niveau;
    TextView carte;
    String code;
    String mLocal;
    String mInternet;
    String mType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);

        imageView = findViewById(R.id.imageView);
        matricule = findViewById(R.id.textMtle);
        nom = findViewById(R.id.textNom);
        date_naiss = findViewById(R.id.textDateNaissance);
        filiere = findViewById(R.id.textFiliere);
        niveau = findViewById(R.id.textNiveau);
        carte = findViewById(R.id.textCarte);



        // Recuperer le data envoyé
        Intent intent=getIntent();
        code= intent.getStringExtra("code");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }

        retourner = findViewById(R.id.retour);
        retourner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Student_info.this,MainActivity.class));
            }
        });


        readFromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
//
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mLocal = prefs.getString("adress_local", "");
        mInternet = prefs.getString("adress_internet", "");
        mType  = prefs.getString("choix_save", "");
        Log.i("internet",mInternet+"/api/v1.0/check_etudiant/"+code+"   "+mType);

        switch (mType){
            case "1":
                new JsonGetInfoTask().execute(mLocal+"/api/v1.0/check_etudiant/"+code);
                break;
            case "2":
                new JsonGetInfoTask().execute(mInternet+"/api/v1.0/check_etudiant/"+code);
                break;

        }
    }

    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i("TAG",StringUtil.toHexStr(myTag.getId()));
            showAlert();
        }
    }

    public static String readTag(Tag tag, Intent intent) {
        if (tag != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ReadTime:").append(TimeUtil.curTime(System.currentTimeMillis()))
                    .append("\n")
                    .append("ID:").append(StringUtil.toHexStr(tag.getId())).append("\n");
            return StringUtil.toHexStr(tag.getId()).toString();
        }
        return null;
    }

    private static void LoadImage () throws IOException {
        URL url = new URL("image url");
        Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        imageView.setImageBitmap(bmp);
    }


    private void sendDialogDataToActivity(String data) {
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
    }


    public void showAlert(){
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                this);

        // Setting Dialog Title
                alertDialog2.setTitle("Confirmation Operation...");

        // Setting Dialog Message
                alertDialog2.setMessage("Voulez vous lier cette carte à cet(te) Etudiant(e) ?");

        // Setting Icon to Dialog
        //        alertDialog2.setIcon(R.drawable.delete);

        // Setting Positive "Yes" Btn
                alertDialog2.setPositiveButton("OUI",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                switch (mType) {
                                    case "1":
                                        new JsonUpdateInfoTask().execute(mLocal + "/api/v1.0/enroll_etudiant/" + code + "/" + StringUtil.toHexStr(myTag.getId()));
                                        break;
                                    case "2":
                                        new JsonUpdateInfoTask().execute(mInternet + "/api/v1.0/enroll_etudiant/" + code + "/" + StringUtil.toHexStr(myTag.getId()));
                                        break;
                                }
                                        Toast.makeText(getApplicationContext(),
                                        "Operation en cours", Toast.LENGTH_SHORT)
                                        .show();

                            }
                        });
        // Setting Negative "NO" Btn
                alertDialog2.setNegativeButton("NON",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                Toast.makeText(getApplicationContext(),
                                        "Operation annulée", Toast.LENGTH_SHORT)
                                        .show();
                                dialog.cancel();
                            }
                });

        // Showing Alert Dialog
                alertDialog2.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readFromIntent(intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i("TAG",myTag.toString());
        }
        Log.i("TAG",myTag.toString());
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }



    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }




    private class JsonGetInfoTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(Student_info.this);
            pd.setMessage("Attendez SVP");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }
//                pd.dismiss();
                return buffer.toString();

            } catch (MalformedURLException e) {
//                pd.dismiss();
                e.printStackTrace();
            } catch (IOException e) {
//                pd.dismiss();
                e.printStackTrace();
            } finally {
//                pd.dismiss();
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
//                        pd.dismiss();
                        reader.close();
                    }
                } catch (IOException e) {
//                    pd.dismiss();
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            try {
                if(result!=null) {
                    Log.i("infos", result.toString());
                    JSONObject jsonObject = new JSONObject(result);
                    matricule.setText(jsonObject.getString("matricule"));
                    nom.setText(jsonObject.getString("nom") + " " + jsonObject.getString("prenoms"));
                    date_naiss.setText(jsonObject.getString("date_naissance"));
                    filiere.setText(jsonObject.getString("filiere"));
                    niveau.setText(jsonObject.getString("niveau"));
                    carte.setText(jsonObject.getString("card_id"));
//                    pd.dismiss();
                }else{
                    Toast.makeText(getBaseContext(),"Une erreur est survenue ",Toast.LENGTH_SHORT).show();

//                        pd.dismiss();

                }
            } catch (JSONException e) {

//                    pd.dismiss();

                e.printStackTrace();
            }
        }
    }

    private class JsonUpdateInfoTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(Student_info.this);
            pd.setMessage("Attendez SVP !");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }
                pd.dismiss();
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                    pd.dismiss();

            } catch (IOException e) {
                e.printStackTrace();
                    pd.dismiss();

            } finally {
                pd.dismiss();

                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                        pd.dismiss();

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                Toast.makeText(getBaseContext(),jsonObject.get("statut").toString(),Toast.LENGTH_SHORT).show();
                jsonObject = jsonObject.getJSONObject("etudiant");
                matricule.setText(jsonObject.getString("matricule"));
                nom.setText(jsonObject.getString("nom") + " " + jsonObject.getString("prenoms"));
                date_naiss.setText(jsonObject.getString("date_naissance"));
                filiere.setText(jsonObject.getString("filiere"));
                niveau.setText(jsonObject.getString("niveau"));
                carte.setText(jsonObject.getString("card_id"));
                pd.dismiss();
                if(jsonObject.get("statut").toString().equals("succes")){
                    startActivity(new Intent(Student_info.this,MainActivity.class));
                }
            } catch (JSONException e) {
                pd.dismiss();
                e.printStackTrace();
                pd.dismiss();

            }

        }
    }
}



class TimeUtil {
    public static String curTime(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH).format(new Date(time));
    }
}

class StringUtil {
    /**
     * 将字节数组转化成16进制字符串
     *Convertir le tableau d'octets en chaîne hexadécimale
     * @param bytes
     * @return
     */
    public static String toHexStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder iStringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String vStr = Integer.toHexString(v);
            if (vStr.length() < 2) {
                iStringBuilder.append(0);
            }
            iStringBuilder.append(vStr);
        }
        return iStringBuilder.toString().toUpperCase();
    }

    /**
     * 将16位的short转换成byte数组
     * @param s
     * @return
     */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }






}
