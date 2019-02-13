/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis.web;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author wflores 
 */
public final class Scheduler {
    
    static {
        Thread thread = new Thread(new ShutdownHook()); 
        Runtime.getRuntime().addShutdownHook(thread);
    }
    
    private static Timer timer; 
    
    private static synchronized Timer getTimer() {
        if (timer == null) {
            timer = new Timer(); 
        }
        return timer;
    }
    
    public static synchronized void schedule(Runnable runnable) {
        getTimer().schedule(new TimerTaskImpl(runnable), 0);
    }
    
    public static synchronized void schedule(Runnable runnable, long delay) {
        getTimer().schedule(new TimerTaskImpl(runnable), delay);
    }   
    
    public static synchronized void schedule(Runnable runnable, long delay, long period) {
        getTimer().schedule(new TimerTaskImpl(runnable), delay, period);
    }     
    
    public static synchronized void scheduleFix(Runnable runnable, long period) {
        getTimer().scheduleAtFixedRate(new TimerTaskImpl(runnable), 0, period);
    }   
    
    public static synchronized void scheduleFix(Runnable runnable, long period, long delay) {
        getTimer().scheduleAtFixedRate(new TimerTaskImpl(runnable), delay, period);
    } 
    
    private static class TimerTaskImpl extends TimerTask {
        private Runnable callback;
        
        TimerTaskImpl(Runnable callback) {
            this.callback = callback; 
        }
        
        @Override
        public void run() { 
            try { 
                callback.run(); 
            } catch(Throwable t) {
                t.printStackTrace(); 
            } 
        } 
    } 
    
    private static class ShutdownHook implements Runnable {
        
        @Override
        public void run() { 
            if (timer != null) {
                try { 
                    timer.cancel(); 
                } catch(Throwable t) {;} 

                try { 
                    timer.purge(); 
                } catch(Throwable t) {;}
            }
        } 
    } 
}
