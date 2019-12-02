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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.Scanner;

import it.unical.mat.dlv.program.Literal;
import it.unical.mat.dlv.program.Program;
import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInputProgramImpl;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import it.unical.mat.wrapper.Model;
import it.unical.mat.wrapper.ModelBufferedHandler;
import it.unical.mat.wrapper.ModelHandler;
import it.unical.mat.wrapper.ModelResult;
import it.unical.mat.wrapper.Predicate;
import it.unical.mat.wrapper.PredicateHandlerWithName;
import it.unical.mat.wrapper.PredicateResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;


public class OfficalDLVWrapperTest {
	
        private static final String PATH_DLV = "C:/Users/kbotange/Desktop/dlv.mingw";
        private static final String PATH_1 = "H:/patientCaregiving_cpBig.txtg";
              //   "/personalMedAssistJournal_cg.txt" this file is for deriving solutions based on contextual goals
              //   "/personalMedAssistJournal.txt" this file is for deriving solutions based on contextual preferences
              //   "/personalMedAssistJournal_cgBig.txt" for experiment on bigger personal medication assistant contextual goals 
              //   "/personalMedAssistJournal_cpBig.txt" for experiment on bigger personal medication assistant contextual preferences
              //   "/patientCaregiving_cpBig.txt" for experiment on patient caregiving system 
              //   "/visitorAssistance_cpBig.txt" for experiment on visitor assistance system
              //   "/personalMedAssistJournal_cpBig_PerfEval1.txt"  for performance evaluation w/o consistency checking
              
        private static final String FILENAME = "C:\\Users\\kbotange\\Desktop\\cg files\\filename.txt";
        
