//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq;

import cn.mljia.ddd.common.AssertionConcern;

/**
 * I am a configuration for making a connection to RabbitMQ. I include information for the host, port, virtual host, and user.
 *
 * @author Vaughn Vernon
 */
public class ConnectionSettings extends AssertionConcern {

	/** My password, which is the password of the connecting user. */
	private String password;

	/** My username, which is the name of the connecting user. */
	private String username;

	/** My virtualHost, which is the name of the RabbitMQ virtual host. */
	private String virtualHost;

	private String[] address;// cluster address

	public ConnectionSettings() {
		super();
	}

	public ConnectionSettings(String[] address, String username, String password, String virtualHost) {
		super();
		this.setPassword(password);
		this.setUsername(username);
		this.setAddress(address);
		this.setVirtualHost(virtualHost);
	}

	public static ConnectionSettings instance(String[] address, String aVirtualHost, String aUsername, String aPassword) {
		return new ConnectionSettings(address, aUsername, aPassword, aVirtualHost);
	}

	/**
	 * Answers my password.
	 * 
	 * @return String
	 */
	protected String password() {
		return this.password;
	}

	/**
	 * Sets my password.
	 * 
	 * @param aPassword
	 *            the String to set as my password
	 */
	private void setPassword(String aPassword) {
		this.password = aPassword;
	}

	/**
	 * Answers whether or not the user credentials are included.
	 * 
	 * @return boolean
	 */
	protected boolean hasUserCredentials() {
		return this.username() != null && this.password() != null;
	}

	/**
	 * Answers my username.
	 * 
	 * @return String
	 */
	protected String username() {
		return this.username;
	}

	/**
	 * Sets my username.
	 * 
	 * @param aUsername
	 *            the String to set as my username
	 */
	private void setUsername(String aUsername) {
		this.username = aUsername;
	}

	/**
	 * Answers my virtualHost.
	 * 
	 * @return String
	 */
	protected String virtualHost() {
		return this.virtualHost;
	}

	/**
	 * Sets my virtualHost.
	 * 
	 * @param aVirtualHost
	 *            the String to set as my virtualHost
	 */
	private void setVirtualHost(String aVirtualHost) {
		this.assertArgumentNotEmpty(aVirtualHost, "Virtual host must be provided.");

		this.virtualHost = aVirtualHost;
	}

	protected String[] address() {
		return this.address;
	}

	private void setAddress(String[] address) {
		this.address = address;
	}

}
