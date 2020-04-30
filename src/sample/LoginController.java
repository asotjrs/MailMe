package sample;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController {
    @FXML
    public TextField emailField;

    @FXML
    public PasswordField passwordField;

    private List<String> loginErrors = new ArrayList<String>() {
        @Override
        public String toString() {
            StringBuilder error = new StringBuilder();
            for (String e : loginErrors) {
                error.append(e).append("\n");
            }
            return error.toString();
        }
    };
    static String EMAIL;
    static String PASSWD;

    List<String> getLoginErrors() {
        return loginErrors;
    }

    public boolean login() {
        if (validateEmail() & validatePassword()) {
            EMAIL = emailField.getText();
            PASSWD = passwordField.getText();
            return true;
        }
        return false;
    }

    private boolean validateEmail() {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._]*@[a-zA-Z0-9]+([.][a-zA-Z]+)+");
        Matcher matcher = pattern.matcher(emailField.getText());
        if (matcher.find() && matcher.group().equals(emailField.getText())) {
            return true;
        } else if (emailField.getText().isEmpty()) {
            loginErrors.add("Email  Field is empty");
            return false;

        } else {
            loginErrors.add("Please Enter a valid email. abc@xyz.example");
            return false;
        }
    }

    private boolean validatePassword() {
        Pattern pattern = Pattern.compile("^[a-zA-Z]\\w{8,}$");
        Matcher matcher = pattern.matcher(passwordField.getText());
        if (matcher.find() && matcher.group().equals(passwordField.getText())) {
            return true;
        } else if (passwordField.getText().isEmpty()) {
            loginErrors.add("Password  Field is empty");
            return false;

        } else {
            loginErrors.add("Please Enter a valid Password . example Aa012348");
            return false;
        }
    }


}
