package project.server;

public class IncorrectActionException extends RuntimeException {

    public IncorrectActionException() {
        super("It seems like you have not filled in all text fields needed. Please provide all information required to perform the action.");
    }

    public IncorrectActionException(String message) {
        super(message);
    }
}
