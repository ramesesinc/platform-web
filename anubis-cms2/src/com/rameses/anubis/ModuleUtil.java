/*
 * ModuleUtil.java
 *
 * Created on March 14, 2013, 8:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.InputStream;

/**
 *
 * @author Elmo
 */
public class ModuleUtil {
    
    public static InputStream findResource( Module m, String ctxDir, String resname ) {
        InputStream is = ContentUtil.findResource(m.getUrl(), ctxDir, resname);
        if(is!=null) return is;
        if( m.getProvider()!=null ) {
            try {
                is = ContentUtil.findResource(m.getProvider() , ctxDir,  resname);
                return is;
            } catch(Exception e) {
                System.out.println("error findStrean in provider->"+e.getMessage());
            }
        }
        return null;
    }
    
    
    public static boolean checkFileExists(Module m, String filename) {
        boolean fileExist = ContentUtil.fileExists( m.getUrl() , null, filename );
        if(!fileExist && m.getProvider()!=null) {
            try {
                fileExist = ContentUtil.fileExists( m.getProvider() , null, filename );
            } catch(Exception e) {
                System.out.println("error checking file exist in provider->"+e.getMessage());
            }
        }
        return fileExist;
    }
    
}
