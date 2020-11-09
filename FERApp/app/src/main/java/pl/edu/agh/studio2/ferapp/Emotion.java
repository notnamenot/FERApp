package pl.edu.agh.studio2.ferapp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Emotion {
    Happiness(1, R.drawable.emo_happiness),
    Sadness(2, R.drawable.emo_sadness),
    Fear(3, R.drawable.emo_fear ),
    Disgust(4, R.drawable.emo_disgust),
    Anger(5, R.drawable.emo_angry),
    Surprise(6, R.drawable.emo_surprised),
    Neutral(7, R.drawable.emo_neutral);

    private int id;
    private final int drawable;

    Emotion(int id, int drawable) {
        this.id = id;
        this.drawable = drawable;
    }

    public int getId() {
        return this.id;
    }
    public int getDrawable() { return this.drawable; }

    private static final List<Emotion> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Emotion randomEmotion()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
