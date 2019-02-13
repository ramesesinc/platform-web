/*
 * ActionCommand.java
 *
 * Created on July 10, 2012, 10:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.util.Map;

/**
 *
 * @author Elmo
 */
public interface ActionCommand {
    
    public Map getResult();
    public Object execute(Map params) throws Exception;
    
}
