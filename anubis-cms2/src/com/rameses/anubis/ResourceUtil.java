/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author wflores
 */
class ResourceUtil {
    
    public URL createURL( String basePath, String fileName ) {
        return findResource(new String[]{ basePath }, new String[]{ fileName }); 
    }
    
    public URL findResource( String[] basePaths, String[] fileNames ) {
        for ( String basePath : basePaths ) {
            URL baseURL = createURL0(basePath, null); 
            for ( String fname : fileNames ) { 
                URL url = baseURL; 
                String[] arr = fname.split("/"); 
                for ( int i=0; i<arr.length; i++ ) {  
                    url = findResource( url, arr[i] );  
                    if ( url == null ) break; 
                } 
                
                if (url != null) return url;
            }
        }
        return null; 
    }
    
    private URL findResource( URL baseURL, String name ) {
        String[] arr = name.split("\\.");
        if ( "".equals(arr[0])) return baseURL;
        if ( "info".equals(name) && !baseURL.toString().endsWith(".pg")) return null; 
        
        StringBuilder sb = new StringBuilder();
        if ( "info".equals(arr[0])) {
            sb.append(arr[0]); 
        } else {
            sb.append(arr[0]).append(".pg").append(",").append(arr[0]); 
        }
        
        String[] fnames = sb.toString().split(","); 
        for ( String fname : fnames ) {
            InputStream inp = null; 
            URLConnection conn = null; 
            try { 
                URL u = new URL( baseURL.toString() +"/"+ fname ); 
                inp = u.openStream(); 
                return u;
            } catch(Throwable t) {
                //do nothing 
            } finally {
                try { inp.close(); }catch(Throwable t){;} 
            }
        }
        return null; 
    }
    
    private URL createURL0( String basePath, String fileName ) {
        StringBuilder sb = new StringBuilder( basePath ); 
        if ( sb.toString().endsWith("/")) {
            sb.deleteCharAt( sb.length()-1 ); 
        } 
        
        if ( fileName != null && fileName.length() > 0 ) {
            sb.append("/").append(fileName); 
        }
        
        InputStream inp = null; 
        try {
            URL u = new URL( sb.toString() ); 
            inp = u.openStream(); 
            return u; 
        } catch(Throwable t) {
            return null; 
        } finally {
            try { inp.close(); }catch(Throwable t){;} 
        }
    }     
    
}
