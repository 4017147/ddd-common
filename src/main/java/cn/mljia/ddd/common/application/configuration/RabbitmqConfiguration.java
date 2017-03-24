package cn.mljia.ddd.common.application.configuration;

public class RabbitmqConfiguration {

	
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
	
	private String useModel;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}

	public String[] getAddress() {
		return address;
	}

	public void setAddress(String[] address) {
		this.address = address;
	}

    public String getUseModel()
    {
        return useModel;
    }

    public void setUseModel(String useModel)
    {
        this.useModel = useModel;
    }

	
	
	
}
