package sample;

import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import sample.datamodel.Contact;

import java.io.File;


public class EmailController {
    private static String PATH;

    @FXML
    private DialogPane sendEmailDialogPane;

    @FXML
    private TextField toId;

    @FXML
    private TextField subjectId;

    @FXML
    private TextArea contentId;


    boolean sendEmail() {
        return JavaMailUtil.sendEmail(toId.getText(),
                subjectId.getText(),
                contentId.getText(),
                PATH);
    }

    void prepareToSendTo(Contact selectedContact) {
        toId.setText(selectedContact.getEmail());
    }

    @FXML
    public void handleClick() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(sendEmailDialogPane.getScene().getWindow());
        PATH = file.getPath();
    }
}