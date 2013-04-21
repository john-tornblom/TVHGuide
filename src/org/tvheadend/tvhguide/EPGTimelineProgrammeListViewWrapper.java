package org.tvheadend.tvhguide;

import java.util.Calendar;
import java.util.Date;

import org.tvheadend.tvhguide.model.Programme;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class EPGTimelineProgrammeListViewWrapper extends
		ProgrammeListViewWrapper {

	private TypedArray colors;
	LinearLayout container;
	private LinearLayout container2;
	private LinearLayout container3;
	private static final int WIDTH_PER_MINUTE = 10;

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
		int index = p.contentType % colors.length();
		int color = colors.getColor(index, 0);
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
					(int) minutes * WIDTH_PER_MINUTE, LayoutParams.MATCH_PARENT);
			container.setLayoutParams(layoutParams);
			container.setVisibility(LinearLayout.VISIBLE);

			container2.setLayoutParams(layoutParams);
			container3.setLayoutParams(layoutParams);

			System.out.println("minutes:" + remainingMillis + ":" + minutes
					+ ", width:" + (minutes * WIDTH_PER_MINUTE));
		} else {
			container.setVisibility(LinearLayout.GONE);
		}
		container.invalidate();
		container2.invalidate();
		container3.invalidate();
	}
}
