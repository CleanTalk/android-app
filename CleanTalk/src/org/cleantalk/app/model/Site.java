package org.cleantalk.app.model;

public class Site {
	private final int siteId_;
	private final String siteName_;
	private final int todayAllowed_;
	private final int todayBlocked_;
	private final int weekAllowed_;
	private final int weekBlocked_;
	private final int yesterdayAllowed_;
	private final int yesterdayBlocked_;

	public Site(String siteName, int siteId, int todayBlocked, int todayAllowed, int yesterdayBlocked, int yesterdayAllowed,
			int weekBlocked, int weekAllowed) {
		siteId_ = siteId;
		siteName_ = siteName;
		todayAllowed_ = todayAllowed;
		todayBlocked_ = todayBlocked;
		weekAllowed_ = weekAllowed;
		weekBlocked_ = weekBlocked;
		yesterdayAllowed_ = yesterdayAllowed;
		yesterdayBlocked_ = yesterdayBlocked;
	}

	public int getSiteId() {
		return siteId_;
	}
	public String getSiteName() {
		return siteName_;
	}
	public int getTodayAllowed() {
		return todayAllowed_;
	}

	public int getTodayBlocked() {
		return todayBlocked_;
	}

	public int getWeekAllowed() {
		return weekAllowed_;
	}

	public int getWeekBlocked() {
		return weekBlocked_;
	}

	public int getYesterdayAllowed() {
		return yesterdayAllowed_;
	}

	public int getYesterdayBlocked() {
		return yesterdayBlocked_;
	}
}