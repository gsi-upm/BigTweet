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

import bigtweet.model.BatchExperimentsResults;
import bigtweet.model.ComparingToRealData;
import bigtweet.model.SpreadModel;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Batch of experiments The parameters values are read from an input file The
 * output file contains a: 1. list of experiments with:  1.1 parameters values, 1.2 results for each real dataset to be compared with
 * ; and 2. simulation best results for experiments
 *
 * The batch can be stoped and re-launched
 * 
 * BTSimBAtch deals with execution and input, while the experimentResultsObject calculates the metrics and output accordind to the kind of study
 *
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public abstract class  BTSimBatch {

    /**
     * number of seeds
     */
    private static final int NSEEDS = (new Integer(BTSim.getProperty("nseeds"))).intValue();
    /**
     * List of random seeds
     */
    private List<Long> seedsList;
    /**
     * Results stored for each real dataset to be compared with simulated data
     * generated
     */
    protected List<BatchExperimentsResults> resultsForDatasets;
    /**
     * Generate with R, a JSON object per parameters values for experiments
     */
    private static final String batchInputFile = BTSim.getProperty("batchInputFile");
    private static final String batchOutputFile = BTSim.getProperty("batchOutputFile");
    
    
    /**
     * 
     * Array of parameters in input
     */
    private JSONArray parametersValues;
    /**
     * JSON object with all output
     */
    private JSONObject output;
    /**
     * Array of experiments in output
     */
    protected JSONArray experiments = new JSONArray();
    private int experimentConducted = -1;



    /**
     * Read a input batch file with set of experiments (one line for parameters
     * configuration) and execute them The file is generated with R
     */
    public void runBatchExperiments() {


        generateSeeds();
        generateExperimentResultsObjects();
        loadBatchInputFile();
        loadBatchOutputFile();


        Logger LOG = Logger.getLogger(BTSimBatch.class.getName()); //log variable, the attribute would not read "./configuration/logging.properties

        for (int i = 0; i < parametersValues.size(); i++) {
            JSONObject jo = (JSONObject) parametersValues.get(i);
            LOG.info("Parameters " + jo.toString());
            if (i < this.experimentConducted) {//skip experiments already conducted
                LOG.info("ALREADY DONE IN PREVIOUS EXECUTION (" + i + " of " + parametersValues.size() + ")");
                continue;
            }
         
            
            for (int seed = 0; seed < NSEEDS; seed++) {
                LOG.info("Seed: " + seed);
                BTSim bt = new BTSim(seed);
                bt.setParametersSetForBatch(jo);
                bt.start();
               
                while (bt.schedule.step(bt)) {
                }//execute simulation until finishing

                for (BatchExperimentsResults r : resultsForDatasets) {//for each dataset, add distance for the seed
                    r.addResultsForSeed(bt,new Long(seed));
                }

            }
            updateOutputJsonFile(i);
            LOG.info("DONE (" + (i+1) + " of " + parametersValues.size() + ")");

        }



    }

    /**
     * Load parametersValues from batchInputFile
     */
    private void loadBatchInputFile() {
        JSONParser parser = new JSONParser();

        try {
            JSONArray jsonarray = (JSONArray) parser.parse(new FileReader(batchInputFile));
            parametersValues = jsonarray;
        } catch (IOException ex) {
            Logger.getLogger(SpreadModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SpreadModel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Load the previous output file, if it does not exist, the output json
     * object is initializated
     */
    private void loadBatchOutputFile() {
        JSONParser parser = new JSONParser();

        if (!(new File(batchOutputFile)).exists()) {//output does not exists, initialize the output JSon object
            output = new JSONObject();
            experiments = new JSONArray();
        } else {


            Object obj = null;
            try {
                obj = parser.parse(new FileReader(batchOutputFile));
            } catch (Exception ex) {
                Logger.getLogger(BTSimBatch.class.getName()).log(Level.SEVERE, null, ex);
            }

            output = (JSONObject) obj;
            //read for each experiment results the best values
            for (BatchExperimentsResults r : resultsForDatasets) {
                r.loadPreviousBestResults((JSONObject) ((JSONObject) output.get("bestResults")).get(r.getName()));
            }
            experiments = ((JSONArray) output.get("experiments"));
            experimentConducted = experiments.size();
        }
    }

    /**
     * Update the output json object and copy it to the output file
     *
     * @param distances
     * @param get
     */
    private void updateOutputJsonFile(int parametersValuesIndex) {
        //calculate elements for update    
        JSONObject parameters = (JSONObject) parametersValues.get(parametersValuesIndex);

        //update attributes and json ouput object
        this.experimentConducted = parametersValuesIndex + 1;
        JSONObject experimentObject = new JSONObject();
        experimentObject.put("experimentID", parametersValuesIndex);
        experimentObject.put("parameters", parameters);


        //read for each experiment results the best values and values for this experiment
        JSONObject results = new JSONObject();
        JSONObject bestResults = new JSONObject();
        for (BatchExperimentsResults r : resultsForDatasets) {
            r.updateMetricsForParametersValues(parametersValues,parametersValuesIndex); //update metrics for one expermioments and for all
            results.put(r.getName(), r.getMetricsForLastExperiment());
            bestResults.put(r.getName(), r.getMetricsForAllExperiments());
        }
        experimentObject.put("results", results);
        experiments.add(experimentObject);
        output.put("experiments", experiments);
        output.put("bestResults", bestResults);


        //write json file
        FileWriter file;
        try {
            file = new FileWriter(batchOutputFile);
            file.write(output.toJSONString());
            file.flush();
            file.close();
        } catch (Exception ex) {
            Logger.getLogger(BTSimBatch.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void generateSeeds() {
        this.seedsList = new ArrayList<Long>();
        for (int i = 0; i < NSEEDS; i++) {
            seedsList.add(new Long(i));
        }
    }
/**
 * Generate results for experiments
 */
    protected abstract void generateExperimentResultsObjects();
}
