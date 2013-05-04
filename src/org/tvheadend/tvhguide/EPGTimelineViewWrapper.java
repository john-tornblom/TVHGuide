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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.tvheadend.tvhguide.EPGTimelineActivity.OnEPGScrollListener;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.Programme;
import org.tvheadend.tvhguide.ui.HorizontalListView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

/**
 * 
 * @author mike toggweiler
 */
public class EPGTimelineViewWrapper implements OnItemClickListener,
		OnTouchListener, OnEPGScrollListener {

	private ImageView icon;
	private final EPGTimelineActivity context;
	private HorizontalListView horizontalListView;
	private boolean locked;

	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			try {
				locked = true;
				context.notifyOnScoll(horizontalListView.getScrollPositionX());
			} finally {
				locked = false;
			}
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				locked = true;
				context.notifyOnFling(velocityX);
			} finally {
				locked = false;
			}
			return false;
		};
	};
	private GestureDetector mGesture;
	private final EventLoadHandler loadHandler;
	static final int WIDTH_PER_MINUTE = 5;

	public EPGTimelineViewWrapper(EPGTimelineActivity context, View base,
			EventLoadHandler loadHandler) {
		this.context = context;
		this.loadHandler = loadHandler;
		icon = (ImageView) base.findViewById(R.id.ch_icon);

		mGesture = new GestureDetector(context, mOnGesture);

		horizontalListView = (HorizontalListView) base
				.findViewById(R.id.ch_timeline);
		horizontalListView.setClickable(true);
		horizontalListView.setOnItemClickListener(this);
		horizontalListView.setOnTouchListener(this);

		context.registerForContextMenu(horizontalListView);
	}

	public void repaintHeader() {
		Calendar cal = Calendar.getInstance();
		List<Date> dates = new ArrayList<Date>();
		dates.add(cal.getTime());

		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		for (int i = 0; i < 32; ++i) {
			cal.add(Calendar.HOUR_OF_DAY, 1);
			dates.add(cal.getTime());
		}

		TimelineHeaderAdaper adapter = new TimelineHeaderAdaper(context, dates);
		horizontalListView.setAdapter(adapter);
		horizontalListView.scrollTo(context.getLastEPGScrollPosition());
		horizontalListView.invalidate();
	}

	public void repaint(Channel channel) {

		if (channel.id == 0) {
			repaintHeader();
			return;
		}

		// SharedPreferences prefs = PreferenceManager
		// .getDefaultSharedPreferences(icon.getContext());
		// Boolean showIcons = prefs.getBoolean("showIconPref", false);
		// icon.setVisibility(showIcons ? ImageView.VISIBLE : ImageView.GONE);
		icon.setBackgroundDrawable(new BitmapDrawable(context.getResources(),
				channel.iconBitmap));

		if (channel.isRecording()) {
			icon.setImageResource(R.drawable.ic_rec_small);
		} else {
			icon.setImageDrawable(null);
		}
		icon.invalidate();

		TimelineProgrammeAdapter adapter = new TimelineProgrammeAdapter(
				context, new ArrayList<Programme>(channel.epg));
		horizontalListView.setAdapter(adapter);
		horizontalListView.scrollTo(context.getLastEPGScrollPosition());
		horizontalListView.invalidate();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		mGesture.onTouchEvent(event);
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		Object obj = adapterView.getItemAtPosition(position);
		if (!(obj instanceof Programme)) {
			return;
		}
		Programme p = (Programme) obj;

		if (p == null) {
			// load next
			loadHandler.loadNextEvents();
		} else {
			Intent intent = new Intent(context, ProgrammeActivity.class);
			intent.putExtra("eventId", p.id);
			intent.putExtra("channelId", p.channel.id);
			context.startActivity(intent);
		}
	}

	protected void loadNext() {

	}

	@Override
	public void scrollTo(int scrollTo) {
		if (locked) {
			return;
		}
		synchronized (horizontalListView) {
			horizontalListView.scrollTo(scrollTo);
		}
	}

	@Override
	public void flingBy(float velocityX) {
		if (locked) {
			return;
		}
		synchronized (horizontalListView) {
			horizontalListView.flingBy(velocityX);
		}
	}

	class TimelineHeaderAdaper extends ArrayAdapter<Date> {
		TimelineHeaderAdaper(Context context, List<Date> epg) {
			super(context, R.layout.epgtimeline_programme_header, epg);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			EPGTimelineProgrammeHeaderViewWrapper wrapper;

			Activity activity = (Activity) getContext();
			Date dt = getItem(position);
			if (row == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				row = inflater.inflate(R.layout.epgtimeline_programme_header,
						null, false);
				row.requestLayout();
				wrapper = new EPGTimelineProgrammeHeaderViewWrapper(context,
						row);
				row.setTag(wrapper);

			} else {
				wrapper = (EPGTimelineProgrammeHeaderViewWrapper) row.getTag();
			}

			if (wrapper != null) {
				wrapper.repaint(dt);
			}
			return row;
		}
	}

	class TimelineProgrammeAdapter extends ArrayAdapter<Programme> {

		public static final int VIEW_TYPE_END = 100;
		private Button button;

		TimelineProgrammeAdapter(Context context, List<Programme> epg) {
			super(context, R.layout.epgtimeline_programme_widget, epg);
			button = new Button(context);
			button.setBackgroundResource(android.R.drawable.ic_menu_more);
		}

		public void sort() {
			sort(new Comparator<Programme>() {

				public int compare(Programme x, Programme y) {
					return x.compareTo(y);
				}
			});
		}

		@Override
		public int getCount() {
			return super.getCount() + 1;
		}

		@Override
		public int getItemViewType(int position) {
			if (position == super.getCount()) {
				return VIEW_TYPE_END;
			}
			return super.getItemViewType(position);
		}

		@Override
		public Programme getItem(int position) {
			if (position == super.getCount()) {
				return null;
			}
			return super.getItem(position);
		}

		@Override
		public long getItemId(int position) {
			if (position == super.getCount()) {
				return VIEW_TYPE_END;
			}
			return super.getItemId(position);
		}

		@Override
		public int getViewTypeCount() {
			return super.getViewTypeCount() + 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			EPGTimelineProgrammeListViewWrapper wrapper;

			if (position == super.getCount()) {
				return button;
			} else {
				Programme pr = getItem(position);
				Activity activity = (Activity) getContext();

				if (row == null) {
					LayoutInflater inflater = activity.getLayoutInflater();
					row = inflater.inflate(
							R.layout.epgtimeline_programme_widget, null, false);
					row.requestLayout();
					wrapper = new EPGTimelineProgrammeListViewWrapper(row);
					row.setTag(wrapper);

				} else {
					wrapper = (EPGTimelineProgrammeListViewWrapper) row
							.getTag();
				}

				if (wrapper != null) {
					wrapper.repaint(pr);
				}
				return row;
			}
		}
	}
}
