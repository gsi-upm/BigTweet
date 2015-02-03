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

import bigtweet.model.EvaluationTools;
import bigtweet.view.*;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import sim.display.*;
import sim.engine.SimState;
import sim.portrayal.Inspector;

public class BTSimWithUI extends GUIState {

    protected static BTSim bt;
    protected GraphDisplay gd;
    protected UsersStatesChart uc;
 

    public static void main(String[] args) {
        System.setProperty("java.util.logging.config.file", "./configuration/logging.properties");
        BTSimWithUI vid = new BTSimWithUI();
        bt = (BTSim) vid.state;   
        bt.setSeed(1);

        
        Console c = new Console(vid);
        c.setLocationRelativeTo(null);
        c.setSize(500, 650);
        c.setVisible(true);              
        //c.setIncrementSeedOnStop(true);
        c.setIncrementSeedOnStop(false);
        c.getTabPane().add(new AdvancedTab(bt).getContentPane());
        c.setIconImage(BTSimWithUI.getLocoIcon().getImage());
        
        

    }

    public BTSimWithUI() {
        super(new BTSim(System.currentTimeMillis()));
    }

    public BTSimWithUI(SimState state) {
        super(state);
    
    }

    public static String getName() {
        return "BigTweet";
    }

    @Override
    public Object getSimulationInspectedObject() {
        return state;
    }

    @Override
    public Inspector getInspector() {
        Inspector i = super.getInspector();
        i.setVolatile(true);
        return i;
    }

    @Override
    public void start() {        
        super.start();
        uc.start();
 
    
    if (bt.getAutoLoadDisplay()) {
            gd.setVisible(true);
            uc.getDisplayFrame().setVisible(true);
        }



    }

    @Override
    public void init(Controller c) {     
        super.init(c);              
       gd = new GraphDisplay(bt);
       c.registerFrame(gd);
        uc= new UsersStatesChart(bt,c);
       c.registerFrame(uc.getDisplayFrame());
       
    }
    
    
    
    
    @Override
    public boolean step(){
        boolean b=   super.step();
        uc.step(bt);
        return b;
    }

    @Override
    public void quit() {

        super.quit();
        if (gd != null) {
          gd.dispose();
        }

    }
    
    @Override
    public void finish(){       
         if(bt.schedule.getSteps()>0)    EvaluationTools.compareWithRealData(true, bt.getNumUsers(), (int) bt.seed());                 
        bt.schedule.reset();
        //volver a fijar usuarios por defecto por si se han añadido beacons
        bt.setNumUsers( new Integer( BTSim.getProperty("numusers")));
        
    }

    
     public static ImageIcon getLocoIcon(){
        File f = new File(AdvancedTab.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String logoPath = f.getPath() + "/bigtweet/logo.png";
        ImageIcon image = new ImageIcon(logoPath);
        return image;
    }


}