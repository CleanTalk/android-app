package org.cleantalk.app.model;

public class Site {
	private final String siteId_;
	private final String siteName_;
	private final String hostname_;
	private final String faviconUrl_;

	public Site(String siteId, String siteName, String hostname, String faviconUrl) {
		siteId_ = siteId;
		siteName_ = siteName;
		hostname_ = hostname;
		faviconUrl_ = faviconUrl;
	}

	public String getSiteId() {
		return siteId_;
	}

	public String getSiteName() {
		return siteName_;
	}

	public String getHostname() {
		return hostname_;
	}

	public String getFaviconUrl() {
		return faviconUrl_;
	}

}
