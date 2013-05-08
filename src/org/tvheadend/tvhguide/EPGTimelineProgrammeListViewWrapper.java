package org.tvheadend.tvhguide;

import java.util.Calendar;
import java.util.Date;

import org.tvheadend.tvhguide.model.Programme;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.LinearLayout;

public class EPGTimelineProgrammeListViewWrapper extends
		ProgrammeListViewWrapper {

	private TypedArray colors;
	private LinearLayout container;
	private LinearLayout container2;
	private LinearLayout container3;
	private static final int LAYOUT_HEIGHT = 68;

	public EPGTimelineProgrammeListViewWrapper(View base) {
		super(base);
		Resources res = base.getResources();
		colors = res.obtainTypedArray(R.array.pref_color_content_type);

		container = (LinearLayout) base.findViewById(R.id.programme_container);
		container2 = (LinearLayout) base
				.findViewById(R.id.programme_container2);
		container3 = (LinearLayout) base
				.findViewById(R.id.programme_container3);
	}

	@Override
	public void repaint(Programme p) {
		super.repaint(p);

		// colorize based on series category
		// the first byte of hex number represents the main category
		int type = 0;
		if (p.contentType > 0) {
			type = ((p.contentType) / 16) - 1;
		}
		// there are 11 categories, calculate modulo if more categories are
		// returned than colors are defined
		int index = type % colors.length();
		int color = colors.getColor(index, 0);

		// use first byte of hex number to calculate color offset
		if (type > 0) {
			int subType = p.contentType & 0x0F;
			color -= subType * 0x040404;
		}

		System.out.println(p.title + "-" + p.contentType + ":" + type + ":"
				+ index + ":" + color);
		container.setBackgroundColor(color);

		// define width based on duration
		Date start = Calendar.getInstance().getTime();
		if (p.stop.after(start)) {
			if (p.start.after(start)) {
				start = p.start;
			}

			long remainingMillis = p.stop.getTime() - start.getTime();
			long minutes = remainingMillis / (60 * 1000);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					(int) minutes * EPGTimelineViewWrapper.WIDTH_PER_MINUTE,
					LAYOUT_HEIGHT);
			container.setLayoutParams(layoutParams);
			container.setVisibility(LinearLayout.VISIBLE);

			container2.setLayoutParams(layoutParams);
			container3.setLayoutParams(layoutParams);
		} else {
			container.setVisibility(LinearLayout.GONE);
		}
		container.invalidate();
		container2.invalidate();
		container3.invalidate();
	}
}
