/*
 * AnubisPollServlet.java
 *
 * Created on March 29, 2013, 8:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.JsonUtil;
import com.rameses.util.SealedMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

/**
 *
 * @author Elmo
 *
 * to poll example (get)
 *
 * http://host/poll/channel1/token1
 *
 * to send message (post)
 *
 * http://host/poll/channel1?data=hello
 */
public class AnubisPollServlet extends HttpServlet 
{
    private static ExecutorService thread = Executors.newCachedThreadPool();
    private static Map<String, Channel> channels = new Hashtable();
    private static final long EXPIRY_TIME = 60000;
    
    static {
        Scheduler.schedule(new MessageTokenCleaner(), 0, EXPIRY_TIME);
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
    {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try 
        {
            in = new ObjectInputStream(req.getInputStream());
            
            Object o = in.readObject();
            if (o instanceof SealedMessage) 
                o = ((SealedMessage)o).getMessage(); 

            Collection collection = null;
            if (o instanceof Collection)
                collection = (Collection) o; 
            else if (o instanceof Object[]) 
                collection = Arrays.asList((Object[]) o);
            
            if (collection == null) return;

            String channelName = req.getPathInfo().substring(1);
            Channel channel = getChannel(channelName); 
            Iterator itr = collection.iterator(); 
            while (itr.hasNext()) {
                Map data = (Map) itr.next();
                if (data == null) continue;
                                
                Object oid = data.get("tokenid"); 
                String tokenid = (oid == null? null: oid.toString()); 
                if (tokenid == null || tokenid.length() == 0) {
                    channel.send(data); 
                } else {
                    MessageToken mt = channel.createToken(tokenid); 
                    if (mt != null) mt.handle(data); 
                } 
            } 
            
            out = new ObjectOutputStream(resp.getOutputStream());
            out.writeObject("OK");
        } catch(IOException ioe) { 
            throw ioe; 
        } catch(Exception e) {
            throw new ServletException(e.getMessage(), e);
        } finally {
            try {in.close();} catch(Throwable ign){;}
            try {out.close();} catch(Throwable ign){;}
        }      
    }
    
    private void writeResponse(String msg, HttpServletResponse res)  throws ServletException, IOException{
        res.setContentType("text/json");
        PrintWriter pw = res.getWriter();
        if(msg==null) msg = "#NULL";
        pw.write( msg );
        pw.close();
    }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqkey = PollWorker.class.getName(); 
        PollWorker worker = (PollWorker) req.getAttribute( reqkey );
        if ( worker == null ) { 
            String path = req.getPathInfo().substring(1);
            String[] arr = path.split("/");
            String channelName = arr[0];
            String tokenid = arr[1];
            Channel channel = getChannel(channelName);
            Continuation cont = ContinuationSupport.getContinuation(req);
            if ( cont.isInitial() ) {
                cont.setTimeout( 30000 );
                cont.suspend();
            }
            
            MessageToken mt = channel.createToken(tokenid);
            worker = new PollWorker(mt, cont);
            req.setAttribute(reqkey, worker);
            worker.future = thread.submit( worker );
            
        } else {
            //timed out, no messages available
            if ( worker.isExpired() ) { 
                worker.cancel(); 
                writeResponse("{\"status\": \"timeout\"}", resp);
            } else {
                String result = worker.result;
                writeResponse(result, resp);
            }
            worker.destroy();
        }
    }
    
    private class PollWorker implements Runnable {
        
        private MessageToken token;
        private Continuation continuation;
        private String result;
        private Future future;
        
        public PollWorker( MessageToken mt, Continuation c ) {
            continuation = c;
            token = mt;
        }
        
        Object getResult() { 
            return result; 
        }
        
        boolean isExpired() { 
            return (continuation == null? true: continuation.isExpired()); 
        }
        
        void cancel() { 
            if ( future != null ) {
                future.cancel( true ); 
            } 
        } 
        
        public void run() {
            try { 
                runImpl(); 
            } finally { 
                continuation.resume(); 
            }
        } 
        
