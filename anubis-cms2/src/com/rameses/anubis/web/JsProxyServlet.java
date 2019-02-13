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
import com.rameses.anubis.Module;
import com.rameses.anubis.Project;
import com.rameses.util.ExceptionManager;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Elmo
 * Service pattern should be
 * server:8080/js-proxy/{name-of-service-adapter}/AdminService.js
 */
public class JsProxyServlet extends AbstractAnubisServlet {
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        AnubisContext actx = AnubisContext.getCurrentContext();
        try {
            actx.setConnectionContext(new ConnectionContext("js-proxy"));
            String pathInfo = hreq.getPathInfo();
            Project project = actx.getProject();
            
            String[] arr = pathInfo.substring(1).split("/");
            
            String connection = (arr.length >= 3? arr[1]: arr[0]);
            String serviceName    = (arr.length >= 3? arr[2]: arr[1]);
            serviceName = serviceName.substring(0, serviceName.indexOf("."));
            Module module = actx.getModule();
            if(module!=null) {
                actx.getConnectionContext().setModule( module );
            }
            
            Map info = project.getServiceManager().getClassInfo( connection+"/"+serviceName );
            StringWriter w = new StringWriter();
            if ( info !=null ) writeJs( info, w );

            ByteArrayInputStream bis = new ByteArrayInputStream(w.toString().getBytes());
            ResponseUtil.write(hreq, hres, "text/javascript", bis);
        } 
        catch(Exception ex) {
            System.out.println("[ERROR] " + ex.getMessage());
            ex = ExceptionManager.getOriginal(ex);
            hres.setStatus(hres.SC_INTERNAL_SERVER_ERROR);
            writeError(ex.getMessage(), hres);
            //ex.printStackTrace();
        } 
        finally {
            actx.removeConnectionContext();
        }
    }
    
    
    private void writeError(String result, HttpServletResponse hres) throws Exception {
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
    
    private void writeJs( Map m, Writer w) throws Exception {
        w.write( "function " + m.get("name") + "( p ) {\n"  );
        w.write( "this.proxy =  p;\n"  );
        
        Collection<Map> methods = (Collection)m.get("methods");
        for( Map mth : methods ) {
            StringBuffer args = new StringBuffer();
            StringBuffer parms = new StringBuffer();
            
            String methodName = (String)mth.get("name");
            List params = (List)mth.get("parameters");
            
            int i=0;
            for (i=0; i<params.size(); i++) {
                String clz = (String)params.get(i);
                //arguments
                args.append( "p" + i + ",");
                
                //parameters
                if ( i > 0 ) parms.append(", ");
                parms.append( "p" + i);
            }
            
            w.write("this." + escapeMethodName(methodName) + "= function(");
            w.write(args.toString());
            w.write("handler ) {\n");
            //if( !mth.get("returnType").equals("void") ) w.write("return ");
            w.write( "return this.proxy.invoke(\"" + methodName + "\"" );
            w.write( ",");
            w.write("["+parms.toString()+"]");
            w.write(", handler ); \n");
            w.write("} \n");
        }
        
        w.write( "}" );
    }
    
    private String escapeMethodName(String name) {
        if("delete".equals(name)) {
            return "_" + name;
        } else if("export".equals(name)) {
            return "_" + name;
        } else if("function".equals(name)) {
            return "_" + name;
        } else if("var".equals(name)) {
            return "_" + name;
        } else if("yield".equals(name)) {
            return "_" + name;
        }
        return name;
    }
}
