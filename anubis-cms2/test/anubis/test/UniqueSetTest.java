/*
 * UniqueSetTest.java
 * JUnit based test
 *
 * Created on March 22, 2013, 1:29 PM
 */

package anubis.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import junit.framework.*;

/**
 *
 * @author Elmo
 */
public class UniqueSetTest extends TestCase {
    
    public UniqueSetTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public class Person implements Comparable {
        private String name;
        private String id;
        public Person(String n,String id) {
            this.name = n;
            this.id = id;
        }

        public int hashCode() {
            return this.name.hashCode();
        }

        public boolean equals(Object obj) {
            return hashCode() == obj.hashCode();
        }

        public String toString() {
            return name + ";" + id;
        }

        public int compareTo(Object o) {
            Person p = (Person)o;
            return name.compareTo( p.name );
        }

        
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testHello() {
        Set<Person> s1 = new LinkedHashSet();
        s1.add( new Person("elmo","1") );
        s1.add( new Person("worgie","1") );
        s1.add( new Person("jess","1") );
        
        Set s2 = new LinkedHashSet();
        s2.add( new Person("elmo","2") );
        s2.add( new Person("worgie","2") );
        s2.add( new Person("jess","2") );
        s2.add( new Person("jojo","2") );

        s1.addAll(s2);
        List<Person> list = new ArrayList();
        list.addAll( s1 );
        Collections.sort( list );
        for(Person p: list) {
            System.out.println(p);
        }
       
    }
    
}
