/*
 * PermalinkManager.java
 *
 * Created on March 8, 2013, 8:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.util.ConfigProperties;
import java.net.URL;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class PermalinkManager {
    
    private Project project;
    private PageMapper mapper;
    
    public PermalinkManager( Project project ) {
        this.project = project;
        this.mapper = new PageMapper();
    }
    
    public void init(ConfigProperties conf) {
        URL url = null;
        try {
            url = new URL( project.getUrl() + "/page-mapping.conf" );
            mapper.load( url.openStream() ); 
        } catch(Throwable t) {
            System.out.println("failed to load "+ url); 
        }
    }
        
    public String resolveName(String path, Map params) {
        PageMapperResult res = mapper.getFileSource(path); 
        params.putAll( res.getParams() ); 
        return res.getFilePath() + ".pg"; 
    }
}
