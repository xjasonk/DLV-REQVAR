/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlvplanner;

/**
 *
 * @author kbotange
 */
public class Solution{
    private String task; 
    private double score1;
    private double score2;
    private double total;

public Solution(String task, double score1, double score2, double total){
    
    this.task = task;
    this.score1 = score1;
    this.score2 = score2;
    this.total = total;
    
}
public String getTask(){
return task;
}

public double getScore1(){
return score1;
}

public double getScore2(){
return score2;
}

public double getTotal(){
return total;
}

   
   

}
