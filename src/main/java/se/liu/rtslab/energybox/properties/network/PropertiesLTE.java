package energybox.properties.network;

import java.util.Properties;
import javafx.beans.property.SimpleDoubleProperty;
/**
 * @author Gunnar Berg
 * Linkoping University
 */
public class PropertiesLTE extends Network
{ 
    // The default values are taken from the tele2 configuration
    public SimpleDoubleProperty ACTIVE_IDLE_INACTIVITY_TIME = new SimpleDoubleProperty(15.0);
    public SimpleDoubleProperty ACTIVE_SHORTDRX_INACTIVITY_TIME = new SimpleDoubleProperty(5.0);
    public SimpleDoubleProperty SHORTDRX_LONGDRX_INACTIVITY_TIME = new SimpleDoubleProperty(10.0);
    //public SimpleDoubleProperty DATA_THRESHOLD = new SimpleDoubleProperty(28000);

    public String TYPE = "LTE";

    public SimpleDoubleProperty IDLE_TO_ACTIVE_TRANSITION_TIME = new SimpleDoubleProperty(0.2);
    public SimpleDoubleProperty SHORTDRX_TO_ACTIVE_TRANSITION_TIME = new SimpleDoubleProperty(0.2);
    public SimpleDoubleProperty LONGDRX_TO_ACTIVE_TRANSITION_TIME = new SimpleDoubleProperty(0.2);
    public SimpleDoubleProperty ACTIVE_TO_SHORTDRX_TRANSITION_TIME = new SimpleDoubleProperty(0.15);
    public SimpleDoubleProperty SHORTDRX_TO_IDLE_TRANSITION_TIME = new SimpleDoubleProperty(0.2);
    public SimpleDoubleProperty ACTIVE_TO_IDLE_TRANSITION_TIME = new SimpleDoubleProperty(0.2);
  
    public PropertiesLTE(Properties properties)
    {
            ACTIVE_IDLE_INACTIVITY_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("ACTIVE_IDLE_INACTIVITY_TIME"))*1000);
            ACTIVE_SHORTDRX_INACTIVITY_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("ACTIVE_SHORTDRX_INACTIVITY_TIME"))*1000);
            SHORTDRX_LONGDRX_INACTIVITY_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("SHORTDRX_LONGDRX_INACTIVITY_TIME"))*1000);
            //DATA_THRESHOLD = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("DATA_THRESHOLD")));
            TYPE = properties.getProperty("TYPE");
     
            IDLE_TO_ACTIVE_TRANSITION_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("IDLE_TO_ACTIVE_TRANSITION_TIME"))*1000);
            ACTIVE_TO_SHORTDRX_TRANSITION_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("ACTIVE_TO_SHORTDRX_TRANSITION_TIME"))*1000);
            SHORTDRX_TO_IDLE_TRANSITION_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("SHORTDRX_TO_IDLE_TRANSITION_TIME"))*1000);
            SHORTDRX_TO_ACTIVE_TRANSITION_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("SHORTDRX_TO_ACTIVE_TRANSITION_TIME"))*1000);
            LONGDRX_TO_ACTIVE_TRANSITION_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("LONGDRX_TO_ACTIVE_TRANSITION_TIME"))*1000);
            ACTIVE_TO_IDLE_TRANSITION_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("ACTIVE_TO_IDLE_TRANSITION_TIME"))*1000);
            //FACH_TO_IDLE_TRANSITION_TIME = new SimpleDoubleProperty(Double.parseDouble(properties.getProperty("FACH_TO_IDLE_TRANSITION_TIME"))*1000);*/
    }
    
    // GETTERS
    public double getACTIVE_IDLE_INACTIVITY_TIME() {return ACTIVE_IDLE_INACTIVITY_TIME.get();}
    public double getACTIVE_SHORTDRX_INACTIVITY_TIME() {return ACTIVE_SHORTDRX_INACTIVITY_TIME.get();} 
    public double getSHORTDRX_LONGDRX_INACTIVITY_TIME() {return SHORTDRX_LONGDRX_INACTIVITY_TIME.get();} 
    //public double getDATA_THRESHOLD() {return DATA_THRESHOLD.get();} 
    
    public String getTYPE() {return TYPE;}
    
    public double getIDLE_TO_ACTIVE_TRANSITION_TIME() {return IDLE_TO_ACTIVE_TRANSITION_TIME.get();}
    public double getACTIVE_TO_SHORTDRX_TRANSITION_TIME() {return ACTIVE_TO_SHORTDRX_TRANSITION_TIME.get();}
    public double getSHORTDRX_TO_IDLE_TRANSITION_TIME() {return SHORTDRX_TO_IDLE_TRANSITION_TIME.get();}
    public double getACTIVE_TO_IDLE_TRANSITION_TIME() {return ACTIVE_TO_IDLE_TRANSITION_TIME.get();}
    public double getSHORTDRX_TO_ACTIVE_TRANSITION_TIME() {return SHORTDRX_TO_ACTIVE_TRANSITION_TIME.get();}
    public double getLONGDRX_TO_ACTIVE_TRANSITION_TIME() {return LONGDRX_TO_ACTIVE_TRANSITION_TIME.get();}
    
    // SETTERS
    public void setACTIVE_IDLE_INACTIVITY_TIME(double fName) {ACTIVE_IDLE_INACTIVITY_TIME.set(fName);}
    public void setACTIVE_SHORTDRX_INACTIVITY_TIME(double fName) {ACTIVE_SHORTDRX_INACTIVITY_TIME.set(fName);}
    public void setSHORTDRX_LONGDRX_INACTIVITY_TIME(double fName) {SHORTDRX_LONGDRX_INACTIVITY_TIME.set(fName);} 
    //public void setDATA_THRESHOLD(double fName) {DATA_THRESHOLD.set(fName);}
    
    public void setIDLE_TO_ACTIVE_TRANSITION_TIME(double fName) {IDLE_TO_ACTIVE_TRANSITION_TIME.set(fName);}
    public void setACTIVE_TO_SHORTDRX_TRANSITION_TIME(double fName) {ACTIVE_TO_SHORTDRX_TRANSITION_TIME.set(fName);}
    public void setSHORTDRX_TO_IDLE_TRANSITION_TIME(double fName) {SHORTDRX_TO_IDLE_TRANSITION_TIME.set(fName);}
    public void setACTIVE_TO_IDLE_TRANSITION_TIME(double fName) {ACTIVE_TO_IDLE_TRANSITION_TIME.set(fName);}
    public void setSHORTDRX_TO_ACTIVE_TRANSITION_TIME(double fName) {SHORTDRX_TO_ACTIVE_TRANSITION_TIME.set(fName);}
    public void setLONGDRX_TO_ACTIVE_TRANSITION_TIME(double fName) {LONGDRX_TO_ACTIVE_TRANSITION_TIME.set(fName);}
}
