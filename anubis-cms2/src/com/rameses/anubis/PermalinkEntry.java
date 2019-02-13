/*
 * PageMapping.java
 *
 * Created on June 14, 2012, 8:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Elmo
 * the basic match is as follows:
 *
 * if url mapping is /blogs/[year]/[month]
 * it should match exactly /blogs/2001/01 and not /blogs/2001/01/01
 * 2001 will be placed in year while 01 will be month
 */
public class PermalinkEntry {
    
    private Pattern parser = Pattern.compile("\\[.*?\\]");
    private MessageFormat formatter;
    private String pattern;
    private List tokens;
    private String mappingPattern;
    private String page;
    
    public PermalinkEntry(String mapping, String path) {
        init(mapping);
        this.mappingPattern = mapping;
        this.page = path;
    }
    
    private void init(String text) {
        tokens = new ArrayList();
        Matcher m = parser.matcher(text);
        StringBuilder sb = new StringBuilder();
        StringBuilder sf = new StringBuilder();
        int start = 0;
        int counter = 0;
        while(m.find()) {
            sb.append( text.substring(start, m.start()) + "([\\w|-]{1,})?" );
            sf.append( text.substring(start, m.start()) + "{"+  (counter++) + "}" );
            String stext = text.substring(m.start(),m.end()).trim();
            tokens.add( stext.substring(1, stext.length()-1)  );
            start = m.end();
        }
        if( start < text.length()  ) {
            sb.append( text.substring(start));
            sf.append( text.substring(start) );
        }
        pattern = sb.toString();
        
        String fp = sf.toString();
        if( fp.endsWith(".*")) fp = fp.substring(0, fp.lastIndexOf(".*"));
        formatter = new MessageFormat(fp);
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public List getTokens() {
        return tokens;
    }
    
    /*
     * This routine will check if the path is matched.
     * A successful match will return the tokens, if not it returns null;
     */
    public boolean matches(String path ) {
        //System.out.println("Permalink match: "+ path +" == "+ pattern +" ? "+ path.matches(pattern));
        return path.matches(pattern);
    }
    
    private Map getTokens(String path) {
        Map m = new LinkedHashMap();
        try {
            Object[] objs = formatter.parse( path, new ParsePosition(0));
            if ( objs == null ) return m; 
            
            for( int i=0; i<tokens.size(); i++) {
                m.put( tokens.get(i), objs[i] );
            }
            return m;
        }
        catch(Exception e) {
            System.out.println("Error in PermalinkEntry.getTokens -> "+ path);
            return m;
        }
    }
    
    private String getPage() {
        return page;
    }
    
    private String replaceWithTokens(String spath, Map tokens) {
        String s = spath;
        for(Object o : tokens.entrySet()) {
            Map.Entry me = (Map.Entry)o;
            s = s.replace( "["+me.getKey()+"]", me.getValue().toString() );
        }
        return s;
    }
    
    /*
    private String getResolvedTargetPath(String path) {
        return getResolvedTargetPath( getTokens(path));
    }
    */ 
    
    public String toString() {
        return this.pattern + " " + this.page;
    }
    
    public static class MatchResult {
        String resolvedPath;
        Map tokens;

        public String getResolvedPath() {
            return resolvedPath;
        }

        public Map getTokens() {
            return tokens;
        }
    }

    //spath is the path specified by the user.
    private String getResolvedPathWildcard( String spath, Map tks ) {
        //replace mapping pattern with the tokens
        String s = mappingPattern.substring(0, mappingPattern.lastIndexOf("/.*"));
        String prefix = replaceWithTokens(  s, tks );
        String newSuffix = spath.substring(prefix.length());
        String newPrefix = page.substring(0, page.lastIndexOf("/.*"));
        return newPrefix + newSuffix +".pg";
    }
    
    public MatchResult buildResult(String spath) {
        MatchResult mr = new MatchResult();
        mr.tokens = getTokens(spath);
        if( this.page.endsWith("/.*") ) {
            mr.resolvedPath = getResolvedPathWildcard( spath, mr.tokens );
        }
        else {
            mr.resolvedPath = replaceWithTokens(getPage(), mr.tokens);
        }
        return mr;
    }
    
}
