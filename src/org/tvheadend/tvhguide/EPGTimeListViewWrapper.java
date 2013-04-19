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

public class EPGTimeListViewWrapper extends ProgrammeListViewWrapper {

	private final Date timeSlot;
	private ImageView icon;
	private final Activity context;

	public EPGTimeListViewWrapper(Activity context, View base, Date timeSlot) {
		super(base);
		this.context = context;

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

		Programme pr = EPGTimeListActivity.getProgrammeStartingAfter(channel,
				timeSlot);
		if (!channel.isTransmitting) {
			title.setText(R.string.ch_no_transmission);
		} else if (pr == null) {
			// title.setText(R.string.ch_no_transmission);
			// if last, preload next programmes of this channel
			pr = channel.epg.last();
			long nextId = pr.nextId;
			if (nextId == 0) {
				nextId = pr.id;
			}

			Intent intent = new Intent(context, HTSService.class);
			intent.setAction(HTSService.ACTION_GET_EVENTS);
			intent.putExtra("eventId", nextId);
			intent.putExtra("channelId", channel.id);
			intent.putExtra("count", 2);
			context.startService(intent);
		} else {
			super.repaint(pr);
		}
	}

}
