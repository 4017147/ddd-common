package cn.mljia.ddd.common.application.configuration;

import cn.mljia.ddd.common.AssertionConcern;

public class CommonConfiguration extends AssertionConcern
{
    
    private String domainName;
    
    public String getDomainName()
    {
        return domainName;
    }
    
    public void setDomainName(String domainName)
    {
        this.assertArgumentNotEmpty(domainName, "The event domain name is required.");
        this.domainName = domainName;
    }
    
}
