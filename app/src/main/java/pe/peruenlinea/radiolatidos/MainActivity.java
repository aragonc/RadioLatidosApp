package pe.peruenlinea.radiolatidos;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ak.sh.ay.musicwave.MusicWave;


public class MainActivity extends AppCompatActivity {


    private Button startButton, pauseButton;
    private MediaPlayer mediaPlayer;
    private String url = "http://iplinea.com:7230";
    private ProgressDialog pd;
    private boolean initialStage = true;
    private boolean playPause;
    private ImageView imgProgram;
    private TextView message, txtProgram, txtHour, txtSpeaker;
    protected Visualizer mVisualizer;
    private MusicWave musicWave;
    protected BottomNavigationView menuButton;
    public static String FACEBOOK_URL = "https://www.facebook.com/radiolatidosperu";
    public static String FACEBOOK_PAGE_ID = "radiolatidosperu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pd = new ProgressDialog(this);

        mediaPlayer = new MediaPlayer();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        startButton = (Button) findViewById(R.id.btnPlay);
        pauseButton = (Button) findViewById(R.id.btnPause);
        message = (TextView) findViewById(R.id.txtMessage);

        //Items View Programs
        txtProgram = (TextView) findViewById(R.id.txtProgram);
        txtHour = (TextView) findViewById(R.id.txtHour);
        txtSpeaker = (TextView) findViewById(R.id.txtSpeaker);
        menuButton = (BottomNavigationView) findViewById(R.id.navigationView);
        imgProgram = (ImageView) findViewById(R.id.imgProgram);

        //Load Audio Connect Open App

        //Visualizer Audio

        musicWave = (MusicWave) findViewById(R.id.musicWave);
        prepareVisualizer();

        if (initialStage) {

            new Player().execute(url);

        } else {

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                //lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
                pauseButton.setEnabled(true);
            }
        }

        startButton.setEnabled(false);

        getPrograms();

        //Menu Bottom
        menuButton.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_facebook:
                        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                        String facebookUrl = getFacebookPageURL(MainActivity.this);
                        facebookIntent.setData(Uri.parse(facebookUrl));
                        startActivity(facebookIntent);
                        break;
                    case R.id.navigation_web:
                        Uri uri = Uri.parse("http://latidos.pe/");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                    case R.id.navigation_whatsapp:
                        AbrirWhatsApp("954189939");
                        break;
                }
                return true;
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!playPause) {

                    message.setText(R.string.connected);

                    if (initialStage) {

                        new Player().execute(url);

                    } else {

                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    }

                    playPause = true;
                    pauseButton.setEnabled(true);
                    startButton.setEnabled(false);
                }


            }
        });


        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                message.setText(R.string.pause);

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                playPause = false;
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);

            }
        });

    }

    private void AbrirWhatsApp(String telefono)
    {
        Intent _intencion = new Intent("android.intent.action.MAIN");
        _intencion.setComponent(new ComponentName("com.whatsapp","com.whatsapp.Conversation"));
        _intencion.putExtra("jid", PhoneNumberUtils.stripSeparators("51" + telefono)+"@s.whatsapp.net");
        startActivity(_intencion);
    }

    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

    private void prepareVisualizer() {
        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        musicWave.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
        mVisualizer.setEnabled(true);
    }

    void downloadImageProgram(String imageHttpAddress) {
        URL imageUrl = null;
        try {
            imageUrl = new URL(imageHttpAddress);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.connect();
            Bitmap loadImage = BitmapFactory.decodeStream(conn.getInputStream());
            imgProgram.setImageBitmap(loadImage);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error cargando la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    protected void onPause() {
        super.onPause();

        /*if(mediaPlayer != null ){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }*/
    }


    // Classe Player

    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            Boolean prepared = false;

            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        initialStage = true;
                        playPause = false;
                        message.setText(R.string.pause);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });

                mediaPlayer.prepare();
                prepared = true;

            } catch (Exception e) {
                Log.e("MyAudioStreamingApp", e.getMessage());
                prepared = false;
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (pd.isShowing()) {
                pd.cancel();
            }

            mediaPlayer.start();
            initialStage = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd.setMessage("Conectando...");
            pd.show();
        }
    }

    public void getPrograms() {
        String urlApi = "http://blenderperu.org/latidosapi/public/api/v1/programs";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url = null;
        HttpURLConnection conn;

        try {

            url = new URL(urlApi);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            String json = "";

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            json = response.toString();

            JSONArray jsonArray = null;

            jsonArray = new JSONArray(json);

            String message = "";

            for (int i = 0; i < jsonArray.length(); i++) {
                if (i == 1) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    txtProgram.setText(jsonObject.getString("nombre"));
                    String conductor = "con " + jsonObject.getString("conductor");
                    txtSpeaker.setText(conductor);
                    String hora = jsonObject.getString("hora_inicio") + " a " + jsonObject.getString("hora_fin");
                    txtHour.setText(hora);
                    String urlImage = jsonObject.getString("avatar");
                    downloadImageProgram(urlImage);
                    //Log.d("SLIDA",jsonObject.optString("description"));

                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
