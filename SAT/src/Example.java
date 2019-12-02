
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kbotange
 */
public class Example {

    public static void main(String[] args) throws TimeoutException, ParseFormatException, IOException{
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(10); // 1 hour timeout
        Reader reader = new DimacsReader(solver);
        PrintWriter out = new PrintWriter(System.out,true);
        // CNF filename is given on the command line 
        
        try{
            IProblem problem = reader.parseInstance("H:/cg files/testcnf.cnf");
            if (problem.isSatisfiable()) {
                System.out.println("Satisfiable !");
                reader.decode(problem.model(),out);
            } else {
                System.out.println("Unsatisfiable !");
            }
        }catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        }
        
    }
}