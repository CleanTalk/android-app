package org.cleantalk.app.model;

public class Site {
	private final String siteId_;
	private final String siteName_;
	private final String faviconUrl_;
	private final int todayAllowed_;
	private final int todayBlocked_;
	private final int yesterdayAllowed_;
	private final int yesterdayBlocked_;
	private final int weekAllowed_;
	private final int weekBlocked_;

	public Site(String siteName, String siteId, String faviconUrl, int todayAllowed, int todayBlocked, int yesterdayAllowed,
			int yesterdayBlocked, int weekAllowed, int weekBlocked) {
		siteId_ = siteId;
		siteName_ = siteName;
		faviconUrl_ = faviconUrl;
		todayAllowed_ = todayAllowed;
		todayBlocked_ = todayBlocked;
		weekAllowed_ = weekAllowed;
		weekBlocked_ = weekBlocked;
		yesterdayAllowed_ = yesterdayAllowed;
		yesterdayBlocked_ = yesterdayBlocked;
	}

	public String getSiteId() {
		return siteId_;
	}

	public String getSiteName() {
		return siteName_;
	}

	public String getFaviconUrl() {
		return faviconUrl_;
	}

	public int getTodayAllowed() {
		return todayAllowed_;
	}

	public int getTodayBlocked() {
		return todayBlocked_;
	}

	public int getYesterdayAllowed() {
		return yesterdayAllowed_;
	}

	public int getYesterdayBlocked() {
		return yesterdayBlocked_;
	}

	public int getWeekAllowed() {
		return weekAllowed_;
	}

	public int getWeekBlocked() {
		return weekBlocked_;
	}
}
