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
import bigtweet.model.agents.MonitorAgent;
import bigtweet.model.agents.UserAgent;
import bigtweet.model.agents.UserAgentM1;
import bigtweet.model.agents.UserAgentM2;
import org.graphstream.graph.Graph;

/**
 * Extension of M1 to limit the influence of the initial conditions (specifically, the infected and beacons nodes which now depends on being the first added in BA, i.e., agents with more betweeness)
 * It also allow to start beacons on if wanted 
 * Cured agents do not spread (counted as "endorsers")
 * A neutral node conneted to an infected can be become a VACCINATED
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */





public class SpreadModelM2 extends SpreadModelM1 {
    
    
  private float probMakeDenier;
  /**
   * Pattern factory method
   * @param id
   * @param sm
   * @return 
   */
    @Override
    public UserAgent createAgent(int id, SpreadModel sm) {
        return new UserAgentM2(id,this);    
    }
   
 
    public float getProbMakeDenier() {
        return probMakeDenier;
    }
   
   
   public SpreadModelM2(BTSim bt){
       super(bt);
       
   }
   
   /**
    * Extended to use a new selecting method "first"
    */
     @Override
     public  void loadModel(){   
       loadParameters();             
        this.setGraph(GraphTools.generateGraph(bt.getSeedNetwork(),bt.getMaxLinkPerNode(),bt.getNumUsers())); 
       generateUsers();//schedule agents and set list agents
       infectInitialNodes(bt.getNUsers(this.getInitiallyInfected(),BTSim.getProperty("selectionMethodForInfectedAndBeaconsM2"), null));
       makeBeaconsNodes(bt.getNUsers(this.getBeacons(), BTSim.getProperty("selectionMethodForInfectedAndBeaconsM2"), State.INFECTED.toString()),false);     
       makeBeaconsNodesWithCentrality();
       new MonitorAgent(getBt());//create and schedule monitor agent       

     }
     
     
     
    @Override
     protected void loadParameters() {       
       super.loadParameters();          
       probMakeDenier=new Float(this.getlModelParameterFromParametersSet("probMakeDenier")); //load new parameter  
    }
    

     
   
 
   
      /**
     * Extended to stop counting cured agents as denier and include them  in endorser
     * @param previousState
     * @param currentState 
     */
    @Override
      public void recordAgentStateChange(String previousState, String currentState){
         super.recordAgentStateChange(previousState,currentState);
         if( getAgentsPerState()!=null) getAgentsPerState().put(SpreadModelM1.State.DENIER.toString(),  getAgentsWithState(SpreadModelM1.State.VACCINATED.toString())+ getAgentsWithState(SpreadModelM1.State.BEACONON.toString()));          
         if( getAgentsPerState()!=null) getAgentsPerState().put(State.ENDORSER.toString(), getAgentsWithState(State.CURED.toString())+ getAgentsWithState(State.INFECTED.toString()));          
      }
    
    
        
    
}
