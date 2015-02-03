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
import bigtweet.BTSimWithUI;
import bigtweet.model.agents.MonitorAgent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.WindowConstants;
import rcaller.RCaller;
import rcaller.RCode;
import rcaller.RPlotViewer;

/**
 * This class compares data from simulation with real data. It uses R code and
 * shows a plot
 *
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class EvaluationTools {

 
    private static String RPATH = BTSim.getProperty("rpath");
    
    private static final Logger LOG = Logger.getLogger(EvaluationTools.class.getName());
  

    /**
     * Pearson correlation
     *
     * @return 
     * denies, and sum of the first and second position
     */
    /**
     * 
     * @param realDataFile
     * @param inputFile
     * @return euclidean distance between realDataFile and inputFile
     */
    public static double[] getDistance(String realDataFile, String simFile) {

        RCaller caller = new RCaller();
        RCode code = new RCode();

        caller.setRscriptExecutable(RPATH);

        code.addRCode("source('" + BTSim.getProperty("rcode") + "')");
        String command = "co<- distanceFromFiles(\"" + realDataFile + ("\", \"") + simFile + "\")";
        LOG.fine("R command for distance: " + command);
        code.addRCode(command);
        caller.setRCode(code);
        caller.runAndReturnResult("co");


        double[] r = caller.getParser().getAsDoubleArray("co");
        LOG.fine("Distance found " + Arrays.toString(r));


        return r;



    }
    
    
    public static double getDistanceComparingWithRealData(){
       return (getDistance( BTSim.getProperty("comparingdata"),  BTSim.getProperty("statesPerAgentAndDay")))[0];
    }

    /**
     * Executes a plot command stored in the r code file, given in property "rcode" of file config.properties
     * datasetWithRealData
     */
    public static void plotWithR(String frameTitle,String command) {

        RCaller caller = new RCaller();
        RCode code = new RCode();
        File plotFile = null;
        ImageIcon plotImage = null;

        caller.setRscriptExecutable(RPATH);

        code.R_require("lattice");

        try {
            plotFile = code.startPlot();
   code.addRCode("source('" + BTSim.getProperty("rcode") + "')");       
            LOG.config("R command for plotting: " + command);
            code.addRCode(command);

        } catch (Exception err) {
            System.out.println("Can not create plot");
        }

        caller.setRCode(code);
        caller.runOnly();


        plotImage = code.getPlot(plotFile);
        RPlotViewer plotter = new RPlotViewer(plotImage);
        plotter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        plotter.setVisible(true);
        plotter.setIconImage(BTSimWithUI.getLocoIcon().getImage());
        plotter.setTitle(frameTitle);
                
    }
    
    

    
/**
 * 
 * @param showPlot show plot comparing
 * @param users users to get the % of users
 * @param seed seed of simulation
 */    
   public static void compareWithRealData(boolean showPlot, int users, int seed) { 
    writeSimDataSet(true,users, BTSim.getProperty("statesPerAgentAndDay"));
    if(showPlot){
        String title ="Seed " + seed + " , distance: " + getDistanceComparingWithRealData() ;
        String command = "getLinearChartComparingFromFile(\"" +  BTSim.getProperty("comparingdata") + ("\", \"") + BTSim.getProperty("statesPerAgentAndDay") + "\")";
        plotWithR(title, command);
    }
    
   }
   
 public static void showNumberOfStates(boolean showPlot, int users, int seed) { 
    writeSimDataSet(false, users, BTSim.getProperty("statesPerAgentAndStep"));
    
        if(showPlot){
        String title ="Users states, seed: " + seed;  
        String command = "getLinearChartChartWithStatesFromFile(\"" +  BTSim.getProperty("statesPerAgentAndStep") + "\")";    
            System.out.println(command);
              
        plotWithR(title, command);
    }
   }

    /**
     * Store a temporal file with the agents states to be plot or studied with R
     *
     * @param onlyDays Si true, se guarda cada 24 steps un valor (simulas horas,
     * comparas días)
     */
    private static void writeSimDataSet(boolean onlyDays, int users, String simulatedDataFile) {

        List<Map<String, Integer>> history = MonitorAgent.getHistoryOfStates();

        if (history == null || history.isEmpty()) {
            LOG.severe("NO HISTORY RECORDED BY THE MONITOR, EVALUATOR WILL NOT OVER WRITE THE DATASET FOR DE SIMULATION, LAST DATASET WILL BE USED");
            return;
        }
        try {
            PrintWriter writer = null;
            writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(simulatedDataFile))));
        
            int index = -1;
            SpreadModelM1.State states[] = SpreadModelM1.State.values();
            for (int i = 0; i < states.length; i++) {
                writer.print("\"" + states[i].toString() + "\"" + "\t");
            }
            writer.println("");

            for (Map<String, Integer> m : history) {
                index++;
                if (onlyDays && ((index % 24) != 0) && index != (history.size() - 1)) {
                    continue;  //si sólo días, index es múltiplo de 24 horas y no es el último valor de la simulación                              
                }
                DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
                DecimalFormat df = new DecimalFormat("0.00", otherSymbols);



                for (int i = 0; i < states.length; i++) {
                    float usersPerState;
                    if (m.containsKey(states[i].toString())) {
                        usersPerState = ((float)  (m.get(states[i].toString()) * 100) / (float)  users);
                    } else {
                        usersPerState = 0;
                    }
                    writer.print(df.format(usersPerState) + "\t");
                }
                writer.println("");
            }
            writer.close();
            LOG.config("Generated dataset " + simulatedDataFile);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
