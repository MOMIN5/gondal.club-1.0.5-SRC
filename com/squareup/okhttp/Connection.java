// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp;

import java.net.Socket;

public interface Connection
{
    Route getRoute();
    
    Socket getSocket();
    
    Handshake getHandshake();
    
    Protocol getProtocol();
}
