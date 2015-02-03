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

package bigtweet.model;

import bigtweet.BTSim;
import bigtweet.model.agents.UserAgent;
import bigtweet.model.agents.UserAgentM2;
import bigtweet.model.agents.UserAgentM3;

/**
 * Same than M2 but with beacons following neigbourghs of infected
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class SpreadModelM3 extends SpreadModelM2{

       @Override
    public UserAgent createAgent(int id, SpreadModel sm) {
        return new UserAgentM3(id,this);    
    }
   
   
   public SpreadModelM3(BTSim bt){
       super(bt);
       
   }
}


