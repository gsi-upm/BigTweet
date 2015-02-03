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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public abstract class BatchExperimentsResults {
    protected JSONObject metricsForLastExperiment = new JSONObject();

    private String name;
    
    protected JSONObject metricsForAllExperiments = new JSONObject();

    public BatchExperimentsResults(String name){
        this.name=name;
    }
    
      /**
     * Returns the historical best results
     *
     * @return
     */
    public JSONObject getMetricsForAllExperiments() {
        return metricsForAllExperiments;
    }
        /**
     * Restunrs the last metrics stored 
     *
     * @return
     */
    public JSONObject getMetricsForLastExperiment() {
        return metricsForLastExperiment;
    }


    public void setName(String name) {
        this.name = name;
    }

    
        public String getName() {
        return name;
    }
    
        
        
     public abstract void addResultsForSeed(BTSim bt, Long seed);
     
         public abstract void loadPreviousBestResults(JSONObject o);

             public abstract void updateMetricsForParametersValues( JSONArray parametersValues, int parametersValuesIndex);
}
