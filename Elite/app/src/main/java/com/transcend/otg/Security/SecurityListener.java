package com.transcend.otg.Security;

import java.util.ArrayList;

/**
 * Created by RD13_win10 on 2017/5/19.
 */

public class SecurityListener {
    private static final Object mMute = new Object();
    private static SecurityListener securityListener;
    private ArrayList<SecurityStatusListener> securityStatusListeners;

    public enum SecurityStatus{
        Attached , Detached
    }

    public interface SecurityStatusListener{
        public void onSecurityChange(SecurityStatus status);
    }

    public SecurityListener(){
        securityStatusListeners = new ArrayList<SecurityStatusListener>();
    }

    public static SecurityListener getInstance(){
        synchronized (mMute){
            if(securityListener == null){
                securityListener = new SecurityListener();
            }
        }
        return securityListener;
    }

    public void addSecurityListener(SecurityStatusListener listener){
        if( securityStatusListeners == null ){
            securityStatusListeners = new ArrayList<SecurityStatusListener>();
        }
        if(!securityStatusListeners.contains(listener)){
            securityStatusListeners.add(listener);
        }
    }

    public void removeSecurityListener(SecurityStatusListener listener){
        if(securityStatusListeners != null){
            securityStatusListeners.remove(listener);
        }
    }

    public void notifySecurityListener(SecurityStatus status){
        if(securityStatusListeners != null && securityStatusListeners.size() > 0 ){
            for(SecurityStatusListener listeners : securityStatusListeners){
                listeners.onSecurityChange(status);
            }
        }
    }
}
