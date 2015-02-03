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

package bigtweet.view;

import bigtweet.BTSim;
import bigtweet.model.GraphTools;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;

/**
 * @deprecated 
 * Se puede llamar desde aquí, pero al llamarlo desde cualquier otro sitio da una excepción diciendo que está en uso ese nodo
 * @author Emilio Serrano, Ph.d.; eserrano [at] gsi.dit.upm.es
 */
public class Clicks  extends Thread implements ViewerListener{

    protected boolean loop = true;
    BTSim bt;
    Graph graph;
    ViewerPipe fromViewer;

    public static void main(String args[]) {
        (new Clicks(null)).start();
    }

    public Clicks(BTSim bt) {
        this.bt = bt;
        if (bt != null && bt.getSpreadModel().getGraph() != null) {//example to test view with main (without bt objsect)
            graph = bt.getSpreadModel().getGraph();

        } else {
            graph = GraphTools.getExample();
        }
        Viewer viewer = graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);
  
    }

    public void run() {
        while (loop) {
            fromViewer.pump(); // or fromViewer.blockingPump();     
        }
        System.exit(0);
        
    }

    public void viewClosed(String id) {
        try {
            loop = false;
            this.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(Clicks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void buttonPushed(String id) {
        System.out.println("Button pushed on node " + id);
    }

    public void buttonReleased(String id) {
        System.out.println("Button released on node " + id);
    }
}