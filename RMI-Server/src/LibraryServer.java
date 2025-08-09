import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class LibraryServer implements LibraryManager {

    private final MongoCollection<Document> usersCollection;
    private final MongoCollection<Document> booksCollection;


    public LibraryServer() {
        MongoClient mongoClient = MongoClients.create("mongodb+srv://moezkr:moezkr@rmicluster.b9vwsfv.mongodb.net/");
        MongoDatabase database = mongoClient.getDatabase("libraryDB");
        usersCollection = database.getCollection("users");
        booksCollection = database.getCollection("books");
    }


    @Override
    public boolean loginUser(String username, String password) throws RemoteException {
        Document user = usersCollection.find(Filters.eq("username", username)).first();
        if (user == null) return false;
        return user.getString("password").equals(password);
    }


    @Override
    public boolean addBook(String title, String author, String ISBN) throws RemoteException {
        Document existing = booksCollection.find(Filters.eq("ISBN", ISBN)).first();
        if (existing != null) {
            System.out.println("⚠️ Book already exists: " + ISBN);
            return false;
        }

        Document book = new Document("title", title)
                .append("author", author)
                .append("ISBN", ISBN)
                .append("available", true);
        booksCollection.insertOne(book);
        System.out.println("📘 Book added: " + book.toJson());
        return true;
    }


    @Override
    public String getBook(String ISBN) throws RemoteException {
        Document book = booksCollection.find(Filters.eq("ISBN", ISBN)).first();

        if (book != null) {
            String title = book.getString("title");
            String author = book.getString("author");
            String isbn = book.getString("ISBN");
            boolean available = book.getBoolean("available");

            return String.format("📖 Title: %s | ✍️ Author: %s | 🔢 ISBN: %s | 📦 Available: %b",
                    title, author, isbn, available);
        } else {
            return "📚 Book not found.";
        }
    }


    @Override
    public boolean updateBookAvailability(String ISBN, boolean availability) throws RemoteException {
        Document book = booksCollection.find(Filters.eq("ISBN", ISBN)).first();

        if (book != null) {
            booksCollection.updateOne(Filters.eq("ISBN", ISBN),
                    new Document("$set", new Document("available", availability)));
            System.out.println("🔄 Updated availability for ISBN: " + ISBN);
            return true;
        } else {
            System.out.println("❌ Book with ISBN " + ISBN + " not found.");
            return false;
        }
    }


    @Override
    public boolean removeBook(String ISBN) throws RemoteException {
        boolean bookExists = booksCollection.countDocuments(Filters.eq("ISBN", ISBN)) > 0;

        if (bookExists) {
            booksCollection.deleteOne(Filters.eq("ISBN", ISBN));
            System.out.println("🗑️ Book removed with ISBN: " + ISBN);
            return true;
        } else {
            System.out.println("📚 Book with ISBN " + ISBN + " not found.");
            return false;
        }
    }


    @Override
    public boolean updateBookDetails(String ISBN, String newTitle, String newAuthor) throws RemoteException {
        Document existingBook = booksCollection.find(Filters.eq("ISBN", ISBN)).first();
        if (existingBook == null) {
            System.out.println("❌ Book with ISBN " + ISBN + " not found.");
            return false;
        }

        Document updateFields = new Document();
        if (newTitle != null && !newTitle.isEmpty()) {
            updateFields.append("title", newTitle);
        }

        if (newAuthor != null && !newAuthor.isEmpty()) {
            updateFields.append("author", newAuthor);
        }

        if (updateFields.isEmpty()) {
            System.out.println("⚠️ No new details provided for update.");
            return false;
        }

        booksCollection.updateOne(Filters.eq("ISBN", ISBN), new Document("$set", updateFields));
        System.out.println("✏️ Book details updated for ISBN: " + ISBN);
        return true;
    }


    @Override
    public String listBooks() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        FindIterable<Document> allBooks = booksCollection.find();
        for (Document doc : allBooks) {
            sb.append("📖 Title: ").append(doc.getString("title"))
                    .append(" | ✍️ Author: ").append(doc.getString("author"))
                    .append(" | 🔢 ISBN: ").append(doc.getString("ISBN"))
                    .append(" | 📦 Available: ").append(doc.getBoolean("available"))
                    .append("\n");
        }

        return sb.length() > 0 ? sb.toString() : "📂 No books found.";
    }


    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "20.64.147.176");

            LibraryServer obj = new LibraryServer();

            LibraryManager stub = (LibraryManager) UnicastRemoteObject.exportObject(obj, 5002);

            Registry registry = LocateRegistry.createRegistry(5000);

            registry.rebind("LibraryManager", stub);

            System.out.println("✅ Library Server is running at 20.64.147.176:5000");

            synchronized (LibraryServer.class) {
                LibraryServer.class.wait(); // Keep server alive
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