        private void runImpl() {
            boolean has_entries = false; 
            StringBuilder buffer = new StringBuilder();
            buffer.append("["); 
            
            List<Map> list = token.readAll(); 
            while ( !list.isEmpty() ) { 
                Map map = list.remove(0); 
                if (map != null) {
                    appendResult(buffer, map, has_entries); 
                    has_entries = true; 
                }
            }

            buffer.append("]"); 
            result = buffer.toString(); 
        }
        
        private void appendResult( StringBuilder buffer, Map map, boolean has_entries ) {
            map.put("status", "OK"); 
            if (has_entries) {
                buffer.append(", "); 
            }
            buffer.append(JsonUtil.toString(map));
        }
        
        public void destroy() {
            future.cancel(true);
            token = null;
            continuation = null;
            result = null;
            future = null;
        }
    }
    
    private class Channel {
        
        private CopyOnWriteArrayList<MessageToken> handlers = new CopyOnWriteArrayList();
        private String name;
        
        public Channel(String name) {
            this.name = name;
        }
        
        public void send(Map map) {
            for(MessageToken mh: handlers) {
                mh.handle( map );
            }
        }
        
        public synchronized MessageToken createToken(String id) {
            MessageToken mt = getToken(id);
            if (mt == null) {
                mt = new MessageToken(name, id);
                handlers.add(mt); 
            } 
            return mt;
        } 
        
        public MessageToken getToken(String id) {
            if (id == null || id.length() == 0) {
                return null; 
            }
            
            for (MessageToken handler : handlers) {
                if(handler.tokenId.equals(id)) {
                    return handler; 
                }
            }
            return null; 
        }
        
        void removeExpiredTokens() {
            List<MessageToken> list = new ArrayList();
            for (MessageToken mt : handlers) {
                if (mt.isExpired()) {
                    mt.queue.clear(); 
                    list.add(mt); 
                }
            }
            handlers.removeAll(list); 
        }
    }
    
    public class MessageToken { 
        private String cname;
        private String tokenId; 
        private LinkedBlockingQueue<Map> queue = new LinkedBlockingQueue();
        private long last_time_accessed;
                
        public MessageToken(String cname, String tokenid) {
            this.cname = cname; 
            this.tokenId = tokenid;
        }
        
        public void handle(Map map) {
            try { 
                if ( map == null ) { return; } 
                
                queue.add( map );
            } finally {
                last_time_accessed = System.currentTimeMillis(); 
            }
        }
        
        public Map read() {
            try {
                return (Map) queue.poll( 30, TimeUnit.SECONDS );
            } catch (InterruptedException ex) {
                return null;
            } finally {
                last_time_accessed = System.currentTimeMillis();
            }
        }
        
        public List<Map> readAll() {
            List<Map> list = new ArrayList();
            try { 
                Map map = read(); 
                if (map != null) { 
                    list.add( map ); 
                    
                    while ( !queue.isEmpty() ) {
                        map = queue.poll(); 
                        if (map != null) { 
                            list.add( map ); 
                        }
                    }
                } 
            } finally {
                last_time_accessed = System.currentTimeMillis(); 
            }
            return list; 
        } 
                
        boolean isExpired() {
            long diff = (System.currentTimeMillis() - last_time_accessed);
            return (diff >= EXPIRY_TIME); 
        }
    }
    
    public synchronized Channel getChannel(String name) {
        return getChannel(name, true);
    }
    
    public synchronized Channel getChannel(String name, boolean autoCreate) { 
        Channel channel = channels.get( name ); 
        if ( channel != null ) {
            return channel; 
        } 
        
        if ( autoCreate ) {
            channel = new Channel(name);
            channels.put(name, channel);
            return channel; 
        }
        
        throw new RuntimeException("Channel " + name + " does not exist!"); 
    }
    
    
    private static class MessageTokenCleaner implements Runnable {
        
        @Override
        public void run() {
            Iterator<Channel> itr = channels.values().iterator(); 
            while (itr.hasNext()) {
                itr.next().removeExpiredTokens(); 
            }
        }
    }
}
