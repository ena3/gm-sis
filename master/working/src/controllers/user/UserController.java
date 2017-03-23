package controllers.user;

import domain.User;
import domain.UserType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import logic.authentication.AuthenticationSystem;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Dillon Vaghela, Marcello De Bernardi
 */
public class UserController implements Initializable {
    final ObservableList tableEntries = FXCollections.observableArrayList();
    private AuthenticationSystem auth = AuthenticationSystem.getInstance();
    @FXML private Button addButton, newButton, clearButton, deleteButton;
    @FXML private Label userLabel;
    @FXML private TextField UID, P, FN, SN;
    @FXML private ComboBox UT;
    @FXML private TableView<User> tUsers;
    @FXML private TableColumn<User, String> userIDColumn;
    @FXML private TableColumn<User, String> passwordColumn;
    @FXML private TableColumn<User, String> firstnameColumn;
    @FXML private TableColumn<User, String> surnameColumn;
    @FXML private TableColumn<User, UserType> userTypeColumn;
    @FXML private TextField searchBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AllUsers();
    }


    @FXML private void searchUsers() {
        displayTable(auth.searchUsers(searchBar.getText()));
    }


    public void addEditUser() throws Exception {
        String addOrEdit;
        if (userLabel.equals("Edit User")) {
            addOrEdit = "edit";
            UID.setDisable(true);
            // true is edit
        }
        else {
            addOrEdit = "add";
            // false is add
        }
        boolean checkFields = checkFields();
        if (!checkFields) {
            showAlert("complete all fields");
        }
        else {
            if (!checkFieldFormat()) {
                return;
            }
            boolean add = showAlertC("Are you sure you want to " + addOrEdit + " this User, have you checked the User details?");
            if (add) {
                UserType userType;
                if (UT.getSelectionModel().getSelectedItem().toString().equals("Admin")) {
                    userType = UserType.ADMINISTRATOR;
                }
                else {
                    userType = UserType.NORMAL;
                }
                boolean checker = auth.commitUser(UID.getText(), P.getText(), FN.getText(), SN.getText(), userType);
                AllUsers();
                clear();
                // showAlert("User " + addOrEdit + ": " + checker);

            }
        }
    }

    public void deleteUser() {
        try {
            if (UID.getText().equals(auth.getLoggedInUser())) {
                showAlert("Stop trying to delete yourself");
                return;
            }
            else {
                if (getConfirmation("Are you sure you want to delete this user? ")) {
                    delete(UID.getText());
                    AllUsers();
                    clear();
                }
            }
        }
        catch (Exception e) {
            showAlert("Unable to delete user...check User ID entered");
        }
    }

    public void deleteFromList() {
        User user = ((User) tUsers.getSelectionModel().getSelectedItem());
        if (getConfirmation("Are you sure you want to delete this user? ")) {
            delete(user.getUserID());
        }
        AllUsers();
        clear();
    }

    public void delete(String userID) {
        try {
            boolean check = auth.deleteUser(userID);
            clear();
            showAlert("User deleted: " + check);
        }
        catch (Exception e) {
            showAlert("cant delete User, check User ID entered/selected");
        }
    }

    public void getUser() {
        try {
            User user = ((User) tUsers.getSelectionModel().getSelectedItem());
            setUserDets(user);
        }
        catch (Exception e) {
            showAlert("No selected Item");
        }
    }

    public void setUserDets(User user) {
        UID.setText(user.getUserID());
        P.setText(user.getPassword());
        FN.setText(user.getFirstName());
        SN.setText(user.getSurname());
        String theUser;
        if (user.getUserType().toString().equals("NORMAL")) {
            theUser = "Normal";
        }
        else {
            theUser = "Admin";
        }
        UT.setValue(theUser);
        userLabel.setText("Edit User");
        addButton.setText("Edit");
        clearButton.setDisable(true);
        newButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    public boolean checkFields() {
        if ((!UID.getText().equals("")) && UID.getText().length() == 5 && (!P.getText().equals("")) && (!FN.getText().equals("")) && (!SN.getText().equals("")) && (!UT.getSelectionModel().getSelectedItem().toString().equals(""))) {
            return true;
        }
        return false;
    }

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Message");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public void AllUsers() {
        List<User> arrayList = auth.getAllUsers();
        displayTable(arrayList);
    }

    @SuppressWarnings("Duplicates")
    private boolean showAlertC(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(message);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            return true;
        }
        else {
            return false;
        }
    }

    public void displayTable(List<User> arrayList) {
        if (arrayList.size() == 0) {
            //showAlert("Nothing to display");
            return;
        }
        try {
            tUsers.setDisable(false);
            tableEntries.removeAll(tableEntries);
            for (int i = 0; i < arrayList.size(); i++) {

                tableEntries.add(arrayList.get(i));
            }

            userIDColumn.setCellValueFactory(new PropertyValueFactory<User, String>("userID"));
            userIDColumn.setCellFactory(TextFieldTableCell.<User>forTableColumn());
            //userID.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<User, String>>() {
            //   @Override
            //  public void handle(TableColumn.CellEditEvent<User, String> event) {
            //     ( event.getTableView().getItems().get(event.getTablePosition().getRow())).set(event.getNewValue());
            //}
            //});

            passwordColumn.setCellValueFactory(new PropertyValueFactory<User, String>("password"));
            passwordColumn.setCellFactory(TextFieldTableCell.<User>forTableColumn());
            passwordColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<User, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<User, String> event) {
                    (event.getTableView().getItems().get(event.getTablePosition().getRow())).setPassword(event.getNewValue());
                }
            });

            firstnameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
            firstnameColumn.setCellFactory(TextFieldTableCell.<User>forTableColumn());
            firstnameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<User, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<User, String> event) {
                    (event.getTableView().getItems().get(event.getTablePosition().getRow())).setFirstName(event.getNewValue());
                }
            });

            surnameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("surname"));
            surnameColumn.setCellFactory(TextFieldTableCell.<User>forTableColumn());
            surnameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<User, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<User, String> event) {
                    (event.getTableView().getItems().get(event.getTablePosition().getRow())).setSurname(event.getNewValue());
                }
            });


            userTypeColumn.setCellValueFactory(new PropertyValueFactory<User, UserType>("userType"));
            userTypeColumn.setCellFactory(TextFieldTableCell.<User, UserType>forTableColumn(new StringConverter<UserType>() {
                @Override
                public String toString(UserType object) {
                    return object.toString();
                }

                @Override
                public UserType fromString(String string) {
                    if (string.equals("ADMINISTRATOR")) {
                        return UserType.ADMINISTRATOR;
                    }
                    return UserType.NORMAL;
                }
            }));
            userTypeColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<User, UserType>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<User, UserType> event) {

                    (event.getTableView().getItems().get(event.getTablePosition().getRow())).setUserType(event.getNewValue());
                }
            });


            tUsers.setItems(tableEntries);
        }
        catch (Exception e) {
            showAlert("Error Displaying table");
        }

    }

    public void ClearFields() {
        UID.clear();
        P.clear();
        FN.clear();
        SN.clear();
        UT.getSelectionModel().clearSelection();
    }

    public void NewVehicle() {
        ClearFields();
        UID.setDisable(false);
        userLabel.setText("Add User");
        addButton.setText("Add");
        clearButton.setDisable(false);
        newButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private boolean checkFieldFormat() {
        try {
            int userID = Integer.parseInt(UID.getText());
            if (!(FN.getText().matches("[a-zA-Z]+"))) {
                showAlert("first name must not contains numbers");
                return false;
            }
            if (!(SN.getText().matches("[a-zA-Z]+"))) {
                showAlert("last name must not contains numbers");
                return false;
            }
            return true;
        }
        catch (Exception e) {
            if (e instanceof NumberFormatException) {
                showAlert("Please enter 5 NUMERICAL VALUES!");
            }
            else {
                showAlert(e.getMessage());
            }
            return false;
        }
    }

    private boolean getConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Confirm deletion");
        alert.setContentText(message);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    public void clear() {
        UID.clear();
        P.clear();
        FN.clear();
        SN.clear();
        AllUsers();
    }
}
