package com.rameses.anubis;

/**
 * this is so we know what module the widget is working on
 */
public class ConnectionContext {
    
    private String name;
    private Module module;
    
    public ConnectionContext(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module  mod) {
        this.module = mod;
    }

    
}