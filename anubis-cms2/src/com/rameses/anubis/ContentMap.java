/*
 * ContentMap.java
 *
 * Created on July 1, 2012, 7:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;



public class ContentMap extends HashMap {
    
    private boolean editable;
    private ContextAttrs attrs; 
    
    public ContentMap() {
        this.editable = AnubisContext.getCurrentContext().getProject().isEditable();
    }
    
    public Object get(Object key) {
        String skey = key.toString();
        if( skey.equals("_content")) {
            return super.get("content");
        } 
        else if(skey.startsWith("_")) {
            skey = skey.substring(1);
            return getBlockContent(skey);
        } 
        
        AnubisContext ctx = AnubisContext.getCurrentContext(); 
        if ( skey.equals("ATTRS") ) { 
            if ( attrs == null ) {
                attrs = new ContextAttrs( ctx ); 
            }
            return attrs; 
        }
        else if( skey.equals("PARAMS")) {
            Map params = ctx.getParams(); 
            return (params == null? new HashMap(): params); 
        } 
        else if (skey.equals("PAGE")) {
            return ctx.getCurrentPage();
        } 
        else if( skey.equals("ANUBIS")) {
            return this;
        } 
        else if( skey.equals("VARS")) {
            return ctx.getCurrentPage().getVars();
        } 
        else if( skey.equals("SERVICE")) {
            return  ctx.getProject().getServiceManager();
        } 
        else if( skey.equals("MODULE")) {
            Module m = ctx.getModule();
            if(m==null) return new HashMap();
            return m;
        } 
        else if( skey.equals("THEME")) {
            Theme t = ctx.getProject().getDefaultTheme();
            if(t!=null) return t;
            return new HashMap();
        } 
        else if( skey.equals("PROJECT")) {
            return  ctx.getProject();
        } 
        else if( skey.equals("REQUEST")) {
            return  ctx.getRequest();
        }    
        else if( skey.equals("RESPONSE")) {
            return ctx.getResponse();
        }
        else if( skey.equals("SESSION")) {
            return ctx.getSession();
        } 
        else if(skey.equals("anubisContext")) {
            return ctx;
        } 
        else if (skey.equals("ERROR")) {
            Object o = super.get("ERROR");  
            if ( o instanceof Throwable ) {
                return new ErrorInfo((Throwable) o); 
            } else { 
                return null; 
            } 
        }
        else {
            return super.get(key);
        }
    }
    
    
    public String getBlockContent(String skey) {
        try {
            Project project = AnubisContext.getCurrentContext().getProject();
            return project.getBlockManager().getBlockContent( skey, this );
        } catch(Throwable t) {
            return "<span class='element-error' title='" + t.getMessage() + "'>[block:"+skey+"]</span>";
        }
    }
    
    public String getWidget( String name, Map options ) {
        Project project = AnubisContext.getCurrentContext().getProject();
        try {
            return project.getWidgetManager().getWidgetContent(name, options);
        } catch (Exception ex) {
            //ex.printStackTrace();
            return "<span class='element-error' title='" + ex.getMessage() + "'>[widget:"+name+"]</span>";
        }
    }
    
    public Object call(String action, Map params) throws Exception {
        Project project = AnubisContext.getCurrentContext().getProject();
        return project.getActionManager().getActionCommand(action).execute(params);
    }
    
    public Folder getFolder( String name ) {
        return getFolder(name, null); 
    }

    public Folder getFolder( String name, String moduleName ) 
    {
        try 
        {
            Project project = AnubisContext.getCurrentContext().getProject();
            return project.getFolderManager().getFolder(name, moduleName);
        } 
        catch(Exception e) { 
            return null; 
        } 
    }    
    
    public File getFile(String name) {
        try {
            Project project = AnubisContext.getCurrentContext().getProject();
            return project.getFileManager().getFile(name);
        } catch(Exception e){
            return null;
        }
    }
    
    public String translate(String key, String value) {
        if(AnubisContext.getCurrentContext()==null || AnubisContext.getCurrentContext().getCurrentLocale()==null) return null;
        return AnubisContext.getCurrentContext().getCurrentLocale().translate(key, value);
    }
    
    public String translate(String key, String value, String lang) {
        Project project = AnubisContext.getCurrentContext().getProject();
        LocaleSupport support = project.getLocaleSupport( lang );
        if(support==null) return null;
        return support.translate( key, value );
    }
        
    // <editor-fold defaultstate="collapsed" desc=" ContextAttrs ">
    private class ContextAttrs extends HashMap {
        
        private AnubisContext ctx; 
        
        ContextAttrs( AnubisContext ctx ) {
            this.ctx = ctx; 
        }

        public Object get(Object key) {
            if (key == null || ctx == null) return null; 
            
            String skey = key.toString(); 
            if ( skey.equals("contextPath")) {
                Object value = ctx.getAttribute( skey ); 
                return (value == null ? "" : value.toString()); 
            } else {
                return ctx.getAttribute( skey ); 
            }
        }

        public Object put(Object key, Object value) {
            return null; 
        }
        public void putAll(Map m) {
        }
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc=" ErrorInfo ">
    public class ErrorInfo {
        
        private Throwable error; 
        private Throwable cause;
        private StringBuilder buffer;
        
        ErrorInfo( Throwable error ) { 
            this.error = error; 
            this.cause = getCause( error ); 
        }
        public Throwable getSource() {
            return error; 
        }
        public Throwable getCause() {
            return (cause == null ? error : cause);
        }
        public String getMessage() { 
            Throwable e = getCause();
            if ( e == null ) e = error; 
            return (e == null? null : e.getMessage()); 
        }
        public String getStackTrace() { 
            return getStackTrace( true ); 
        }
        public String getStackTrace( boolean beginFromCause ) { 
            Throwable e = (beginFromCause ? getCause() : error ); 
            if ( e == null ) {
                return null; 
            }
            StringBuilder buffer = new StringBuilder(); 
            ErrorWriter writer = new ErrorWriter( buffer );
            e.printStackTrace(new PrintWriter(writer)); 
            return buffer.toString();  
        } 
        private Throwable getCause( Throwable e ) {
            if ( e == null ) return null; 
            if ( e.getCause() == null ) return e; 
            if ( e instanceof RuntimeException && e.getMessage()==null ) {
                return getCause( e.getCause()); 
            }
            else if ( e instanceof IllegalStateException && e.getMessage()==null ) {
                return getCause( e.getCause()); 
            } 
            else {
                return e; 
            }
        }
    }
    
    private class ErrorWriter extends Writer {
        
        private StringBuilder buffer;
        
        ErrorWriter( StringBuilder buffer ) {
            this.buffer = buffer; 
        }
        
        public void write(char[] cbuf, int off, int len) throws IOException { 
            if ( buffer != null) buffer.append(cbuf, off, len);
        }

        public void flush() throws IOException {;}
        public void close() throws IOException {;}
    }
    
    // </editor-fold>    
}
