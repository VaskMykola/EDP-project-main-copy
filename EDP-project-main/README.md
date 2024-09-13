# EDP-project: Class Scheduler with GUI client

## Project Overview
This University Project was developed as part of our coursework to demonstrate our knowledge of TCP/UDP protocols and Java programming. It implements a client-server architecture to create and manage class schedules for students. The project has been completed, submitted, and received the maximum grade possible.

## Project Status
- **Grade Received:** A
- **Development Status:** Completed (No Further Development Planned)

## Project History
1. **Initial Development:** The project began as a collaborative effort between team members, focusing on TCP implementation. It was initially hosted at https://github.com/VaskMykola/University-Project-javaFX-tcp.

2. **Repository Transfer:** During development, a team member requested to host the project in their personal repository. The team agreed, and development continued there with contributions from all members. If it will be public again here a link: https://github.com/oleksandr-kardash/EDP-project

3. **Project Completion:** The project was successfully completed and submitted for grading.

4. **Repository Privacy Change:** After grading, the repository owner made the project repository private.

5. **Current Public Version:** This repository is a copy of the final project state just before it was made private. It's shared publicly to showcase our work and maintain access to the completed project.

## Features
- Client GUI for easy interaction
- Server handling multiple client connections
- TCP-based communication between client and server
- Add, remove, and display class schedules
- Automatic rescheduling of classes to early morning slots

## Technical Highlights
- Multi-threaded server to handle concurrent client connections
- Implementation of synchronization for thread-safe operations
- Use of Java Swing for the client GUI
- Fork-Join framework for parallel processing of schedule modifications

## Project Structure
- `ClassSchedulerApplication.java`: Main client application with GUI
- `Server.java`: Multi-threaded server implementation
- `Schedule.java`: Core logic for managing class schedules
- `Class.java`: Represents a single class in the schedule
- `IPAddressUtil.java`: Utility for retrieving server IP address

## How to Run
1. Start the server:
   ```
   java project.server.Server
   ```
2. Launch the client application:
   ```
   java project.fxpart.ClassSchedulerApplication
   ```
3. Enter the server's IP address when prompted (or press Enter for localhost)

## Key Components
- **Client:** JavaFX-based GUI for user interaction
- **Server:** Handles client requests and manages the shared schedule
- **Schedule:** Maintains the class timetable with thread-safe operations
- **Class:** Represents individual classes with start/end times and room information

## Advanced Features
- Early morning rescheduling using Fork-Join framework
- Conflict detection for overlapping classes
- Dynamic GUI updates based on user actions

## Contributors
- [Your Name]
- [Team Member Names]

## Acknowledgements
This project was developed as part of the [Course Name] at [University Name]. We extend our gratitude to our instructors for their guidance and support throughout the development process.

## Disclaimer
This repository represents the final, graded version of our team project. It is no longer under active development. While the original repository is now private, this public version ensures continued access to our completed work. It serves as a testament to our collaborative effort and the skills we developed during this course.