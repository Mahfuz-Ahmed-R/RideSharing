import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

abstract class User {
    String name;
    String email;
    String phone;

    public User() {
    }

    public User(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    abstract void displayProfile(Ride ride);
}

class Admin extends User {
    // public String currentLocation;

    public Admin() {
    }

    public Admin(String name, String email, String phone) {
        super(name, email, phone);
    }

    public static boolean register(String name, String email, String phone, String password) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("admins.txt", true))) {
            bw.write(name + "," + email + "," + password + "," + phone);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Admin login(String email, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader("admins.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] adminData = line.split(",");
                // Ensure the line has exactly 4 fields
                if (adminData.length == 4) {
                    String storedEmail = adminData[1].trim();
                    String storedPassword = adminData[2].trim();
                    if (storedEmail.equals(email) && storedPassword.equals(password)) {
                        return new Admin(adminData[0].trim(), storedEmail, adminData[3].trim());
                    }
                } else {
                    System.out.println("Warning: Malformed admin entry in file - " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteRider(Ride ride, int index) {
        try {
            index = index - 1;
            if (index >= 0 && index < ride.riders.size())
                ride.riders.remove(index);
            else {
                throw new IndexOutOfBoundsException("Invalid index");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void totalCash(Ride ride) {
        System.out.println("Total Cash: " + ride.totalCash);
    }

    public void totalRides(Ride ride) {
        System.out.println("Total rides:");
        for (String details : ride.oldBookings) {
            System.out.println(details);
        }
    }

    @Override
    void displayProfile(Ride ride) {
        System.out.println("All Riders Profile:");
        for (int i = 0; i < ride.riders.size(); i++) {
            System.out.println((i + 1) + " " + ride.riders.get(i));
        }
    }
}

class Customer extends User {
    int amount;
    String currentLocation;
    String destination;
    String vehicleType;
    int wallet;
    String bookingDetails;

    public Customer() {
    }

    public Customer(String name, String email, String phone) {
        super(name, email, phone);
    }

    public static boolean register(String name, String email, String phone, String password) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("customers.txt", true))) {
            bw.write(name + "," + email + "," + password + "," + phone);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Customer login(String email, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader("customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] customerData = line.split(",");
                if (customerData.length >= 4) {
                    String storedEmail = customerData[1].trim();
                    String storedPassword = customerData[2].trim();
                    if (storedEmail.equals(email) && storedPassword.equals(password)) {
                        return new Customer(customerData[0].trim(), storedEmail, customerData[3].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void rideRequest(Ride ride, String currentLocation, String destination, String vehicleType) {
        // this.currentLocation = currentLocation;
        // this.destination = destination;
        // this.vehicleType = vehicleType;
        amount = calculateFare(ride, currentLocation, destination);
        bookingDetails = "Name: " + name + ", Current Location: " + currentLocation + ", Destination: " + destination
                + ", Vehicle Type: " + vehicleType + ", Fare: " + amount;
        System.out.println("Fare: " + amount);
        System.out.print("Want to confirm? (yes or no): ");
        Scanner sc = new Scanner(System.in);
        String choice = sc.nextLine();
        if (choice.toLowerCase().equals("yes")) {
            ride.bookings.add(bookingDetails);
            ride.fares.add(amount);
            ride.customer = this; // Set the current customer to the ride
            System.out.println("Booking Confirmed");
            System.out.println("Searching for riders!!");
        } else if (choice.toLowerCase().equals("no")) {
            System.out.println("Booking Cancelled");
        } else {
            System.out.println("Invalid Input");
        }
    }

    public void ongoingRides(Ride ride) {
        if (ride.ongoingBookings.isEmpty()) {
            System.out.println("No ongoing rides");
        } else {
            System.out.println("Ongoing Rides:");
            for (int i = 0; i < ride.ongoingBookings.size(); i++) {
                System.out.println((i + 1) + " " + ride.ongoingBookings.get(i));
            }
            System.out.println("Finish ride?");
            Scanner sc = new Scanner(System.in);
            String choice;
            boolean validChoice = false;
            do {
                System.out.print("Enter 'yes' to finish the ride or 'no' to cancel: ");
                choice = sc.nextLine().toLowerCase();
                if (choice.equals("yes") || choice.equals("no")) {
                    validChoice = true;
                } else {
                    System.out.println("Invalid Input");
                }
            } while (!validChoice);

            if (choice.equals("yes")) {
                System.out.print("To pay, enter the index number: ");
                try {
                    int index = sc.nextInt();
                    index = index - 1;
                    if (index >= 0 && index < ride.ongoingBookings.size()) {
                        sendMoney(ride, ride.rider, ride.fares.get(index));
                        ride.fares.remove(index);
                        ride.oldBookings.add(ride.ongoingBookings.get(index));
                        ride.ongoingBookings.remove(index);
                        System.out.println("Ride successfully done!");
                    } else {
                        System.out.println("Invalid index");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input format. Please enter a valid index.");
                }
            } else {
                System.out.println("Ride not finished");
            }
        }
    }

    public void viewRides(Ride ride) {
        System.out.println("Rides you have taken:");
        for (int i = 0; i < ride.bookings.size(); i++) {
            System.out.println((i + 1) + " " + ride.bookings.get(i));
        }
    }

    public void cancelRide(Ride ride, int index) {
        for (int i = 0; i < ride.bookings.size(); i++) {
            System.out.println((i + 1) + " " + ride.bookings.get(i));
        }
        ride.bookings.remove(index);
        System.out.println("Ride canceled");
    }

    public int calculateFare(Ride ride, String currentLocation, String destination) {
        ride.fareReader.readFares("fares.txt");
        return new Location(ride.fareReader).run(currentLocation, destination);
    }

    public void addCash() {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.println("Available Banking systems:");
            System.out.println("1. Paytm");
            System.out.println("2. PhonePe");
            System.out.println("3. GooglePay");
            System.out.println("4. UPI");
            System.out.print("Which banking system do you want to use? Enter the index: ");
            int a = sc.nextInt();

            System.out.print("How much amount do you want to add?: ");
            int amountToAdd = sc.nextInt();

            if (amountToAdd < 0) {
                System.out.println("Invalid amount. Please enter a positive value.");
                return;
            }

            wallet += amountToAdd;
            System.out.println("Successfully Added " + amountToAdd + " rupees");
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

    public void sendMoney(Ride ride, Rider rider, int amount) {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.println("Sending amount: " + amount + " rupees");
            System.out.print("Confirm? (yes or no): ");
            String confirm = sc.next();

            if (confirm.equalsIgnoreCase("yes")) {
                if (wallet >= amount) {
                    System.out.println("Successfully Sent " + amount + " rupees");
                    wallet -= amount;
                    rider.totalCash += amount;
                    ride.totalCash += amount;
                } else {
                    System.out.println("Insufficient Balance");
                }
            } else {
                System.out.println("Sending money canceled");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid option.");
        }
    }

    public void editDetails() {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Enter new name (or press Enter to skip): ");
            String newName = scanner.nextLine();
            if (!newName.isEmpty()) {
                name = newName;
            }

            System.out.println("Enter new email (or press Enter to skip): ");
            String newEmail = scanner.nextLine();
            if (!newEmail.isEmpty()) {
                email = newEmail;
            }

            System.out.println("Enter new Phone (or press Enter to skip): ");
            String newPhone = scanner.nextLine();
            if (!newPhone.isEmpty()) {
                phone = newPhone;
            }
            System.out.println("Details updated successfully!");
        } catch (Exception e) {
            System.out.println("An error occurred while updating details: " + e.getMessage());
        }
    }

    @Override
    void displayProfile(Ride ride) {
        System.out.println("Profile:");
        System.out.println("Name: " + name + " Email: " + email + " Phone: " + phone + " Wallet balance: " + wallet);
    }
}

class Rider extends User {
    String id;
    String vehicleType;
    String licensePlate;
    String currentLocation;
    int totalCash = 0;

    public Rider() {
    }

    public Rider(String name, String email, String phone, String id, String vehicleType, String licensePlate,
            String currentLocation) {
        super(name, email, phone);
        this.id = id;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
        this.currentLocation = currentLocation;

    }

    public boolean registerRider(Ride ride, String name, String email, String phone, String id, String vehicleType,
            String licensePlate, String currentLocation) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("riders.txt", true))) {
            bw.write(name + "," + email + "," + phone + "," + id + "," + vehicleType + "," + licensePlate + ","
                    + currentLocation);
            bw.newLine();
            String riderProfile = "ID: " + id + ", Name: " + name + ", Email: " + email + ", Phone: " + phone
                    + ", Vehicle Type: " + vehicleType + ", License Plate: " + licensePlate;
            ride.riders.add(riderProfile);
            ride.rider = new Rider(name, email, phone, id, vehicleType, licensePlate, currentLocation); // Create new
                                                                                                        // Rider object
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Rider login(Ride ride, String email, String currentLocation) {
        try (BufferedReader br = new BufferedReader(new FileReader("riders.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] riderData = line.split(",");
                if (riderData.length == 7) {
                    String storedEmail = riderData[1].trim();
                    String storedCurrentLocation = riderData[6].trim(); // Ensure this matches your data format
                    if (storedEmail.equals(email) && storedCurrentLocation.equals(currentLocation)) {
                        Rider rider = new Rider(riderData[0].trim(), storedEmail, riderData[2].trim(),
                                riderData[3].trim(), riderData[4].trim(), riderData[5].trim(), storedCurrentLocation);
                        ride.rider = rider;
                        return rider;
                    }
                } else {
                    System.out.println("Invalid rider data format: " + Arrays.toString(riderData));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void allRequest(Ride ride) {
        System.out.println("All Requests:");
        for (int i = 0; i < ride.bookings.size(); i++) {
            System.out.println((i + 1) + " " + ride.bookings.get(i));
        }
    }

    public void acceptRide(Ride ride, Customer customer) {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.print("Accept ride? Enter the index: ");
            int index = sc.nextInt();
            index = index - 1;

            if (index >= 0 && index < ride.bookings.size()) {
                if (currentLocation.equals(customer.currentLocation) && vehicleType.equals(customer.vehicleType)) {
                    System.out.println("Ride Accepted!! Waiting for payment");
                    ride.ongoingBookings.add(ride.bookings.get(index));
                    ride.bookings.remove(index);
                    currentLocation = ride.customer.destination;
                } else {
                    System.out.println("Ride conditions not met. Cannot accept the ride.");
                }
            } else {
                System.out.println("Invalid ride index. Please enter a valid index.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input format. Please enter a valid index.");
        }
    }

    public void editDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter new name (or press Enter to skip): ");
        String newName = scanner.nextLine();
        if (!newName.isEmpty()) {
            name = newName;
        }

        System.out.println("Enter new email (or press Enter to skip): ");
        String newEmail = scanner.nextLine();
        if (!newEmail.isEmpty()) {
            email = newEmail;
        }

        System.out.println("Enter new Phone (or press Enter to skip): ");
        String newPhone = scanner.nextLine();
        if (!newPhone.isEmpty()) {
            phone = newPhone;
        }

        System.out.println("Enter new Phone (or press Enter to skip): ");
        String newid = scanner.nextLine();
        if (!newPhone.isEmpty()) {
            id = newid;
        }
        System.out.println("Enter new Phone (or press Enter to skip): ");
        String newLicenseplate = scanner.nextLine();
        if (!newPhone.isEmpty()) {
            licensePlate = newLicenseplate;
        }
        System.out.println("Enter new Phone (or press Enter to skip): ");
        String newVehicleType = scanner.nextLine();
        if (!newPhone.isEmpty()) {
            vehicleType = newVehicleType;
        }
        System.out.println("Details updated successfully!");
    }

    @Override
    void displayProfile(Ride ride) {
        System.out.println("Profile:");
        System.out.println("Id: " + id + " Name: " + name + " Email: " + email + " Phone: " + phone
                + " Wallet balance: " + totalCash);
    }
}

class Ride {
    List<String> bookings = new ArrayList<>();
    List<String> ongoingBookings = new ArrayList<>();
    List<String> oldBookings = new ArrayList<>();
    List<Integer> fares = new ArrayList<>();
    List<String> riders = new ArrayList<>();
    int totalCash = 0;
    Customer customer;
    Rider rider;
    FareReader fareReader;

    public Ride(FareReader fareReader) {
        this.fareReader = fareReader;
    }
}

class FareReader {

    private List<String> routes = new ArrayList<>();
    private List<Integer> fares = new ArrayList<>();

    public void readFares(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("->|=");
                String from = parts[0];
                String to = parts[1];
                int fare = Integer.parseInt(parts[2]);
                routes.add(from + "->" + to);
                fares.add(fare);
                routes.add(to + "->" + from);
                fares.add(fare);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getFare(String route) {
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).equals(route)) {
                return fares.get(i);
            }
        }
        return null;
    }

    public List<String> getLocations() {
        List<String> locations = new ArrayList<>();
        for (String route : routes) {
            String[] parts = route.split("->");
            if (!locations.contains(parts[0])) {
                locations.add(parts[0]);
            }
            if (!locations.contains(parts[1])) {
                locations.add(parts[1]);
            }
        }
        return locations;
    }
}

class Location {

    private FareReader fareReader;

    public Location(FareReader fareReader) {
        this.fareReader = fareReader;
    }

    public int run(String currentLocation, String destination) {
        List<String> locations = fareReader.getLocations();

        System.out.println("Available locations:");
        for (String location : locations) {
            System.out.println(location);
        }

        String route = currentLocation + "->" + destination;
        Integer fare = fareReader.getFare(route);
        if (fare != null) {
            return fare;
        } else {
            return 0;
        }
    }
}

public class RideHailingApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Ride ride = new Ride(new FareReader());
        Admin admin = null;
        boolean firstAdminLogin = true;
        boolean firstCustomerLogin = true;
        while (true) {
            System.out.println("Welcome to Ride Hailing App");
            System.out.println("1. Admin");
            System.out.println("2. Customer");
            System.out.println("3. Rider");
            System.out.println("4. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    if (firstAdminLogin) {
                        System.out.print("Enter name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter email: ");
                        String email = scanner.nextLine();
                        System.out.print("Enter phone: ");
                        String phone = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();

                        if (Admin.register(name, email, phone, password)) {
                            System.out.println("Admin registered successfully");
                            admin = new Admin(name, email, phone);
                            firstAdminLogin = false;
                        } else {
                            System.out.println("Admin registration failed");
                        }
                    } else {
                        System.out.print("Enter email: ");
                        String email = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();

                        admin = Admin.login(email, password);
                        if (admin != null) {
                            System.out.println("Admin login successful");
                        } else {
                            System.out.println("Invalid email or password");
                        }
                    }

                    if (admin != null) {
                        while (true) {
                            System.out.println("Admin Menu:");
                            System.out.println("1. Register Rider");
                            System.out.println("2. Delete Rider");
                            System.out.println("3. View Total Cash");
                            System.out.println("4. View Total Rides");
                            System.out.println("5. View Riders Profile");
                            System.out.println("6. Logout");

                            System.out.print("Enter your choice: ");
                            int adminChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline character

                            switch (adminChoice) {
                                case 1:
                                    System.out.print("Enter name: ");
                                    String riderName = scanner.nextLine();
                                    System.out.print("Enter email: ");
                                    String riderEmail = scanner.nextLine();
                                    System.out.print("Enter phone: ");
                                    String riderPhone = scanner.nextLine();
                                    System.out.print("Enter ID: ");
                                    String id = scanner.nextLine();
                                    System.out.print("Enter vehicle type: ");
                                    String vehicleType = scanner.nextLine();
                                    System.out.print("Enter license plate: ");
                                    String licensePlate = scanner.nextLine();
                                    System.out.print("Enter current location: ");
                                    String currentLocation = scanner.nextLine();

                                    Rider newRider = new Rider();
                                    if (newRider.registerRider(ride, riderName, riderEmail, riderPhone, id, vehicleType,
                                            licensePlate, currentLocation)) {
                                        newRider.currentLocation = currentLocation;
                                        newRider.vehicleType = vehicleType;
                                        System.out.println("Rider registered successfully");
                                    } else {
                                        System.out.println("Rider registration failed");
                                    }
                                    break;
                                case 2:
                                    System.out.print("Enter rider index to delete: ");
                                    int riderIndex = scanner.nextInt();
                                    admin.deleteRider(ride, riderIndex);
                                    break;
                                case 3:
                                    admin.totalCash(ride);
                                    break;
                                case 4:
                                    admin.totalRides(ride);
                                    break;
                                case 5:
                                    admin.displayProfile(ride);
                                    break;
                                case 6:
                                    System.out.println("Admin logged out");
                                    admin = null;
                                    break;
                                default:
                                    System.out.println("Invalid choice");
                                    break;
                            }

                            if (admin == null) {
                                break;
                            }
                        }
                    }
                    break;
                case 2:
                    System.out.print("1. Register\n2. Login\nEnter your choice: ");
                    int customerChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character
                    Customer customer = null;

                    if (customerChoice == 1) {
                        System.out.print("Enter name: ");
                        String customerName = scanner.nextLine();
                        System.out.print("Enter email: ");
                        String customerEmail = scanner.nextLine();
                        System.out.print("Enter phone: ");
                        String customerPhone = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String customerPassword = scanner.nextLine();

                        if (Customer.register(customerName, customerEmail, customerPhone, customerPassword)) {
                            System.out.println("Customer registered successfully");
                            customer = new Customer(customerName, customerEmail, customerPhone);
                        } else {
                            System.out.println("Customer registration failed");
                        }
                    } else if (customerChoice == 2) {
                        System.out.print("Enter email: ");
                        String customerEmail = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String customerPassword = scanner.nextLine();

                        customer = Customer.login(customerEmail, customerPassword);
                        if (customer != null) {
                            System.out.println("Customer login successful");
                        } else {
                            System.out.println("Invalid email or password");
                        }
                    }

                    if (customer != null) {
                        while (true) {
                            System.out.println("Customer Menu:");
                            System.out.println("1. Book Ride");
                            System.out.println("2. View Ride");
                            System.out.println("3. Ongoing Ride");
                            System.out.println("4. Cancle Ride");
                            System.out.println("5. Add Money");
                            System.out.println("6. View Profile");
                            System.out.println("7. Edit Profile");
                            System.out.println("8. Logout");

                            System.out.print("Enter your choice: ");
                            int customerMenuChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline character

                            switch (customerMenuChoice) {
                                case 1:
                                    System.out.print("Enter your current location: ");
                                    String customerCurrentLocation = scanner.nextLine();
                                    System.out.print("Enter your destination: ");
                                    String customerDestination = scanner.nextLine();
                                    System.out.print("Enter your Vehicle type: ");
                                    String vehicleType = scanner.nextLine();
                                    customer.rideRequest(ride, customerCurrentLocation.toUpperCase(),
                                            customerDestination.toUpperCase(),
                                            vehicleType);
                                    break;
                                case 2:
                                    customer.viewRides(ride);
                                    break;
                                case 3:
                                    customer.ongoingRides(ride);
                                    break;
                                case 4:
                                    System.out.println("Enter the index you want to delete: ");
                                    int index = scanner.nextInt();
                                    customer.cancelRide(ride, index);
                                    break;
                                case 5:
                                    customer.addCash();
                                    break;
                                case 6:
                                    customer.displayProfile(ride);
                                    break;
                                case 7:
                                    customer.editDetails();
                                    break;
                                case 8:
                                    System.out.println("Customer logged out");
                                    customer = null;
                                    break;
                                default:
                                    System.out.println("Invalid choice");
                                    break;
                            }

                            if (customer == null) {
                                break;
                            }
                        }
                    }
                    break;
                case 3:
                    Rider rider = null;
                    System.out.print("Enter email: ");
                    String riderEmail = scanner.nextLine();
                    System.out.print("Enter Current Location: ");
                    String riderLocation = scanner.nextLine();

                    rider = Rider.login(ride, riderEmail, riderLocation);
                    if (rider != null) {
                        System.out.println("Rider login successful");
                    } else {
                        System.out.println("Invalid email or password");
                    }

                    if (rider != null) {
                        while (true) {
                            System.out.println("Rider Menu:");
                            System.out.println("1. Accept Ride");
                            System.out.println("2. View Profile");
                            System.out.println("3. Edit details");
                            System.out.println("4. Total Earnings");
                            System.out.println("5. Logout");

                            System.out.print("Enter your choice: ");
                            int riderMenuChoice = scanner.nextInt();
                            scanner.nextLine();

                            switch (riderMenuChoice) {
                                case 1:
                                    rider.allRequest(ride);
                                    rider.acceptRide(ride, ride.customer);
                                    break;
                                case 2:
                                    rider.displayProfile(ride);
                                    break;
                                case 3:
                                    rider.editDetails();
                                    break;
                                case 4:
                                    System.out.println("Total earnings: " + rider.totalCash);
                                    break;
                                case 5:
                                    System.out.println("Rider logged out");
                                    rider = null;
                                    break;
                                default:
                                    System.out.println("Invalid choice");
                                    break;
                            }

                            if (rider == null) {
                                break;
                            }
                        }
                    }
                    break;
                case 4:
                    System.out.println("Exiting the app. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }
    }
}
