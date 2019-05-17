/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 *
 * @author elmonazareno
 */
public class PageMapper {
    
    private LinkedList<PageMapperEntry> entries = new LinkedList<PageMapperEntry>();
    
    public void addEntry( String path, String filesource  ) {
        entries.add( new PageMapperEntry(path, filesource) );
    }
    
    public PageMapperResult getFileSource( String path ) {
        //1. check first if matches the pattern;
        path = path.endsWith("/")?path : path + "/";
        PageMapperEntry pMap = null;
        for(PageMapperEntry pi: entries ) {
            if( pi.match(path) ) {
                pMap = pi;
                break;
            }
        }
        path = path.substring(0, path.lastIndexOf("/"));
        if( pMap == null ) {
            return new PageMapperResult(path, null);
        }
        return pMap.getFileSource(path);
    }
    
    /**
     * @param filesource
     * @throws Exception 
     */
    public void load( InputStream filesource  ) {
        InputStreamReader ir = null;
        BufferedReader br = null;
        LinkedList list = new LinkedList();
        try {
            ir = new InputStreamReader(filesource);
            br = new BufferedReader(ir);
            String s = null;
            while(( s = br.readLine())!=null) {
                String[] arr = s.split("=");
                if(arr.length == 2) {
                    list.add( new PageMapperEntry(arr[0].trim(), arr[1].trim()) );
                }
            }
            //existing entries and replace the entries with new one to put existing down the list
            list.addAll( entries );
            entries = list;
        }
        catch(Exception e) {
            System.out.println("Error load file " + e.getMessage());
        }
        finally {
            try { ir.close(); } catch(Exception ign){;}
            try { br.close(); } catch(Exception ign){;}
            try { filesource.close(); } catch(Exception ign){;}
        }
    }
    
    
    
}
