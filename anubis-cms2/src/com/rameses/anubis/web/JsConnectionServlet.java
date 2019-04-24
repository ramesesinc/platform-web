package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.ConnectionContext;
import com.rameses.anubis.JsonUtil;
import com.rameses.anubis.Module;
import com.rameses.anubis.Project;
import com.rameses.util.ExceptionManager;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author wflores
 */
public class JsConnectionServlet extends AbstractAnubisServlet {
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        AnubisContext actx = AnubisContext.getCurrentContext();
        try {
            actx.setConnectionContext(new ConnectionContext("connections"));
            
            String pathInfo = hreq.getPathInfo();
            Project project = actx.getProject();
            
            // /admin/default
            
            String[] arr = pathInfo.substring(1).split("/");
            String moduleName = (arr.length >= 2 ? arr[0]: null);
            String connectionName = (arr.length >= 2 ? arr[1]: arr[0]);
            
            if (connectionName == null || connectionName.length() == 0) {
                connectionName = "default";
            }
            
            Module module = actx.getModule();
            if ( module != null) { 
                actx.getConnectionContext().setModule( module );
            }
            
            Map result = project.getServiceManager().getConnection( connectionName ); 
            writeResponse( JsonUtil.toString(result), hres );
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
}
