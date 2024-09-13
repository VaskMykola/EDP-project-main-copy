package project.server;

import java.time.Duration;
import java.time.LocalTime;

public class Class implements Comparable<Class> {

    private LocalTime startTime;
    private LocalTime finishTime;
    private String name;
    private String room;

    public Class(LocalTime startTime, LocalTime finishTime, String name, String room) {
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.name = name;
        this.room = room;
    }

    public boolean overlapsWith(Class other) {
        return !finishTime.isBefore(other.startTime) && !startTime.isAfter(other.finishTime);
    }

    @Override
    public int compareTo(Class other) {
        return startTime.compareTo(other.startTime);
    }

    @Override
    public String toString() {
        return "%s: from %s to %s, in %s".formatted(name, startTime, finishTime, room);
    }

    public Duration getDuration() {
        return Duration.between(startTime, finishTime);
    }

    public String getName() {
        return name;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getFinishTime() {
        return finishTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(LocalTime finishTime) {
        this.finishTime = finishTime;
    }
}
