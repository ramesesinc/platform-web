/*
 * SubstituteTest.java
 * JUnit based test
 *
 * Created on August 31, 2012, 10:36 AM
 */

package anubis.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.*;

/**
 *
 * @author Elmo
 */
public class SubstituteTest extends TestCase {
    
    public SubstituteTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testHello() {
        String ss = "file:///${user.dir}/home/${user.home}/myfile";
        Pattern p = Pattern.compile("\\$\\{.*?\\}");
        Matcher m = p.matcher(ss);
        StringBuffer sb = new StringBuffer();
        String s1 = ss;
        int start = 0;
        while(m.find()) {
            sb.append( s1.substring(start, m.start()) );
            String s = m.group();
            s = s.replaceAll("\\$|\\{|\\}","");
            sb.append(System.getProperty(s));
            start = m.end();
        }
        if( start < s1.length()  ) sb.append( s1.substring(start));
        System.out.println(sb.toString().replace("\\", "/"));
    }
    
}
