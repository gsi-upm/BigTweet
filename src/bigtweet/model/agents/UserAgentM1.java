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
import bigtweet.model.SpreadModelM1;
import bigtweet.model.SpreadModelM1.State;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import sim.engine.SimState;

/**
 * Agent for SpreadModelM1, see documentation in SpreadModelM1.
 *
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class UserAgentM1 extends UserAgent {

    /**
     * If no beacons are added, a cured model start from a single infected node
     * after timelag
     */
    private static boolean delayedStartModelStartedFlag = false;
    private SpreadModelM1 smM1;

    public UserAgentM1(int nodeId, SpreadModelM1 smM1) {
        super(nodeId, smM1);
        this.smM1 = smM1;
        setState(State.NEUTRAL.toString());
        delayedStartModelStartedFlag = false;

    }

    public void step(SimState simstate) {

        /*Fixed state if previously marked*/
        updateFromNextState();

        /*Infected behavior*/
        if (State.INFECTED.toString().equals(this.getState())) {
            infectBehaviour();
        }
        if (State.BEACONON.toString().equals(this.getState()) || State.CURED.toString().equals(this.getState()) || State.VACCINATED.toString().equals(this.getState())) {
            denierBehaviour();
        }
    }
    private static final Logger LOG = Logger.getLogger(UserAgentM1.class.getName());

    /**
     * Behaviour for infected nodes
     */
    protected void infectBehaviour() {
        List<UserAgent> n = this.getNeighbours();

        for (UserAgent a : n) {
            //beacon activated when first message arrieved
            if (State.BEACONOFF.toString().equals(a.getState())) {
                a.setNextState(State.BEACONON.toString());
            } else {

                if (getBt().random.nextFloat() <= smM1.getProbInfect()) {
                    if (State.NEUTRAL.toString().equals(a.getState())) {
                        a.setNextState(State.INFECTED.toString());
                    }
                }
            }
        }
        //If no beacons are added, a cured  model start from a single infected node after timelag 
        if (smM1.getBeacons() == 0 && !delayedStartModelStartedFlag && getBt().schedule.getSteps() >= smM1.getTimeLag()) {
            delayedStartModelStartedFlag = true;
            this.setNextState(State.CURED.toString());
        }
    }

    /**
     * Denier behaviour
     */
    protected void denierBehaviour() {
        List<UserAgent> n = this.getNeighbours();
        /*Cure neighbours*/
        for (UserAgent a : n) {
            /**beacon se activa siempre si ve denier*/
            if (State.BEACONOFF.toString().equals(a.getState())) {
                a.setNextState(State.BEACONON.toString());
            }
            if (getBt().random.nextFloat() <= smM1.getProbAcceptDeny() || this.getState().equals(State.BEACONON.toString())) {
                if (State.NEUTRAL.toString().equals(a.getState())) {
                    a.setNextState(State.VACCINATED.toString());
                }
                if (State.INFECTED.toString().equals(a.getState())) {
                    a.setNextState(State.CURED.toString());
                }
            }
        }
    }
    
    
    
}
/*
    protected boolean canBeInfected(UserAgent a) {
        boolean b1 = State.NEUTRAL.toString().equals(a.getState());//is neutral
       boolean b2 = a.getStateOfAgentWhoFixedNextState() != null && a.getStateOfAgentWhoFixedNextState().equals(State.BEACONON.toString());//the next state was not changed by a beacon      
        return b1 && !b2;
    }
    */

