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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class PermalinkManager {
    
    private List<PermalinkEntry> mappings = new ArrayList();
    
    public void init(ConfigProperties conf) {
        Map permaLinks = conf.getProperties("permalink-mapping");
        if(permaLinks!=null) {
            for(Object o: permaLinks.entrySet()) {
                Map.Entry me = (Map.Entry)o;
                addMapping( me.getKey()+"", me.getValue()+"" );
            }
        }
    }
    
    public void addMapping(String pattern, String page) {
        pattern = pattern.trim();
        if(!pattern.startsWith("/")) pattern = "/" + pattern;
        page = page.trim();
        if(!page.startsWith("/")) page = "/" + page;
        if( page.endsWith("/.*")) {
            //do nothing
        }
        else if(!page.endsWith(".pg") ) {
            page = page + ".pg";
        }
        mappings.add( new PermalinkEntry(pattern, page) );
    }
    
    public String resolveName(String path, Map params) {
        String resolvedName = null;
        for(PermalinkEntry m: mappings) {
            if( m.matches(path)) {
                PermalinkEntry.MatchResult mr = m.buildResult(path);
                resolvedName = mr.getResolvedPath();
                params.putAll( mr.getTokens() );
                break;
            }
        }
        return resolvedName;
    }
    
    
}
