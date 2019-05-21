/*
 * MediaContentProvider.java
 *
 * Created on March 15, 2013, 8:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class MediaContentProvider extends ContentProvider {
    
    public String getExt() {
        return "media";
    }
    
    public InputStream getContent(File file, Map params) {
        String block = "content";
        if(params.containsKey("category")) {
            block = (String) params.get("category");
        }
        
        AnubisContext ctx = AnubisContext.getCurrentContext();
        Module modu = ctx.getModule();
        
        String path = file.getPath();
        String arr[] = path.split("\\.");
        path = arr[0] + "/" + block + "." + arr[1];
        
        String moduleName = null;
        if (modu != null) {
            moduleName = modu.getName(); 
            path = path.substring(("/"+modu.getName()).length());
        }
        
        ArrayList<String> baseURLs = new ArrayList();
        if(modu!=null) {
            baseURLs.add( ctx.getProject().getUrl() +"/files/"+ moduleName );
            baseURLs.add( ctx.getProject().getUrl() +"/content/"+ moduleName );
            baseURLs.add( modu.getUrl() + "/files" ); 
            baseURLs.add( modu.getUrl() + "/content" ); 
            if ( modu.getProvider() != null ) {
                baseURLs.add( modu.getProvider() +"/files" ); 
                baseURLs.add( modu.getProvider() +"/content" ); 
            } 
        } else {
            baseURLs.add( ctx.getProject().getUrl() +"/files" );
            baseURLs.add( ctx.getProject().getUrl() +"/content" );
            baseURLs.add( ctx.getSystemUrl() +"/files" );
            baseURLs.add( ctx.getSystemUrl() +"/content" );
        }

        ArrayList<String> paths =  new ArrayList(); 
        String[] names = new String[]{ file.getPath()+"/content", path }; 
        for ( String fname : names ) { 
            buildURLPaths(paths, baseURLs, fname ); 
            try {
                InputStream inp = ContentUtil.getResources(paths.toArray(new String[]{}), fname );
                return new MediaInputStream( inp ); 
            } catch(Throwable e) {
                //do nothing 
            } finally {
                paths.clear(); 
            }
        }
        return null; 
    } 
    
    private void buildURLPaths(ArrayList<String> paths, ArrayList<String> baseURLs, String name ) {
        for (String basepath : baseURLs) {
            paths.add( basepath +"/"+ name +"/info"); 
            paths.add( basepath +"/"+ name ); 
        }
    }
}
