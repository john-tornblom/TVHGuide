package org.tvheadend.tvhguide;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

public class EPGPrimeTimeListActivity extends EPGTimeListActivity {

	private static final int SLOTS = 24;

	@SuppressWarnings("deprecation")
	@Override
	protected List<Date> createTimeSlots() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		java.text.DateFormat format = DateFormat.getTimeFormat(this);

		Set<String> defaultPrimeTimeSlots = new TreeSet<String>();
		defaultPrimeTimeSlots.add("18:30");
		defaultPrimeTimeSlots.add("20:15");
		defaultPrimeTimeSlots.add("22:00");

		List<String> primeTimeSlots = new ArrayList<String>(prefs.getStringSet(
				"epg.prime.timeslots", defaultPrimeTimeSlots));

		List<Date> timeSlots = new ArrayList<Date>();

		//
		Calendar cal = Calendar.getInstance();
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		List<Date> primeTimeSlotDates = new ArrayList<Date>();
		try {
			for (String dateStr : primeTimeSlots) {
				primeTimeSlotDates.add(format.parse(dateStr));
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return timeSlots;
		}

		int primeSlotIndex = 0;

		// first find timeslot occurance after actual time
		long lastDiff = -1;
		for (int i = primeTimeSlots.size() - 1; i >= 0; --i) {
			Date time = primeTimeSlotDates.get(i);
			int hour = time.getHours();
			int minute = time.getMinutes();

			Calendar cal2 = Calendar.getInstance();
			cal2.set(Calendar.HOUR_OF_DAY, hour);
			cal2.set(Calendar.MINUTE, minute);
			cal2.clear(Calendar.SECOND);
			cal2.clear(Calendar.MILLISECOND);

			if (cal2.after(cal)) {
				long diff = cal2.getTimeInMillis() - cal.getTimeInMillis();
				if (lastDiff == -1 || diff < lastDiff) {
					primeSlotIndex = i;
				}
				lastDiff = diff;
			}
		}

		for (int i = 0; i < SLOTS; i++) {
			Date time = primeTimeSlotDates.get(primeSlotIndex++);
			primeSlotIndex %= primeTimeSlotDates.size();
			int hour = time.getHours();
			int minute = time.getMinutes();

			// find next occurange of timeslot after actual time
			int currHour = cal.get(Calendar.HOUR_OF_DAY);
			while (currHour != hour) {
				cal.add(Calendar.HOUR_OF_DAY, 1);
				currHour = cal.get(Calendar.HOUR_OF_DAY);
			}
			cal.set(Calendar.MINUTE, minute);

			timeSlots.add(cal.getTime());
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		return timeSlots;
	}

}
