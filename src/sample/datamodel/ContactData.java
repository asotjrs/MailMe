package sample.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;


public class ContactData {

    private static ContactData instance = new ContactData();
    private ObservableList<Contact> contacts;

    private ContactData() {
        contacts = FXCollections.observableArrayList();
    }

    public static ContactData getInstance() {
        return instance;
    }

    private static final String DB_NAME = "inttic.db";

    private static final String CONNECTION_STRING = "jdbc:sqlite:C:\\Users\\abdou\\" +
            "Desktop\\gmooor\\" + DB_NAME;

    private static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_ID = "_id";

    private static final int INDEX_FIRST_NAME = 1;
    private static final int INDEX_LAST_NAME = 2;
    private static final int INDEX_PHONE_NUMBER = 3;
    private static final int INDEX_EMAIL = 4;
    private static final int INDEX_ID = 5;

    private static final String INSERT_CONTACT = "INSERT INTO " + TABLE_CONTACTS +
            " VALUES (?,?,?,?,?)";

    private static final String QUERY_CONTACT = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " +
            COLUMN_ID + "  = (?)";

    private static final String UPDATE_CONTACT = "UPDATE " + TABLE_CONTACTS + " SET " +
            COLUMN_FIRST_NAME + " = ? " +
            " , " + COLUMN_LAST_NAME + " = ? , " + COLUMN_PHONE_NUMBER + " = ? , " +
            COLUMN_EMAIL + " = " +
            " ? WHERE " + COLUMN_ID + " = ? ";

    private static final String DELETE_CONTACT = "DELETE FROM " + TABLE_CONTACTS + " WHERE " +
            COLUMN_ID + " = ?";

    private Connection conn;
    private PreparedStatement insertIntoContacts;
    private PreparedStatement queryContact;
    private PreparedStatement updateContact;
    private PreparedStatement deleteContact;

    public boolean open() {
        try {

            conn = DriverManager.getConnection(CONNECTION_STRING);

            insertIntoContacts = conn.prepareStatement(INSERT_CONTACT, Statement.RETURN_GENERATED_KEYS);
            queryContact = conn.prepareStatement(QUERY_CONTACT);
            updateContact = conn.prepareStatement(UPDATE_CONTACT, Statement.RETURN_GENERATED_KEYS);
            deleteContact = conn.prepareStatement(DELETE_CONTACT);

            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (queryContact != null)
                queryContact.close();

            if (insertIntoContacts != null)
                insertIntoContacts.close();

            if (updateContact != null)
                updateContact.close();

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }


    public ObservableList<Contact> getContacts() {
        return contacts;
    }

    public void addContact(Contact item) {
        contacts.add(item);
    }

    public void loadContacts() {

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT * FROM " + TABLE_CONTACTS)) {

            while (results.next()) {
                Contact contact = new Contact();
                contact.setFirstName(results.getString(INDEX_FIRST_NAME));
                contact.setLastName(results.getString(INDEX_LAST_NAME));
                contact.setPhoneNumber(results.getString(INDEX_PHONE_NUMBER));
                contact.setEmail(results.getString(INDEX_EMAIL));
                contact.set_id(results.getString(INDEX_ID));
                this.contacts.add(contact);
            }

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());

        }
    }

    public boolean insertContact(String firstName, String lastName,
                                 String phoneNumber, String email, String id)
            throws SQLException {

        queryContact.setString(1, id);
        ResultSet result = queryContact.executeQuery();
        if (result.next()) {
            return false;
        } else {
            insertIntoContacts.setString(1, firstName);
            insertIntoContacts.setString(2, lastName);
            insertIntoContacts.setString(3, phoneNumber);
            insertIntoContacts.setString(4, email);
            insertIntoContacts.setString(5, id);
            int affectedRows = insertIntoContacts.executeUpdate();
            if (affectedRows != 1)
                throw new SQLException("couldn't insert the contact");
        }
        ResultSet generatedkeys = insertIntoContacts.getGeneratedKeys();
        if (generatedkeys.next()) {
            addContact(new Contact(firstName, lastName, phoneNumber, email, id));
            return true;
        } else
            throw new SQLException("couldn't get the id for contact");
    }

    public boolean updateContact(String id, String firstName, String lastName,
                                 String phoneNumber, String email)
            throws SQLException {

        updateContact.setString(1, firstName);
        updateContact.setString(2, lastName);
        updateContact.setString(3, phoneNumber);
        updateContact.setString(4, email);
        updateContact.setString(5, id);

        int affectedRows = updateContact.executeUpdate();
        return affectedRows == 1;
    }

    public boolean deleteContact(Contact item) throws SQLException {
        contacts.remove(item);
        deleteContact.setString(1, item.get_id());
        int affectedRows = deleteContact.executeUpdate();
        return affectedRows == 1;
    }
}

