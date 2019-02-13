/*
 * ServiceManager.java
 *
 * Created on July 1, 2012, 9:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.anubis.service.JsonServiceHandler;
import com.rameses.anubis.service.ScriptServiceHandler;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class ServiceManager {
    
    private Map<String, ServiceAdapter> handlers = new  Hashtable();
    private Map<String, Map> cache = new Hashtable();
    private Map<String, Map> infoCache = new Hashtable();
    
    private static String SERVICE_DIR = "connections";
    
    public ServiceManager() {
        for(ServiceAdapter handler: getServiceAdapters()) {
            handlers.put( handler.getName(), handler );
        }
    }
    
    private Map findAdapterInfo(String confName ) {
        return findAdapterInfo(confName,  null);
    }
    
    private Map findAdapterInfo(String confName, String moduleName) 
    {
        if (!cache.containsKey(confName)) 
        {
            /*
             *  if module name is passed in the parameter, load the adapter info from 
             *  the specified module otherwise uses its default implementation 
             */ 
            if (moduleName != null) 
            {
                try
                {
                    Module module = AnubisContext.getCurrentContext().getProject().getModules().get(moduleName);
                    if (module == null) throw new Exception("'"+moduleName+"' module not found");

                    Map conf = ContentUtil.findJsonResource(new String[]{  
                                    module.getUrl() +  SERVICE_DIR + "/" + confName, 
                                    module.getProvider() +  SERVICE_DIR + "/" + confName
                                }, confName);  
                    cache.put(confName, new PropertyMap(conf)); 
                }
                catch(Exception e) {
                    throw new RuntimeException(e); 
                }
            }
            else 
            {
                Map conf = getConnection(confName);
                cache.put(confName, new PropertyMap(conf));
            }
        }
        return cache.get(confName);
    }    
    
    private ServiceAdapter findServiceAdapter(Map conf) {
        //find the handler info
        String handlerName = (String)conf.get("type");
        if(handlerName == null ) handlerName = "script";
        return handlers.get(handlerName);
    }
    
    public Object lookup(String name) {
        /*
        String confName = name.substring(0, name.indexOf("/"));
        String svcName = name.substring( name.indexOf("/") + 1);
         */
        Map conf = findAdapterInfo("default");
        ServiceAdapter svcHandler = findServiceAdapter(conf);
        return svcHandler.create( name, conf );
    }

    public Object lookup(String serviceName, String connName) {
        return lookup(serviceName, connName, null); 
    }
    
    public Object lookup(String serviceName, String connName, String moduleName) {
        if (connName == null) connName = "default";
        Map conf = findAdapterInfo(connName, moduleName);
        ServiceAdapter svcHandler = findServiceAdapter(conf);
        return svcHandler.create( serviceName, conf );
    }
    
    public Map getClassInfo(String name) {
        if(infoCache.containsKey(name) ) return infoCache.get(name);
        String confName = name.substring(0, name.indexOf("/"));
        String svcName = name.substring( name.indexOf("/") + 1);
        Map conf = findAdapterInfo(confName);
        ServiceAdapter svcHandler = findServiceAdapter(conf);
        Map info = svcHandler.getClassInfo( svcName, conf );
        infoCache.put(name, info);
        return info;
    }
    
    /**
     * Determine which connection is used. In case a widget is calling it,
     * we need to check the widget context
     */
    public Map getConnection(String name) {
        AnubisContext ctx = AnubisContext.getCurrentContext();
        
        //check first if the requester is a widget
        Module module = null;
        if(ctx.getConnectionContext()!=null) {
            module = ctx.getConnectionContext().getModule();
        }
        /*
        if(module==null) {
            module = ctx.getModule();
        }
         */
        
        //if module is not null, then get the connection from module only
        //wlse get if from the project!
        try {
            if(module!=null) {
                return ContentUtil.findJsonResource( new String[]{
                    module.getUrl() +  SERVICE_DIR + "/" + name,
                    module.getProvider() +  SERVICE_DIR + "/" + name
                }, name );
            } else {
                Project project = ctx.getProject();
                return ContentUtil.findJsonResource( new String[]{
                    project.getUrl() + "/" + SERVICE_DIR +  "/" + name
                }, name );
            }
        } 
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public ServiceAdapter[] getServiceAdapters() {
        return new ServiceAdapter[] {
            new JsonServiceHandler(),
            new ScriptServiceHandler()
        };
    }
}