        public static void main(String[] args) throws DLVInvocationException, IOException {
		
                long startTime = System.currentTimeMillis();
                
                /* I create a new instance of DLVInputProgram */
		
                DLVInputProgram inputProgram = new DLVInputProgramImpl();

		/* I can add some file to the DLVInputProgram */
		
		inputProgram.addFile(PATH_1);
		// inputProgram.addFile(file2);

		/* I can specify a part of DLV program using simple strings */
		//inputProgram.addText("arc(a,b). arc(b,c). arc(b,d). path(X,Y) :- arc(X,Y).");

		/*
		 * I can specify a part of DLV program using an entire Program or an
		 * Expression
		 */
		//Program program = new Program();
		// program.add(new Rule(" A STRING "));
		//inputProgram.includeProgram(program);

		/*
		 * I can add directly an Expression that will be added into the default
		 * program stored in the DLVInvocation
		 */
		// inputProgram.addExpression(new Rule(" A STRING "));
		// The next step is to prepare a new DLV invocation, the class
		// DLVWrapper provides a new instance of DLVInvocation, using the
		// default path or specifying another path of DLV executable:
		/*
		 * I create a new instance of DLVInvocation using the DLVWrapper class
		 * specifying a path for DLV executable
		 */
		DLVInvocation invocation = DLVWrapper.getInstance().createInvocation(PATH_DLV);
		// The DLVInvocation object is configurable using different parameters
		// before the real execution. The DLVInvocation class provides methods
		// necessary to set the query and to set the execution options.
		/*
		 * Set the query of DLV invocation. If the Query in the DLVInvocation is
		 * not null, any queries in the inputProgram(in the text, files or
		 * defaultProgram) is ignored.
		 */
		// invocation.setQuery(new Query( QUERY STRING );

		/*
		 * The class DLVInvocation have a method to specify the execution
		 * options by simple strings
		 */
		invocation.addOption("-nofacts");
                
                
		/*
		 * Alternatively DLVInvocation provides specified methods to set the
		 * execution options
		 */

		/*
		 * Set the max-int '-N' option. Set the limit of the integer to [0,N] in
		 * the DLV invocation. This function remove automatically any existing
		 * max-int '-N' option.
		 */
		invocation.setMaxint(100000);

		/*
		 * Sets the max number of models computed in the DLV execution. If n is
		 * 0, all models are computed. This method deletes each option that
		 * contains the string "-n=" and adds a new option "-n='n'.
		 */
		invocation.setNumberOfModels(1000000);

		/*
		 * Includes only instances of the predicate specified in the filter
		 * parameter in output. This method deletes each option that contains
		 * the string filter= or -pfilter=, and adds a new option with the new
		 * filter
		 */
		List<String> filters = new ArrayList<>();
		filters.add("perform");
                filters.add("posContribTo");
                filters.add("negContribTo");
		filters.add("softPriority");
                filters.add("hardPriority");
                filters.add("cntxt");
                invocation.setFilter(filters, true);
		
                // The options can be eliminated with the respective methods:
		/* Remove any n° of models option */
		//invocation.resetNumberOfModels();

		/* Remove any max-int option */
		//invocation.resetMaxint();

		/* Reset any filter option */
		//invocation.resetFilter();

		/* Reset all options */
		//invocation.resetFilter();
		// When the invocation setting is complete, can be create the observers
		// that receive the notifications about the results of execution. Every
		// DLVHandler (the observer) must be registered on the DLVInvocation
		// (the observable) to receive the notifications.
		/*
		 * I create a new observer that receive a notification for the models
		 * computed and store them in a list
		 */
		final List<ModelResult> models = new ArrayList<ModelResult>();
		ModelHandler modelHandler = new ModelHandler() {
			@Override
			final public void handleResult(DLVInvocation obsd, ModelResult res) {
				models.add(res);
			}
		};

		/* Subscribe the handler from the DLVInvocation */
		invocation.subscribe(modelHandler);

		/*
		 * I create a new observer that receive a notification for the
		 * predicates computed with a specified name and store them in a list
		 */
		/**
                final List<PredicateResult> predicates = new ArrayList<PredicateResult>();
		PredicateHandlerWithName predicateHandler = new PredicateHandlerWithName() {
			public void handleResult(DLVInvocation obsd, PredicateResult res) {
				predicates.add(res);
			}

			public List<String> getPredicateNames() {
				List<String> predicates = new ArrayList<String>();
				predicates.add("perform");
				predicates.add("posContribTo");
				return predicates;
			}
		};
                */
		/* Subscribe the handler from the DLVInvocation */
		//invocation.subscribe(predicateHandler);

		/*
		 * Another mode to scroll the computed models is to use the
		 * ModelBufferedHandler that is a concrete observer that work like
		 * Enumeration. NOTE: the ModelBufferedHandler subscribe itself to the
		 * DLVInvocation
		 */
		ModelBufferedHandler modelBufferedHandler = new ModelBufferedHandler(invocation);

		/* In this moment I can start the DLV execution */
		invocation.setInputProgram(inputProgram);
                invocation.run();
                
		/* Scroll all models computed */
		
                
                
                List<String> softgoalList = new ArrayList<>();
                List<String> hardgoalList = new ArrayList<>();
                List<Integer> softgoalWeight = new ArrayList<>();
                List<Integer> hardgoalWeight = new ArrayList<>();
                
                List<Integer> softgoalPosCount = new ArrayList<>();
                List<Integer> softgoalNegCount = new ArrayList<>();
                List<Double> softgoalScore = new ArrayList<>();
                List<Double> hardgoalScore = new ArrayList<>();
                List<Double> preferenceDegree = new ArrayList<>();
                List<String> solution = new ArrayList<>();
                List<String> contextList = new ArrayList<>();
                
                               
                List<Integer> cnfList = new ArrayList<>();
                Set<Integer> cnfListTemp = new HashSet<>();
                
                List<Integer> cnfListIndiv = new ArrayList<>();
                Set<Integer> cnfListTempIndiv = new HashSet<>();
                
                
                int modelCounter = 0;
                int sat = 0;
                int unsat = 0;
               
                /**               
   //prints all candidate solutions
                while (modelBufferedHandler.hasMoreModels()) { 
                System.out.print(modelCounter+1 + " {");
                    Model model = modelBufferedHandler.nextModel();			                      
                        model.beforeFirst(); 
                        while (model.hasMorePredicates()) {                                                                                                                  
                            Predicate predicate = model.nextPredicate();                                         			        
                            predicate.beforeFirst();
                            while (predicate.hasMoreLiterals()) {                                
                            Literal literal = predicate.nextLiteral();                                
                                
                                System.out.print(literal);
                                System.out.print(" ");                            
                            }
                        
                        }
                System.out.print("}");
                System.out.println();
                
                long endTime = System.currentTimeMillis();

                System.out.println("That took " + (endTime - startTime) + " milliseconds");
                
                modelCounter++;
                }                  
                
                modelCounter=0;
                
                */
                
                while (modelBufferedHandler.hasMoreModels()) {                                                            
                                                
                        softgoalList.clear();
                        hardgoalList.clear();
                        softgoalPosCount.clear();
                        softgoalNegCount.clear();
                        softgoalWeight.clear();
                        hardgoalWeight.clear();
                        contextList.clear();                        
                        cnfList.clear();
                        cnfListTemp.clear();
                        
                        int softnum = 0;
                        int hardnum = 0;
                        int contextnum = 0;
//prints the opening bracket                       
                        //System.out.print("{ ");
                        Model model = modelBufferedHandler.nextModel();			
                        
                        model.beforeFirst();                                   
                       
                        
              //looks for softgoals with positive or negative contributions by a solution
                        Predicate softgoal = model.getPredicate("softPriority");
                        if(softgoal != null){
                        softgoal.beforeFirst();
                        //while (softgoal.hasMoreLiterals()) {                                
                        //    Literal literal = softgoal.nextLiteral();
                        //    softgoalList.add(literal.getAttributeAt(0).toString());
                        //    softgoalWeight.add(literal.getAttributeAt(1).toString());
                        //    softnum++;                                    
                        //}
                        
                        while (softgoal.hasMoreLiterals()) {                                
                            Literal literal = softgoal.nextLiteral();
                            if(softgoalList.isEmpty()){
                                softgoalList.add(literal.getAttributeAt(0).toString());
                                softnum++;    
                            }else{
                                if(!(softgoalList.contains(literal.getAttributeAt(0).toString()))){
                                   softgoalList.add(literal.getAttributeAt(0).toString()); 
                                   softnum++;
                                }
                            }
                        }
                        }
                        
   //stores the weights of preferred softgoals
                        int j = softnum;
                        while(j != 0){ 
                        int maxWeight = 0;
                        if(softgoal != null){
                        softgoal.beforeFirst();
                        while (softgoal.hasMoreLiterals()) {                                
                            Literal literal = softgoal.nextLiteral();
                            if(softgoalList.get(softnum-j).compareTo(literal.getAttributeAt(0).toString())==0 && maxWeight < Integer.valueOf(literal.getAttributeAt(1).toString())){
                                maxWeight=Integer.valueOf(literal.getAttributeAt(1).toString());   
                            }                          
                        }
                        }
                        softgoalWeight.add(maxWeight);
                        //System.out.print(hardgoalList.get(hardnum-i)+":"+hardgoalWeight.get(hardnum-i));
                        j--;
                        }
                        
                        int index = softnum;
                        while (index!=0){
                            int countPos=0;  
                            int countNeg=0;
                        //counts  positive contributions to the preferred softgoals    
                            Predicate posContrib = model.getPredicate("posContribTo");
                            if(posContrib != null){
                            posContrib.beforeFirst();
                            while(posContrib.hasMoreLiterals()) { 
                                Literal literal = posContrib.nextLiteral();
                                if(literal.getAttributeAt(1).toString().compareTo(softgoalList.get(softnum-index))==0){
                                    countPos++;
                                }
                            }
                            }
                            
                        //counts  negative contributions to the preferred softgoals    
                            Predicate negContrib = model.getPredicate("negContribTo");
                            if(negContrib != null){
                            negContrib.beforeFirst();
                            while(negContrib.hasMoreLiterals()) { 
                                Literal literal = negContrib.nextLiteral();
                                if(literal.getAttributeAt(1).toString().compareTo(softgoalList.get(softnum-index))==0){
                                    countNeg++;
                                }
                            }
                            }
                            softgoalPosCount.add(countPos);    
                            softgoalNegCount.add(countNeg);                            
                        
                           // System.out.print(softgoalList.get(softnum-index)+"+:"+ softgoalPosCount.get(softnum-index)+" ");
                           // System.out.print(softgoalList.get(softnum-index)+"-:"+ softgoalNegCount.get(softnum-index)+" ");
                            
                            index--;
                        }
                                                                  
                        
                        //computes the softgoal score of a solution
                        index = 0;
                        double totalSoftScore = 0;
                        while (index < softnum){
                        double score = 0;
                        double countLinks = softgoalPosCount.get(index) + softgoalNegCount.get(index);
                        if (countLinks > 0)                                                                        
                            score = ((softgoalPosCount.get(index)/(softgoalPosCount.get(index) + softgoalNegCount.get(index))) * softgoalWeight.get(index)) - ((softgoalNegCount.get(index)/(softgoalPosCount.get(index) + softgoalNegCount.get(index))) * softgoalWeight.get(index));
                        totalSoftScore = totalSoftScore + score;     
                        //System.out.print(softgoalList.get(index)+"+:"+softgoalPosCount.get(index)+" ");
                        //System.out.print(softgoalList.get(index)+"-:"+softgoalNegCount.get(index)+" ");
                        //System.out.print(softgoalList.get(index)+":"+softgoalWeight.get(index)+" ");    
                        //System.out.print("total_"+softgoalList.get(index)+":"+score+"     ");
                        index++;    
                        }
                        softgoalScore.add(totalSoftScore);
                        //System.out.print("sps:"+softgoalScore.get(modelCounter)+" ");
                        
                        //looks for preferred hardgoals satisfied in the solution
                        Predicate hardgoal = model.getPredicate("hardPriority");
                        if(hardgoal != null){
                        hardgoal.beforeFirst();
                        while (hardgoal.hasMoreLiterals()) {                                
                            Literal literal = hardgoal.nextLiteral();
                            if(hardgoalList.isEmpty()){
                                hardgoalList.add(literal.getAttributeAt(0).toString());
                                hardnum++;    
                            }else{
                                if(!(hardgoalList.contains(literal.getAttributeAt(0).toString()))){
                                   hardgoalList.add(literal.getAttributeAt(0).toString()); 
                                   hardnum++;
                                }
                            }
                        }  
                        }
                        //stores the weights of preferred hardgoals
                        int i = hardnum;
                        while(i != 0){ 
                        int maxWeight = 0;
                        if(hardgoal != null){
                        hardgoal.beforeFirst();
                        while (hardgoal.hasMoreLiterals()) {                                
                            Literal literal = hardgoal.nextLiteral();
                            if(hardgoalList.get(hardnum-i).compareTo(literal.getAttributeAt(0).toString())==0 && maxWeight < Integer.valueOf(literal.getAttributeAt(1).toString())){
                                maxWeight=Integer.valueOf(literal.getAttributeAt(1).toString());   
                            }                          
                        }
                        }
                        hardgoalWeight.add(maxWeight);
    //prints preferred hardgoals                    
                        //System.out.print(hardgoalList.get(hardnum-i)+":"+hardgoalWeight.get(hardnum-i)+" ");
                        i--;
                        }    
                        
                        //computes the hardgoal score of a solution
                        int x = 0;
                        double totalHardScore = 0;
                        while(x < hardnum){
                            totalHardScore = totalHardScore + hardgoalWeight.get(x);
                            x++;    
                        }
                        
                        hardgoalScore.add(totalHardScore);
                        
    //prints hps
                        //System.out.print("hps:"+hardgoalScore.get(modelCounter)+" ");
                        
    //stores into an array the tasks to be performed per solution                          
                        String str="";
                        Predicate performtask = model.getPredicate("perform");
                        performtask.beforeFirst();
                        while (performtask.hasMoreLiterals()) {                                
                            Literal literal = performtask.nextLiteral();
                            str = str.concat(literal.toString());
                        }         
                        
                        solution.add(str);
    //prints a solution                
                    //    System.out.print(str);
                        
    //computes the preference degree of a solution                        
                        double preference = totalSoftScore + totalHardScore;
                        preferenceDegree.add(preference);
                    //    System.out.print("psd:"+preference);

/**                        
// CheckSAT module                      
//stores into an array the contextual goals found in a solution, this will be later used in the context consistency analysis                          
                        String ctx="";
                        Predicate contextualgoal = model.getPredicate("cntxt");
                        if(contextualgoal != null){
                        contextualgoal.beforeFirst();
                        while (contextualgoal.hasMoreLiterals()) {                                
                            Literal literal = contextualgoal.nextLiteral();
                                contextList.add(literal.getAttributeAt(0).toString());
                                contextnum++;    
                        }  
                        }
                        
                        
                        int y = contextnum;
                        while(y!=0){
                           ctx = contextList.get(contextnum-y);
                           
                           cnfListIndiv.clear();
                           cnfListTempIndiv.clear();
                           
                        // reads the cnf files associated with the solution and store it to cnfList  
                           File file = new File("H:/cg files/" + ctx + ".cnf");
                           Scanner sc = new Scanner(file);
                           
                           while (sc.hasNextInt()){
                                int value = sc.nextInt();
                                cnfList.add(value);                                                                 
                                cnfListIndiv.add(value);
                           }
                                                   
                        // // // checks consistency of the particular cg   
                            
                           File f = new File("H:/cg files/relations.cnf");
                           Scanner scan = new Scanner(f);                           
                           while (scan.hasNextInt()){
                                cnfListIndiv.add(scan.nextInt());                                                                 
                           } 
                           
                           int z = cnfListIndiv.size();
                           int lines = 0;
                           while (z > 0){
                           if(cnfListIndiv.get(cnfListIndiv.size()-z) < 0)
                            cnfListTempIndiv.add(cnfListIndiv.get(cnfListIndiv.size()-z) * -1);
                           else if(cnfListIndiv.get(cnfListIndiv.size()-z) > 0)
                            cnfListTempIndiv.add(cnfListIndiv.get(cnfListIndiv.size()-z));
                           else 
                            lines ++;                                                   
                           z--;
                           }
                           
                           try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {

                           String content = "p cnf " + cnfListTempIndiv.size() + " " + lines;

                           bw.write(content);
                           bw.newLine();
                        
                           z = cnfListIndiv.size();
                           String s = "";
                           while (z > 0){
                           if (cnfListIndiv.get(cnfListIndiv.size()-z)!= 0)
                               s = s + " " + cnfListIndiv.get(cnfListIndiv.size()-z);
                           else{
                               s = s + " 0"; 
                               bw.write(s);
                               bw.newLine();
                               s ="";
                           }
                           
                           z--;  
                           }
			
                           } catch (IOException e) {
                            System.out.println("Error writing file "+ e);
                           }        
                           
                           ISolver solver = SolverFactory.newDefault();
                           solver.setTimeout(10); // 1 hour timeout
                           Reader reader = new DimacsReader(solver);
                           PrintWriter out = new PrintWriter(System.out,true);
                       
                           try{
                           IProblem problem = reader.parseInstance("H:/cg files/filename.txt");
                           if (problem.isSatisfiable()) {
                           System.out.print(ctx + " ");
                           //System.out.print("Satisfiable!");
                           //reader.decode(problem.model(),out);                          
                           } else {
                           System.out.print(ctx + "(Unsatisfiable!) ");                        
                           }
                           }catch (ContradictionException e) {
                           System.out.print(ctx + "(Unsatisfiable!) ");                       
                           } catch (ParseFormatException | org.sat4j.specs.TimeoutException ex) {
                           Logger.getLogger(OfficalDLVWrapperTest.class.getName()).log(Level.SEVERE, null, ex);
                           }                        
                        // // //                        
                           
                                                                              
                           y--;                           
                        }
                        
                        //reads the logical relations file and adds the content to cnfList
                           File file = new File("H:/cg files/relations.cnf");
                           Scanner sc = new Scanner(file);
                           
                           while (sc.hasNextInt()){
                                cnfList.add(sc.nextInt());                                                                 
                           }
                        
                        
                        int z = cnfList.size();
                        int lines = 0;
                        
                        // counts the number of lines and unique variables in the cnf, cnfListTemp is of type SET, thus only stores unique values
                        while (z > 0){
                        if(cnfList.get(cnfList.size()-z) < 0)
                            cnfListTemp.add(cnfList.get(cnfList.size()-z) * -1);
                        else if(cnfList.get(cnfList.size()-z) > 0)
                            cnfListTemp.add(cnfList.get(cnfList.size()-z));
                        else 
                            lines ++;    //no of lines                        
                            
                        //System.out.print(cnfList.get(cnfList.size()-z) + " "); //outputs the content of the cnfList
                        z--;
                        }
                        
                        //creates the single cnf file that combines all the associated contextual goals and the logical relations
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {

			String content = "p cnf " + cnfListTemp.size() + " " + lines;

			bw.write(content);
                        bw.newLine();
                        
                        z = cnfList.size();
                        String s = "";
                        while (z > 0){
                           if (cnfList.get(cnfList.size()-z)!= 0)
                               s = s + " " + cnfList.get(cnfList.size()-z);
                           else{
                               s = s + " 0"; 
                               bw.write(s);
                               bw.newLine();
                               s ="";
                           }
                           
                           z--;  
                        }
			
                        } catch (IOException e) {
                        System.out.println("Error writing file "+ e);
                        }                                
                                               
                        
                       //System.out.print("Number of variables: "+cnfListTemp.size());
                       //System.out.print("Number of lines: "+lines);
                     
                       //Determines satisfiability
                       ISolver solver = SolverFactory.newDefault();
                       solver.setTimeout(10); // 1 hour timeout
                       Reader reader = new DimacsReader(solver);
                       PrintWriter out = new PrintWriter(System.out,true);
                       
                       try{
                       IProblem problem = reader.parseInstance("H:/cg files/filename.txt");
                       if (problem.isSatisfiable()) {
                        System.out.print(">>>solution satisfiable!");
                        reader.decode(problem.model(),out);
                        sat++;
                       } else {
                        System.out.print(">>>solution unsatisfiable!");
                        unsat++;
                       }
                       }catch (ContradictionException e) {
                        System.out.print(">>>solution unsatisfiable(trivial)!");
                        unsat++;
                       } catch (ParseFormatException | org.sat4j.specs.TimeoutException ex) {
                        Logger.getLogger(OfficalDLVWrapperTest.class.getName()).log(Level.SEVERE, null, ex);
                       }
//code for CheckSAT analysis ends here                       
*/                        

//prints the close bracket
                    //System.out.println("}");
			
                    modelCounter++;    

                }
                
                                
		/* If i want to wait the finish of execution, i can use this method */
		invocation.waitUntilExecutionFinishes();
                
		/*
		 * At the term of execution, I can control the errors created by DLV
		 * invocation
		 */
		List<DLVError> errors = invocation.getErrors();
               
                /**
                int j = 0;
                Solution[] sol1 = new Solution[modelCounter];
                while(j < modelCounter){
                    sol1[j] = new Solution(solution.get(j), softgoalScore.get(j), hardgoalScore.get(j), preferenceDegree.get(j));    
                    j++;
                }
                */              
                
//transfers the solutions to an array for sorting 
                int j = 0;
                ArrayList<Solution> sol = new ArrayList<>();
                while(j < modelCounter){                
                    sol.add(new Solution(solution.get(j), softgoalScore.get(j), hardgoalScore.get(j), preferenceDegree.get(j)));
                    j++;
                }    
    
                Collections.sort(sol, new SolutionCompare());                             
                
                j = 0;
                while(j < modelCounter){
                    System.out.println((j+1) +":   "+ sol.get(j).getTask() + "psd:" + sol.get(j).getTotal());                    
                    j++;
//prints time to get the optimal solution
                    if(j==1){
                        long optimalTime = System.currentTimeMillis();
                        System.out.println("optimalTime " + (optimalTime - startTime) + " milliseconds");        
                    }
//prints time to get the top 10 solutions                    
                    if(j==10){
                        long toptenTime = System.currentTimeMillis();
                        System.out.println("toptenTime " + (toptenTime - startTime) + " milliseconds");        
                    }
                    
                }
//prints time to derive all solutions        
        long allTime = System.currentTimeMillis();
        System.out.println("allTime " + (allTime - startTime) + " milliseconds");        
                
        }
}
