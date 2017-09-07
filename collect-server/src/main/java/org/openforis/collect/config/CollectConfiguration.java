package org.openforis.collect.config;

public abstract class CollectConfiguration {

	private static boolean developmentMode;
	private static String usersRestfulApiUrl;
	
	public static void initUsersServiceConfiguration(ServiceConfiguration usersServiceConfiguration) {
		usersRestfulApiUrl = usersServiceConfiguration.generateUrl() + "/of-users/api";
	}
	
	public static String getUsersRestfulApiUrl() {
		return usersRestfulApiUrl;
	}
	
	public static boolean isDevelopmentMode() {
		return developmentMode;
	}
	
	public static void setDevelopmentMode(boolean developmentMode) {
		CollectConfiguration.developmentMode = developmentMode;
	}
	
	public static class ServiceConfiguration {
		
		private String protocol;
		private String host;
		private int port;
		
		public ServiceConfiguration(String protocol, String host, int port) {
			super();
			this.protocol = protocol;
			this.host = host;
			this.port = port;
		}
		
		public String generateUrl() {
			return protocol + "://" + host + ":" + String.valueOf(port);
		}
		
		public String getProtocol() {
			return protocol;
		}
		
		public String getHost() {
			return host;
		}
		
		public int getPort() {
			return port;
		}
	}
}
