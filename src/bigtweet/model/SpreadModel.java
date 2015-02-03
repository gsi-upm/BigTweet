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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Generic class for a spread model
 *
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public abstract class SpreadModel {


  
/**
 * Fixed a parameter set as a json object instead of the config file
 */
    private  JSONObject parametersSetForBatch;


    protected BTSim bt;


    public static String listOfModels = getListOfModels().get(0);
    /**
     * Number of agents for each state 
    *
     */
    protected Map<String, Integer> agentsPerState = new HashMap<>();
    protected int lastStepStored = 0;
    protected Map<String, Integer> lastStatesStored;

 
    public Map<String, Integer> getStatesStoredInPreviousStep() {
        return lastStatesStored;
    }
    /**
     * The graph is separated from the agents, although agents could extend
     * nodes, this is more flexible for using graphstream generators and to
     * export to gefx with minimal informatoin
     */
    private Graph graph;


    /**
     * get name of the model, used to load parameters from json file by
     * getlModelParameterFromParametersSet
     *
     * @return
     */
    public String getName() {
        return getBt().getSpreadModelName();
    }

    
      
     /**
     * schedule agents and set list agents
     */
    protected void generateUsers() {        
        for (int i = 0; i < getBt().getNumUsers(); i++) {
            createAgent(i,this);      
        }                
    }
    
    
    /**
     * Called to load the model by the simulator
     */
    public abstract void loadModel();

 
    protected void setBt(BTSim bt) {
        this.bt = bt;
    }

    public BTSim getBt() {
        return bt;
    }



    public Graph getGraph() {
        return graph;
    }
    
      public void setGraph(Graph g) {
        this.graph=g;
    }

    /**
     * Read the list of models in modelParameters
     *
     * @return
     */
    public static List<String> getListOfModels() {        
        JSONObject jo = (JSONObject) SpreadModel.getModelConfiguration().get("spreadModels");
        List<String> l = new ArrayList();
        for (Object o : jo.keySet()) {
            l.add((String) o);
        }
        Collections.sort(l);
        return l;
    }

    /**
     * Get parameters in config json file specified in modelConfPath
     *
     * @return
     */
    protected static JSONObject getModelConfiguration() {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(BTSim.getProperty("modelConfPath")));

        } catch (IOException ex) {
            Logger.getLogger(SpreadModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SpreadModel.class.getName()).log(Level.SEVERE, null, ex);
        }
   
        return jsonObject;
    }

    /**
     * Get parameter value from the model parameters
     *
     * @param parameterName
     * @return
     */
    protected String getModelParameter(String parameterName) {
        JSONObject jo = ((JSONObject) SpreadModel.getModelConfiguration().get("spreadModels"));
        jo = ((JSONObject) jo.get(this.getName()));
        return jo.get(parameterName).toString();
    }

    /**
     * Get parameter value from the selected parameter set in json file
     *
     * @param parameterName
     * @return
     */
    protected String getlModelParameterFromParametersSet(String parameterName) {
        JSONObject jo= getParametersSetSelected();
        
        return (jo.get(parameterName) == null) ? null : jo.get(parameterName).toString();



    }

    /**
     * The json object with parameters values, can be set for batch or read from configFile
     * If the batch parameters are set from BTSimBAtch, those are used
     * @return 
     */
    protected JSONObject getParametersSetSelected() {
             
        JSONObject jo ;
        
        if(getParametersSetForBatch()!=null) return getParametersSetForBatch();
        
        jo= ((JSONObject) SpreadModel.getModelConfiguration().get("spreadModels"));
        jo = ((JSONObject) jo.get(this.getName()));
        String selectedSetOfParametersValues = jo.get("selectedSetOfParametersValues").toString();
        jo = ((JSONObject) jo.get("setsOfParametersValues"));
        return ((JSONObject) jo.get(selectedSetOfParametersValues));
    }

    /**
     * Update the agentsPerState field. It also initialize the agentsPerState
     * attribute. If previousState is null, the agent is initializate with
     * currentState If previousState is null and currentState are null, only the
     * lastStatesStored is updated
     *
     * @param previousState can be null
     * @param currentState
     */
    public void recordAgentStateChange(String previousState, String currentState) {
        int step = (int) bt.schedule.getSteps();

        if (step > lastStepStored) {//before changing the map, if it is the first call of new step, the last map is stored so monitor can recover it
            lastStepStored = step;
            lastStatesStored = new HashMap(this.agentsPerState); //copy of the ashmap
        }

        /*Called only to update lastStatesStored, monitor agent can be the first agent of the step of no change can happen in the step*/
        if (previousState == null && currentState == null) {
            return;
        }

        /*Update current state when a previous state is given */
        if (previousState != null && getAgentsPerState().get(previousState) != null) {
            getAgentsPerState().put(previousState, getAgentsPerState().get(previousState) - 1);
        }


        if (getAgentsPerState().get(currentState) == null) {
            getAgentsPerState().put(currentState, 0);
        }
        getAgentsPerState().put(currentState, getAgentsPerState().get(currentState) + 1);



    }

    /**
     * 0 if state not used or no agent
     *
     * @param state
     * @return
     */
    public int getAgentsWithState(String state) {

        Integer i = null;
        if (getAgentsPerState() != null) {
            i = getAgentsPerState().get(state);
        }
        return ((i == null) ? 0 : i);
    }

    /**
     * Get agents per state in current step
     * @return 
     */
    public Map<String, Integer> getAgentsPerState() {
        return this.agentsPerState;
    }

  


    /**
     * Load (or reload) parameters from the json config file
     */
    protected void loadParameters() {
        
        if (getlModelParameterFromParametersSet("users") != null) {
            getBt().setNumUsers(new Integer(getlModelParameterFromParametersSet("users")).intValue());
        }
        if (getlModelParameterFromParametersSet("maxLinkPerNode") != null) {
            this.bt.setMaxLinkPerNode(new Integer(getlModelParameterFromParametersSet("maxLinkPerNode")).intValue());
        }

        if (getlModelParameterFromParametersSet("seed") != null) {
            this.getBt().setSeed(new Integer(getlModelParameterFromParametersSet("seed")).intValue());
        }
        if (getlModelParameterFromParametersSet("seedNetwork") != null) {
            bt.setSeedNetwork(new Integer(getlModelParameterFromParametersSet("seedNetwork")).intValue());
        }
        else{//after reading general seed from file, is used for network
            bt.setSeedNetwork((int) this.getBt().seed());
        }

    }
    
    
    
    public  JSONObject getParametersSetForBatch() {
        return parametersSetForBatch;
    }

    /**
     * IF fixed, parameters are recovered from there
     * @param parametersSetForBatch 
     */
    public  void setParametersSetForBatch(JSONObject parametersSetForBatch) {
        this.parametersSetForBatch = parametersSetForBatch;
    }
    
    
    
    /**
     * factory method implementation
     */
    public abstract UserAgent createAgent(int id, SpreadModel sm);
}
