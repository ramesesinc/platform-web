/*
 * HttpServiceHandler.java
 *
 * Created on June 24, 2012, 12:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.service;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.ServiceAdapter;
import com.rameses.anubis.ServiceInvoker;
import com.rameses.service.ScriptServiceContext;
import com.rameses.service.ServiceProxy;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class ScriptServiceHandler  implements ServiceAdapter {
    
    private GroovyClassLoader classLoader =  new GroovyClassLoader(getClass().getClassLoader());
    private Class metaClass;
    
    public String getName() {
        return "script";
    }

    private Class getMetaClass() throws Exception {
        if( metaClass == null ) {
            StringBuilder builder = new StringBuilder();
            builder.append( "public class MyMetaClass  { \n" );
            builder.append( "    def invoker; \n");
            builder.append( "    public Object invokeMethod(String string, Object args) { \n");
            builder.append( "        return invoker.invokeMethod(string, args); \n" );
            builder.append( "    } \n");
            builder.append(" } ");
            metaClass = classLoader.parseClass( builder.toString() );
        }
        return metaClass;
    }
     
    public Map getClassInfo(String name, Map conf) { 
        if (conf.get("app.cluster") == null) 
            throw new RuntimeException("cluster is not defined");        
        if (conf.get("app.host") == null) 
            throw new RuntimeException("app.host is not defined");        
        if (conf.get("app.context") == null)
            throw new RuntimeException("app.context is not defined");
        return getServiceClassInfo( name, conf );
    }
    
    @Override
    public Object create(String name, Map conf) {
        if (conf.get("app.cluster") == null) 
            throw new RuntimeException("cluster is not defined");        
        if (conf.get("app.host") == null) 
            throw new RuntimeException("app.host is not defined");        
        if (conf.get("app.context") == null)
            throw new RuntimeException("app.context is not defined");
        
        //build the env. include special parameters
        Map env = new HashMap();
        for (Object es : conf.entrySet()) {
            Map.Entry me = (Map.Entry)es;
            if( me.getKey().toString().startsWith("ds.")) {
                env.put( me.getKey(), me.getValue() );
            }
        }
        AnubisContext actx = AnubisContext.getCurrentContext();
        env.putAll( actx.getEnv() );
        try {
            LocalScriptInvoker si = new LocalScriptInvoker(name, conf, env);
            Object obj =  getMetaClass().newInstance();
            ((GroovyObject)obj).setProperty( "invoker", si );
            return obj;
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }        
    }
    
    

    /***************************************************************************
     * The following code is to get the class info for local and remote
     **************************************************************************/ 
    private interface IScriptService  {
        String stringInterface();
        Map metaInfo();
    }
    
     private Map getServiceClassInfo( String name, Map conf ) {
        ScriptServiceContext ctx = new ScriptServiceContext(conf);
        IScriptService svc = ctx.create(name, IScriptService.class );
        Map metainfo = svc.metaInfo();
        Collection methods = null;
        Object o = metainfo.get("methods");
        if(o instanceof Map) {
            methods = ((Map)o).values();
        }
        else {
            methods = (Collection)o;
        }
        Map results = new HashMap();
        results.put("methods", methods); 
        results.put("name", metainfo.get("serviceName")); 
        return results; 
    }
    
   
    private class LocalScriptInvoker implements ServiceInvoker {
        
        private ServiceProxy serviceProxy;
        
        public LocalScriptInvoker(String name, Map conf, Map env) {
            ScriptServiceContext ctx = new ScriptServiceContext(conf);
            serviceProxy = ctx.create( name, env );
        }
        public Object invokeMethod(String methodName, Object[] args) {
            try {
                return serviceProxy.invoke( methodName, args );
            } catch(Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
                //return "<font color=red>error service:" + ex.getMessage()+"</font>";
            }
        }
    }
    
    
}
