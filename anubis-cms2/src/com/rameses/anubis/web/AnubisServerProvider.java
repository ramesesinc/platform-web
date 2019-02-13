/*
 * AnubisServerProvider.java
 *
 * Created on March 27, 2013, 1:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.server.ServerLoader;
import com.rameses.server.ServerLoaderProvider;
/**
 *
 * @author Elmo
 */
public class AnubisServerProvider implements ServerLoaderProvider {
    
   
    public String getName() {
        return "anubis";
    }

    public ServerLoader createServer(String name) {
        return new AnubisWebServer(name);
    }
    
}
