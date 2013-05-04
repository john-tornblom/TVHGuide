package org.tvheadend.tvhguide;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EPGTimelineProgrammeHeaderViewWrapper {
	TextView date;
	TextView hour;
	private LinearLayout container;
	private java.text.DateFormat timeFormat;
	private java.text.DateFormat dateFormat;

	public EPGTimelineProgrammeHeaderViewWrapper(Context context, View base) {
		date = (TextView) base.findViewById(R.id.title_date);
		hour = (TextView) base.findViewById(R.id.title_hour);
		container = (LinearLayout) base.findViewById(R.id.programme_container);
		timeFormat = DateFormat.getTimeFormat(context);
		dateFormat = DateFormat.getDateFormat(context);
	}

	public void repaint(Date dt) {
		date.setText(dateFormat.format(dt));
		hour.setText(timeFormat.format(dt));

		// calculate remaining minutes till next hour
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		long remainingMillis = cal.getTimeInMillis() - dt.getTime();
		long minutes = remainingMillis / (60 * 1000);

		int width = (int) (minutes * EPGTimelineViewWrapper.WIDTH_PER_MINUTE);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				width, 45);
		LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
				width, LinearLayout.LayoutParams.WRAP_CONTENT);
		container.setLayoutParams(layoutParams);
		container.setVisibility(LinearLayout.VISIBLE);

		date.setLayoutParams(layoutParams2);
		hour.setLayoutParams(layoutParams2);

		date.invalidate();
		hour.invalidate();
		container.invalidate();
	}
}
