/*
 * ProjectUtils.java
 *
 * Created on July 19, 2012, 3:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.rameses.anubis;

import java.util.Iterator;

/**
 *
 * @author Elmo
 * This is a utility file for handling different routines related to the project
 */
public class ProjectUtils {
        
    //finds the first file in the folder that is visible.
    public static File findFirstVisibleFile( Folder folder ) {
        if( folder.getChildren().size() > 0) {
            Iterator<File> iter = folder.getChildren().iterator();
            while(iter.hasNext()) {
                File firstFile = iter.next();
                if(!firstFile.isHidden() || firstFile.getPath().equals("/index")) {
                    return firstFile;
                }
            }
        }
        return null;
    }
    
    /**
    Use this utility to extract the path less the module's name. 
    */
    public static String correctModuleFilePath( String path, Module module ) {
        if(module==null) return path;
        String name = module.getName();
        return path.substring( path.indexOf(name)+name.length()  );
    }
    
    
}
