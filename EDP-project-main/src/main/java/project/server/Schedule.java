package project.server;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Schedule {

    private final Map<String, List<Class>> classes;
    private final List<String> listOfClassNames;
    private final int MAX_NUMBER_OF_CLASSES = 5;

    public Schedule() {
        classes = new LinkedHashMap<>(); // TreeMap maintains all the elements sorted by the value of keys (dates in this case)
        classes.put("Monday", new ArrayList<>());
        classes.put("Tuesday", new ArrayList<>());
        classes.put("Wednesday", new ArrayList<>());
        classes.put("Thursday", new ArrayList<>());
        classes.put("Friday", new ArrayList<>());
        listOfClassNames = new ArrayList<>();
    }

    // IMPLEMENTATION OF SYNCHRONIZATION AND CONTROL ACCESS TO DATA AND (CODE) METHODS
    public synchronized boolean addClass(String dayOfWeek, Class classToAdd) {

        boolean classWithSuchNameIsAlreadyInSchedule = findClassWithNameSpecified(classToAdd.getName());
        if (!classWithSuchNameIsAlreadyInSchedule) { // check if the class specified is NOT in the schedule already
            if (listOfClassNames.size() == MAX_NUMBER_OF_CLASSES) { // if the number of classes (modules) reached the limit
                throw new IncorrectActionException("You have reached the limit (" + MAX_NUMBER_OF_CLASSES + ") of different classes (modules) you can have in your schedule. You cannot add more at the moment.");
            }
        }

        List<Class> classesForTheDay = classes.get(dayOfWeek); // the list of classes for the specified date

        if (classesForTheDay != null) { // if there is a key for the day specified
            for (Class classToCheckAgainst : classesForTheDay) {
                if (classToAdd.overlapsWith(classToCheckAgainst)) { // for every class for the specified date, check if the class we are trying to add overlaps with any of them
                    throw new IncorrectActionException("The class " + classToAdd + " overlaps with some other class in the schedule.");
                }
            }
            int positionWhereToAddClass = Collections.binarySearch(classesForTheDay, classToAdd) * -1 - 1; // guarantees that classes are sorted based on their starting time
            classesForTheDay.add(positionWhereToAddClass, classToAdd); // add the class
        }
        if (!classWithSuchNameIsAlreadyInSchedule) {
            listOfClassNames.add(classToAdd.getName().toLowerCase());
        }

        return true;
    }

    public synchronized Class removeClass(String dayOfWeek, String nameOfClassToRemove, LocalTime startTimeOfClassToRemove) {

        List<Class> classesForTheDay = classes.get(dayOfWeek); // get the list of classes for the specified date

        for (Class aClass : classesForTheDay) { // check every class if there is a match in name and startTime values. If there is, remove the class and return true
            if (aClass.getName().equalsIgnoreCase(nameOfClassToRemove) && aClass.getStartTime().equals(startTimeOfClassToRemove)) {
                classesForTheDay.remove(aClass);

                String removedClassName = aClass.getName().toLowerCase();
                if (!findClassWithNameSpecified(removedClassName)) {
                    listOfClassNames.remove(removedClassName);
                }

                if (classesForTheDay.isEmpty()) {
                    classes.remove(dayOfWeek);
                }

                return aClass;
            }
        }
//        return null;
        throw new IncorrectActionException("There is no class with specified name and start time on " + dayOfWeek + ".");
    }

    public synchronized boolean findClassWithNameSpecified(String className) {

        for (List<Class> listOfClassesForSomeDay : classes.values()) {
            for (Class aClass : listOfClassesForSomeDay) {
                if (aClass.getName().equalsIgnoreCase(className)) {
                    return true; // if there is at least one class at any day that has the same name as the value of className
                }
            }
        }
        return false; // indicates that there is no class scheduled (for any date) with the name specified
    }

    public synchronized String getAllClassesInfoAsString() {

        if (classes.isEmpty()) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        classes.forEach((date, classesList) -> {
            stringBuilder.append(date).append("\n");
            classesList.forEach(aClass -> stringBuilder.append(aClass).append("\n"));
            stringBuilder.append("\n");
        });

        return stringBuilder.toString().trim();
    }

    public synchronized void performEarlyMorningsOperation() {

        ForkJoinPool.commonPool().invoke(new EarlyMorningsForkJoinTask(classes, 0, classes.size()));
    }
}

// IMPLEMENTATION OF FORK-JOIN RULE USING DIVIDE AND CONQUER
class EarlyMorningsForkJoinTask extends RecursiveTask<Void> {

    private final Map<String, List<Class>> classes;
    private final List<String> daysOfWeek;
    private final int start;
    private final int end;

    public EarlyMorningsForkJoinTask(Map<String, List<Class>> classes, int start, int end) {
        this.classes = classes;
        daysOfWeek = new ArrayList<>(classes.keySet());
        this.start = start;
        this.end = end;
    }

    @Override
    protected Void compute() {

        if (end - start < 3) {
            shiftAllClassesToMorningTime();
        } else {
            int middle = (start + end) / 2;
            EarlyMorningsForkJoinTask task1 = new EarlyMorningsForkJoinTask(classes, start, middle);
            EarlyMorningsForkJoinTask task2 = new EarlyMorningsForkJoinTask(classes, middle, end);
            invokeAll(task1, task2);
        }
        return null;
    }

    private void shiftAllClassesToMorningTime() {

        for (int i = start; i < end; i++) {

            String dayOfWeek = daysOfWeek.get(i);
            List<Class> classesForTheDay = classes.get(dayOfWeek);
            LocalTime startTime = LocalTime.of(9, 0);
            for (Class aClass : classesForTheDay) {
                Duration classDuration = aClass.getDuration();
                aClass.setStartTime(startTime);
                aClass.setFinishTime(startTime.plus(classDuration));

                startTime = aClass.getFinishTime();
            }
        }
    }
}