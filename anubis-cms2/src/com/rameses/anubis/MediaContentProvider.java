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
        
        String path = file.getPath();
        String arr[] = path.split("\\.");
        path = arr[0] + "/" + block + "." + arr[1];
        
        String moduleName = null;
        String[] arr2 = ProjectUtils.getModuleNameFromFile(path, ctx.getProject());
        if(arr2!=null) {
            moduleName = arr2[0];
            path = arr2[1];
        }
        
        ArrayList<String> baseURLs = new ArrayList();
        if(moduleName!=null) {
            Module mod = ctx.getProject().getModules().get( moduleName );
            baseURLs.add( ctx.getProject().getUrl() +"/files/"+ moduleName );
            baseURLs.add( ctx.getProject().getUrl() +"/content/"+ moduleName );
            baseURLs.add( mod.getUrl() + "/files" ); 
            baseURLs.add( mod.getUrl() + "/content" ); 
            if ( mod.getProvider() != null ) {
                baseURLs.add( mod.getProvider() +"/files" ); 
                baseURLs.add( mod.getProvider() +"/content" ); 
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
