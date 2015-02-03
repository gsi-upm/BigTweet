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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.graphstream.graph.Graph;

/**
 *Cascade model (rumour) and delayed start model (anti-rumours or denies). Model based on paper by Tripathy et al.:
 * A Study of Rumor Control Strategies on Social Networks.  Initially there are initiallyInfected agents, each step 
 * they can infect their neighbors with probInfect (but they will not be able to infect after the next timestep). 
 * After a timeLag, a single agent infected can start an anti-rumour and their neighbors will accept it with a probAcceptDeny  
 * (and they will deny the rumor next time step). If the beacon model is added  (anti-rumours or denies), there is no time lag 
 * but a number of beacons which, in case of being activated, instead of being infected, start spreading anti-rumours. 
 * Unlike in Tripathy et al., beacons are added following BA model instead of randomly. 
 * Some extensions could be: allows developers 
 * to add a trustworthy beacons network (not explicit in the graph, when a single beacon is activated all of them are), specify
 * the probability of activating monitor (same than of getting infected in the present paper).
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class SpreadModelM1 extends SpreadModel {

   private int initiallyInfected;
   private float probInfect;
   private int timeLag;
   private float probAcceptDeny;
   private int beacons=0;
   
   /**
    * Parameters to add a beacon connected to the beaconLinksNumber nodes most important according to beaconLinksCentrality
    */  
   private int beaconLinksNumber;
   /**
    * Parameters to add a beacon connected to the beaconLinksNumber nodes most important according to beaconLinksCentrality
    * centrality: b, c, or d for betwenness, closeness or degree, respectively. r for random is also possible
    */
   private String beaconLinksCentrality;

  /**
   * Pattern factory method
   * @param id
   * @param sm
   * @return 
   */
    @Override
    public UserAgent createAgent(int id, SpreadModel sm) {
        return new UserAgentM1(id,this);    
    }

    
    /**if parameters beaconLinksNumber and beaconLinksCentrality are used, a beacon is created according to them*/
    protected void makeBeaconsNodesWithCentrality() {
        if(this.beaconLinksCentrality==null || this.beaconLinksNumber==0) return; //no se ha fijado parametro
        String idNodesArray[] = GraphTools.getImportantNodes(getGraph(), this.beaconLinksCentrality.substring(0,1), this.beaconLinksNumber);       
        List<Integer> idNodes= new ArrayList<>();
        for (int i = 0; i < idNodesArray.length; i++) {
            idNodes.add(new Integer(idNodesArray[i]));            
        }
        addBeaconTowards(idNodes);
    }
   
 


   /**
    * States fiven in de paper. Beacons are also added as a sate (on and off if they are activated or not). Denier are also added (to sum all possible denier) and endorser (which are infected)
    */ 
   public static enum State {
        NEUTRAL,
        INFECTED,
        VACCINATED,
        CURED,
        BEACONON,
        BEACONOFF,
        ENDORSER,
        DENIER
    }
   
   
   public SpreadModelM1(BTSim bt){
       this.setBt(bt);

       
   }
   
   
       
    @Override
     public  void loadModel(){         
 
       loadParameters();       
              
       this.setGraph(GraphTools.generateGraph(bt.getSeedNetwork(),bt.getMaxLinkPerNode(),bt.getNumUsers()));
       generateUsers();//schedule agents and set list agents
       infectInitialNodes(bt.getNRandomUsers(this.getInitiallyInfected(), null));
       makeBeaconsNodes(bt.getNRandomUsers(this.getBeacons(), State.INFECTED.toString()),false);
       makeBeaconsNodesWithCentrality();
       new MonitorAgent(getBt());//create and schedule monitor agent
       
       
     }
  
       
    public int getBeacons() {
        return beacons;
    }

     public void setBeacons(int b) {
         beacons=b;
    }

   

    public int getInitiallyInfected() {
        return initiallyInfected;
    }

    public float getProbInfect() {
        return probInfect;
    }

    public int getTimeLag() {
        return timeLag;
    }

    public float getProbAcceptDeny() {
        return probAcceptDeny;
    }
    
    private static final Logger LOG = Logger.getLogger(SpreadModelM1.class.getName());
    
   
    
     public String toString(){
     return getName();
 }

  

   
    
       
    /**
     * Add n monitors keeping considering BA network (more probably in hubs).
     * This method is used when called from the buttun, to initial beacons makeBeacons is sued
     * @param n 
     */
    public void addBeaconsBA(){
        List<Integer> l = new ArrayList<>();
        l.add(bt.random.nextInt(bt.getAgents().size()));                
        addBeaconTowards(l);
        
    }   
    
    /**
     * Add a beacon connected to the agent indexes
     * @param agentsIndexes 
     */
    public void addBeaconTowards(List<Integer> agentsIndexes){
        Graph g=getBt().getSpreadModel().getGraph();
        this.beacons++;
        int nodes= g.getNodeCount();         
        //create node and edge
        String idNode= Integer.toString(nodes);
        g.addNode(idNode);
        for(Integer i: agentsIndexes){
            g.addEdge(idNode + g.getNode(i).getId() , idNode, g.getNode(i).getId());
        }
                
        //create agent
        UserAgent a = createAgent(nodes,this);
        a.setState(State.BEACONOFF.toString());         
        getBt().setNumUsers(getBt().getNumUsers()+1);

        
    }
    
  

    @Override
     protected void loadParameters() {
       super.loadParameters();          
       initiallyInfected=new Integer(this.getlModelParameterFromParametersSet("initiallyInfected"));
       probInfect=new Float(this.getlModelParameterFromParametersSet("probInfect"));
       if(getlModelParameterFromParametersSet("timeLag")!=null) timeLag=new Integer(this.getlModelParameterFromParametersSet("timeLag"));
       probAcceptDeny=new Float(this.getlModelParameterFromParametersSet("probAcceptDeny"));      
       if(getlModelParameterFromParametersSet("beacons")!=null) beacons=new Integer(this.getlModelParameterFromParametersSet("beacons"));       
       if(getlModelParameterFromParametersSet("beaconLinksNumber")!=null) beaconLinksNumber=new Integer(this.getlModelParameterFromParametersSet("beaconLinksNumber"));
       if(getlModelParameterFromParametersSet("beaconLinksCentrality")!=null) beaconLinksCentrality=new String(this.getlModelParameterFromParametersSet("beaconLinksCentrality"));
       
       LOG.config("Spread Model: " + getName() +  ", parameters:" + this.getParametersSetSelected().toJSONString());
                      
    }
    

     
 
    
    /**
     *  infect nodes passed in the list
     */
      protected void infectInitialNodes(List<UserAgent> agentsToInfect) {                               
        LOG.config("Agents initially infected: " + agentsToInfect );     
        for(UserAgent a: agentsToInfect){
            a.setState(State.INFECTED.toString());                         
        }                                
     }
      
          /**
     *  make beacons the nodes passed in the list. Can be off or on
     */
      protected void makeBeaconsNodes(List<UserAgent> agentsBeacons, boolean on) {              
    
         for(UserAgent a: agentsBeacons){      
            if(on) a.setState(State.BEACONON.toString()); 
            else a.setState(State.BEACONOFF.toString());           
        }                                
     }
      

      
   
    /**
     * Extended to sum in denier the states which are denying a rumour
     * @param previousState
     * @param currentState 
     */
    @Override
      public void recordAgentStateChange(String previousState, String currentState){
          super.recordAgentStateChange(previousState,currentState);
         if( getAgentsPerState()!=null){
             getAgentsPerState().put(State.DENIER.toString(), getAgentsWithState(State.CURED.toString())+ getAgentsWithState(State.VACCINATED.toString())+ getAgentsWithState(State.BEACONON.toString()));
             getAgentsPerState().put(State.ENDORSER.toString(), getAgentsWithState(State.INFECTED.toString()));          
         }          
         
      }
    
}
