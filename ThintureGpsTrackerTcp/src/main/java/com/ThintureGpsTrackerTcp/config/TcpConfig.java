package com.ThintureGpsTrackerTcp.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpConfig {

    @Value("${tcp.server.port:5000}")
    private int tcpPort;

    @Value("${tcp.server.ip:127.0.0.1}")
    private String serverIp;
    
    //@Value("${tcp.server.ip:3.109.116.92}")
   // private String serverIp;

    // Getters for the configuration values
    public int getTcpPort() {
        return tcpPort;
    }

    public String getServerIp() {
        return serverIp;
    }
}

