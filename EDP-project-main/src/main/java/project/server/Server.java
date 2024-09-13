package project.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;

public class Server {

    private final ServerSocket serverSocket;
    private final int PORT = 1234;
    private final Schedule schedule;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        schedule = new Schedule(); // one schedule is shared among all clients
    }

    public void launch() {

        while (true) {

            try {
                Socket clientSocket = serverSocket.accept();
                // IMPLEMENTATION OF MULTI-THREADING FOR DEALING WITH MULTIPLE CLIENTS
                new Thread(new ClientManager(clientSocket, schedule)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        System.out.println("Server is running.");
        System.out.println("Server is ready to accept client requests.");
        System.out.println("Server is listening on port " + server.PORT + "...");
        System.out.println("To connect to the server, use the following IPv4 address: " + IPAddressUtil.getIPv4Address());
        server.launch();

    }
}

class ClientManager implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Schedule schedule;

    public ClientManager(Socket clientSocket, Schedule schedule) {

        this.clientSocket = clientSocket;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        this.schedule = schedule;
    }

    @Override
    public void run() {

        processMessageFromClientAndRespond();
        closeResources();
    }


    private void processMessageFromClientAndRespond() {

        String messageFromClient = "";
        String messageToSendToClient = "";

        while (!messageFromClient.equals("DISCONNECT")) {

            try {

                messageFromClient = in.readLine();
                System.out.println("Message from Client: " + messageFromClient);

                if (messageFromClient.equals("Early Mornings")) {
                    schedule.performEarlyMorningsOperation();
                    messageToSendToClient = "The 'Early Mornings' request has been processed. Classes are shifted to the morning time.";
                    continue;
                }

                String[] arguments = messageFromClient.split(",");
                if (containsNullAsStringValue(arguments) || containsBlankString(arguments)) { // check if there is any null value provided
                    throw new IncorrectActionException("Please provide all the required information.");
                }
                String actionToPerform = arguments[0];
                switch (actionToPerform) {
                    case "Display Schedule" -> {
                        String scheduleAsString = schedule.getAllClassesInfoAsString();
                        if (scheduleAsString == null) {
                            messageToSendToClient = "The schedule does not contain any classes so far.";
                        } else {
                            messageToSendToClient = scheduleAsString;
                        }
                        System.out.println("The client's request to display the schedule has been processed.");
                    }
                    case "Add New Class" -> {
                        if (arguments.length != 8) {
                            throw new IncorrectActionException();
                        }
                        String dayOfClass = arguments[1];

                        Class classToAdd = createAClassUsingDataProvidedByClient(arguments);

                        if (classToAdd == null) {
                            throw new IncorrectActionException("Start time of a class cannot be after finish time.");
                        }

                        if (schedule.addClass(dayOfClass, classToAdd)) {
                            messageToSendToClient = "A new class " + classToAdd + " on " + dayOfClass + " was successfully added to the schedule.";
                            System.out.println(messageToSendToClient);
                        }
                    }
                    case "Remove Class" -> {
                        if (arguments.length != 5) {
                            throw new IncorrectActionException();
                        }
                        String dayOfClassToRemove = arguments[1];
                        int startHours = Integer.parseInt(arguments[2]);
                        int startMinutes = Integer.parseInt(arguments[3]);
                        String className = arguments[4];
                        Class removedClass = schedule.removeClass(dayOfClassToRemove, className, LocalTime.of(startHours, startMinutes));
                        if (removedClass != null) {
                            messageToSendToClient = "The class " + removedClass + " was removed from the schedule.";
                            System.out.println(messageToSendToClient);
                        }
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IncorrectActionException iae) {
                messageToSendToClient = "ERROR MESSAGE: " + iae.getMessage();
            } finally {
                out.println(messageToSendToClient.length());
                out.println(messageToSendToClient);
            }

        }
    }

    private static Class createAClassUsingDataProvidedByClient(String[] arguments) {

        int startHours = Integer.parseInt(arguments[2]);
        int startMinutes = Integer.parseInt(arguments[3]);
        int finishHours = Integer.parseInt(arguments[4]);
        int finishMinutes = Integer.parseInt(arguments[5]);
        String className = arguments[6];
        String roomNumber = arguments[7];

        LocalTime providedStartTime = LocalTime.of(startHours, startMinutes);
        LocalTime providedFinishTime = LocalTime.of(finishHours, finishMinutes);

        if (providedStartTime.isAfter(providedFinishTime)) {
            return null;
        }
        return new Class(providedStartTime, providedFinishTime, className, roomNumber);
    }

    private static boolean containsNullAsStringValue(String[] array) {
        for (String element : array) {
            if (element.equals("null")) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsBlankString(String[] array) {
        for (String element : array) {
            if (element.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private void closeResources() {

        try {
            clientSocket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}