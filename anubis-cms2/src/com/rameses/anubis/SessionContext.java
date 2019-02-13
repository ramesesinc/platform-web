/*
 * SessionContext.java
 *
 * Created on July 6, 2012, 3:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public abstract class SessionContext {
    
    private String username;
    
    //this is used to store any kind of information that will be retained
    //until the request is completed
    protected Map info = new HashMap();
   
    public Map getInfo() {
        return info;
    }
    
    public abstract String getSessionid();
    public abstract String createSession(String sessionid);
    
    public abstract String destroySession();
    
    public abstract boolean checkPermission(String domain, String role, String key);

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    
    
}

