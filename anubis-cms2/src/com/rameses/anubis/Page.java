/*
 * PageInstance.java
 *
 * Created on July 1, 2012, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Elmo
 * This is used during requests.
 */
public class Page  extends HashMap {
    
    private File file;
    
    private Set themeStyles = new LinkedHashSet();
    private Set scripts = new LinkedHashSet();
    private Set styles = new LinkedHashSet();
    
    private Set tags = new LinkedHashSet();
    private Map vars = new HashMap();
    
    //private Map processQueue = new HashMap();
    
    /** Creates a new instance of PageInstance */
    public Page(File file) {
        this.file  = file;
        putAll( file );
        put("scripts",getScripts());
        put("styles", getStyles());
        put("themeStyles", getThemeStyles());
        //put("theme", getTheme().getName());
        put("secured", file.isSecured() );
        put("name",file.getName() );
        put("pagename", file.getPagename() );
    }
    
    /*
    public Theme getTheme() {
        Project project = AnubisContext.getCurrentContext().getProject();
        Module module = AnubisContext.getCurrentContext()
        String themeName = (String)getFile().get("theme");
        Theme theme = null;
        if( themeName !=null) {
            theme = project.getThemes().get(themeName);
            if(theme!=null) return theme;
        }
        if(getModule()!=null) {
            theme = getModule().getDefaultTheme();
            if(theme!=null) return theme;
        }
        theme = getProject().getDefaultTheme();
        if(theme!=null) return theme;
        return getProject().getSystemTheme();
    }
     */
    public Set getScripts() {
        return scripts;
    }
    
    public Set getStyles() {
        return styles;
    }
    
    public Set getThemeStyles() {
        return themeStyles;
    }
    
    public Set getTags() {
        return tags;
    }
    
    public Map getVars() {
        return vars;
    }
    
    public File getFile() {
        return file;
    }

    public void addScript(String fileName) {
        scripts.add(fileName);
    }

    public void addStyle(String name) {
        styles.add( name );
    }

    
    public void addThemeStyle(String styleName) {
        themeStyles.add(styleName);
    }
    
}
