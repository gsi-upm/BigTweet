
/*
* 
* 
* This file is part of Big Tweet. 
* 
* Big Tweet has been developed by members of the research Group on 
* Intelligent Systems [GSI] (Grupo de Sistemas Inteligentes), 
* acknowledged group by the  Technical University of Madrid [UPM] 
* (Universidad Politécnica de Madrid) 
* 
* Authors:
* Mercedes Garijo
* Carlos A. Iglesias
* Emilio Serrano
* 
* Contact: 
* http://www.gsi.dit.upm.es/;
* 
* 
* 
* Big Tweet is free software: 
* you can redistribute it and/or modify it under the terms of the GNU 
* General Public License as published by the Free Software Foundation, 
* either version 3 of the License, or (at your option) any later version. 
*
* 
* Big Tweet is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Big Tweet. If not, see <http://www.gnu.org/licenses/>
 */


package bigtweet.view;

import bigtweet.BTSim;
import bigtweet.BTSimWithUI;
import bigtweet.model.SpreadModelM1;
import bigtweet.model.SpreadModelM1.State;
import bigtweet.model.agents.MonitorAgent;
import java.awt.GraphicsEnvironment;
import java.util.Locale;
import javax.swing.JFrame;
import org.jfree.data.xy.XYSeries;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.MutableDouble;

/**
 *
 * Generic class to create a chart frame.
 */
public class UsersStatesChart extends GUIState {
    /*El esquema 1 es para articulos*/

    private static int colorScheme = 1;
    private int timestepsToRedraw = 1;
    private Display2D display;
    private JFrame displayFrame;
    private XYSeries series[];    // the data series we'll add to     
    private State states[];
    private sim.util.media.chart.TimeSeriesChartGenerator chart;  // the charting facility
    Thread timer = null;
    private String title = "Users states";
    private String xlabel = "Step";
    private String ylabel = "Users per state";
    private BTSim bt;

    /**
     *
     * @param state
     * @param title Title of ghe chart
     * @param objectinfo Object with information of x and y (methodx and y)
     * @param x Double to be read in chart, don't change this reference! only
     * its value (attribute val)
     * @param y Double to be read in chart, don't change this reference! only
     * its value (attribute val)
     * @param xlabel label for x
     * @param ylabel label for y
     */
    public UsersStatesChart(BTSim bt, Controller c) {
        super(bt);
        this.bt = bt;
        this.init(c);
        this.start();
    }

    public void startTimer(final long milliseconds) {
        if (timer == null) {
            timer = sim.util.gui.Utilities.doLater(milliseconds, new Runnable() {
                @Override
                public void run() {
                    if (chart != null) {
                        chart.update(state.schedule.getSteps(), true);
                    }
                    timer = null;  // reset the timer
                }
            });
        }
    }

    @Override
    public void start() //cuando se pulsa play
    {
        //super.start();


        chart.removeAllSeries();
        states = SpreadModelM1.State.values();
        series = new XYSeries[states.length];
        for (int i = 0; i < series.length; i++) {
            series[i] = new org.jfree.data.xy.XYSeries(states[i].toString(), false);
            chart.addSeries(series[i], null);
        }
        
       /*) state.schedule.scheduleRepeating(new Steppable() {
            @Override
      
        });*/
        

    }
    
    /**
     * Called from step de BTSImWithUI, introducing chart as agent gives considerable changes in results
     * @param state 
     */
    
          public void step(SimState state) {    
                int step = (int) state.schedule.getSteps();
                
             
               
                if (step >= Schedule.EPOCH && step < Schedule.AFTER_SIMULATION) {
                    for (int i = 0; i < series.length; i++) {
                         
                          Integer agents=(bt.getAgentPerState().get(states[i].toString()));
                          int x;
                          if(agents!=null){//check that there were agents for that state
                                x = new Integer(agents);
                          }
                          else{
                              x=0;
                          } 
                              series[i].add(step, x, false);  // don't update automatically                                                
                    }
                }            
                startTimer(1000);  // once a second (1000 milliseconds)
            }

    @Override
    public void init(Controller c) {
        super.init(c);
        chart = new sim.util.media.chart.TimeSeriesChartGenerator();
        chart.setTitle(title);
        chart.setRangeAxisLabel(ylabel);
        chart.setDomainAxisLabel(xlabel);
        JFrame frame = chart.createFrame(this);
        // perhaps you might move the chart to where you like.
        frame.setVisible(false);
        frame.pack();
        frame.setIconImage(BTSimWithUI.getLocoIcon().getImage());
        
        frame.setLocation((int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth() - frame.getWidth(), 0);
  
        
        displayFrame=frame;

        //  display.attach(warningsPortrayal, "Warnings");
    }

    public JFrame getDisplayFrame() {
        return displayFrame;
    }
  

    @Override
    public void quit() {
        super.quit();

        if (displayFrame != null) {
            displayFrame.dispose();
        }
        displayFrame = null;
        display = null;
    }
}
