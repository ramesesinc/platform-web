/*
 * AnubisResourceServer.java
 *
 * Created on September 25, 2013, 11:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.server.ServerLoader;
import com.rameses.server.ServerPID;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 *
 * @author wflores
 */
public class AnubisResourceServer implements ServerLoader  
{
    private String name;
    private int port;    
    private int low_resources_connections = 0; //set if > 0 
    private int low_resources_max_idle_time = 0; //set if > 0 
    private int max_buffers = 0; //set if > 0  
    private int max_idle_time = 30000;
    private int request_header_size = 8192;
    private int request_buffer_size = 0; //set if > 0
    private int response_buffer_size = 0; //set if > 0
    private int response_header_size = 0; //set if > 0
    private int thread_pool_size = 250; 
    
    private Map conf;
    private Server svr;
    
    public AnubisResourceServer(String name) {
        this.name = name; 
        this.port = 80;
    }

    public Map getInfo() { return conf; }

    public void init(String baseUrl, Map conf) throws Exception {
        this.conf  = conf;

        Properties webconf = new Properties();
        if (webconf != null) webconf.putAll(conf);
        
        port = Integer.parseInt(webconf.getProperty("port", "80")); 

        String homeUrl = webconf.getProperty("home.url");         
        System.out.println("********************************************************************************");
        System.out.println("  STARTING " + getClass().getSimpleName() + " ("+name+" @ "+port+")");
        System.out.println("     home.url=" + homeUrl);
        System.out.println("********************************************************************************");
        
        if (homeUrl == null || homeUrl.trim().length() == 0) {
            System.out.println("[WARN] " + getClass().getSimpleName() + ": Please specify home.url property in the conf file"); 
            return; 
        } 
        
        loadConfProperties(webconf);
        
        boolean dirsListed = false; 
        if ("true".equalsIgnoreCase(webconf.getProperty("list-directories")+"")) dirsListed = true; 
        
        HandlerList handlerlist = new HandlerList(); 
        ResourceHandler static_resource_handler = new ResourceHandler();
        static_resource_handler.setResourceBase(homeUrl.toString());
        static_resource_handler.setDirectoriesListed(dirsListed);
        handlerlist.addHandler(static_resource_handler);   
        
        SelectChannelConnector conn = new SelectChannelConnector();
        conn.setName(name);
        conn.setPort(port);
                
        if (low_resources_connections > 0) 
            conn.setLowResourcesConnections(low_resources_connections);
        if (low_resources_max_idle_time > 0) 
            conn.setLowResourcesMaxIdleTime(low_resources_max_idle_time);
        if (max_buffers > 0) 
            conn.setMaxBuffers(max_buffers);
        
        conn.setMaxIdleTime(max_idle_time); 
        conn.setRequestHeaderSize(request_header_size); 
        
        if (request_buffer_size > 0) conn.setRequestBufferSize(request_buffer_size);
        if (response_buffer_size > 0) conn.setResponseBufferSize(response_buffer_size); 
        if (response_header_size > 0) conn.setResponseHeaderSize(response_header_size); 

        conn.setThreadPool(new QueuedThreadPool(thread_pool_size));
        
        svr = new Server();
        svr.addConnector(conn);
        svr.setHandler(handlerlist);        
    }

    public void start() throws Exception {
        try {
            svr.start();
        } catch(Exception ex) {
            System.out.println("Failed to start server!");
            return;
        } finally {
            ServerPID.remove(this.name); 
        }
        
        System.out.println("Server: "+ this.name +" has started");        
        
        try {
            svr.join();
        } catch(Exception ex) {
            throw ex;
        } finally {
            try { svr.stop(); }catch(Exception ign){;}
        }        
    }

    public void stop() throws Exception { 
        System.out.println("Shutting down " + getClass().getSimpleName() + " ("+name+"@"+port+")...");
        try { svr.stop(); }catch(Exception ign){;}        
    }    
    
    private void loadConfProperties(Properties conf) {        
        Integer num = getInteger(conf, "low-resources-connections");
        if (num != null) low_resources_connections = num.intValue();
        
        num = getInteger(conf, "low-resources-max-idle-time");
        if (num != null) low_resources_max_idle_time = num.intValue();
        
        num = getInteger(conf, "max-buffers");
        if (num != null) max_buffers = num.intValue();
        
        num = getInteger(conf, "max-idle-time");
        if (num != null) max_idle_time = num.intValue();
        
        num = getInteger(conf, "request-header-size");
        if (num != null) request_header_size = num.intValue();        
        
        num = getInteger(conf, "request-buffer-size");
        if (num != null) request_buffer_size = num.intValue();        

        num = getInteger(conf, "response-buffer-size");
        if (num != null) response_buffer_size = num.intValue();        
               
        num = getInteger(conf, "response-header-size");
        if (num != null) response_header_size = num.intValue();        
        
        num = getInteger(conf, "thread-pool-size");
        if (num != null) thread_pool_size = num.intValue();        
    }
    
    private Integer getInteger(Map conf, String name) {
        Object ov = conf.get(name);
        String sv = (ov == null? null: ov.toString()); 
        if (sv == null || sv.trim().length() == 0) return null;
        
        return new Integer(sv.trim()); 
    }
}
