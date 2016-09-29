package energybox.engines;

import energybox.Packet;
import energybox.StatisticsEntry;
import energybox.properties.device.PropertiesDeviceLTE;
import energybox.properties.network.PropertiesLTE;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
/**
 * @author Gunnar Berg
 * Linkoping University
 */
public class EngineLTE extends Engine
{
    enum State 
    { 
        IDLE(0), LONGDRX(1), SHORTDRX(2), ACTIVE(3);
        private final int value;
        private State(int value){this.value = value;}
        public int getValue() { return this.value; }
    }
    
    // VARIABLES TAKEN FROM THE CONSTRUCTOR
    PropertiesLTE networkProperties; 
    PropertiesDeviceLTE deviceProperties;
    
    // CHART VARIABLES
    XYChart.Series<Long, Integer> ShortDrxSeries = new XYChart.Series();
    XYChart.Series<Long, Integer> LongDrxSeries = new XYChart.Series();
    XYChart.Series<Long, Integer> ActiveSeries = new XYChart.Series();
    
    // MAIN CONSTRUCTOR
    public EngineLTE(ObservableList<Packet> packetList,
            String sourceIP,
            PropertiesLTE networkProperties, 
            PropertiesDeviceLTE deviceProperties)
    {
        this.packetList = packetList;
        this.networkProperties = networkProperties;
        this.deviceProperties = deviceProperties;
        this.packetList = sortUplinkDownlink(packetList, sourceIP);
        this.sourceIP = sourceIP;
    }
    
