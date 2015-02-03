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


package bigtweet;
import bigtweet.model.SpreadModel;
import bigtweet.model.SpreadModelM1;
import bigtweet.model.SpreadModelM2;
import bigtweet.model.SpreadModelM3;
import bigtweet.model.agents.MonitorAgent;
import bigtweet.model.agents.UserAgent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import org.json.simple.JSONObject;
import org.openide.util.Exceptions;
import sim.engine.*;
/**
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */

public class BTSim extends SimState {

    private int numUsers = new Integer( BTSim.getProperty("numusers"));
    private int maxLinkPerNode = 1;
    
    private boolean autoLoadDisplay = true;
    private SpreadModel sm;
    private String SpreadModelName = BTSim.getProperty("initialspreadmodel");
    private List<UserAgent> agents;
    private JSONObject parametersSetForBatch;

    private long seedNetwork;

    
    public void setMaxLinkPerNode(int maxLinkPerNode) {
        this.maxLinkPerNode = maxLinkPerNode;
    }

    public int getMaxLinkPerNode() {
        return maxLinkPerNode;
    }
    public  JSONObject getParametersSetForBatch() {
        return parametersSetForBatch;
    }

    /**
     * IF fixed, parameters are recovered from there
     * @param parametersSetForBatch 
     */
    
    
    
    public void setSeedNetwork(int seedNetwork) {
        this.seedNetwork = seedNetwork;
    }

    public int getSeedNetwork() {
        return (int) seedNetwork;
    }

    
    
    public  void setParametersSetForBatch(JSONObject parametersSetForBatch) {
        this.parametersSetForBatch = parametersSetForBatch;
    }    
    public String getSpreadModelName() {
        return SpreadModelName;
    }

    public void setSpreadModelName(String SpreadModelName) {
        this.SpreadModelName = SpreadModelName;
    }

    public List<UserAgent> getAgents() {
        return agents;
    }

    public void setAgents(List<UserAgent> agents) {
        this.agents = agents;
    }

    
    

    
    public void setSpreadModel(SpreadModel sm) {
        this.sm = sm;
    }

    public SpreadModel getSpreadModel() {
        return sm;
    }

    public Map<String, Integer> getAgentPerState() {
        if (sm == null) {
            return null;
        } else {
            return sm.getAgentsPerState();
        }
    }

    public void setAutoLoadDisplay(boolean autoLoadDisplay) {
        this.autoLoadDisplay = autoLoadDisplay;
    }

    public boolean getAutoLoadDisplay() {
        return autoLoadDisplay;
    }



    public void setNumUsers(int numUsers) {
        if (numUsers < 2) {
            return;
        }
        this.numUsers = numUsers;
    }

    public int getNumUsers() {
        return numUsers;
    }
    
 

 
    public BTSim(long seed) {        
        super(seed);        
        setSeedNetwork((int) seed);
        
    }
    
    public static String getProperty(String key){
        Properties properties=new Properties();
         try {	
        String filename = "./configuration/config.properties";         
        InputStream input = new FileInputStream(filename);           
        properties.load(input);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        String r= properties.getProperty(key); 
        return r;
    }

    public static void main(String[] args) {
        System.setProperty("java.util.logging.config.file", "./configuration/logging.properties");        
        doLoop(BTSim.class, args);
        System.exit(0);
    }

    public void start() {      
      
       

        agents = new ArrayList<>();
        switch (SpreadModelName) {
            case "M1":
                sm = new SpreadModelM1(this);
                break;
            case "M2":
                sm = new SpreadModelM2(this);   
                break;
            case "M3":
                sm = new SpreadModelM3(this);   
                break;                
        }
        if(this.getParametersSetForBatch()!=null) sm.setParametersSetForBatch(getParametersSetForBatch());
        sm.loadModel();
        

    


    }
    
    
      /**
     *
     * @param na number of agents
     * @param selectingMethod: , last, or first in the list of agents. (or null,
     * random, to random)
     * @param exceptState agents except of that state, can be null
     * @return
     */
    public List<UserAgent> getNUsers(int na, String selectingMethod, String exceptState) {
        List<UserAgent> r = new ArrayList<UserAgent>();
        UserAgent aux;
        int added = 0;

        switch (selectingMethod) {
            case "last":
                ListIterator<UserAgent> li = getAgents().listIterator(getAgents().size());
                while (li.hasPrevious() && added < na) {

                    aux = li.previous();
                    if (exceptState == null || !aux.getState().equals(exceptState)) {
                        r.add(aux);
                        added++;
                    }
                }
                if (added < na) {
                    throw new RuntimeException("There is no " + na + " agents to be added.");
                }
                break;
            case "first":
                li = getAgents().listIterator();
                while (li.hasNext() && added < na) {
                    aux = li.next();
                    if (exceptState == null || !aux.getState().equals(exceptState)) {
                        r.add(aux);
                        added++;
                    }
                }
                if (added < na) {
                    throw new RuntimeException("There is no " + na + " agents to be added.");
                }
                break;

            default:
                r = getNRandomUsers(na, exceptState);
        }

        return r;

    }

    /**
     * get N random Users
     *
     * @param exceptState agents except of that state, can be null
     * @return
     */
    public List<UserAgent> getNRandomUsers(int na, String exceptState) {
        List<UserAgent> r = new ArrayList<UserAgent>(); 
        if (na == 0) {
            return r;
        }
        List<UserAgent> agents = getAgents();
        UserAgent aux;
        int added = 0;
        int tries = 0;

        //System.out.println("SEMILLA AÑADIENDO" + this.seed() + " nextint" + random.nextInt() + " size agents" + agents.size());
        do {
            tries++;
            aux = agents.get(random.nextInt(agents.size()));
            //System.out.println(aux);
            if (!r.contains(aux) && (exceptState == null || !aux.getState().equals(exceptState))) {
                r.add(aux);
                added++;
            }
        } while (added < na && tries < na * 10); //10 tries to fill the list
        if (added < na) {
            throw new RuntimeException("There is no " + na + " agents to be added.");
        }
        return r;
    }


    public void stop() {
    }

    /**
     * get user agent
     *
     * @param id
     */
    public UserAgent getAgent(String name) {
        for (UserAgent a : agents) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    
 
 
}