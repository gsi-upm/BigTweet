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
import bigtweet.model.SpreadModel;
import bigtweet.model.SpreadModelM1;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.graphstream.graph.Node;
import sim.engine.SimState;
import sim.engine.Steppable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public abstract class UserAgent implements Steppable  {
    private BTSim bt;


     /**node id of user in graph*/
    private int nId;
    private String name;
    private SpreadModel sm;

  
        
     /**
     * State of the agent in the rumour simulation
     */
    protected String state;
    /**
     * Variable use to mark a state to be fixed in the next time step so cascade effects in one step are avoided
     * The step when it is fixed is stored in integer     
     * But there was a bug using just one entry instead of the whole map
     * The agent which fixed the state is also stored to give priority if needed
     */
   
    protected  Map<Integer,Entry<String,UserAgent>> nextState=new HashMap<Integer,Entry<String,UserAgent>>();
    
    
    protected SpreadModel getSm() {
        return sm;
    }
    
    public UserAgent(int nodeId, SpreadModel sm){
        nId =nodeId;
        name= "a" + Integer.toString(nId); 
        bt=sm.getBt();
        this.sm=sm;
        sm.getGraph().getNode(nId).addAttribute("label", getName());//gephi label
        bt.getAgents().add(this);
        bt.schedule.scheduleRepeating(this);
    }

    public int getNodeId() {
        return nId;
    }

    public String getName() {
        return name;
    }
    
    public Node getNode(){
        return sm.getGraph().getNode(nId);
    }
    
    /**
     *  LOOKOUT!! getNeighborNodeIterator no es determinista, devuelve distintos ordenes
     */
     
      
    public List<UserAgent> getNeighbours(){
       Iterator<? extends Node> nodes = sm.getGraph().getNode(nId).getNeighborNodeIterator();
       List<Integer> agentsId = new ArrayList<Integer>();
       while(nodes.hasNext()) {
         Node node = nodes.next();
         agentsId.add(new Integer(node.getId()));
        }
       Collections.sort(agentsId);//order according id              
       
       List<UserAgent> agents = new ArrayList<UserAgent>();
       for(Integer i: agentsId){
        agents.add(bt.getAgents().get(i.intValue()));
       }            
       return agents;
   }
    
    
   public void addNeighbours(List<UserAgent> n){
        for(UserAgent a: n){            
            if(sm.getGraph().getEdge(this.getNode().toString() + a.getNode().toString())==null){//edge not created
                sm.getGraph().addEdge( this.getNode().toString() + a.getNode().toString() ,  this.getNode().toString(), a.getNode().toString());
            }
        }
   }
    
    
    public String toString(){
        return name;
    }
    
   
    
   


    
    
        
    
    /**
     * Set the state in the agent, change the class in the graph, and updates the agentsPerState map
     * The colours are fixed in the css configuration file for the graph according to the "ui.class" value, which can be multivaluated.
     * 
     * @param state 
     */
    public void setState(String state) {
        sm.recordAgentStateChange(this.state,state);
        this.state = state;     
        sm.getGraph().getNode(this.getNodeId()).addAttribute("ui.class", state.toString());//default type
    }
    
     
    /**
     * Get state in the rumor for the agent as string
     * LOOK OUT!!! State.INFECTED.toString().equals(this.getState()) is not the same than State.INFECTED.equals()... it must be an string
     * @return 
     */
    public String getState() {
        return state;
    }     

    /**
     * Used to change state in next step
     * @param s 
     */
    protected void setNextState(String s){
        nextState.put(getStep()+1, new SimpleEntry(s,this));
        
    }
    
      /**
     * Used to change state in next step
     * @param s 
     */
    protected String getNextState(){
        Entry<String,UserAgent> e = nextState.get(getStep()+1);
        if( e!=null) return e.getKey();
        else return null;
        
        
    }
    
    
          /**
     * Used to change state in next step
     * @param s 
     */
    protected String getStateOfAgentWhoFixedNextState(){
        Entry<String,UserAgent> e = nextState.get(getStep()+1);      
        if( e!=null) return e.getValue().getState().toString();
        else return null;
        
        
    }
    
    /**
     * Used to change state after using setNextState
     */
    protected void updateFromNextState(){      
         if(nextState.containsKey(getStep())){
             this.setState(nextState.get(getStep()).getKey());
             nextState.remove(getStep());
         }                                            
    }
    
    public BTSim getBt() {
        return bt;
    }
    
    
    protected Integer getStep(){
        return new Integer((int) bt.schedule.getSteps());
    }
    
}