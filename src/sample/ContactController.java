package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import sample.datamodel.Contact;
import sample.datamodel.ContactData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactController {
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField idField;

    private List<String> errors = new ArrayList<String>(){
        @Override
        public String toString() {
            StringBuilder error= new StringBuilder();
            for(String e:errors){
                error.append(e).append("\n");
            }
            return error.toString();
        }
    };

    public List<String> getErrors() {
        return errors;
    }

    public boolean getNewContact() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String email = emailField.getText();
        String id = idField.getText();

        if (validateEmail() & validateFirstname() & validateLastname() &
                validatephone() & validateId()) {

            try {
                return ContactData.getInstance().insertContact(firstName, lastName,
                        phoneNumber, email, id);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void editContact(Contact contact) {
        firstNameField.setText(contact.getFirstName());
        lastNameField.setText(contact.getLastName());
        phoneNumberField.setText(contact.getPhoneNumber());
        emailField.setText(contact.getEmail());
        idField.setText(contact.get_id());
    }

    public boolean updateContact(Contact contact) {
        String firstname = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String email = emailField.getText();
        if (validatephone() & validateLastname() & validateFirstname() &
                validateEmail() & validateId()) {

            contact.setFirstName(firstname);
            contact.setLastName(lastName);
            contact.setPhoneNumber(phoneNumber);
            contact.setEmail(email);
            try { return ContactData.getInstance().updateContact(contact.get_id(),
                        firstname, lastName, phoneNumber, email);
            } catch (SQLException e) {
                e.printStackTrace();
                return false; } }
        return false; }

    private boolean validateFirstname() {
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(firstNameField.getText());
        if (matcher.find() && matcher.group().equals(firstNameField.getText())) {
            return true;
        } else if (firstNameField.getText().isEmpty()) {
            errors.add("First name Field is empty");
            return false;

        } else {
            errors.add("Please Enter a valid first name");
            return false;
        }
    }

    private boolean validateLastname() {
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(lastNameField.getText());
        if (matcher.find() && matcher.group().equals(lastNameField.getText())) {
            return true;
        } else if (lastNameField.getText().isEmpty()) {
            errors.add("Last name Field is empty");
            return false;

        } else {
            errors.add("Please Enter a valid last name");
            return false;
        }
    }

    private boolean validatephone() {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(phoneNumberField.getText());
        if (matcher.find() && matcher.group().equals(phoneNumberField.getText())) {
            return true;
        } else if (phoneNumberField.getText().isEmpty()) {
            errors.add("Phone number Field is empty");
            return false;

        } else {
            errors.add("Please Enter a valid phone number for example : 0123456789");
            return false;
        }
    }

    private boolean validateEmail() {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._]*@[a-zA-Z0-9]+([.][a-zA-Z]+)+");
        Matcher matcher = pattern.matcher(emailField.getText());
        if (matcher.find() && matcher.group().equals(emailField.getText())) {
            return true;
        } else if (emailField.getText().isEmpty()) {
            errors.add("Email  Field is empty");
            return false;

        } else {
            errors.add("Please Enter a valid email . abc@xyz.example");
            return false;
        }
    }

    private boolean validateId() {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
        Matcher matcher = pattern.matcher(idField.getText());
        if (matcher.find() && matcher.group().equals(idField.getText())) {
            return true;
        } else if (idField.getText().isEmpty()) {
            errors.add("ID  Field is empty");
            return false;

        } else {
            errors.add("Please Enter a valid ID . for example abcABC123");
            return false;
        }
    }
}