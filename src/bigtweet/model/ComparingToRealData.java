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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Class with info of one experiment with several random seeds. Used by
 * BTSimBatch It calls evaluator with a specific data set to compare
 *
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class ComparingToRealData extends BatchExperimentsResults {


    private String fileWithRealData;
    private Map<Long, Double> seedAndDistances = new HashMap<Long, Double>();

    /**
     * Control attribute, number of experiments
     */
    private int experimentWithBestDistance = -1;
    private int experimentWithBestMean = -1;
    private double bestDistance = Double.MAX_VALUE;
    private double bestMean = Double.MAX_VALUE;
    double lastDistances[];

    
    /**
     * Pass the property in config property with the path of the real data
     * @param name 
     */
    public ComparingToRealData(String name) {
        super(name);
        this.fileWithRealData = BTSim.getProperty(name);

    }


    /**
     * *
     * Called from batch to calculate and store a distance for a specific seed
     */
    @Override
    public void addResultsForSeed(BTSim bt, Long seed) {
        double d= EvaluationTools.getDistance( fileWithRealData,  BTSim.getProperty("statesPerAgentAndDay"))[0];
        seedAndDistances.put(seed, d) ;
    }

    /**
     * Update the min distnce , mean and sd in all experiments and the min
     * distance mean and sd for one experiments with several seeds in this
     * dataset
     * The experiment id is passed
     */
    @Override
    public void updateMetricsForParametersValues(JSONArray parametersValues, int parametersValuesIndex) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.000", otherSymbols);


        //get distances array for each seed
        Object obDist[] = seedAndDistances.values().toArray();
        double distances[] = new double[obDist.length];
        for (int i = 0; i < obDist.length; i++) {
            distances[i] = (double) obDist[i];
        }

        //get metrics for this experiment and this dataset
        double minDistance = (new Min()).evaluate(distances);
        double mean = (new Mean()).evaluate(distances);
        double sd = (new StandardDeviation()).evaluate(distances);

        //put in metricsForLastExperiment, JSON object
        metricsForLastExperiment= new  JSONObject();//create new object for the last experiment    
        metricsForLastExperiment.put("minDistance", df.format(minDistance));
        metricsForLastExperiment.put("mean", df.format(mean));
        metricsForLastExperiment.put("sd", df.format(sd));

        JSONObject distancesJSON = new JSONObject();
        for (Map.Entry<Long, Double> entry : seedAndDistances.entrySet()) {
            distancesJSON.put(entry.getKey().toString(), df.format(entry.getValue()));

        }

        metricsForLastExperiment.put("randomSeedsAndDistances", distancesJSON);
        




        //update metrics for all experiments

        if (mean < this.bestMean) {
            //System.out.println(name + " bestmean " + df.format(bestMean).toString() + " mean " + df.format(mean).toString());
            bestMean = mean;
            experimentWithBestMean = parametersValuesIndex;
            metricsForAllExperiments.put("bestMean", df.format(bestMean));
            metricsForAllExperiments.put("experimentWithBestMean", parametersValuesIndex);
        }
        if (minDistance < this.bestDistance) {
     
            //System.out.println(name + " bestdos " + df.format(bestDistance).toString() + " dis " + df.format(minDistance).toString());
            bestDistance = minDistance;
            experimentWithBestDistance = parametersValuesIndex;
            metricsForAllExperiments.put("bestDistance", df.format(minDistance));
            metricsForAllExperiments.put("experimentWithBestDistance", parametersValuesIndex);
        }

    }
    
    @Override
    public void loadPreviousBestResults(JSONObject o){
            experimentWithBestDistance = new Integer(o.get("experimentWithBestDistance").toString());
            experimentWithBestMean = new Integer(o.get("experimentWithBestMean").toString());
            bestDistance= new Double(o.get("bestDistance").toString());
            bestMean= new Double(o.get("bestMean").toString());
    }
}
