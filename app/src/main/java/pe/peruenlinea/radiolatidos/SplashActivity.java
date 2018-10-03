package pe.peruenlinea.radiolatidos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.w3c.dom.Text;


public class SplashActivity extends Activity {

    private final int DURATION_SPLASH = 3500; //3 Segundos
    ImageView logo;
    TextView txtUrl, txtCompany, txtCopyright;
    Animation animaLogo, fadeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //llamamos a la plantilla

        setContentView(R.layout.splash);
        logo = (ImageView) findViewById(R.id.imgLogo);
        txtUrl = (TextView) findViewById(R.id.txtWeb);
        txtCompany = (TextView) findViewById(R.id.txtCompany);
        txtCopyright = (TextView) findViewById(R.id.txtCopyright);

        animaLogo = AnimationUtils.loadAnimation(this,R.anim.zoom_back_in);
        fadeText = AnimationUtils.loadAnimation(this,R.anim.fade_in);


        logo.setAnimation(animaLogo);
        txtUrl.setAnimation(fadeText);
        txtCompany.setAnimation(fadeText);
        txtCopyright.setAnimation(fadeText);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Cuando pase los 3 segundos
                Intent intent_a = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent_a);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, DURATION_SPLASH);
    }
}
