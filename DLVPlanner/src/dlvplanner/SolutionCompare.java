/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlvplanner;
import java.util.Comparator;
/**
 *
 * @author kbotange
 */
public class SolutionCompare implements Comparator<Solution>{
   @Override
    public int compare(Solution p2, Solution p1){
        //if(Math.abs(p1.getTotal()-p2.getTotal()) < ERR) return 0;    
        return Double.compare(p1.getTotal(), p2.getTotal());
        /**if (p1.getTotal() < p2.getTotal()) return -1;
        if (p1.getTotal() > p2.getTotal()) return 1;
        return 0;*/
    }  
}
