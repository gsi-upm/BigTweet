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

import bigtweet.model.SpreadModelM1;
import bigtweet.model.SpreadModelM1.State;
import bigtweet.model.SpreadModelM2;
import java.util.List;
import sim.engine.SimState;

/**
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class UserAgentM2 extends UserAgentM1 {

    private SpreadModelM2 smM2;

    public UserAgentM2(int nodeId, SpreadModelM2 smM2) {
        super(nodeId, smM2);
        this.smM2 = smM2;

    }

    /**
     * Cured agents dont have any behaviour here, they are counted as infected
     *
     * @param simstate
     */
    @Override
    public void step(SimState simstate) {

  
    
        updateFromNextState();       
        
     

        /*Infected behavior*/
        if (State.INFECTED.toString().equals(this.getState())) {
            infectBehaviour();
        }
        if (State.BEACONON.toString().equals(this.getState()) || State.VACCINATED.toString().equals(this.getState())) {
            denierBehaviour();
        }
        /**Cured don't do anythong*/
    }

    protected void infectBehaviour() {
        List<UserAgent> n = this.getNeighbours();

        for (UserAgent a : n) {

            if (State.BEACONOFF.toString().equals(a.getState())) {
                a.setNextState(State.BEACONON.toString());
            } else {

                if (getBt().random.nextFloat() <= smM2.getProbInfect()) {
                    if (State.NEUTRAL.toString().equals(a.getState())) {
                        a.setNextState(State.INFECTED.toString());
                    }
                } else {
                    if (getBt().random.nextFloat() <= smM2.getProbMakeDenier()) {
                        if (State.NEUTRAL.toString().equals(a.getState())) {
                            a.setNextState(State.VACCINATED.toString());
                        }
                    }
                }
            }
        }

    }
}
