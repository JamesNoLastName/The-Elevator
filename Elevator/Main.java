import java.util.*;

/**
 * Simple Elevator Simulation using SCAN algorithm
 * -----------------------------------------------
 * Features:
 *  - Custom amount of floors numbered starting from 1
 *  - Elevator moves up and down (SCAN algorithm https://www.geeksforgeeks.org/operating-systems/c-scan-disk-scheduling-algorithm/)
 *  - Shows passengers inside elevator
 *  - Shows passengers waiting on each floor
 *  - Manual and auto step modes
 *  - Randomly generate passenger requests
 * 
 * Assumptions:
 *  - There are n floors, where n should be a non-negative and non-zero integer (Ideally should have more than 1 floor). Floors start at 01.
 *  - The algorithm for the elevator should be well-known and intuitive, so I chose the SCAN algorithm (Similar to a bus with stop requests)
 *  - There is only one elevator in operation for the building.
 *  - A single "step" is a measureable change of floors (For example moving to another floor to allow passengers on)
 *  - Passengers have a start and destination in mind when entering the elevator, and these floor values are not the same.
 *  - This is a "vacuum" simulation, where passengers do not trickle in by themselves at random periods and are added only through add/random
 *  - The elevator has no space limit, so an infinite-approaching number of passengers can board the elevator at any time.
 *  - Commands such as add, random, step, auto, status, and quit were assumed to give the user control over simulating the elevator.
 *  - If the user inputs something not from the commands, the system informs them of the available commands.
 */
public class Main {

    /**
     * Represents a person requesting the elevator
     */
    static class Person {
        int start;          // Floor where person is waiting
        int destination;    // Floor where person wants to go
        boolean inElevator = false;  // True if inside the elevator

        Person(int start, int destination) {
            this.start = start;
            this.destination = destination;
        }

        // Display format for passengers: "3 to 9"
        @Override
        public String toString() {
            return start + " to " + destination;
        }
    }

    /**
     * Elevator object that moves between floors and carries passengers
     */
    static class Elevator {
        int currentFloor = 1;      // Elevator starts at floor 1
        int floors;                // Total number of floors
        boolean goingUp = true;    // Direction flag: true = up, false = down
        List<Person> waiting = new ArrayList<>();  // People waiting on floors
        List<Person> inside = new ArrayList<>();   // People currently inside elevator

        // Constructor: initialize with total number of floors
        Elevator(int floors) {
            this.floors = floors;
        }

        // Add person waiting for elevator
        void addPerson(int start, int destination) {
            waiting.add(new Person(start, destination));
        }

        /**
         * Add random passengers for testing or simulation
         * Ensures start and destination are different
         */
        void addRandom(int count) {
            Random random = new Random();
            for (int i = 0; i < count; i++) {
                int start = 1 + random.nextInt(floors);
                int dest;
                do {
                    dest = 1 + random.nextInt(floors);
                } while (dest == start); // avoid same floor start/dest
                addPerson(start, dest);
            }
            System.out.println("Added " + count + " random passengers.");
        }

        /**
         * Move the elevator one step using SCAN algorithm
         */
        void step() {
            System.out.println("Elevator moving... Current floor: " + currentFloor);

            // 1️: Drop off passengers who reached their destination
            // We use a temporary list to avoid modifying the list while looping
            List<Person> toRemove = new ArrayList<>();
            for (Person p : inside) {
                if (p.destination == currentFloor) {
                    System.out.println("  >> Dropped off passenger " + p);
                    toRemove.add(p); // mark this passenger to remove
                }
            }
            inside.removeAll(toRemove); // actually remove them from elevator

            // 2️: Pick up passengers waiting on this floor
            Iterator<Person> it = waiting.iterator();
            while (it.hasNext()) {
                Person p = it.next();
                if (p.start == currentFloor) {
                    p.inElevator = true;
                    inside.add(p);
                    it.remove();
                    System.out.println("  >> Picked up passenger " + p);
                }
            }

            // 3️: Reverse direction if top or bottom floor reached
            if (goingUp && currentFloor == floors) goingUp = false;
            else if (!goingUp && currentFloor == 1) goingUp = true;

            // 4️: Move one floor in current direction
            if (goingUp) currentFloor++;
            else currentFloor--;

            // 5️: Print visual representation of building and elevator
            printVisual();
        }