    @Override
    public XYChart.Series<Double, Integer> modelStates()
    {
        // Buffer control variables
        int totalSize = 0;
        // Timer variables
        long deltaDownlink = 0, 
                deltaUplink = 0, 
                deltaT = 0,
                timeOfFirstPacket = 0,
                previousTimeUplink =  packetList.get(0).getTimeInMicros(), 
                previousTimeDownlink = packetList.get(0).getTimeInMicros(), 
                previousTime = packetList.get(0).getTimeInMicros(), // might wanna replace the variable with packetList.get(i-1).getTime()
                timeToEmptyUplink = 0; // timeToEmptyDownlink is a constant : networkProperties.getDOWNLINK_BUFFER_EMPTY_TIME; 
        State state = State.IDLE; // State enumeration

        stateSeriesData.beforeChanges();
        // Packet list points
        for (int i = 0; i < packetList.size(); i++) 
        {
            // Populating the packetChart series
            // to temporary data structure, so too many events are not sent
            // to observers. Very bad for performance!
            // Actually update the Chart data using updatePacketChart() later when 
            // all packets have been added.
            packetChartEntry(packetList.get(i));
            
            if(i == 0)
                timeOfFirstPacket = packetList.get(i).getTimeInMicros();
            // Update deltas and previous times (uplink and downlink seperately
            // for buffer calculations)
            deltaT = packetList.get(i).getTimeInMicros() - previousTime;
            
            if (packetList.get(i).getUplink())
                deltaUplink = packetList.get(i).getTimeInMicros() - previousTimeUplink;
            else
                deltaDownlink = packetList.get(i).getTimeInMicros() - previousTimeDownlink;
            
           /* System.out.println("packet time1: " + packetList.get(i).getTimeInMicros() + "   " +
                               "state1: " + state + "   " +
                               "stateValue: " + state.getValue()+ "   " +
                               "deltaT1: " + deltaT +  "\n");*/

            // DEMOTIONS
            if(networkProperties.getTYPE() == "LTE")
            {    
                switch (state)
                {   
                    case ACTIVE:
                    {
                        if (deltaT > networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                        {
                            System.out.println("DEMOTE active to idle");
                            ActiveToShortDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            state = State.SHORTDRX;
                            drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());

                            ShortDrxToLongDrx(previousTime + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                            state = State.LONGDRX;
                            drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());

                            LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            state = State.IDLE;
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                        }
                        else if (deltaT > networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME())
                        {   
                            System.out.println("DEMOTE active to short");
                            ActiveToShortDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            state = State.SHORTDRX;
                            drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                        }

                    }
                    break;

                    case SHORTDRX:
                    {
                        if (deltaT > networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                        {
                            System.out.println("DEMOTE short to idle");
                            //packetList.get(i).getTimeInMicros() - timeOfFirstPacket
                            ShortDrxToLongDrx(previousTime + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                            state = State.LONGDRX;
                            drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());

                            LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            state = State.IDLE;
                            timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                        }
                        else if (deltaT > networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME())
                        {
                            System.out.println("DEMOTE short to long");
                            ShortDrxToLongDrx(previousTime + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                            state = State.LONGDRX;
                            drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                        }
                    }
                    break;

                    case LONGDRX:
                    {
                        if (deltaT > networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                        {
                            System.out.println("DEMOTE long to idle");
                            LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            state = State.IDLE;
                            timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                        }
                    }
                    break;
                }

                // PROMOTIONS
                System.out.println("packet time2: " + packetList.get(i).getTimeInMicros() + "   " +
                                   "state2: " + state + "   " + 
                                   "deltaT2: " + deltaT +  "\n");
                switch (state)
                {
                    case IDLE:
                    {
                        // Uplink packets
                        if (packetList.get(i).getUplink())
                        {
                            // If the packet is larger than 
                                // Bug correction for when the trace is not realistic.
                                // Ignores the the transition time if there are packets
                                // before the transition is suppose to end.
                                //if (packetList.get(i+1).getTimeInMicros() > packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_DCH_TRANSITION_TIME())
                                if ((packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME()) < (double)packetList.get(i+1).getTimeInMicros())
                                {
                                    System.out.println("PROMOTE idle to active1");
                                    System.out.println("Next packet at : " + (double)packetList.get(i+1).getTimeInMicros());
                                    System.out.println("Promotion at: " + (packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME()));
                                    idleToActive((double)packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME());
                                    drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME(), state.getValue());                            
                                    state = State.ACTIVE;
                                    drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME(), state.getValue());
                                }
                                else
                                {
                                    System.out.println("PROMOTE idle to active2");
                                    idleToActive((double)packetList.get(i).getTimeInMicros());
                                    drawState(packetList.get(i).getTimeInMicros(), state.getValue());                            
                                    state = State.ACTIVE;
                                    drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                                }
                        }
                        // Downlink packets
                        else
                        {
                            System.out.println("PROMOTE idle to active3");
                            idleToActive((double)packetList.get(i).getTimeInMicros());// + networkProperties.getIDLE_TO_DCH_TRANSITION_TIME());
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());// + (long)networkProperties.getIDLE_TO_DCH_TRANSITION_TIME(), state.getValue());
                            state = State.ACTIVE;
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());// + (long)networkProperties.getIDLE_TO_DCH_TRANSITION_TIME(), state.getValue());
                        }
                    }
                    break;

                    case SHORTDRX:
                    {
                        if (packetList.get(i).getUplink())
                        {
                            if ((packetList.get(i).getTimeInMicros() + networkProperties.getSHORTDRX_TO_ACTIVE_TRANSITION_TIME()) < (double)packetList.get(i+1).getTimeInMicros())
                            {
                                System.out.println("PROMOTE short to active1");
                                System.out.println("Next packet at : " + (double)packetList.get(i+1).getTimeInMicros());
                                System.out.println("Promotion at: " + (packetList.get(i).getTimeInMicros() + networkProperties.getSHORTDRX_TO_ACTIVE_TRANSITION_TIME()));
                                ShortDrxToActive((double)packetList.get(i).getTimeInMicros() + networkProperties.getSHORTDRX_TO_ACTIVE_TRANSITION_TIME());
                                drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getSHORTDRX_TO_ACTIVE_TRANSITION_TIME(), state.getValue());                            
                                state = State.ACTIVE;
                                drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getSHORTDRX_TO_ACTIVE_TRANSITION_TIME(), state.getValue());
                            }
                            else
                            {
                                System.out.println("PROMOTE short to active2");
                                ShortDrxToActive((double)packetList.get(i).getTimeInMicros());
                                drawState(packetList.get(i).getTimeInMicros(), state.getValue());                            
                                state = State.ACTIVE;
                                drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                            }
                        }
                        else
                        {
                            System.out.println("PROMOTE short to active3");
                            ShortDrxToActive((double)packetList.get(i).getTimeInMicros());
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());                            
                            state = State.ACTIVE;
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                        }
                    }
                    break;

