import java.rmi.Naming;
import java.util.Scanner;
public class LibraryClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Connecting to server...");
            LibraryManager stub = (LibraryManager) Naming.lookup("rmi://20.64.147.176:5000/LibraryManager");
            System.out.println("✅ Connected to Library System");

            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (!stub.loginUser(username, password)) {
                System.out.println("❌ Invalid login !!!");
                return;
            }
            boolean running = true;
            while (running) {
                System.out.println("\n📚 Menu:");
                System.out.println("1. Add a book");
                System.out.println("2. List books");
                System.out.println("3. Remove a book");
                System.out.println("4. Update book availability");
                System.out.println("5. Get book by ISBN");
                System.out.println("6. Update book details (title/author)");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Title: ");
                        String title = scanner.nextLine();
                        System.out.print("Author: ");
                        String author = scanner.nextLine();
                        System.out.print("ISBN: ");
                        String isbn = scanner.nextLine();
                        boolean added = stub.addBook(title, author, isbn);
                        if (added) {
                            System.out.println("✅ Book added successfully!");
                        } else {
                            System.out.println("⚠️ Book with this ISBN already exists.");
                        }
                        break;

                    case 2:
                        System.out.println(stub.listBooks());
                        break;

                    case 3:
                        System.out.print("ISBN to remove: ");
                        String removeISBN = scanner.nextLine();

                        boolean isRemoved = stub.removeBook(removeISBN);

                        if (isRemoved) {
                            System.out.println("📚 Book removed successfully.");
                        } else {
                            System.out.println("📚 Book with ISBN " + removeISBN + " not found.");
                        }
                        break;

                    case 4:
                        System.out.print("ISBN: ");
                        String upISBN = scanner.nextLine();

                        System.out.print("Available (true/false): ");
                        String input = scanner.nextLine().trim().toLowerCase();

                        if (!input.equals("true") && !input.equals("false")) {
                            System.out.println("❗ Invalid input. Please enter 'true' or 'false'.");
                            break;
                        }

                        boolean available = Boolean.parseBoolean(input);
                        boolean updated_availability = stub.updateBookAvailability(upISBN, available);

                        if (updated_availability) {
                            System.out.println("✅ Book availability updated successfully.");
                        } else {
                            System.out.println("❌ Book with ISBN " + upISBN + " not found.");
                        }
                        break;

                    case 5:
                        System.out.print("ISBN: ");
                        String searchISBN = scanner.nextLine();
                        System.out.println(stub.getBook(searchISBN));
                        break;

                    case 6:
                        System.out.print("ISBN of book to update: ");
                        String updateISBN = scanner.nextLine();
                        System.out.print("New title (leave blank to keep unchanged): ");
                        String newTitle = scanner.nextLine();
                        System.out.print("New author (leave blank to keep unchanged): ");
                        String newAuthor = scanner.nextLine();
                        boolean updated = stub.updateBookDetails(updateISBN, newTitle, newAuthor);
                        if (updated) {
                            System.out.println("✅ Book updated.");
                        } else {
                            System.out.println("⚠️ Book not found or no changes provided.");
                        }
                        break;

                    case 7:
                        running = false;
                        break;

                    default:
                        System.out.println("❌ Invalid choice");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ error connection");
        }
    }
}


