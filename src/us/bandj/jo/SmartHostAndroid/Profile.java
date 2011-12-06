package us.bandj.jo.SmartHostAndroid;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Profile implements Serializable {

	public final static String[] GOOGLE_DNS = {"8.8.8.8", "8.8.4.4"};
	public final static String[] V2EX_DNS = {"178.79.131.110", "8.8.8.8"};
	public final static String[] OPEN_DNS = {"208.67.222.222", "208.67.220.220"};

	public final static int GET_GOOGLE_DNS = 0;
	public final static int GET_V2EX_DNS = 1;
	public final static int GET_OPEN_DNS = 2;

	private String hostFileUrl;
	private String dns;

	public Profile() {
		setHostFileUrl(Config.DEFAULT_HOST_FILE_URL);
	}

	public String getHostFileUrl() {
		return hostFileUrl;
	}

	public void setHostFileUrl(String hostFileUrl) {
		this.hostFileUrl = hostFileUrl;
	}

	public String getDefaultHostFileUrl() {
		return Config.DEFAULT_HOST_FILE_URL;
	}

	public String getDns() {
		return dns;
	}

	public String[] getDNS(int which) {
		switch (which) {
			case GET_GOOGLE_DNS :
				return GOOGLE_DNS;
			case GET_V2EX_DNS :
				return V2EX_DNS;
			case GET_OPEN_DNS :
				return OPEN_DNS;
			default :
				return null;
		}
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

}
