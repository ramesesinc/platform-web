/*
 * WidgetContentProvider.java
 *
 * Created on March 15, 2013, 8:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class WidgetContentProvider extends ContentProvider {
    
  
    public String getExt() {
        return "wgt";
    }
    
    public InputStream getContent(File file, Map params) throws ResourceNotFoundException {
        String widgetName = (String) file.get("widget");
        Project project = AnubisContext.getCurrentContext().getProject();
        String s = project.getWidgetManager().getWidgetContent( widgetName, params );
        return new ByteArrayInputStream(s.getBytes());
    }
    
    
    
    
}
