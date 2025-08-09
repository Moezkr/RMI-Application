import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LibraryManager extends Remote {
    boolean loginUser(String username, String password) throws RemoteException;

    boolean addBook(String title, String author, String ISBN) throws RemoteException;
    String getBook(String ISBN) throws RemoteException;
    boolean updateBookAvailability(String ISBN, boolean availability) throws RemoteException;
    boolean removeBook(String ISBN) throws RemoteException;
    String listBooks() throws RemoteException;
    boolean updateBookDetails(String ISBN, String newTitle, String newAuthor) throws RemoteException;
}
