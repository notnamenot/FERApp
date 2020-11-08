package pl.edu.agh.studio2.ferapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView txt_random_emotion;
    Emotion random_emotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random_emotion = Emotion.randomEmotion();

        txt_random_emotion = (TextView) findViewById(R.id.txt_random_emo);
        txt_random_emotion.setText(random_emotion.name());  //String.valueOf(random_emotion))
    }
}
