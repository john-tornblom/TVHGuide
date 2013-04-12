package org.tvheadend.tvhguide;

import java.util.Date;
import java.util.Iterator;

import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.Programme;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

public class EPGTimeListViewWrapper extends ProgrammeListViewWrapper {

	private final Date timeSlot;
	private ImageView icon;

	public EPGTimeListViewWrapper(View base, Date timeSlot) {
		super(base);

		icon = (ImageView) base.findViewById(R.id.ch_icon);

		this.timeSlot = timeSlot;
	}

	@SuppressWarnings("deprecation")
	public void repaint(Channel channel) {

		if (icon != null) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(icon.getContext());
			Boolean showIcons = prefs.getBoolean("showIconPref", false);
			icon.setVisibility(showIcons ? ImageView.VISIBLE : ImageView.GONE);
			icon.setBackgroundDrawable(new BitmapDrawable(channel.iconBitmap));
			icon.invalidate();
		}

		Programme pr = getNextProgramme(channel);
		if (pr == null) {
			title.setText(R.string.ch_no_transmission);
		} else {
			super.repaint(pr);
		}
	}

	/**
	 * get next program based on channel and timeslot
	 * 
	 * @return
	 */
	protected Programme getNextProgramme(Channel channel) {
		Iterator<Programme> it = channel.epg.iterator();
		if (!channel.isTransmitting) {
			return null;
		}
		// find first programm after timeslot
		while (it.hasNext()) {
			Programme pr = it.next();
			if (pr.start.equals(timeSlot) || pr.start.after(timeSlot)) {
				return pr;
			}
		}
		return null;
	}
}
