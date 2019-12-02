/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This java module checks the consistency of contextual goals. It is treated as a separate module for the DLV-ReqVar
 * Inputs are the contextual goal model AND-OR structure elements 
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

public class ConsistencyChecker {
    private static final String PATH_DLV = "C:/Users/kbotange/Desktop/dlv.mingw";
        private static final String PATH_1 = "C:/Users/kbotange/Desktop/personalMedAssistJournal_cpBig_PerfEval9b.txt";
              
              //   "/personalMedAssistJournal_cpBig_PerfEval1b.txt"  for performance evaluation consistency checking
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
                        System.out.print("{ ");
                        Model model = modelBufferedHandler.nextModel();			
                        
                        model.beforeFirst();                                   
                       
                        
              
                        
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
                        System.out.print(str);
                        


                        
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
                           File file = new File("C:/Users/kbotange/Desktop/cg files/" + ctx + ".cnf");
                           Scanner sc = new Scanner(file);
                           
                           while (sc.hasNextInt()){
                                int value = sc.nextInt();
                                cnfList.add(value);                                                                 
                                cnfListIndiv.add(value);
                           }
                                                   
                        // // // checks consistency of the particular cg   
                            
                           File f = new File("C:/Users/kbotange/Desktop/cg files/relations.cnf");
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
                           IProblem problem = reader.parseInstance("C:/Users/kbotange/Desktop/cg files/filename.txt"); //H:/cg files
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
                           File file = new File("C:/Users/kbotange/Desktop/cg files/relations.cnf");
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
                       IProblem problem = reader.parseInstance("C:/Users/kbotange/Desktop/cg files/filename.txt"); 
                       if (problem.isSatisfiable()) {
                        System.out.print(">>>sol sat!");
                        reader.decode(problem.model(),out);
                        sat++;
                       } else {
                        System.out.print(">>>sol unsat!");
                        unsat++;
                       }
                       }catch (ContradictionException e) {
                        System.out.print(">>>sol unsat(trivial)!");
                        unsat++;
                       } catch (ParseFormatException | org.sat4j.specs.TimeoutException ex) {
                        Logger.getLogger(OfficalDLVWrapperTest.class.getName()).log(Level.SEVERE, null, ex);
                       }
//code for CheckSAT analysis ends here                       
                        

//prints the close bracket
                    System.out.println("}");
			
                    modelCounter++;    

                }
                
                System.out.print("\n");
                System.out.println("No of satisfiable solutions: " +sat);
                System.out.println("No of unsatisfiable solutions: " +unsat);
                System.out.print("\n");
                
                long consistencyCheckTime = System.currentTimeMillis();
                System.out.println("consistencyCheckTime " + (consistencyCheckTime - startTime) + " milliseconds");  
                
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
                
      
                
        }
}
