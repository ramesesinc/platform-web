/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author elmonazareno
 * page example = /partners/[code]/**
 * filesource = /partners/lgu/**
 */
public class PageMapperEntry {
    
    private String pattern;
    private LinkedList<PageMapperToken> tokens = new LinkedList<PageMapperToken>();
    private String fileSourceFormat;
    
 

    /**
     * steps:
     * 1. split first the path and store as PageMapperToken
     * 2. scan and build the pattern for matching 
     */
    public PageMapperEntry(String path, String fileSource ) {
        StringBuilder patternBuff = new StringBuilder();
        path = path.startsWith("/") ? path.substring(1) : path;
        for( String s: path.trim().split("/") ) {
            PageMapperToken pt = new PageMapperToken();
            if( s.equals("**") ) {
                pt.setType(PageMapperTokenType.PATH);
                patternBuff.append("/.*");
            }
            else if( s.startsWith("[") ) {
                pt.setType(PageMapperTokenType.PARAMETER);
                s = s.trim();
                pt.setValue( s.substring(1, s.length()-1).trim() );
                patternBuff.append("/.*?");                
            }
            else {
                pt.setValue( s );
                patternBuff.append("/"+s);
            }
            tokens.add( pt );
        }
        
        StringBuilder fileBuff = new StringBuilder();
        int i = 0;
        fileSource = fileSource.startsWith("/") ? fileSource.substring(1) : fileSource;
        for(String s: fileSource.split("/")) {
            if( s.equals("**")) {
                fileBuff.append( "/{"+(i++)+"}" );
            }
            else {
                fileBuff.append("/"+s);
            }
        }
        fileSourceFormat = fileBuff.toString();
        pattern = patternBuff.toString();
        pattern = pattern.endsWith("/")? pattern : pattern+"/";
    }
    
    public String checkString() {
        return "pattern: " + pattern + ";fileformat:" + fileSourceFormat;
    }

    public boolean match(String path) {
        return path.matches(pattern);
    }
       
    public PageMapperResult getFileSource(String path) {
        //1. tokenize the path sent and store in queue for easy processing;
        LinkedList<String> pathList = new LinkedList<String>();
        path = path.startsWith("/") ? path.substring(1) : path;
        for(String s: path.split("/")) {
            pathList.add(s);
        }
        Map parameters = new HashMap();
        List<String> filePaths = new ArrayList<String>();
        StringBuffer buff = new StringBuffer();
        //2. scan each token in pagemapper and compare with the list;
        for(PageMapperToken pt : tokens ) {
            if( pt.getType() == PageMapperTokenType.TEXT ) {
                //lookup list until you find the matching string. if not match store in bufferedPath;
                while( !pathList.isEmpty() ) {
                    String s = pathList.remove();
                    if( !s.equals(pt.getValue()) ) {
                        buff.append( "/"+s );
                    }
                    else {
                        //do nothing
                        break;
                    }
                }
                if( buff.length() > 0 ) {
                    String pp = buff.toString();
                    pp = (pp.startsWith("/")) ? pp.substring(1) : pp;
                    filePaths.add( pp );
                    buff = new StringBuffer();
                }
            }
            else if( pt.getType() == PageMapperTokenType.PARAMETER ) {
                parameters.put(pt.getValue(), pathList.remove() );
            }
        }
        
        while( !pathList.isEmpty() ) {
            buff.append( "/"+pathList.remove() );
        }
        if(buff.length()>0) {
            String pp = buff.toString();
            pp = (pp.startsWith("/")) ? pp.substring(1) : pp;
            filePaths.add( pp );
        }
        
        //rewrite the actual file path
        MessageFormat mf = new MessageFormat(fileSourceFormat);
        String fPath =  mf.format( filePaths.toArray() );
        return new PageMapperResult(fPath, parameters );
    }
    
    
}
