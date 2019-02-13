package com.rameses.anubis;

/**
 * This class is used by MasterTemplateManager
 */ 
public class MappingEntry {
    
    private String pattern;
    private String[] templates;
    
    public MappingEntry(String pattern, String smasters) {
        pattern = pattern.trim();
        if(!pattern.startsWith("/")) pattern = "/" + pattern;
        smasters = smasters.trim();
        if(smasters.startsWith("/")) smasters = smasters.substring(1);
        
        this.pattern = "^"+pattern.replaceAll("\\*\\*", "([\\\\w|-]+)?" )+"$";
        String[] str = smasters.split(",");
        int strlen = str.length;
        this.templates = new String[strlen];
        
        for (int i = 0; i<str.length; i++) {
            this.templates[i] = str[strlen-i-1].trim();
        }
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public String[] getTemplates() {
        return templates;
    }
    
    public boolean matches(String path) {
        //System.out.println("matches->" + path + " with pattern-> "+pattern + " ? "+ path.matches( pattern ));
        return path.matches( pattern );
    }
    
}