package pl.edu.agh.studio2.ferapp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Emotion {
    Happiness(1),
    Sadness(2),
    Fear(3),
    Disgust(4),
    Anger(5),
    Surprise(6),
    Neutral(7);

    private int id;

    Emotion(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    private static final List<Emotion> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Emotion randomEmotion()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
