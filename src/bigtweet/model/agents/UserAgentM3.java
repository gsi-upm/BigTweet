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
import bigtweet.model.SpreadModelM3;
import java.util.ArrayList;
import java.util.List;
import sim.engine.SimState;

/**
 * Same than M2 but when a beacon deny, also follow the neighbourghs of
 * neighbourghs with infection  to deny in following step
 *
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class UserAgentM3 extends UserAgentM2 {

    private SpreadModelM3 smM3;

    public UserAgentM3(int nodeId, SpreadModelM3 smM3) {
        super(nodeId, smM3);
        this.smM3 = smM3;

    }

    @Override
    public void step(SimState simstate) {
        super.step(simstate);
        addNeighboursIfTheyCanSpreadInfection();
    }

    private void addNeighboursIfTheyCanSpreadInfection() {
        if (SpreadModelM1.State.BEACONON.toString().equals(this.getState())) {
            List<UserAgent> thisAgentNeighbourghs = this.getNeighbours();
            List<UserAgent> thisAgentNewNeighbourghs = new ArrayList<>();
            for (UserAgent a : thisAgentNeighbourghs) {
                List<UserAgent> neighbourghsOfNeighbourghs = a.getNeighbours();
                for (UserAgent a2 : neighbourghsOfNeighbourghs) {
                    if (!thisAgentNeighbourghs.contains(a2) && !thisAgentNewNeighbourghs.contains(a2)) {
                        if (SpreadModelM1.State.INFECTED.toString().equals(a2.getState()) ) {
                            // SpreadModelM1.State.VACCINATED.toString().equals(a2.getState())
                            thisAgentNewNeighbourghs.add(a2);
                        }
                    }
                }
                addNeighbours(thisAgentNewNeighbourghs);
           
            }
        }
    }
}
