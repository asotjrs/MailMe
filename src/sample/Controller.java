package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import sample.datamodel.Contact;
import sample.datamodel.ContactData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Optional;

public class Controller {
    private SortedList<Contact> sortedList;
    private FilteredList<Contact> filteredList;

    @FXML
    private BorderPane mainPanel;

    @FXML
    private TableView<Contact> contactsTable;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Label emailLoggedIn;

    private ContextMenu listContextMenu;


    public void initialize() {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.setTitle("Login");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("login.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the login dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            LoginController loginController = fxmlLoader.getController();
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return loginController.login();

                }
            };
            task.setOnSucceeded(e -> {
                if (task.valueProperty().get()) {
                    emailLoggedIn.setText(emailLoggedIn.getText()+JavaMailUtil.USER);
                    ContactData.getInstance().loadContacts();
                    listContacts();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success login");
                    alert.setHeaderText(null);
                    alert.setContentText("you have logged in successfully");
                    alert.show();
                }  else{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Errors to correct !");
                    alert.setHeaderText("Please correct the following errors :");
                    alert.setContentText(loginController.getLoginErrors().toString());
                    alert.showAndWait();
                    Platform.exit();
                }
            });
            new Thread(task).start();
        }
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem editMenuItem = new MenuItem("Edit");
        MenuItem sendEmailMenuItem = new MenuItem("Send Email");

        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteContact();
            }
        });
        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showEditContactDialog();
            }
        });

        sendEmailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendEmail();
            }
        });
        listContextMenu.getItems().addAll(deleteMenuItem);
        listContextMenu.getItems().addAll(editMenuItem);
        listContextMenu.getItems().addAll(sendEmailMenuItem);

        contactsTable.setRowFactory(new Callback<TableView<Contact>, TableRow<Contact>>() {
            @Override
            public TableRow<Contact> call(TableView<Contact> param) {
                TableRow<Contact> rows = new TableRow<Contact>() {
                    @Override
                    protected void updateItem(Contact item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) { //If the cell is empty
                            setText(null);
                            setStyle("");
                        } else {
                            Contact wanted = getTableView().getItems().get(getIndex());
                            if (wanted.get_id().startsWith("E")) {
                                setStyle("-fx-background-color: cyan");
                            } else if (wanted.get_id().startsWith("S")) {
                                setStyle("-fx-background-color: lightblue");
                            } else if (wanted.get_id().startsWith("T")) {
                                setStyle("-fx-background-color: lightgreen");
                            }else if (wanted.get_id().startsWith("O")) {
                                setStyle("-fx-background-color: yellow");
                            } else {
                                setStyle("-fx-background-color: red");
                            }
                        }
                    }
                };
                rows.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                rows.setContextMenu(null);
                            } else {
                                rows.setContextMenu(listContextMenu);
                            }
                        }
                );
                return rows;
            }
        });

        filteredList = new FilteredList<>(ContactData.getInstance().getContacts());

        sortedList = new SortedList<>(filteredList, Comparator.comparing(Contact::getFirstName));

    }

    public void listContacts() {
        Task<ObservableList<Contact>> task = new GetAllContactsTask(sortedList);
        contactsTable.itemsProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    @FXML
    public void showAddContactDialog() {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Add  New Contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contactdialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ContactController contactController = fxmlLoader.getController();
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return contactController.getNewContact();

                }
            };
            task.setOnSucceeded(e -> {
                if (task.valueProperty().get()) {
                    contactsTable.getItems().clear();
                    listContacts();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Add Contact");
                    alert.setHeaderText(null);
                    alert.setContentText("Contact added successfully");
                    alert.showAndWait();
                } else if (contactController.getErrors().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Already exists");
                    alert.setHeaderText(null);
                    alert.setContentText("Contact With this id Already exists");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Errors to correct !!");
                    alert.setHeaderText("Please correct the following errors :");
                    alert.setContentText(contactController.getErrors().toString());
                    alert.showAndWait();
                }
            });
            new Thread(task).start();
        }


    }

    @FXML
    public void showEditContactDialog() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select the contact you want to edit.");
            alert.showAndWait();
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Edit Contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contactdialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the Edit dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        ContactController contactController = fxmlLoader.getController();
        contactController.editContact(selectedContact);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {

                    return contactController.updateContact(selectedContact);
                }
            };
            task.setOnSucceeded(e -> {
                if (task.valueProperty().get()) {
                    contactsTable.refresh();
                    return;
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Errors to correct !");
                    alert.setHeaderText("Please correct the following errors :");
                    alert.setContentText(contactController.getErrors().toString());
                    alert.showAndWait();
                }
            });
            new Thread(task).start();
        }
    }

    public void deleteContact() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText(null);
            alert.setContentText("PLease select the contact you want to delete.");
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Contact");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete the selected contact: " +
                selectedContact.getFirstName() + " " + selectedContact.getLastName());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws SQLException {
                    return ContactData.getInstance().deleteContact(selectedContact);
                }
            };
            task.setOnSucceeded(e -> {
                if (task.valueProperty().get()) {
                    contactsTable.getItems().clear();
                    listContacts();
                }
            });
            new Thread(task).start();
        }
    }

    @FXML
    public void sendEmail() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText(null);
            alert.setContentText("PLease select the contact you want to send email to.");
            alert.showAndWait();
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Send email");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("sendemail.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the send email dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        EmailController emailController = fxmlLoader.getController();
        emailController.prepareToSendTo(selectedContact);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return emailController.sendEmail();
                }
            };
            new Thread(task).start();
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE))
            deleteContact();
    }


    public void handleFilterType(ActionEvent actionEvent) {
        String type = filterComboBox.getValue();
        switch (type) {
            case "All": {
                filteredList.setPredicate(contact -> {
                    return true;
                });
                break;
            }case "Students": {
                filteredList.setPredicate(contact -> contact.get_id().startsWith("S"));
                break;
            }case "Teachers": {
                filteredList.setPredicate(contact -> contact.get_id().startsWith("T"));
                break;
            }case "Employee": {
                filteredList.setPredicate(contact -> contact.get_id().startsWith("E"));
                break;
            }case "Other": {
                filteredList.setPredicate(contact -> contact.get_id().startsWith("O"));
                break;
            }
        }
        contactsTable.getItems().clear();
        listContacts();
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }
}


class GetAllContactsTask extends Task {
    private SortedList<Contact> sortedList;

    public GetAllContactsTask(SortedList<Contact> sortedList) {
        this.sortedList = sortedList;
    }

    @Override
    public ObservableList<Contact> call() {
        return FXCollections.observableArrayList(sortedList);
    }
}