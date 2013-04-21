/*
 *  Copyright (C) 2011 John Törnblom
 *
 * This file is part of TVHGuide.
 *
 * TVHGuide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TVHGuide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TVHGuide.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tvheadend.tvhguide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.tvheadend.tvhguide.EPGTimelineActivity.OnEPGScrollListener;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.Programme;
import org.tvheadend.tvhguide.ui.HorizontalListView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 
 * @author mike toggweiler
 */
public class EPGTimelineViewWrapper implements OnItemClickListener,
		OnTouchListener, OnEPGScrollListener {

	private ImageView icon;
	private LinearLayout timeline;
	private final EPGTimelineActivity context;
	private HorizontalListView horizontialListView;
	private boolean locked;

	public EPGTimelineViewWrapper(EPGTimelineActivity context, View base) {
		this.context = context;
		icon = (ImageView) base.findViewById(R.id.ch_icon);

		timeline = (LinearLayout) base.findViewById(R.id.ch_timeline);
	}

	public void repaint(Channel channel) {

		if (horizontialListView != null) {
			horizontialListView.setOnTouchListener(null);
			horizontialListView.setOnItemClickListener(null);
			horizontialListView = null;
		}

		timeline.removeAllViews();

		// SharedPreferences prefs = PreferenceManager
		// .getDefaultSharedPreferences(icon.getContext());
		// Boolean showIcons = prefs.getBoolean("showIconPref", false);
		// icon.setVisibility(showIcons ? ImageView.VISIBLE : ImageView.GONE);
		icon.setBackground(new BitmapDrawable(context.getResources(),
				channel.iconBitmap));

		if (channel.isRecording()) {
			icon.setImageResource(R.drawable.ic_rec_small);
		} else {
			icon.setImageDrawable(null);
		}
		icon.invalidate();

		horizontialListView = new HorizontalListView(context, null);
		horizontialListView.setClickable(true);
		horizontialListView.setOnItemClickListener(this);
		TimelineProgrammeAdapter adapter = new TimelineProgrammeAdapter(
				context, new ArrayList<Programme>(channel.epg));
		horizontialListView.setAdapter(adapter);
		horizontialListView.setOnTouchListener(this);

		context.registerForContextMenu(horizontialListView);

		timeline.addView(horizontialListView);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		try {
			locked = true;
			context.onHorizontalViewTouch(event);
		} finally {
			locked = false;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		Programme p = (Programme) adapterView.getItemAtPosition(position);
		Intent intent = new Intent(context, ProgrammeActivity.class);
		intent.putExtra("eventId", p.id);
		intent.putExtra("channelId", p.channel.id);
		context.startActivity(intent);
	}

	class TimelineProgrammeAdapter extends ArrayAdapter<Programme> {

		TimelineProgrammeAdapter(Context context, List<Programme> epg) {
			super(context, R.layout.epgtimeline_programme_widget, epg);
		}

		public void sort() {
			sort(new Comparator<Programme>() {

				public int compare(Programme x, Programme y) {
					return x.compareTo(y);
				}
			});
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			EPGTimelineProgrammeListViewWrapper wrapper;

			Programme pr = getItem(position);
			Activity activity = (Activity) getContext();

			if (row == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				row = inflater.inflate(R.layout.epgtimeline_programme_widget,
						null, false);
				row.requestLayout();
				wrapper = new EPGTimelineProgrammeListViewWrapper(row);
				row.setTag(wrapper);

			} else {
				wrapper = (EPGTimelineProgrammeListViewWrapper) row.getTag();
			}

			wrapper.repaint(pr);
			return row;
		}
	}

	@Override
	public void onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (locked) {
			return;
		}
		synchronized (horizontialListView) {
			horizontialListView.flingBy((int) velocityX);
		}
	}

	@Override
	public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (locked) {
			return;
		}
		synchronized (horizontialListView) {
			horizontialListView.scrollTo((int) (horizontialListView
					.getScrollPositionX() + distanceX));
		}
	}
}
