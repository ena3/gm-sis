package controllers;

import logic.AuthenticationSystem;
import logic.CriterionRepository;
import logic.VehicleSys;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import logic.AuthenticationSystem;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import entities.*;
import persistence.DatabaseRepository;

import java.text.*;

import java.util.Date;

/**
 * Created by DillonVaghela on 2/9/17.
 */
public class VehicleController {


    private VehicleSys vSys = VehicleSys.getInstance();
    public TextField reg;
    public TextField cID;
    public TextField vType;
    public TextField mod;
    public TextField manuf;
    public TextField eSize;
    public TextField fType;
    public TextField col;
    public TextField mil;
    public TextField rDateMot;
    public TextField dLastServiced;
    public TextField cByWarranty;
    public TextField wName;
    public TextField wCompAddress;
    public TextField wExpirationDate;

    public VehicleController()
    {

    }

    public void addVehicle()
    {
        try
        {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date reDateMot = df.parse(rDateMot.getText());
            Date daLastServiced = df.parse(dLastServiced.getText());
            Date waExpirationDate = df.parse(wExpirationDate.getText());
            VehicleType vT;
            if (vType.equals("Car"))
            {
                vT = VehicleType.Car;
            }
            else if (vType.equals("Van"))
            {
                vT = VehicleType.Van;
            }
            else
            {
                vT = VehicleType.Truck;
            }
            FuelType fT;
            if (vType.equals("Diesel"))
            {
                fT = FuelType.diesel;
            }
            else
            {
                fT = FuelType.petrol;
            }
            boolean check = vSys.addVehicle( reg.getText(),Integer.parseInt(cID.getText()), vT,mod.getText(),manuf.getText(), Integer.parseInt(eSize.getText()),fT,col.getText(),Integer.parseInt(mil.getText()), reDateMot,daLastServiced,Boolean.parseBoolean(cByWarranty.getText()),wName.getText(),wCompAddress.getText(),waExpirationDate);
            System.out.println(check);
        }
        catch (Exception e)
        {
            e.printStackTrace(  );
        }
    }
}
