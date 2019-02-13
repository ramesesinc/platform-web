/*
 * FileDirTest.java
 * JUnit based test
 *
 * Created on July 19, 2012, 10:25 AM
 */

package anubis.test;

import com.rameses.anubis.PermalinkEntry;
import junit.framework.*;

/**
 *
 * @author Elmo
 */
public class FileDirTest_1 extends TestCase {
    
    public FileDirTest_1(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    
    
    public void testPath() throws Exception {
        String pat1 = "/about/*/*";
        String pattern = "^"+pat1.replaceAll("\\*", "([\\\\w|-]+)?" )+"$";
        
        System.out.println("pattern is " + pattern);
        System.out.println("/about/data/data1-permalink1/matcher".matches(pattern));
        PermalinkEntry pm = new PermalinkEntry("/about/[category]/[title]", "/path1");
        System.out.println(pm.getPattern());
    }
    
}
