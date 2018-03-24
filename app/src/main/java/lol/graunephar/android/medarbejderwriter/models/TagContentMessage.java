package lol.graunephar.android.medarbejderwriter.models;

/**
 * Created by daniel on 3/11/18.
 */

public class TagContentMessage {

    String name;
    String fact;
    Integer points;

    public TagContentMessage(String name, String funFact, Integer points) {
        this.name = name;
        this.fact = funFact;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFact() {
        return fact;
    }

    public void setFact(String fact) {
        this.fact = fact;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
