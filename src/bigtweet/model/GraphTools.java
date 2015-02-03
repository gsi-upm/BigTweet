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
import bigtweet.view.AdvancedTab;
import bigtweet.view.Clicks;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkGEXF;
import org.graphstream.stream.file.FileSinkGraphML;
import rcaller.RCaller;
import rcaller.RCode;

/**
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class GraphTools {
    
    /**
     * Random seed taken for simulator: for each seed the same barabasi network
     * is generated with getMaxLinkPerNode and getNumUsers nodes
     */
    public static Graph generateGraph(long seedNetwork, int maxLinkPerNode, int nodes) {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Graph graph = new SingleGraph("Social network");
        String path = (new File(BTSim.getProperty("graphStylePath"))).getAbsolutePath();
        graph.addAttribute("ui.stylesheet", "url('" + path + "')");  //style of graph
        // Between 1 and maxLinkPerNode new links per node added.

        Generator gen = new BarabasiAlbertGenerator(maxLinkPerNode);
        // Generate numUsers nodes:

        ((BaseGenerator) gen).setRandomSeed(seedNetwork);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < nodes - 2; i++) {//barabasi starts with 2 agents
            gen.nextEvents();
        }
        //gen.end(); //to add more nodes
        return graph;
      //  graphGenerator = gen;

    }
    
    
   
    public static void graphToGexf(Graph g, String filename) {
        FileSinkGEXF out = new FileSinkGEXF();
        try {
            PrintStream ps = new PrintStream(new File(filename));
            out.writeAll(g, ps);
            ps.close();

        } catch (IOException ex) {
            Logger.getLogger(AdvancedTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    
    public static void graphToGraphML(Graph g, String filename) {
        FileSinkGraphML out = new FileSinkGraphML();
        try {
            PrintStream ps = new PrintStream(new File(filename));
            out.writeAll(g, ps);
            ps.close();

        } catch (IOException ex) {
            Logger.getLogger(AdvancedTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void loadGraphInDesktop(String filename) {

        if (Desktop.isDesktopSupported()) {

            File myFile = new File(filename);
            try {
                Desktop.getDesktop().open(myFile);
            } catch (IOException ex) {
                Logger.getLogger(GraphTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Graph getExample() {

        Graph graphExample = new SingleGraph("Graph example");
        graphExample.addNode("A");
        graphExample.addNode("B");
        graphExample.addNode("C");
        graphExample.addEdge("AB", "A", "B");
        graphExample.addEdge("BC", "B", "C");
        graphExample.addEdge("CA", "C", "A");
        return graphExample;

    }

   

    private static final Logger LOG = Logger.getLogger(GraphTools.class.getName());
    
    /**
     * Calls igraph to obtain the n nodes most important
     * @param g
     * @param centrality: b, c, or d for betwenness, closeness or degree, respectively. r for random is also possible
     * @param numberOfNodes
     * @return 
     */
    public static String[] getImportantNodes(Graph g, String centrality, int numberOfNodes) {
        //export temporal graph
        GraphTools.graphToGraphML(g, BTSim.getProperty("tempgraph"));
        
        //execute R code
        RCaller caller = new RCaller();
        RCode code = new RCode();
        caller.setRscriptExecutable(BTSim.getProperty("rpath"));
        
        code.addRCode("source('"+ BTSim.getProperty("rcode") +"')");
        String command = "nodes<- getNMostImportantNodes(\"" +  BTSim.getProperty("tempgraph") + ("\", \"") + centrality +  ("\", ") + numberOfNodes + ")";
        LOG.fine("R command for getting important nodes: " + command);
        code.addRCode(command);
        caller.setRCode(code);
        caller.runAndReturnResult("nodes");
 
        
        String[] r = caller.getParser().getAsStringArray("nodes"); 
        LOG.fine("Important nodes found " + Arrays.toString(r));                
        return r;
      
    }
    


}
