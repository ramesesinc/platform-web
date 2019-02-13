/*
 * FileDirTest.java
 * JUnit based test
 *
 * Created on July 19, 2012, 10:25 AM
 */

package anubis.test;

import java.util.Scanner;
import junit.framework.*;

/**
 *
 * @author Elmo
 */
public class FileDirTest_2 extends TestCase {
    
    public FileDirTest_2(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
  
    public void testPath() throws Exception {
        Scanner c = new Scanner(System.in);
        System.out.println(c.next());
    }
    
}
