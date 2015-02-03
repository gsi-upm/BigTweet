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

package bigtweet.model.agents;

import bigtweet.BTSim;
import bigtweet.model.EvaluationTools;
import bigtweet.model.SpreadModelM1;
import bigtweet.model.SpreadModelM1.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Agent for monitorizing state of simulation and finishing it. The monitor get
 * a copy of the states for each agent in the previous step (because in its
 * exectuion, these states may change for the current step)
 *
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class MonitorAgent implements Steppable {

    private static final Logger LOG = Logger.getLogger(MonitorAgent.class.getName());
    /**
     * list where position x stores the states of agents in step x
     */
    private static List<Map<String, Integer>> historyOfStates;
    private BTSim bt;
    
    private final int stopIfNoChangeInStatesDuringTheseSteps=new Integer(BTSim.getProperty("stopIfNoChangeInStatesDuringTheseSteps"));
    /**
     * Schedule monitor agent in last sim
     */
    public MonitorAgent(BTSim bt) {
        historyOfStates = new ArrayList<>();

        this.bt = bt;
        bt.schedule.scheduleRepeating(this);
        
        

    }

    public static List<Map<String, Integer>> getHistoryOfStates() {
        return historyOfStates;
    }

    /**
     * Print info, finished simulation, etc
     *
     * @param ss
     */
    @Override
    public void step(SimState simstate) {
        //System.out.println(bt.getSpreadModel().getSeedNetwork() + " " + bt.seed());
        
        //the last states are shown
        int step = (int) bt.schedule.getSteps();


        if (step > 0) {
            bt.getSpreadModel().recordAgentStateChange(null, null);//just to update last states in case there is no change calling record            
            historyOfStates.add(new HashMap(bt.getSpreadModel().getStatesStoredInPreviousStep()));
            LOG.fine("Step " + (step - 1) + " " + bt.getSpreadModel().getAgentsPerState());
           
        }

        if (isFinished()) {//finish and show last state                     
            bt.finish();
            LOG.fine("Step " + step + " " + bt.getSpreadModel().getAgentsPerState());
            historyOfStates.add(new HashMap(bt.getSpreadModel().getAgentsPerState()));
            EvaluationTools.compareWithRealData(false, bt.getNumUsers(), (int) bt.seed());    
            EvaluationTools.showNumberOfStates(false, bt.getNumUsers(), (int) bt.seed());          
        }



    }

    /**
     * Check several conditions to finish simulation
     *
     * @return
     */
    private boolean isFinished() {
        /*No infected agents*/
        boolean c1 = bt.getSpreadModel().getAgentsWithState(State.INFECTED.toString()) == 0;
        if (c1) {
            return true;
        }

        /*Repeated states for x steps*/        
        boolean c2 =  getEqualStatesInHistory()>stopIfNoChangeInStatesDuringTheseSteps;
        if (c2) {
            return true;
        }

        /*No condition to finished*/
        return false;
    }

    
    /**
     * Counts states objects in the history to check the number of last steps with the same state objects
     * @return 
     */
    protected int getEqualStatesInHistory() {

        int equalStatescounted = 0;
        ListIterator<Map<String, Integer>> it = historyOfStates.listIterator(historyOfStates.size());
        Map<String, Integer> states1 = null;       
        while (it.hasPrevious()) {
            Map<String, Integer> states2 = it.previous();
            
            if (states2.equals(states1)) {
                equalStatescounted++;
              
            } else {
                states1=states2;
            }
        }
        return equalStatescounted;
    }
}
