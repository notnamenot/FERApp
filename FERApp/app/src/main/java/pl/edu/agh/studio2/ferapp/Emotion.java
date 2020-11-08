package pl.edu.agh.studio2.ferapp;

public enum Emotion {
    happiness(1),
    sadness(2),
    fear(3),
    disgust(4),
    anger(5),
    surprise(6),
    neutral(7);

    private int id;

    Emotion(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
