/*
 * AnubisResourceServerProvider.java
 *
 * Created on September 25, 2013, 11:01 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.server.ServerLoader;
import com.rameses.server.ServerLoaderProvider;

/**
 *
 * @author wflores
 */
public class AnubisResourceServerProvider implements ServerLoaderProvider 
{

    public String getName() { 
        return "resource"; 
    } 

    public ServerLoader createServer(String name) {
        return new AnubisResourceServer(name); 
    }
    
}
