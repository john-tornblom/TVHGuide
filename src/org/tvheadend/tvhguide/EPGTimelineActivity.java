package org.tvheadend.tvhguide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.tvheadend.tvhguide.model.Channel;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author mike toggweiler
 * 
 */
public class EPGTimelineActivity extends ListActivity {

	private TimelineAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean theme = prefs.getBoolean("lightThemePref", false);
		setTheme(theme ? R.style.CustomTheme_Light : R.style.CustomTheme);

		super.onCreate(savedInstanceState);

		TVHGuideApplication tvh = (TVHGuideApplication) getApplication();
		adapter = new TimelineAdapter(this, new ArrayList<Channel>(
				tvh.getChannels()));
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mi_settings: {
			Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
			startActivityForResult(intent, R.id.mi_settings);
			return true;
		}
		// case R.id.mi_refresh: {
		// connect(true);
		// return true;
		// }
		case R.id.mi_recordings: {
			Intent intent = new Intent(getBaseContext(),
					RecordingListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_epg_list: {
			Intent intent = new Intent(getBaseContext(),
					EPGTimeListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_channels: {
			Intent intent = new Intent(getBaseContext(),
					ChannelListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_epg_timeline: {
			return true;
		}
		case R.id.mi_search: {
			onSearchRequested();
			return true;
		}
		// case R.id.mi_tags: {
		// tagDialog.show();
		// return true;
		// }
		default: {
			return super.onOptionsItemSelected(item);
		}
		}
	}

	class TimelineAdapter extends ArrayAdapter<Channel> {

		TimelineAdapter(Activity context, List<Channel> list) {
			super(context, R.layout.epgtimeline_widget, list);
		}

		public void sort() {
			sort(new Comparator<Channel>() {

				public int compare(Channel x, Channel y) {
					return x.compareTo(y);
				}
			});
		}

		public void updateView(ListView listView, Channel channel) {
			for (int i = 0; i < listView.getChildCount(); i++) {
				View view = listView.getChildAt(i);
				int pos = listView.getPositionForView(view);
				Channel ch = (Channel) listView.getItemAtPosition(pos);

				if (view.getTag() == null || ch == null) {
					continue;
				}

				if (channel.id != ch.id) {
					continue;
				}

				EPGTimelineViewWrapper wrapper = (EPGTimelineViewWrapper) view
						.getTag();
				wrapper.repaint(channel);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			EPGTimelineViewWrapper wrapper;

			Channel ch = getItem(position);
			Activity activity = (Activity) getContext();

			if (row == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				row = inflater
						.inflate(R.layout.epgtimeline_widget, null, false);
				row.requestLayout();
				wrapper = new EPGTimelineViewWrapper(EPGTimelineActivity.this,
						row);
				row.setTag(wrapper);

			} else {
				wrapper = (EPGTimelineViewWrapper) row.getTag();
			}

			wrapper.repaint(ch);
			return row;
		}
	}
}
