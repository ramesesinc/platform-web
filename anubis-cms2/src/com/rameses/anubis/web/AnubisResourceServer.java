/*
 * AnubisResourceServer.java
 *
 * Created on September 25, 2013, 11:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.io.IOStream;
import com.rameses.server.ServerLoader;
import com.rameses.server.ServerPID;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
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
        ResourceHandler static_resource_handler = new ResourceHandlerImpl();
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
    
    private class ResourceHandlerImpl extends ResourceHandler {

        @Override
        protected Resource getResource(HttpServletRequest request) throws MalformedURLException {
            Resource res = super.getResource(request);
            if ( res instanceof XmlResource ) {
                return res; 
            }
                
            URI uri = (res == null ? null : res.getURI());
            if ( uri != null && uri.toString().endsWith(".xml") && !res.isDirectory()) {
                return new XmlResource( res ); 
            }
            return res; 
        }
    }
    
    private class XmlResource extends Resource {

        private Resource source; 
        private byte[] bytes; 
        
        XmlResource( Resource source ) {
            this.source = source; 
        }
        
        @Override
        public boolean isContainedIn(Resource rsrc) throws MalformedURLException {
            return source.isContainedIn(rsrc); 
        }

        @Override
        public void release() {
            source.release();
            bytes = null; 
        }

        @Override
        public boolean exists() {
            return source.exists();
        }

        @Override
        public boolean isDirectory() {
            return source.isDirectory();
        }

        @Override
        public long lastModified() {
//            return source.lastModified();
            return System.currentTimeMillis(); 
        }

        byte[] getBytes() {
            if ( bytes == null ) {
                byte[] arr = null; 
                try { 
                    arr = IOStream.toByteArray( source.getInputStream());
                } catch (IOException ex) {
                    throw new RuntimeException( ex );
                }
                
                if ( arr != null ) {
                    Object val = null; 
                    try {
                        val = new String( arr, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        val = new String( arr );
                    }
                    
                    val = resolveValueImpl( val ); 
                    
                    try { 
                        bytes = val.toString().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        bytes = val.toString().getBytes();
                    }
                }
            }
            return bytes; 
        }
        
        int resolve_value_stack_counter = 0; 
        private Object resolveValueImpl(Object value) { 
            resolve_value_stack_counter += 1;
            if ( resolve_value_stack_counter > 10 ) {
                return value; 
            }

            if (value == null) { 
                return null; 
            } else if (!(value instanceof String)) {
                return value; 
            }

            int startidx = 0; 
            boolean has_expression = false; 
            String str = value.toString();         
            StringBuilder builder = new StringBuilder(); 
            while (true) {
                int idx0 = str.indexOf("${", startidx);
                if (idx0 < 0) break;

                int idx1 = str.indexOf("}", idx0); 
                if (idx1 < 0) break;

                has_expression = true; 
                String skey = str.substring(idx0+2, idx1); 
                builder.append(str.substring(startidx, idx0)); 

                Object objval = System.getProperty( skey );
                if (objval == null) {
                    objval = System.getenv(skey); 
                }

                if (objval == null) { 
                    builder.append(str.substring(idx0, idx1+1)); 
                } else { 
                    builder.append( objval ); 
                } 
                startidx = idx1+1; 
            } 

            Object finalResult = null; 
            if (has_expression) {
                builder.append(str.substring(startidx));  
                finalResult = builder.toString(); 
            } 
            else {
                finalResult = value;
            }

            if ( finalResult != null && hasExpression( finalResult)) {
                return resolveValueImpl( finalResult ); 
            } 
            return finalResult;
        } 

        private boolean hasExpression( Object value ) {
            String str = (value == null ? null : value.toString()); 
            if ( str == null || str.length() == 0 ) {
                return false; 
            }

            int idx0 = str.indexOf("${");
            if (idx0 < 0) return false; 

            int idx1 = str.indexOf("}", idx0); 
            return (idx1 > 0); 
        }
        
        
        @Override
        public long length() {
            return getBytes().length; 
        }

        @Override
        public URL getURL() {
            return source.getURL();
        }

        @Override
        public File getFile() throws IOException {
            return source.getFile();
        }

        @Override
        public String getName() {
            return source.getName();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream( getBytes()); 
        }

        @Override
        public OutputStream getOutputStream() throws IOException, SecurityException {
            return source.getOutputStream();
        }

        @Override
        public boolean delete() throws SecurityException {
            return source.delete();
        }

        @Override
        public boolean renameTo(Resource rsrc) throws SecurityException {
            return source.renameTo( rsrc );
        }

        @Override
        public String[] list() {
            return source.list();
        }

        @Override
        public Resource addPath(String string) throws IOException, MalformedURLException {
            return source.addPath( string ); 
        }
    }
}
