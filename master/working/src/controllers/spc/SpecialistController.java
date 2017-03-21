package controllers.spc;


import domain.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import logic.booking.BookingSystem;
import logic.parts.PartsSystem;
import logic.spc.SpecRepairSystem;
import logic.vehicle.VehicleSys;
import persistence.DatabaseRepository;
import domain.PartRepair;
import java.net.URL;
import java.util.*;
import java.time.*;


/**
 * @author: Muhammad Murad Ahmed 11/03/2017.
 * project: SE31
 */
public class SpecialistController implements Initializable{

    private DatabaseRepository databaseRepository = DatabaseRepository.getInstance();
    private SpecRepairSystem specRepairSystem =  SpecRepairSystem.getInstance(databaseRepository);
    private BookingSystem bookingSystem = BookingSystem.getInstance();
    private PartsSystem partsSystem = PartsSystem.getInstance(databaseRepository);
    private DiagRepBooking diagRepBooking;
    private VehicleRepair trackerV;
    private PartRepair trackerP;
    private int spcRepID, installationID;
    @FXML
    private DatePicker bookingDeliveryDate = new DatePicker();
    @FXML
    private DatePicker bookingReturnDate = new DatePicker();
    @FXML
    private DatePicker instaDate, wEndDate = new DatePicker();
    @FXML
    private Label itemLabel = new Label();
    @FXML
    private ComboBox<String> partDes, partSerial ,getBookings= new ComboBox<>();
    @FXML
    private ObservableList<String> partAbs = FXCollections.observableArrayList();
    private List<PartAbstraction> partAbstractions = new ArrayList<>();
    private List<PartOccurrence> partOccurrences = new ArrayList<>();

    public void initialize(URL location, ResourceBundle resources){
        bookingDeliveryDate.setDayCellFactory(dateChecker);
        bookingReturnDate.setDayCellFactory(dateChecker);
        instaDate.setDayCellFactory(dateChecker);
        wEndDate.setDayCellFactory(dateChecker);
        setPartDes();
        try {
           setValues();
            partDes.setItems(partAbs);
        }catch(NullPointerException e){e.printStackTrace();}
        findSRCBookings();

    }
    @FXML
    private ComboBox<String> bookingType = new ComboBox<>();

    private void setValues()
    {
        bookingType.getItems().addAll("Vehicle", "Part");
        bookingType.setValue("Vehicle");
    }

    private void setPartDes() {
        partAbstractions = partsSystem.getPartAbstractions();
        for (PartAbstraction partAbstraction : partAbstractions) {
            partAbs.add(Integer.toString(partAbstraction.getPartAbstractionID()) + " " + partAbstraction.getPartName());
        }
    }

    public void setOccs()
    {
        Character c = partDes.getSelectionModel().getSelectedItem().charAt(0);
        int cd = c.getNumericValue(c);
       // System.out.println(cd);
        partOccurrences.removeAll(partOccurrences);
        partOccurrences.addAll(partsSystem.getAllUninstalled(cd));
        partSerial.getItems().removeAll(partSerial.getItems());
        for(PartOccurrence partOccurrence: partOccurrences) {
            partSerial.getItems().add(Integer.toString(partOccurrence.getPartOccurrenceID()));
        }

    }

    @FXML
    public void queryOutstanding()
    {
        List<SpecRepBooking> specRepBookingList = new ArrayList<>();
        List<PartRepair> partRepairs = specRepairSystem.getOutstandingP();
        List<VehicleRepair>vehicleRepairs = specRepairSystem.getOutstandingV();
        specRepBookingList.addAll(partRepairs);
        specRepBookingList.addAll(vehicleRepairs);
        displaySpecRepBookings(specRepBookingList);
    }

    private Callback<DatePicker, DateCell> dateChecker = dp1 -> new DateCell()
    {
        @Override
        public void updateItem( LocalDate item , boolean empty )
        {

            // Must call super
            super.updateItem(item, empty);
            if (item.isBefore(LocalDate.now()))
            {
                this.setStyle(" -fx-background-color: rgba(171,171,171,0); ");
                this.setDisable ( true );
            }
            if(item.getDayOfWeek().equals(DayOfWeek.SUNDAY))
            {
                this.setDisable(true);
                this.setStyle("-fx-background-color: rgba(171,171,171,0)");
            }

        }
    };
    @FXML
    private TextField idOfBookingItem;
    @FXML
    private Button addSRBooking, cancelEdit,updateSRBooking;
    @FXML
    private TableView<SpecRepBooking> specialistBookings;
    @FXML
    private TableColumn <SpecRepBooking, String>itemID;
    @FXML
    private TableColumn <SpecRepBooking, Date>deliveryDateOfBooking;
    @FXML
    private TableColumn <SpecRepBooking,Date>returnDateOfBooking;
    @FXML
    private TableColumn <SpecRepBooking, Double>costOfBooking;
    @FXML
    private TableColumn <SpecRepBooking, Integer>spcIDOfBooking;
    @FXML
    private TableColumn <SpecRepBooking, Integer>bookingIDOfBooking;
    @FXML
    private TextField bookingID,bookingItemID,bookingSPCID,bookingSPCName,bookingCost;

