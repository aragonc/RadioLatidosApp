package pe.peruenlinea.radiolatidos;

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.media.AudioManager;
        import android.media.MediaPlayer;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import android.util.Log;


public class MainActivity extends AppCompatActivity {


    private Button startButton, pauseButton, lowVolumen, upVolumen;
    private MediaPlayer mediaPlayer;
    private float volumen;
    private String url = "http://iplinea.com:7230";
    private ProgressDialog pd;
    private boolean initialStage= true;
    private boolean playPause;
    private TextView message;
    private ProgressBar progressVolumen;
    private int progressStatus = 0;
    private int currentVolume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pd = new ProgressDialog(this);


        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        progressVolumen = (ProgressBar) findViewById(R.id.progressBar);
        progressVolumen.setMax(100);
        progressVolumen.setScaleY(3f);
        progressVolumen.setProgress(currentVolume);

        mediaPlayer = new MediaPlayer();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);


        startButton = (Button) findViewById(R.id.btnPlay);
        pauseButton = (Button) findViewById(R.id.btnPause);
        lowVolumen = (Button) findViewById(R.id.btnLow);
        upVolumen = (Button) findViewById(R.id.btnUp);
        message = (TextView) findViewById(R.id.txtMessage);



        lowVolumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumen = (float) (volumen - 0.1);
                mediaPlayer.setVolume(volumen,volumen);
                progressStatus -= 10;
                progressVolumen.setProgress(progressStatus);
            }
        });

        upVolumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumen = (float) (volumen + 0.1);
                mediaPlayer.setVolume(volumen,volumen);
                progressStatus += 10;
                progressVolumen.setProgress(progressStatus);
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!playPause){

                    message.setText(R.string.connected);

                    if(initialStage){

                        new Player().execute(url);
                    } else {

                        if(!mediaPlayer.isPlaying()){
                            volumen = (float) currentVolume;
                            mediaPlayer.setVolume(volumen,volumen);
                            mediaPlayer.start();
                        }
                    }

                    playPause = true;
                }


            }
        });


        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.setText(R.string.pause);
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                playPause = false;

            }
        });




    }


    protected void onPause(){
        super.onPause();

        if(mediaPlayer != null ){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
