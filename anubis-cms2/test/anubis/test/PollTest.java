/*
 * PollTest.java
 * JUnit based test
 *
 * Created on March 29, 2013, 10:59 AM
 */

package anubis.test;

import com.rameses.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import junit.framework.*;

/**
 *
 * @author Elmo
 */
public class PollTest extends TestCase {
    
    public PollTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void ztestGet() throws Exception {
        Thread t = new Thread( new Runnable(){
            public void run() {
                System.out.println("starting test 1");
                HttpClient http = new HttpClient("localhost:18090");
                try {
                    String s = (String) http.get("poll/channel1/token1");
                    System.out.println("test get 1 " + s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        });
        t.start();
    }

    public void ztestGet2() throws Exception {
        Thread t = new Thread( new Runnable(){
            public void run() {
                System.out.println("starting test 2");
                HttpClient http = new HttpClient("localhost:18090");
                try {
                    String s  = (String) http.get("poll/channel1/token2");
                    System.out.println("test get 2 " + s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
            }
            
        });
        t.start();
    }

    /*
    public void testGet2() throws Exception {
        HttpClient http = new HttpClient("localhost:18090");
        String s = (String) http.get( "poll/channel1/token2" );
        System.out.println("test get 2 " + s);
    }
     */

    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testPost() throws Exception {
        System.out.println("starting post");
        HttpClient http = new HttpClient("classroom.gazeebu.com:18080");
        Map map = new HashMap();
        map.put("data", "Hello worgie wazzup now again? whats worng with the first two th322");
        map.put("lon", 10.309684);
        map.put("lat", 122.893691);
        map.put("title", "Hello worgie 321 ");
        http.post( "poll/gmap", map );
    }

    

}
