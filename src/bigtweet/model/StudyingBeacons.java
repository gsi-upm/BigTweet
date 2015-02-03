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
import bigtweet.BTSimBatch;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class StudyingBeacons extends BatchExperimentsResults{
    private Map<Long, Map<String,Integer>> seedAndStates = new HashMap<>();
    
     private int experimentWithBestEndorser= -1;
    private int experimentWithBestMean = -1;
    private double bestEndorser = Double.MAX_VALUE;
    private double bestMean = Double.MAX_VALUE;
    
    
     private  String batchOutputFileForChart = (new String(BTSim.getProperty("batchOutputFileForChart")));
     private JSONArray JSONForChart = new JSONArray();
      /**
     * Pass the property in config property with the path of the real data
     * @param name 
     */
    public StudyingBeacons(String name) {
        super(name);     
  

    }
    
    
     @Override
    public void addResultsForSeed(BTSim bt, Long seed) {
          seedAndStates.put(seed, bt.getSpreadModel().getStatesStoredInPreviousStep());         
    }

    @Override
    public void loadPreviousBestResults(JSONObject o) {
            experimentWithBestEndorser = new Integer(o.get("experimentWithBestEndorsers").toString());
            experimentWithBestMean = new Integer(o.get("experimentWithBestMean").toString());
            bestEndorser= new Double(o.get("bestEndorsers").toString());
            bestMean= new Double(o.get("bestMean").toString());
    }

    @Override
    public void updateMetricsForParametersValues( JSONArray parametersValues, int parametersValuesIndex) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.000", otherSymbols);


        //get infected array for each seed
        
        double infected[] = new double[seedAndStates.values().size()];
        
        for (int i = 0; i < infected.length; i++) {            
            //number of infected for each seed            
            //System.out.println(  ((Map<String,Integer>) (seedAndStates.values().toArray()[i])).get("INFECTED"));
            infected[i] = (new Double(((Map<String,Integer>) (seedAndStates.values().toArray()[i])).get("ENDORSER")));
        }

        //get metrics for this experiment and this dataset
        double minInfected = (new Min()).evaluate(infected);
        double mean = (new Mean()).evaluate(infected);
        double sd = (new StandardDeviation()).evaluate(infected);

        //put in metricsForLastExperiment, JSON object
        metricsForLastExperiment= new  JSONObject();//create new object for the last experiment    
        metricsForLastExperiment.put("minEndorsers", df.format(minInfected));
        metricsForLastExperiment.put("mean", df.format(mean));
        metricsForLastExperiment.put("sd", df.format(sd));

        JSONObject statesJSON = new JSONObject();
        for (Map.Entry<Long, Map<String,Integer>> entry : seedAndStates.entrySet()) {
            statesJSON.put(entry.getKey().toString(), entry.getValue());
        }

        metricsForLastExperiment.put("randomSeedsAndStates", statesJSON);
        




        //update metrics for all experiments

        if (mean < this.bestMean) {
            //System.out.println(name + " bestmean " + df.format(bestMean).toString() + " mean " + df.format(mean).toString());
            bestMean = mean;
            experimentWithBestMean = parametersValuesIndex;
            metricsForAllExperiments.put("bestMean", df.format(bestMean));
            metricsForAllExperiments.put("experimentWithBestMean", parametersValuesIndex);
        }
        if (minInfected< this.bestEndorser) {
     
      
            bestEndorser = minInfected;
            experimentWithBestEndorser = parametersValuesIndex;
            metricsForAllExperiments.put("bestEndorsers", df.format(minInfected));
            metricsForAllExperiments.put("experimentWithBestEndorsers", parametersValuesIndex);
        }
        
        
        generateBatchOuputForChart(parametersValues,parametersValuesIndex, mean);

    
    }
    
    
    /**
     * Extra ouput file to generate a chart 
     * call plotBeaconStudy in R project to obtain chart
     */
    private void generateBatchOuputForChart(JSONArray parametersValues,int parametersValuesIndex, double meanEndorsers) {

        
        
        JSONObject parameters = (JSONObject) parametersValues.get(parametersValuesIndex);//parameters
        JSONObject aux= new JSONObject();
        aux.put("links", parameters.get("beaconLinksNumber"));
        aux.put("centrality", parameters.get("beaconLinksCentrality"));
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.000", otherSymbols);
        aux.put("meanEndorsers", df.format(meanEndorsers));
        JSONForChart.add(aux);
        
        
        
        //write json file
        FileWriter file;
        try {
            file = new FileWriter(batchOutputFileForChart);
            file.write(JSONForChart.toJSONString());
            file.flush();
            file.close();
        } catch (Exception ex) {
            Logger.getLogger(BTSimBatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
