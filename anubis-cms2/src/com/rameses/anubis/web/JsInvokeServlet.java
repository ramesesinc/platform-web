/*
 * ScriptInfoServlet.java
 *
 * Created on July 7, 2012, 3:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.ConnectionContext;
import com.rameses.anubis.JsonUtil;
import com.rameses.anubis.Module;
import com.rameses.anubis.Project;
import com.rameses.util.ExceptionManager;
import groovy.lang.GroovyObject;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 *
 * @author Elmo
 */
public class JsInvokeServlet extends AbstractAnubisServlet {
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        AnubisContext actx = AnubisContext.getCurrentContext();
        actx.setConnectionContext( new ConnectionContext("js-invoke") );
        try {
            String pathInfo = hreq.getPathInfo();
            Project project = actx.getProject();
            
            String[] arr = pathInfo.substring(1).split("/");
            String moduleName = (arr.length >= 3? arr[0]: null);
            String connectionName = (arr.length >= 3? arr[1]: arr[0]);
            String serviceName = (arr.length >= 3? arr[2]: arr[1]);
            
            if (connectionName == null || connectionName.length() == 0) {
                connectionName = "default";
                //throw new NullPointerException("Please specify a connection name");
            }
            if (serviceName == null || serviceName.length() == 0) {
                throw new NullPointerException("Please specify a service name");
            }

            serviceName = serviceName.substring(0, serviceName.indexOf("."));
            String action = pathInfo.substring(pathInfo.lastIndexOf(".")+1);
            
            Module module = actx.getModule();
            if(module!=null) {
                actx.getConnectionContext().setModule( module );
            }
            
            //get the arguments
            String _args = hreq.getParameter("args");
            Object[] args = null;
            if (_args!=null && _args.length()>0) {
                if (!_args.startsWith("["))
                    throw new RuntimeException("args must be enclosed with []");
                
                args = JsonUtil.toObjectArray( _args );
            }
            
            //updated since I dont see any reason why we should compile this to groovy
            
            GroovyObject gobj =(GroovyObject) project.getServiceManager().lookup(serviceName, connectionName);
            if (args == null) args = new Object[]{};
            Object result = gobj.invokeMethod( action, args  );
            
            hres.setContentType("application/json"); 
            if ( result instanceof Map || result instanceof List ) {
                JsonConfig jc = new JsonConfig(); 
                jc.registerJsonValueProcessor(java.util.Date.class, new JsonDateValueProcessor()); 
                jc.registerJsonValueProcessor(java.sql.Date.class, new JsonDateValueProcessor()); 
                jc.registerJsonValueProcessor(java.sql.Timestamp.class, new JsonDateValueProcessor()); 
                Object json = JSONSerializer.toJSON(result, jc);     
                writeResponse( json.toString(), hres );
            }
            else {
                writeResponse( JsonUtil.toString(result), hres );
            }
        } 
        catch(Exception e) {
            e.printStackTrace();
            e = ExceptionManager.getOriginal(e);
            hres.setStatus(hres.SC_INTERNAL_SERVER_ERROR);
            writeResponse(e.getMessage(), hres);
        } finally {
            actx.removeConnectionContext();
        }
    }
    
    private void writeResponse(String result, HttpServletResponse hres) throws Exception {
        Writer w = null;
        try {
            w = hres.getWriter();
            w.write( result );
        } catch(Exception e) {
            throw e;
        } finally {
            try { w.close(); } catch(Exception e){;}
        }
    }
    
    private class JsonDateValueProcessor implements JsonValueProcessor {

        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd mm:hh:ss"); 
        
        public Object processArrayValue(Object value, JsonConfig jc) { 
            if ( value instanceof java.util.Date ) {
                return sdf.format((java.util.Date) value); 
            }
            return "";
        }

        public Object processObjectValue(String name, Object value, JsonConfig jc) { 
            if ( value instanceof java.util.Date ) {
                return sdf.format((java.util.Date) value); 
            }
            return "";
        }
    }    
}