        /**
         * Convert a list of passengers to a readable string
         * Example: "3 to 9, 5 to 7"
         */
        private String formatPassengers(List<Person> list) {
            if (list.isEmpty()) return "None";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toString());
                if (i < list.size() - 1) sb.append(", ");
            }
            return sb.toString();
        }

        /**
         * Print a visual representation of the building
         * Shows elevator position, passengers inside, and waiting passengers
         */
        void printVisual() {
            System.out.println("\nBuilding state:");
            for (int f = floors; f >= 1; f--) {
                StringBuilder floorLine = new StringBuilder();
                floorLine.append(String.format("[%02d]  ", f));

                // Show elevator if it's on this floor
                if (f == currentFloor) {
                    floorLine.append(String.format("[ ELEVATOR %s ]  Inside: %s",
                            goingUp ? "UP" : "DOWN", formatPassengers(inside)));
                } else {
                    floorLine.append("[            ]");
                }

                // Show passengers waiting on this floor
                List<Person> waitingHere = new ArrayList<>();
                for (Person p : waiting) {
                    if (p.start == f) waitingHere.add(p);
                }

                if (!waitingHere.isEmpty()) {
                    floorLine.append("  Waiting: ").append(formatPassengers(waitingHere));
                }

                System.out.println(floorLine);
            }
            System.out.println();
        }

        /**
         * Print current status in plain text for debugging
         */
        void status() {
            System.out.println("\n--- STATUS ---");
            System.out.println("Current floor: " + currentFloor + " | Direction: " + (goingUp ? "UP" : "DOWN"));
            System.out.println("Passengers inside: " + formatPassengers(inside));
            System.out.println("Waiting passengers: " + formatPassengers(waiting));
            System.out.println("----------------\n");
        }
    }

    /**
     * Main method: entry point for the program
     */
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in); // Read input from console
        System.out.println("ELEVATOR SIMULATION (SCAN ALGO)");
        System.out.print("Enter number of floors: ");
        int floors = sc.nextInt();
        sc.nextLine(); // consume leftover newline

        Elevator elevator = new Elevator(floors);

        System.out.println("""
        Commands:
          add <start> <dest>   - add a person waiting
          random <count>       - add random passengers
          step                 - move elevator one step (SCAN)
          auto <steps>         - auto run N steps
          status               - show current status
          quit                 - exit simulation
        """);

        // Main interactive loop
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) continue;  // ignore empty input

            String[] parts = input.split(" ");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "add" -> {
                    // Add a single passenger
                    if (parts.length < 3) {
                        System.out.println("Usage: add <start> <dest>");
                        break;
                    }
                    int s = Integer.parseInt(parts[1]);
                    int d = Integer.parseInt(parts[2]);
                    if (s < 1 || d < 1 || s > floors || d > floors) {
                        System.out.println("Invalid floor number.");
                    } else if (s == d) {
                        System.out.println("Start and destination cannot be the same.");
                    } else {
                        elevator.addPerson(s, d);
                        System.out.println("Added person " + s + "→" + d);
                    }
                }
                case "random" -> {
                    // Add multiple random passengers
                    if (parts.length < 2) {
                        System.out.println("Usage: random <count>");
                        break;
                    }
                    int count = Integer.parseInt(parts[1]);
                    elevator.addRandom(count);
                }
                case "step" -> {
                    // Move elevator one step
                    elevator.step();
                    Thread.sleep(800); // pause for clarity
                }
                case "auto" -> {
                    // Automatically move elevator N steps
                    if (parts.length < 2) {
                        System.out.println("Usage: auto <steps>");
                        break;
                    }
                    int steps = Integer.parseInt(parts[1]);
                    for (int i = 0; i < steps; i++) {
                        elevator.step();
                        Thread.sleep(800);
                    }
                }
                case "status" -> elevator.status(); // show current status
                case "quit" -> {                    // exit program
                    System.out.println("Exiting simulation.");
                    sc.close();
                    return;
                }
                default -> System.out.println("Unknown command. Please try: add, random, step, auto, status, quit");
            }
        }
    }
}
