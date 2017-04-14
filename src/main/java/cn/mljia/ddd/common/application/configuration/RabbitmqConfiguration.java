package cn.mljia.ddd.common.application.configuration;

import cn.mljia.ddd.common.AssertionConcern;

public class RabbitmqConfiguration extends AssertionConcern
{
    
    private String[] address;
    
    /** My hostName, which is the name of the host server. */
    private String hostName;
    
    /** My password, which is the password of the connecting user. */
    private String password;
    
    /** My port, which is the host server port. */
    private int port;
    
    /** My username, which is the name of the connecting user. */
    private String username;
    
    /** My virtualHost, which is the name of the RabbitMQ virtual host. */
    private String virtualHost;
    
    
    private String domainName;
    
    
    public String getHostName()
    {
        return hostName;
    }
    
    public void setHostName(String hostName)
    {
        assertArgumentNotEmpty(hostName, "cfg hostName not be empty.");
        this.hostName = hostName;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        assertArgumentNotEmpty(password, "cfg password not be empty.");
        this.password = password;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public void setPort(int port)
    {
        assertArgumentNotNull(port, "cfg port not be Null.");
        this.port = port;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        assertArgumentNotEmpty(username, "cfg username not be empty.");
        this.username = username;
    }
    
    public String getVirtualHost()
    {
        return virtualHost;
    }
    
    public void setVirtualHost(String virtualHost)
    {
        assertArgumentNotEmpty(virtualHost, "cfg virtualHost not be empty.");
        this.virtualHost = virtualHost;
    }
    
    public String[] getAddress()
    {
        return address;
    }
    
    public void setAddress(String[] address)
    {
        assertArgumentNotNull(address, "cfg address not be Null.");
        this.address = address;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public void setDomainName(String domainName)
    {
        assertArgumentNotEmpty(domainName, "cfg domainName not be empty.");
        this.domainName = domainName;
    }
    
    
    
}
