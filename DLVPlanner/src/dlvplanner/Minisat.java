/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlvplanner;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.LecteurDimacs;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * SAT4J-based Solver.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Minisat{

    public boolean isSatisfiable(String input){
        boolean result = true;
        ISolver solver = null;
        try {
            solver = SolverFactory.newLight();
            // timer-based timeout creates a thread per problem :\
            // for large matching tasks this exhausts resources
            solver.setTimeoutOnConflicts(Integer.MAX_VALUE);
            Reader reader = new LecteurDimacs(solver);
            IProblem problem = reader.parseInstance(new ByteArrayInputStream(input.getBytes()));
            result = problem.isSatisfiable();
        } catch (ParseFormatException | TimeoutException | IOException e) {
            
        } catch (ContradictionException e) {
            result = false;
        } finally {
            if (null != solver) {
                solver.reset();
            }
        }
        return result;
    }
}