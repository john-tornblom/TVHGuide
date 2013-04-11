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
	private static final int WIDTH_PER_MINUTE = 10;

	public EPGTimelineProgrammeListViewWrapper(View base) {
		super(base);
		Resources res = base.getResources();
		colors = res.obtainTypedArray(R.array.pref_color_content_type);

		container = (LinearLayout) base.findViewById(R.id.programme_container);
	}

	@Override
	public void repaint(Programme p) {
		super.repaint(p);

		// colorize based on series category
		int color = colors.getColor(p.contentType, 0);
		container.setBackgroundColor(color);

		// define width based on duration
		Date start = Calendar.getInstance().getTime();
		if (p.stop.after(start)) {
			if (p.start.after(start)) {
				start = p.start;
			}

			long remainingMillis = p.stop.getTime() - start.getTime();
			long minutes = remainingMillis / (60 * 60 * 1000);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					(int) minutes * WIDTH_PER_MINUTE, LayoutParams.MATCH_PARENT);
			container.setLayoutParams(layoutParams);
			container.setVisibility(LinearLayout.VISIBLE);
		} else {
			container.setVisibility(LinearLayout.GONE);
		}
		container.invalidate();
	}
}