                    case LONGDRX:
                    {
                         if (packetList.get(i).getUplink())
                        {
                            if ((packetList.get(i).getTimeInMicros() + networkProperties.getLONGDRX_TO_ACTIVE_TRANSITION_TIME()) < (double)packetList.get(i+1).getTimeInMicros())
                            {
                                System.out.println("PROMOTE long to active1");
                                System.out.println("Next packet at : " + (double)packetList.get(i+1).getTimeInMicros());
                                System.out.println("Promotion at: " + (packetList.get(i).getTimeInMicros() + networkProperties.getLONGDRX_TO_ACTIVE_TRANSITION_TIME()));
                                LongDrxToActive((double)packetList.get(i).getTimeInMicros() + networkProperties.getLONGDRX_TO_ACTIVE_TRANSITION_TIME());
                                drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getLONGDRX_TO_ACTIVE_TRANSITION_TIME(), state.getValue());                            
                                state = State.ACTIVE;
                                drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getLONGDRX_TO_ACTIVE_TRANSITION_TIME(), state.getValue());
                            }
                            else
                            {
                                System.out.println("PROMOTE long to active2");
                                LongDrxToActive((double)packetList.get(i).getTimeInMicros());
                                drawState(packetList.get(i).getTimeInMicros(), state.getValue());                            
                                state = State.ACTIVE;
                                drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                            }
                        }
                        else
                        {
                            System.out.println("PROMOTE long to active3");
                            LongDrxToActive((double)packetList.get(i).getTimeInMicros());
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());                            
                            state = State.ACTIVE;
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                        }
                    }
                    break;

                    case ACTIVE:
                    {
                        drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                    }
                    break;
                }
            }
            else //LTE-tele2 config
            {
                switch (state)
                {   
                    case ACTIVE:
                    {
                        boolean toLong = false;
                        if(timeOfFirstPacket + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME() + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME() < packetList.get(i-1).getTimeInMicros()){
                            toLong = true;
                        }
                        if (deltaT > networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                        {
                            //System.out.println("DEMOTE active to idle");
                            if(toLong){
                                ActiveToLongDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.LONGDRX;
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            }
                            else{
                                ActiveToShortDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.SHORTDRX;
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            }
                            
                            
                            if(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME() < previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                            {
                                if(!toLong){
                                    ShortDrxToLongDrx(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                                    drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                                    state = State.LONGDRX;
                                    drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                                }
                                
                                LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                                state = State.IDLE;
                                totalSize = 0;
                                timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            }
                            else
                            {
                                ShortDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                                state = State.IDLE;
                                totalSize = 0;
                                timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            }
                        }
                        else if (packetList.get(i).getTimeInMicros() - timeOfFirstPacket > networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME())
                        {
                            if(toLong){
                                ActiveToLongDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.LONGDRX;
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            }else{
                                ActiveToShortDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.SHORTDRX;
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());

                                ShortDrxToLongDrx(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                                drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.LONGDRX;
                                drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                            }
                        }
                        else if (deltaT > networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME())
                        {
                            if(toLong){
                                ActiveToLongDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.LONGDRX;
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            }else{
                                ActiveToShortDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.SHORTDRX;
                                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            }
                        }

                    }
                    break;

                    case SHORTDRX:
                    {
                        if (deltaT > networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                        {
                           // System.out.println("DEMOTE short to idle");
                            if(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME() < previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                            {
                                ShortDrxToLongDrx(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                                drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                                state = State.LONGDRX;
                                drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                                
                                LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                                state = State.IDLE;
                                totalSize = 0;
                                timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            }
                            else
                            {    
                                ShortDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                                state = State.IDLE;
                                totalSize = 0;
                                timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            }
                        }
                        else if(packetList.get(i).getTimeInMicros() - timeOfFirstPacket > networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME())
                        {
                            ShortDrxToLongDrx(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                            drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                            state = State.LONGDRX;
                            drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                        }
                    }
                    break;

                    case LONGDRX:
                    {
                        if (deltaT > networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                        {
                            //System.out.println("DEMOTE long to idle");
                            LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                            state = State.IDLE;
                            totalSize = 0;
                            timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                            drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                        }
                    }
                    break;
                }

                // PROMOTIONS
               /* System.out.println("packet time2: " + packetList.get(i).getTimeInMicros() + "   " +
                                   "state2: " + state + "   " + 
                                   "stateValue: " + state.getValue() + "   " +
                                   "deltaT2: " + deltaT +  "\n");*/
                switch (state)
                {
                    case IDLE:
                    {
                        timeOfFirstPacket = packetList.get(i).getTimeInMicros();
                        // Uplink packets
                        if (packetList.get(i).getUplink())
                        {
                            // If the packet is larger than 
                                // Bug correction for when the trace is not realistic.
                                // Ignores the the transition time if there are packets
                                // before the transition is suppose to end.
                                //if (packetList.get(i+1).getTimeInMicros() > packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_DCH_TRANSITION_TIME())
                              /*  if ((packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME()) < (double)packetList.get(i+1).getTimeInMicros())
                                {
                                    System.out.println("PROMOTE idle to active1");
                                    System.out.println("Next packet at : " + (double)packetList.get(i+1).getTimeInMicros());
                                    System.out.println("Promotion at: " + (packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME()));
                                    idleToActive((double)packetList.get(i).getTimeInMicros() + networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME());
                                    drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME(), state.getValue());                            
                                    state = State.ACTIVE;
                                    drawState(packetList.get(i).getTimeInMicros() + (long)networkProperties.getIDLE_TO_ACTIVE_TRANSITION_TIME(), state.getValue());
                                }*/
                              // else
                                //{
                                    //System.out.println("PROMOTE idle to active2");
                                    idleToActive((double)packetList.get(i).getTimeInMicros());
                                    drawState(packetList.get(i).getTimeInMicros(), state.getValue());                            
                                    state = State.ACTIVE;
                                    drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                                //}
                        }
                        // Downlink packets
                        else
                        {
                            //System.out.println("PROMOTE idle to active3");
                            idleToActive((double)packetList.get(i).getTimeInMicros());// + networkProperties.getIDLE_TO_DCH_TRANSITION_TIME());
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());// + (long)networkProperties.getIDLE_TO_DCH_TRANSITION_TIME(), state.getValue());
                            state = State.ACTIVE;
                            drawState(packetList.get(i).getTimeInMicros(), state.getValue());// + (long)networkProperties.getIDLE_TO_DCH_TRANSITION_TIME(), state.getValue());
                        }
                    }
                    break;

                    case SHORTDRX:
                    {
                        //drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                        ShortDrxToActive((double)packetList.get(i).getTimeInMicros());
                        drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                        state = State.ACTIVE;
                        drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                    }
                    break;

                    case LONGDRX:
                    {
                       //drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                        LongDrxToActive((double)packetList.get(i).getTimeInMicros());
                        drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                        state = State.ACTIVE;
                        drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                    }
                    break;

                    case ACTIVE:
                    {
                       // drawState(packetList.get(i).getTimeInMicros(), state.getValue());
                    }
                    break;
                }
            }
            // Save timestamps for the next loop
            
            //
            
            totalSize += packetList.get(i).getLength();
            DRXInactivityTimeControl(totalSize);
            
            previousTime = packetList.get(i).getTimeInMicros();            
            if (packetList.get(i).getUplink())            
                previousTimeUplink = packetList.get(i).getTimeInMicros();            
            else            
                previousTimeDownlink = packetList.get(i).getTimeInMicros();
        }
        
        // update charts here for performance reasons
        updatePacketCharts();
        
        // Finish the trace if the final state is FACH or DCH
        if(networkProperties.getTYPE() == "LTE")
        {    
            if (state == State.ACTIVE)
            {
                    ActiveToShortDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                    drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                    state = State.SHORTDRX;
                    drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());

                    ShortDrxToLongDrx(previousTime + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                    drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                    state = State.LONGDRX;
                    drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());

                    LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                    state = State.IDLE;
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());  
            }
            else if (state == State.SHORTDRX)
            {
                ShortDrxToLongDrx(previousTime + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                state = State.LONGDRX;
                drawState(previousTime + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());

                LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                state = State.IDLE;
                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
            }
            else if (state == State.LONGDRX)
            {
                LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                state = State.IDLE;
                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
            }
        }
        else //LTE-tele2 config
        {
            if(state == State.ACTIVE)
            {
                ActiveToShortDrx(previousTime + networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME());
                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                state = State.SHORTDRX;
                drawState(previousTime + (long)networkProperties.getACTIVE_SHORTDRX_INACTIVITY_TIME(), state.getValue());
                            
                if(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME() < previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                {
                    ShortDrxToLongDrx(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                    drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                    state = State.LONGDRX;
                    drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                                
                    LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                    state = State.IDLE;
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                }
                else
                {
                    ShortDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                    state = State.IDLE;
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                }
            }
            else if(state == State.SHORTDRX)
            {
                if(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME() < previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME())
                {
                    ShortDrxToLongDrx(timeOfFirstPacket + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
                    drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                    state = State.LONGDRX;
                    drawState(timeOfFirstPacket + (long)networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME(), state.getValue());
                                
                    LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                    state = State.IDLE;
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                }
                else
                {
                    ShortDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                    state = State.IDLE;
                    drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                }
            }
            else if(state == State.LONGDRX)
            {
                LongDrxToIdle(previousTime + networkProperties.getACTIVE_IDLE_INACTIVITY_TIME());
                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
                state = State.IDLE;
                drawState(previousTime + (long)networkProperties.getACTIVE_IDLE_INACTIVITY_TIME(), state.getValue());
            }
        }

        stateSeriesData.afterChanges();

        linkDistrData.add(new PieChart.Data("Uplink", uplinkPacketCount));
        linkDistrData.add(new PieChart.Data("Downlink", packetList.size()-uplinkPacketCount));
        distrStatisticsList.add(new StatisticsEntry("Nr of UL packets",uplinkPacketCount));
        distrStatisticsList.add(new StatisticsEntry("Nr of DL packets",packetList.size()-uplinkPacketCount));
        return stateSeries;
    }
    
    @Override
    public void calculatePower()
    {
        //Double power = Double.valueOf(0);
        int timeInIDLE = 0, timeInLONGDRX = 0, timeInSHORTDRX = 0, timeInACTIVE = 0;
        for (int i = 1; i < stateSeries.getData().size(); i++)
        {
            double timeDifference = (stateSeries.getData().get(i).getXValue() - stateSeries.getData().get(i-1).getXValue());
            switch(stateSeries.getData().get(i-1).getYValue()) //could be changed for if-statements to use with double in ENGINE.JAVA.DRAWSTATE
            {
                case 0:
                {
                    power += timeDifference * deviceProperties.getPOWER_IN_IDLE();
                    timeInIDLE += timeDifference;
                }
                break;
                    
                case 1:
                {
                    power += timeDifference * deviceProperties.getPOWER_IN_LONGDRX();
                    timeInLONGDRX += timeDifference;
                }
                break;
                
                case 2:
                {
                    power += timeDifference * deviceProperties.getPOWER_IN_SHORTDRX();
                    timeInSHORTDRX += timeDifference;
                }
                break;
                    
                case 3:
                {
                    power += timeDifference * deviceProperties.getPOWER_IN_ACTIVE();
                    timeInACTIVE += timeDifference;
                }
                break;
            }
        }
        
       for (int i = 0; i < packetList.size()-1; ++i)
        {
            int dataRate = 0;
            int dataSize = 0;
            int j = i;
            while(packetList.get(j).getTimeInMicros() < packetList.get(i).getTimeInMicros()+ 100000)
            {
                dataSize += packetList.get(j).getLength();
                ++j;
                if(j == packetList.size()-1)
                    break;
            }
            
            i = j; 
            dataRate = dataSize*10;
            if(dataRate > 1000)
            {
                //System.out.println("Data rate detected:" + dataRate);
                power += 1.1309*0.1;
            }
            //else
                //System.out.println("Almost no data sent  " + dataRate);
                
            /*if(dataRate < 750000 && dataRate > 1000)
            {
                System.out.println("Data rate closest to 0.5 MB/s  " + dataRate);
                power += 2.2356*0.1;
            }
            else if(dataRate >= 750000 && dataRate < 1500000)
            {
                System.out.println("Data rate closest to 1 MB/s  " + dataRate);
                power += 2.5595*0.1;
            }
            else if(dataRate >= 1500000)
            {
                System.out.println("Data rate closest to 2 MB/s  " + dataRate);
                power += 2.6831*0.1;
            }
            else
               System.out.println("Almost no data sent  " + dataRate); */
        }
        
        // Total power used rounded down to four decimal places
        statisticsList.add(new StatisticsEntry("Total Power Used",((double) Math.round(power * 10000) / 10000)));
        stateTimeData.add(new PieChart.Data("SHORTDRX", timeInSHORTDRX));
        stateTimeData.add(new PieChart.Data("LONGDRX", timeInLONGDRX));
        stateTimeData.add(new PieChart.Data("ACTIVE", timeInACTIVE));
        stateTimeData.add(new PieChart.Data("IDLE", timeInIDLE));
    }

    @Override
    public String getName() {
        return "LTE";
    }

    // State transition drawing methods to seperate state series
    private void ActiveToShortDrx(Double time)
    {
        time = time / 1000000;
        ActiveSeries.getData().add(new XYChart.Data(time, State.ACTIVE.getValue()));
        ActiveSeries.getData().add(new XYChart.Data(time, 0));
        
        ShortDrxSeries.getData().add(new XYChart.Data(time, 0));
        ShortDrxSeries.getData().add(new XYChart.Data(time, State.SHORTDRX.getValue()));
    }
    
   /*private void ActiveToIdle(Double time)
    {
        time = time / 1000000;
        ActiveSeries.getData().add(new XYChart.Data(time, State.ACTIVE.getValue()));
        ActiveSeries.getData().add(new XYChart.Data(time , 0));
    }
    */
    private void ShortDrxToIdle(Double time)
    {
        time = time / 1000000;
        ShortDrxSeries.getData().add(new XYChart.Data(time, State.SHORTDRX.getValue()));
        ShortDrxSeries.getData().add(new XYChart.Data(time, 0));
    }
    
    private void ActiveToLongDrx(Double time)
    {
        time = time / 1000000;
        ActiveSeries.getData().add(new XYChart.Data(time, State.ACTIVE.getValue()));
        ActiveSeries.getData().add(new XYChart.Data(time, 0));
        
        LongDrxSeries.getData().add(new XYChart.Data(time, 0));
        LongDrxSeries.getData().add(new XYChart.Data(time, State.LONGDRX.getValue()));
    }
    
    private void ShortDrxToLongDrx(Double time)
    {
        time = time / 1000000;
        ShortDrxSeries.getData().add(new XYChart.Data(time, State.SHORTDRX.getValue()));
        ShortDrxSeries.getData().add(new XYChart.Data(time, 0));
        
        LongDrxSeries.getData().add(new XYChart.Data(time, 0));
        LongDrxSeries.getData().add(new XYChart.Data(time, State.LONGDRX.getValue()));
    }
    
    private void LongDrxToIdle(Double time)
    {
        time = time / 1000000;
        LongDrxSeries.getData().add(new XYChart.Data(time, State.LONGDRX.getValue()));
        LongDrxSeries.getData().add(new XYChart.Data(time, 0));
    }
    
    private void ShortDrxToActive(Double time)
    {
        time = time / 1000000;
        ShortDrxSeries.getData().add(new XYChart.Data(time, State.SHORTDRX.getValue()));
        ShortDrxSeries.getData().add(new XYChart.Data(time, 0));
        
        ActiveSeries.getData().add(new XYChart.Data(time, 0));
        ActiveSeries.getData().add(new XYChart.Data(time, State.ACTIVE.getValue()));
    }
    
    private void LongDrxToActive(Double time)
    {
        time = time / 1000000;
        LongDrxSeries.getData().add(new XYChart.Data(time, State.LONGDRX.getValue()));
        LongDrxSeries.getData().add(new XYChart.Data(time, 0));
        
        ActiveSeries.getData().add(new XYChart.Data(time, 0));
        ActiveSeries.getData().add(new XYChart.Data(time, State.ACTIVE.getValue()));
    }
    
    private void idleToActive(Double time)
    {
        time = time / 1000000;
        ActiveSeries.getData().add(new XYChart.Data(time, 0));
        ActiveSeries.getData().add(new XYChart.Data(time, State.ACTIVE.getValue()));
    }
    
    private void DRXInactivityTimeControl(int size)
    {
        if((13 - size*0.0000013)*1000000 > 0)
            networkProperties.setSHORTDRX_LONGDRX_INACTIVITY_TIME((13 - size*0.0000013)*1000000);
        else
            networkProperties.setSHORTDRX_LONGDRX_INACTIVITY_TIME(1);
        //System.out.println("New inactivity time: " + networkProperties.getSHORTDRX_LONGDRX_INACTIVITY_TIME());
    }
    
    // GETTERS
    public XYChart.Series<Long, Integer> getSHORTDRX(){ return ShortDrxSeries; }
    public XYChart.Series<Long, Integer> getLONGDRX(){ return LongDrxSeries; }
    public XYChart.Series<Long, Integer> getACTIVE(){ return ActiveSeries; }
}
