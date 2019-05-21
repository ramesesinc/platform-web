/*
 * ProjectResolver.java
 *
 * Created on July 16, 2012, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.util.ConfigProperties;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class ProjectResolver {
    
    private final static Object LOCKED = new Object();
    
    private Project project; 
    private Boolean cached;
    
    /** Creates a new instance of ProjectResolver */
    public ProjectResolver() {
    }
    
    public Project removeProject() {
        synchronized( LOCKED ) {
            Project old = this.project; 
            this.project = null; 
            return old; 
        }
    }  
    
    public boolean isCached() { 
        if ( cached == null ) { 
            try { 
                String webcached = (System.getProperty("web.cached")+"").toLowerCase();
                if ( webcached.matches("true|false")) {
                    cached = new Boolean( webcached );
                } 
                else {
                    String path = System.getProperty("osiris.base.dir") +"/webroot/project.conf"; 
                    Map conf = new ConfigProperties(new java.io.File(path).toURI().toURL()).getProperties();
                    cached = new Boolean( "true".equals( conf.get("cached")+"" ));
                }
            } catch(Throwable t) {
                return true; 
            }
        }
        return cached.booleanValue(); 
    }
    
    public Project getProject() {
        synchronized( LOCKED ) {
            if ( project == null ) {
                String webrootpath = System.getProperty("osiris.base.dir") +"/webroot"; 
                try {
                    java.io.File f = new java.io.File( webrootpath ); 
                    project =  new Project("webroot", f.toURI().toURL().toString()); 
                } catch(RuntimeException re) {
                    throw re; 
                } catch(Throwable t) {
                    throw new RuntimeException(t); 
                }
            }
            return project; 
        }
    }
}