    @FXML
    public void showDetails()
    {
        try {
            if(specialistBookings.getSelectionModel().getSelectedItem() instanceof  VehicleRepair) {
                itemLabel.setText("Vehicle registration:");
                VehicleRepair vehicleRepair = (VehicleRepair) specialistBookings.getSelectionModel().getSelectedItem();
                diagRepBooking = BookingSystem.getInstance().getBookingByID(vehicleRepair.getBookingID());
                trackerV = vehicleRepair;
                bookingID.setText(Integer.toString(vehicleRepair.getBookingID()));
                bookingID.setEditable(false);
                bookingItemID.setText(vehicleRepair.getVehicleRegNumber());
                bookingItemID.setEditable(false);
                java.time.LocalDate deliveryDate = toLocalDate(vehicleRepair.getDeliveryDate());
                bookingDeliveryDate.setValue(deliveryDate);
                bookingDeliveryDate.setEditable(false);
                java.time.LocalDate returnDate = toLocalDate(vehicleRepair.getReturnDate());
                bookingReturnDate.setValue(returnDate);
                bookingReturnDate.setEditable(false);
                SpecialistRepairCenter specialistRepairCenter = specRepairSystem.getByID(vehicleRepair.getSpcID());
                bookingSPCID.setText(Integer.toString(specialistRepairCenter.getSpcID()));
                bookingSPCID.setEditable(false);
                bookingSPCName.setText(specialistRepairCenter.getName());
                bookingCost.setText(Double.toString(vehicleRepair.getCost()));
                bookingCost.setEditable(false);
                spcRepID = vehicleRepair.getSpcRepID();
            }
            if(specialistBookings.getSelectionModel().getSelectedItem() instanceof PartRepair)
            {
                itemLabel.setText("Part ID:");
                PartRepair partRepair = (PartRepair)specialistBookings.getSelectionModel().getSelectedItem();
                diagRepBooking = BookingSystem.getInstance().getBookingByID(partRepair.getBookingID());
                trackerP = partRepair;
                bookingID.setText(Integer.toString(partRepair.getBookingID()));
                bookingID.setEditable(false);
                bookingItemID.setText(Integer.toString(partRepair.getPartOccurrenceID()));
                bookingItemID.setEditable(false);
                java.time.LocalDate deliveryDate = toLocalDate(partRepair.getDeliveryDate());
                bookingDeliveryDate.setValue(deliveryDate);
                bookingDeliveryDate.setEditable(false);
                java.time.LocalDate returnDate = toLocalDate(partRepair.getReturnDate());
                bookingReturnDate.setValue(returnDate);
                bookingReturnDate.setEditable(false);
                SpecialistRepairCenter specialistRepairCenter = specRepairSystem.getByID(partRepair.getSpcID());
                bookingSPCID.setText(Integer.toString(specialistRepairCenter.getSpcID()));
                bookingSPCID.setEditable(false);
                bookingSPCName.setText(specialistRepairCenter.getName());
                bookingCost.setText(Double.toString(partRepair.getCost()));
                bookingCost.setEditable(false);
                spcRepID = partRepair.getSpcRepID();
            }
        }
        catch (NullPointerException  e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    public void findSRCBookings() {
        try {
            specialistBookings.getItems().clear();
            List<PartRepair> partRepairs = specRepairSystem.getAllPartRepairs(Integer.parseInt(idOfBookingItem.getText()));
            displaySpecRepBookings(partRepairs);

        }
        catch (NumberFormatException ex) {
            if (specRepairSystem.getByName(idOfBookingItem.getText()).size()>0) {
                List<Customer> customers = specRepairSystem.getByName(idOfBookingItem.getText());
                List<Vehicle> vehicles = new ArrayList<>();
                List<VehicleRepair> vehicleRepairs = new ArrayList<>();
                for (Customer customer : customers) {
                    vehicles.addAll(customer.getVehicles());
                }
                for (Vehicle vehicle : vehicles) {
                    if (specRepairSystem.getVehicleBookings(vehicle.getVehicleRegNumber()) != null) {
                        vehicleRepairs.addAll(specRepairSystem.getVehicleBookings(vehicle.getVehicleRegNumber()));
                    }
                }
                displaySpecRepBookings(vehicleRepairs);
            } else {
                    specialistBookings.getItems().clear();
                    List<VehicleRepair> vehicleRepairs = specRepairSystem.getVehicleBookings(idOfBookingItem.getText());
                    displaySpecRepBookings(vehicleRepairs);
            }
        }
        }


    public void addBooking()
    {
        try {
            if(bookingID.getText().equals(""))
            {
                lbl_booking_notFound.setVisible(true);
            }
            else
            {
                lbl_booking_notFound.setVisible(false);
            }
            if(bookingItemID.getText().equals(""))
            {
                item_found_lbl.setText("Item ID not entered");
                item_found_lbl.setTextFill(Color.RED);
                item_found_lbl.setVisible(true);
            }
            else
            {
                item_found_lbl.setVisible(false);
            }
            if(bookingSPCID.getText().equals(""))
            {
                spc_found_lbl.setText("No SPC found");
                spc_found_lbl.setTextFill(Color.RED);
                spc_found_lbl.setVisible(true);
            }
            else
            {
                spc_found_lbl.setVisible(false);
            }
            if(bookingDeliveryDate.getValue().equals(null))
            {
                showAlert("No delivery date selected");
            }
            if(bookingReturnDate.getValue().equals(null))
            {
                showAlert("No return date selected");
            }
            DiagRepBooking diagRepBookings = specRepairSystem.findBooking(Integer.parseInt(bookingID.getText()));
            if(diagRepBookings!=null) {


                if(!isBefore(bookingDeliveryDate.getValue()))
                {
                    throw new  InvalidDateException("Delivery date before today's date.");
                }

                Date deliveryDate = fromLocalDate(bookingDeliveryDate.getValue());
                Date returnDate = fromLocalDate(bookingReturnDate.getValue());

                if(returnDate.before(deliveryDate))
                {
                    throw new InvalidDateException("Return date before delivery date.");
                }
                else
                {
                    if (bookingType.getSelectionModel().getSelectedItem().equals("Vehicle")) {
                        Vehicle vehicle = VehicleSys.getInstance().searchAVehicle(bookingItemID.getText());
                        if(vehicle !=null) {
                            if(vehicle.isCoveredByWarranty())
                            {
                                bookingCost.setText("0");
                            }
                            System.out.println(diagRepBookings.getBillAmount());
                            Bill bill = new Bill(Double.parseDouble(bookingCost.getText()) + diagRepBookings.getBillAmount(), false);
                            diagRepBookings.setBill(bill);
                            VehicleRepair vehicleRepair= new VehicleRepair(Integer.parseInt(bookingSPCID.getText()), deliveryDate, returnDate, Double.parseDouble(bookingCost.getText()), Integer.parseInt(bookingID.getText()), bookingItemID.getText());
                            specRepairSystem.addSpecialistBooking(vehicleRepair);
                            bookingSystem.commitBooking(diagRepBookings);
                            findSRCBookings();
                            clearBookingFields();
                            showAlert("Successfully added vehicle booking");
                        }
                    } else if(bookingType.getSelectionModel().getSelectedItem().equals("Part")) {
                        PartOccurrence partOccurrence = specRepairSystem.getPartOcc(Integer.parseInt(bookingItemID.getText()));
                        Installation installation = specRepairSystem.getByInstallationID(partOccurrence.getInstallationID());
                        if (installation != null) {
                            if (installation.getEndWarrantyDate().isAfter(ZonedDateTime.now())) {
                                bookingCost.setText("0");
                            }


                            Bill bill = new Bill(Double.parseDouble(bookingCost.getText()) + diagRepBookings.getBillAmount(), false);
                            diagRepBookings.setBill(bill);
                            PartRepair partRepair = new PartRepair(Integer.parseInt(bookingSPCID.getText()), deliveryDate, returnDate, Double.parseDouble(bookingCost.getText()), Integer.parseInt(bookingID.getText()), Integer.parseInt(bookingItemID.getText()));
                            PartRepair repair = specRepairSystem.findPartRepairBooking(Integer.parseInt(bookingID.getText()));
                            partOccurrence.setSpecRepID(repair.getSpcRepID());
                            partsSystem.addPartOccurrence(partOccurrence);
                            specRepairSystem.addSpecialistBooking(partRepair);
                            bookingSystem.commitBooking(diagRepBookings);
                            findSRCBookings();
                            clearBookingFields();
                            showAlert("Successfully added part booking ");
                        }
                        else
                        {
                            showAlert("No part found.");
                        }
                    }
                }

            }
            else{
                showAlert("Please enter a valid booking ID!");
            }
            clearBookingFields();
        }
        catch (NumberFormatException | InvalidDateException | IndexOutOfBoundsException | NullPointerException e) {
            if (e instanceof InvalidDateException) {
                showAlert(e.getMessage());
            }
            if (e instanceof NumberFormatException)
            {
                showAlert("Please check all fields have been entered correctly.");
            }
            if(e instanceof IndexOutOfBoundsException)
            {
                e.printStackTrace();
                showAlert("No Booking found");
            }
            if(e instanceof NullPointerException)
            {
                showAlert("Booking fields empty.");
            }

        }
    }

    public void cancelEditing()
    {
        bookingID.setEditable(false);
        bookingItemID.setEditable(false);
        bookingSPCID.setEditable(false);
        bookingCost.setEditable(false);
        cancelEdit.setVisible(false);
        bookingType.setVisible(true);
        updateSRBooking.setVisible(false);
        addSRBooking.setVisible(true);

    }

    public void editBooking()
    {
        cancelEdit.setVisible(true);
        bookingID.setEditable(true);
        bookingItemID.setEditable(true);
        bookingSPCID.setEditable(true);
        bookingCost.setEditable(true);
        bookingType.setVisible(false);
        addSRBooking.setVisible(false);
        updateSRBooking.setVisible(true);
        bookingDeliveryDate.setEditable(true);
        bookingReturnDate.setEditable(true);
    }


    @FXML
    private Label lbl_booking_found, lbl_booking_notFound,item_found_lbl,spc_found_lbl = new Label();

    @FXML
    public void findBooking()
    {
        try
        {
            int criteria  = Integer.parseInt(bookingID.getText());
            DiagRepBooking diagRepBookings = specRepairSystem.findBooking(criteria);
            if (diagRepBookings !=null) {
                lbl_booking_notFound.setVisible(false);
                lbl_booking_found.setText("Booking found");
                lbl_booking_found.setVisible(true);
            }
            else{
                lbl_booking_found.setVisible(false);
                lbl_booking_notFound.setVisible(true);
            }
        }
        catch(NumberFormatException | IndexOutOfBoundsException e)
        {
            lbl_booking_found.setVisible(false);
            lbl_booking_notFound.setVisible(true);
        }
    }

    @FXML
    public void findItem()
    {
        try{
            try {
                PartOccurrence partOccurrence = specRepairSystem.findAPart(Integer.parseInt(bookingItemID.getText()));

                if (partOccurrence != null) {
                    item_found_lbl.setTextFill(Color.valueOf("#4aff02"));
                    item_found_lbl.setText("Part found");
                    item_found_lbl.setVisible(true);
                }
            }
            catch (IndexOutOfBoundsException exx)
            {
                item_found_lbl.setTextFill(Color.RED);
                item_found_lbl.setText("No part found");
                item_found_lbl.setVisible(true);
            }

        }
        catch (NumberFormatException e)
        {
            try {
                Vehicle vehicle = specRepairSystem.findVehicle(bookingItemID.getText());
                if (vehicle != null) {
                    item_found_lbl.setTextFill(Color.valueOf("#4aff02"));
                    item_found_lbl.setText("Vehicle found");
                    item_found_lbl.setVisible(true);
                }
            }
                catch (NullPointerException ex)
            {
                item_found_lbl.setTextFill(Color.RED);
                item_found_lbl.setText("Please enter exact vehicle reg");
                item_found_lbl.setVisible(true);
            }
        }
    }

    public void clearBookingFields()
    {
        item_found_lbl.setVisible(false);
        lbl_booking_found.setVisible(false);
        lbl_booking_notFound.setVisible(false);
        item_found_lbl.setVisible(false);
        bookingID.clear();
        bookingID.setEditable(true);
        bookingReturnDate.setValue(null);
        bookingDeliveryDate.setValue(null);
        bookingItemID.clear();
        bookingItemID.setEditable(true);
        bookingCost.clear();
        bookingCost.setEditable(true);
        bookingSPCName.clear();
        bookingSPCID.clear();
        bookingSPCID.setEditable(true);
        spc_found_lbl.setVisible(false);
        itemLabel.setText("Item ID: ");
    }

    public void findSRC()
    {
        try
        {
            SpecialistRepairCenter specialistRepairCenter = specRepairSystem.getByID(Integer.parseInt(bookingSPCID.getText()));
            bookingSPCName.setText(specialistRepairCenter.getName());
            spc_found_lbl.setText("SPC Found");
            spc_found_lbl.setTextFill(Color.valueOf("#04eb04"));
            spc_found_lbl.setVisible(true);
        }
        catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException e)
        {
            spc_found_lbl.setTextFill(Color.RED);
            spc_found_lbl.setText("No SPC Found");
            spc_found_lbl.setVisible(true);
        }
    }

    public void updateBooking()
    {
        try {
            if(diagRepBooking.getBookingID() != Integer.parseInt(bookingID.getText()))
            {
                if(bookingType.getSelectionModel().getSelectedItem().equals("Vehicle"))
                {
                    Bill bill = new Bill(-trackerV.getCost() + diagRepBooking.getBillAmount(), diagRepBooking.getBillSettled());
                    diagRepBooking.setBill(bill);
                    BookingSystem.getInstance().commitBooking(diagRepBooking);
                }
                else
                {
                    Bill bill = new Bill(-trackerP.getCost()+ diagRepBooking.getBillAmount(),diagRepBooking.getBillSettled());
                    diagRepBooking.setBill(bill);
                    diagRepBooking.setBill(bill);
                }
            }
            Date deliveryDate = fromLocalDate(bookingDeliveryDate.getValue());
            Date returnDate = fromLocalDate(bookingReturnDate.getValue());
            if (bookingType.getSelectionModel().getSelectedItem().equals("Vehicle")) {
                VehicleRepair vehicleRepair = specRepairSystem.findVehicleRepairBooking(spcRepID);
                if (vehicleRepair != null) {
                    Vehicle vehicle = VehicleSys.getInstance().searchAVehicle(vehicleRepair.getVehicleRegNumber());
                    if(vehicle.isCoveredByWarranty())
                    {
                        bookingCost.setText("0");
                    }
                    vehicleRepair.setVehicleRegNumber(bookingItemID.getText());
                    vehicleRepair.setCost(Double.parseDouble(bookingCost.getText()));
                    vehicleRepair.setSpcID(Integer.parseInt(bookingSPCID.getText()));
                    if (!(vehicleRepair.setDeliveryDate(deliveryDate) || vehicleRepair.setReturnDate(returnDate))) {
                        throw new InvalidDateException("The delivery date is before the current date : " + new Date().toString() + "or the return date is before the delivery date");
                    }
                    vehicleRepair.setReturnDate(deliveryDate);
                    vehicleRepair.setDeliveryDate(returnDate);
                    vehicleRepair.setBookingID(Integer.parseInt(bookingID.getText()));
                    Bill bill = new Bill(-trackerV.getCost() + Double.parseDouble(bookingCost.getText() )+ diagRepBooking.getBillAmount(), diagRepBooking.getBillSettled());
                    diagRepBooking.setBill(bill);
                    bookingSystem.commitBooking(diagRepBooking);
                    specRepairSystem.updateBookings(vehicleRepair);
                    findSRCBookings();
                }

            }

            if (bookingType.getSelectionModel().getSelectedItem().equals("Part")) {
                PartRepair partRepair = specRepairSystem.findPartRepairBooking(spcRepID);
                if (partRepair != null) {
                    partRepair.setPartOccurrenceID(Integer.parseInt(bookingItemID.getText()));
                    partRepair.setCost(Double.parseDouble(bookingCost.getText()));
                    partRepair.setSpcID(Integer.parseInt(bookingSPCID.getText()));
                    if(!(partRepair.setDeliveryDate(deliveryDate) || partRepair.setReturnDate(returnDate)))
                    {
                        throw new InvalidDateException("Check Dates!");
                    }
                    partRepair.setReturnDate(deliveryDate);
                    partRepair.setDeliveryDate(returnDate);
                    partRepair.setBookingID(Integer.parseInt(bookingID.getText()));
                    Bill bill = new Bill(-trackerP.getCost()+ Double.parseDouble(bookingCost.getText()), diagRepBooking.getBillSettled());
                    diagRepBooking.setBill(bill);
                    bookingSystem.commitBooking(diagRepBooking);
                    specRepairSystem.updateBookings(partRepair);
                    findSRCBookings();
                }
            }
            else if(bookingType.getSelectionModel().getSelectedItem().equals(""))
                {
                showAlert("No booking was found of this type!");
            }
            cancelEditing();
        }
                catch(InvalidDateException e)
                {
                    showAlert(e.getMessage());
                }

    }

    public void deleteBooking()
    {
        if(deleteConfirmation("Are you sure you want to delete this booking?"))
        {
            if(bookingType.getSelectionModel().getSelectedItem().equals("Vehicle"))
            {
                Bill bill;
                if(-trackerV.getCost() + diagRepBooking.getBillAmount()<=0)
                {
                    bill =  new Bill(-trackerV.getCost()+ diagRepBooking.getBillAmount(), true);
                }
                else
                {
                   bill = new Bill(-trackerV.getCost()+ diagRepBooking.getBillAmount(), diagRepBooking.getBillSettled());
                }
                diagRepBooking.setBill(bill);
                bookingSystem.commitBooking(diagRepBooking);
                clearBookingFields();
                clearFields();
                specRepairSystem.deleteByRepIDV(spcRepID);
                findSRCBookings();
            }
            else if(bookingType.getSelectionModel().getSelectedItem().equals("Part"))
            {
                Bill bill;
                if(-trackerP.getCost() + diagRepBooking.getBillAmount()<=0)
                {
                    bill =  new Bill(-trackerP.getCost()+ diagRepBooking.getBillAmount(), true);
                }
                else
                {
                    bill = new Bill(-trackerP.getCost()+ diagRepBooking.getBillAmount(), diagRepBooking.getBillSettled());
                }
                diagRepBooking.setBill(bill);
                bookingSystem.commitBooking(diagRepBooking);
                specRepairSystem.deleteByRepIDP(spcRepID);
                clearBookingFields();
                clearFields();
                findSRCBookings();
            }
            else
            {
               showAlert("Please select the correct type of booking");
            }

        }

    }

    @FXML
    @SuppressWarnings("Duplicates")
    private boolean deleteConfirmation(String message)
    {
        Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
        deleteAlert.setTitle("Delete Booking");
        deleteAlert.setHeaderText(message);
        deleteAlert.showAndWait();
        return deleteAlert.getResult() == ButtonType.OK;

    }

    @FXML
    private ObservableList<SpecRepBooking> specRepBookingObservableList = FXCollections.observableArrayList();

    @SuppressWarnings("Duplicates")
    private  <E> void displaySpecRepBookings (List<E> specRepBookings) {
        try {
            specialistBookings.getItems().clear();
            if (specRepBookings.get(0) instanceof SpecRepBooking) {
                if (specRepBookings.get(0) instanceof SpecRepBooking) {
                    for (E s : specRepBookings) {
                        SpecRepBooking specRepBooking = (SpecRepBooking) s;
                        specRepBookingObservableList.add(specRepBooking);
                    }
                }
                itemID.setCellValueFactory(p -> {
                    if (p.getValue() instanceof VehicleRepair) {
                        VehicleRepair vehicleRepair = (VehicleRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(vehicleRepair.getVehicleRegNumber());
                    } else {
                        PartRepair partRepair = (PartRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(Integer.toString(partRepair.getPartOccurrenceID()));
                    }

                });
                itemID.setCellFactory(TextFieldTableCell.forTableColumn());
                deliveryDateOfBooking.setCellValueFactory(p -> {
                    if (p.getValue() instanceof VehicleRepair) {
                        VehicleRepair vehicleRepair = (VehicleRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(vehicleRepair.getDeliveryDate());
                    } else {
                        PartRepair partRepair = (PartRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(partRepair.getDeliveryDate());
                    }

                });
                deliveryDateOfBooking.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
                returnDateOfBooking.setCellValueFactory(p -> {
                    if (p.getValue() instanceof VehicleRepair) {
                        VehicleRepair vehicleRepair = (VehicleRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(vehicleRepair.getReturnDate());
                    } else {
                        PartRepair partRepair = (PartRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(partRepair.getReturnDate());
                    }

                });
                returnDateOfBooking.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
                costOfBooking.setCellValueFactory(p -> {
                    if (p.getValue() instanceof VehicleRepair) {
                        VehicleRepair vehicleRepair = (VehicleRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(vehicleRepair.getCost());
                    } else {
                        PartRepair partRepair = (PartRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(partRepair.getCost());
                    }

                });
                costOfBooking.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
                spcIDOfBooking.setCellValueFactory(p -> {
                    if (p.getValue() instanceof VehicleRepair) {
                        VehicleRepair vehicleRepair = (VehicleRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(vehicleRepair.getSpcID());
                    } else {
                        PartRepair partRepair = (PartRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(partRepair.getspcID());
                    }

                });
                spcIDOfBooking.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                bookingIDOfBooking.setCellValueFactory(p -> {
                    if (p.getValue() instanceof VehicleRepair) {
                        VehicleRepair vehicleRepair = (VehicleRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(vehicleRepair.getBookingID());
                    } else {
                        PartRepair partRepair = (PartRepair) p.getValue();
                        return new ReadOnlyObjectWrapper<>(partRepair.getBookingID());
                    }

                });
                bookingIDOfBooking.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                specialistBookings.setItems(specRepBookingObservableList);

            }
        }
        catch (IndexOutOfBoundsException e)
        {
            System.out.println("No Bookings found");
        }
    }

    @FXML
    private TableView<Installation> Installations;
    @FXML
    private TableColumn<Installation, Date> instDate, warrDate;
    @FXML
    private TableColumn<Installation, Integer> partOccID;
    @FXML
    private TableColumn<Installation, String> VehicleReg;
    @FXML
    private ObservableList<Installation> installationObservableList = FXCollections.observableArrayList();
    @FXML
    private Button hideInstalls, addInsta, editInsta, deleteInsta,allowUpdate,clearPartsFields;

    @FXML
    private TextField instaVReg;
    @FXML
    private Label instaAbsID_lbl, instaOccID_lbl, instaVReg_lbl,instaDate_lbl, wEndDate_lbl = new Label();


    public void showInstallations()
    {
        if(specialistBookings.getSelectionModel().getSelectedItem() instanceof VehicleRepair) {
            VehicleRepair vehicleRepair = (VehicleRepair) specialistBookings.getSelectionModel().getSelectedItem();
            Vehicle vehicle = VehicleSys.getInstance().searchAVehicle(vehicleRepair.getVehicleRegNumber());
            Installations.setVisible(true);
            hideInstalls.setVisible(true);
            List<Installation> installations = specRepairSystem.getVehicleInstallations(vehicle.getVehicleRegNumber());
            displayInstallations(installations);
            addInsta.setVisible(true);
            editInsta.setVisible(true);
            deleteInsta.setVisible(true);
            instaDate.setVisible(true);
            wEndDate.setVisible(true);
            partDes.setVisible(true);
            partSerial.setVisible(true);
            instaVReg.setVisible(true);
            instaAbsID_lbl.setVisible(true);
            instaDate_lbl.setVisible(true);
            instaOccID_lbl.setVisible(true);
            instaVReg_lbl.setVisible(true);
            wEndDate_lbl.setVisible(true);
            clearPartsFields.setVisible(true);
        }
        else
        {
            showAlert("Selected item is not a vehicle!");
            hideInstallations();
        }

    }

    public void clearFields()
    {
        instaDate.setValue(null);
        wEndDate.setValue(null);
        partDes.getItems().clear();
        setPartDes();
        partSerial.getItems().clear();
        instaVReg.clear();
    }




    private void displayInstallations(List<Installation> installations) {
        Installations.getItems().clear();
        installationObservableList.addAll(installations);
        VehicleReg.setCellValueFactory(new PropertyValueFactory<>("vehicleRegNumber"));
        VehicleReg.setCellFactory(TextFieldTableCell.forTableColumn());
        instDate.setCellValueFactory(new PropertyValueFactory<>("installationDate"));
        instDate.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
        warrDate.setCellValueFactory(new PropertyValueFactory<>("endWarrantyDate"));
        warrDate.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
        partOccID.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Installation, Integer>,
                ObservableValue<Integer>>() {
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Installation, Integer> p) {
                return new ReadOnlyObjectWrapper<>(p.getValue().getPartOccurrence().getPartOccurrenceID());
            }
        });
        partOccID.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        Installations.setItems(installationObservableList);
    }

    @FXML
    public void showInstallationDetails()
    {
        try {
            Installation installation = Installations.getSelectionModel().getSelectedItem();
            PartOccurrence partOccurrence = partsSystem.getByInstallationID(installation.getInstallationID());
            PartAbstraction partAbstraction = partsSystem.getPartbyID(partOccurrence.getPartAbstractionID());
            instaVReg.setText(installation.getVehicleRegNumber());
            partDes.setValue(partAbstraction.getPartName());
            installationID = installation.getInstallationID();
            partSerial.setValue(Integer.toString(installation.getPartOccurrence().getPartOccurrenceID()));
            java.time.LocalDate installationDate = installation.getInstallationDate().toLocalDate();
            java.time.LocalDate returnDate = installation.getEndWarrantyDate().toLocalDate();
            instaDate.setValue(installationDate);
            wEndDate.setValue(returnDate);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    public void hideInstallations()
    {
        Installations.setVisible(false);
        hideInstalls.setVisible(false);
        addInsta.setVisible(false);
        editInsta.setVisible(false);
        deleteInsta.setVisible(false);
        instaDate.setVisible(false);
        wEndDate.setVisible(false);
        instaVReg.setVisible(false);
        instaDate.setValue(null);
        wEndDate.setValue(null);
        instaVReg.clear();
        instaAbsID_lbl.setVisible(false);
        instaDate_lbl.setVisible(false);
        instaOccID_lbl.setVisible(false);
        instaVReg_lbl.setVisible(false);
        wEndDate_lbl.setVisible(false);
        clearPartsFields.setVisible(false);
        partDes.setVisible(false);
        partSerial.setVisible(false);
        clearFields();
    }



    private boolean isBefore(LocalDate localDate)
    {
        LocalDate now = LocalDate.now();
        return !localDate.isBefore(now);
    }

    private java.time.LocalDate toLocalDate(Date date)
    {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = date.toInstant();
        return instant.atZone(zoneId).toLocalDate();
    }

    private Date fromLocalDate(java.time.LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    public void addInstallation()
    {
        try {
            Vehicle vehicle = VehicleSys.getInstance().searchAVehicle(instaVReg.getText());
            if(vehicle == null)
            {
                throw new Exception();
            }
            PartOccurrence partOccurrence = specRepairSystem.getPartOcc(Integer.parseInt(partSerial.getSelectionModel().getSelectedItem().trim()));
            Character c = partDes.getSelectionModel().getSelectedItem().trim().charAt(0);
            int partAbs = c.getNumericValue(c);
            Installation installation = new Installation(ZonedDateTime.of(instaDate.getValue(), LocalTime.now(), ZoneId.systemDefault()), ZonedDateTime.of(wEndDate.getValue(), LocalTime.now(), ZoneId.systemDefault()), instaVReg.getText(), partAbs, partOccurrence);
            specRepairSystem.commitInstallations(installation);
            showAlert("Installation added");
            List<Installation> installations = specRepairSystem.getVehicleInstallations(installation.getVehicleRegNumber());
            displayInstallations(installations);
        }
        catch (Exception e)
        {
            showAlert("Please enter a valid Vehicle registration.");
        }

    }

    public void editInstallation()
    {

        cancelEdit.setVisible(true);
        allowUpdate.setVisible(true);

    }

    public void updateInstallation() {
        Installation installation = specRepairSystem.getByInstallationID(installationID);
        installation.setVehicleRegNumber(instaVReg.getText());
        Character c = partDes.getSelectionModel().getSelectedItem().charAt(0);
        int abs = c.getNumericValue(c);
        installation.setPartAbstractionID(abs);
        PartOccurrence partOccurrence = specRepairSystem.getPartOcc(Integer.parseInt(partSerial.getSelectionModel().getSelectedItem().trim()));
        installation.setPartOccurrence(partOccurrence);
        wEndDate.setValue(LocalDate.now());
        installation.setEndWarrantyDate(ZonedDateTime.of(instaDate.getValue(), LocalTime.now(), ZoneId.systemDefault()));
        installation.setInstallationDate(ZonedDateTime.of(wEndDate.getValue(), LocalTime.now(), ZoneId.systemDefault()));
        specRepairSystem.commitInstallations(installation);
        showAlert("Installation updated");
    }

    public void cancelUpdate()
    {
        cancelEdit.setVisible(false);
        allowUpdate.setVisible(false);
    }

    public void deleteInstallation()
    {
        try {
            Installation installation = specRepairSystem.getByInstallationID(installationID);
            if (deleteConfirmation("Are you sure you want to delete this installation?")) {
                List<Installation> installations = specRepairSystem.getVehicleInstallations(installation.getVehicleRegNumber());
                specRepairSystem.deleteInstallation(installation.getInstallationID());
                findSRCBookings();
                displayInstallations(installations);
            }
        }
        catch (NullPointerException e)
        {
            showAlert("Unable to delete...");
        }
    }

    @FXML
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Message");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

}
