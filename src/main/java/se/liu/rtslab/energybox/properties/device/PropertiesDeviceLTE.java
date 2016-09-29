package energybox.properties.device;

import java.util.Properties;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * @author Gunnar Berg
 * Linkoping University
 */

public class PropertiesDeviceLTE extends Device
{
    public SimpleDoubleProperty POWER_IN_IDLE = new SimpleDoubleProperty(0.5917);
    public SimpleDoubleProperty POWER_IN_LONGDRX = new SimpleDoubleProperty(0.7000);
    public SimpleDoubleProperty POWER_IN_SHORTDRX = new SimpleDoubleProperty(0.8861);
    public SimpleDoubleProperty POWER_IN_ACTIVE = new SimpleDoubleProperty(1.1227);
    
    public PropertiesDeviceLTE(Properties properties)
    {
        POWER_IN_IDLE = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("POWER_IN_IDLE")));
        POWER_IN_LONGDRX = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("POWER_IN_LONGDRX")));
        POWER_IN_SHORTDRX = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("POWER_IN_SHORTDRX")));
        POWER_IN_ACTIVE= new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("POWER_IN_ACTIVE")));
    }
    
    // GETTERS
    public double getPOWER_IN_IDLE() {return POWER_IN_IDLE.get();}
    public double getPOWER_IN_LONGDRX() {return POWER_IN_LONGDRX.get();}
    public double getPOWER_IN_SHORTDRX() {return POWER_IN_SHORTDRX.get();} 
    public double getPOWER_IN_ACTIVE() {return POWER_IN_ACTIVE.get();}
    
    // SETTERS
    public void setPOWER_IN_IDLE(double fName) {POWER_IN_IDLE.set(fName);}
    public void setPOWER_IN_LONGDRX(double fName) {POWER_IN_LONGDRX.set(fName);}
    public void setPOWER_IN_SHORTDRX(double fName) {POWER_IN_SHORTDRX.set(fName);}
    public void setPOWER_IN_ACTIVE(double fName) {POWER_IN_ACTIVE.set(fName);}
}