package pe.peruenlinea.radiolatidos;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import ak.sh.ay.musicwave.MusicWave;

import static android.Manifest.permission.RECORD_AUDIO;


public class MainActivity extends AppCompatActivity {


    private Button startButton, pauseButton, btnClose;
    private MediaPlayer mediaPlayer;
    private String url = "http://iplinea.com:9944";
    private ProgressDialog pd;
    private boolean initialStage = true;
    private boolean playPause;
    //private ImageView imgProgram;
    private TextView message; // txtProgram, txtHour, txtSpeaker;
    protected Visualizer mVisualizer;
    private MusicWave musicWave;
    protected BottomNavigationView menuButton;
    public static String FACEBOOK_URL = "https://www.facebook.com/radiolatidosHuaral";
    public static String FACEBOOK_PAGE_ID = "radiolatidosHuaral";

    private static final int ACCESS_PERMISSION_RECORD_AUDIO = 0;
    private static final String CHANNEL_ID = "NOTIFICACION";
    private static final int NOTIFICATION_ID = 0;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pd = new ProgressDialog(this);

        mediaPlayer = new MediaPlayer();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        startButton = (Button) findViewById(R.id.btnPlay);
        pauseButton = (Button) findViewById(R.id.btnPause);
        btnClose = (Button) findViewById(R.id.btnClose);
        message = (TextView) findViewById(R.id.txtMessage);
        menuButton = (BottomNavigationView) findViewById(R.id.navigationView);


        //Load Audio Connect Open App

        //Visualizer Audio

        musicWave = (MusicWave) findViewById(R.id.musicWave);


        if (initialStage) {
            new Player().execute(url);
        } else {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                pauseButton.setEnabled(true);

            }
        }



        //Permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this, RECORD_AUDIO);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                prepareVisualizer();
            } else {
                final String[] permissions = new String[]{RECORD_AUDIO};
                requestPermissions(permissions,ACCESS_PERMISSION_RECORD_AUDIO);
            }
        } else {
            prepareVisualizer();
        }


        startButton.setEnabled(false);

        createNotificationChannelApp();
        notificactionCompatApp();

        //Menu Bottom
        menuButton.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_facebook:
                        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                        String facebookUrl = getFacebookPageURL(MainActivity.this);
                        facebookIntent.setData(Uri.parse(facebookUrl));
                        if (getPackageManager().resolveActivity(facebookIntent, 0) != null) {
                            startActivity(facebookIntent);
                        }

                        break;
                    case R.id.navigation_web:
                        Uri uri = Uri.parse("http://latidos.pe/");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        if (getPackageManager().resolveActivity(intent, 0) != null) {
                            startActivity(intent);
                        }

                        break;
                    case R.id.navigation_whatsapp:
                        openWhatsApp("966407223");
                        break;
                }
                return true;
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaying();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ACCESS_PERMISSION_RECORD_AUDIO){
            if(shouldShowRequestPermissionRationale(RECORD_AUDIO)){
                AlertDialog.Builder  builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_permissions);
                builder.setMessage(R.string.content_permissions);
                builder.setPositiveButton(R.string.to_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String[] permissions = new String[]{RECORD_AUDIO};
                        requestPermissions(permissions,ACCESS_PERMISSION_RECORD_AUDIO);
                    }
                });
                builder.show();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannelApp(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Notificacion";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    public void notificactionCompatApp(){

        //Intent intent = new Intent(this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon_radio);
        builder.setContentText("Reproduciendo radio en segundo plano");
        builder.setContentTitle("Radio Latidos");
        builder.setColor(Color.argb(1,250,165,225));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setLights(Color.YELLOW, 1000,1000);
        //builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());

    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        MainActivity.this.finish();
    }

    private void openWhatsApp(String phone)
    {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage("com.whatsapp");
        intent.setData(Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s", "51" + phone)));
        if (getPackageManager().resolveActivity(intent, 0) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.install_wps, Toast.LENGTH_SHORT).show();
        }

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


    protected void onPause() {
        super.onPause();

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

}
