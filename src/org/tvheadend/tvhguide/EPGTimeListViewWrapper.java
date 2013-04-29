package org.tvheadend.tvhguide;

import java.util.Date;

import org.tvheadend.tvhguide.htsp.HTSService;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.Programme;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class EPGTimeListViewWrapper extends ProgrammeListViewWrapper {

	private final Date timeSlot;
	private ImageView icon;
	private ProgressBar progressbar;
	private final Activity context;

	public EPGTimeListViewWrapper(Activity context, View base, Date timeSlot) {
		super(base);
		this.context = context;

		icon = (ImageView) base.findViewById(R.id.ch_icon);
		progressbar = (ProgressBar) base.findViewById(R.id.ct_loading);

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

		Programme pr = ((EPGTimeListActivity) context)
				.getProgrammeStartingAfter(channel, timeSlot);
		progressbar.setVisibility(ProgressBar.GONE);
		if (!channel.isTransmitting || channel.epg.size() == 0) {
			title.setText(R.string.ch_no_transmission);
		} else if (pr == null) {
			// if last, preload next programmes of this channel
			pr = channel.epg.last();
			long nextId = pr.nextId;
			if (nextId == 0) {
				nextId = pr.id;
			}

			long diff = timeSlot.getTime() - pr.stop.getTime();
			int hoursDiff = (int) (diff / (1000 * 60 * 60));
			if (hoursDiff < 0) {
				hoursDiff = hoursDiff * -1;
			}
			if (hoursDiff == 0) {
				hoursDiff = 1;
			}
			// load events for the different of hours
			Intent intent = new Intent(context, HTSService.class);
			intent.setAction(HTSService.ACTION_GET_EVENTS);
			intent.putExtra("eventId", nextId);
			intent.putExtra("channelId", channel.id);
			intent.putExtra("count", hoursDiff + 1);
			context.startService(intent);

			// show loading
			progressbar.setVisibility(ProgressBar.VISIBLE);
		} else {
			super.repaint(pr);
		}
		progressbar.invalidate();
	}

}
