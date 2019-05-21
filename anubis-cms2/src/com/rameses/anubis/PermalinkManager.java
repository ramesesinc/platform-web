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
import java.util.ArrayList;
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
    
    public void clear() {
        mapper.clear();
    }
    
    public void init(ConfigProperties conf) {
        ArrayList<String> urls = new ArrayList();
        urls.add( project.getUrl() + "page-mapping.conf" ); 
        for (Module mod : project.getModules().values()) {
            urls.add( mod.getUrl() + "page-mapping.conf" ); 
        }

        URL url = null;
        for ( String path : urls ) {
            try {
                url = new URL( path );
                mapper.load( url.openStream() ); 
            } catch(Throwable t) {
                System.out.println("failed to load "+ url); 
            }
        }
    }
        
    public String resolveName(String path, Map params) {
        PageMapperResult res = mapper.getFileSource(path); 
        params.putAll( res.getParams() ); 
        return res.getFilePath() + ".pg"; 
    }
    
    public PageMapperResult resolve( String path ) {
        return mapper.getFileSource(path); 
    }
}
