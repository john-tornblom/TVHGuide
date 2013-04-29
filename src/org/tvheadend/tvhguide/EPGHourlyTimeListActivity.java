package org.tvheadend.tvhguide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EPGHourlyTimeListActivity extends EPGTimeListActivity {

	private static final int DEFAULT_HOURS = 24;

	@Override
	protected List<Date> createTimeSlots() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		List<Date> timeSlots = new ArrayList<Date>();

		Calendar cal = Calendar.getInstance();
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		int maxHours = prefs.getInt("epg.hourly.timeslots", DEFAULT_HOURS);
		for (int i = 0; i < maxHours; i++) {
			timeSlots.add(cal.getTime());
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		return timeSlots;
	}

}
