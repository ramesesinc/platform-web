/*
 * LocaleManager.java
 *
 * Created on July 16, 2012, 9:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.anubis.FileDir.FileFilter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class LocaleSupport {
    
    protected Project project;
    private String lang;
    private final static String I18N_DIR = "/i18n/";
    private final static String RES_DIR = "/resources";
    private Map translations;
    
    
    public LocaleSupport(String lang, Project project) {
        this.project = project;
        this.lang = lang;
    }
    
    
    public String getLang() {
        return lang;
    }
    
    //called by labels, etc.
    public String translate(String key, String value) {
        String _v = (String) getResourceFile().get(key);
        
        if( _v !=null && _v.trim().length()>0) {
            return _v;
        } else if(_v==null && value!=null) {
            //try lowercased values where spaces are replaced with "_"
            String skey = value.replaceAll("\\s{1,}", "_").toLowerCase();
            _v = (String)getResourceFile().get(skey);
        }
        return null;
    }
    
    
    
    
    
    protected Map getResourceFile() {
        if(translations!=null) return translations;
        translations = new HashMap();
        String urlpath = ContentUtil.correctUrlPath(project.getUrl(), I18N_DIR+getLang(), RES_DIR );
        
        FileDir.scan( urlpath, new FileFilter(){
            public void handle(FileDir.FileInfo f) {
                translations.putAll( ContentUtil.getProperties( f.getUrl() ));
            }
            
        });
        return translations;
    }
    
    /*
    private class LocaleBlockContentProvider extends BlockContentProvider {
        protected InputStream getResource(String name, PageFileInstance page) {
            return ContentUtil.findResource( project.getUrl() , I18N_DIR +getLang()+ "/pages", name );
        }
    }
     */
}
