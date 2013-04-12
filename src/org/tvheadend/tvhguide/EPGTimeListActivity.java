package org.tvheadend.tvhguide;

import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.tvheadend.tvhguide.htsp.HTSListener;
import org.tvheadend.tvhguide.htsp.HTSService;
import org.tvheadend.tvhguide.intent.SearchEPGIntent;
import org.tvheadend.tvhguide.intent.SearchIMDbIntent;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.Programme;
import org.tvheadend.tvhguide.model.Recording;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author mike.toggweiler time based epg list. Show next starting programm by
 *         channel
 * 
 */
public class EPGTimeListActivity extends FragmentActivity {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean theme = prefs.getBoolean("lightThemePref", false);
		setTheme(theme ? R.style.CustomTheme_Light : R.style.CustomTheme);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.epgnow_list_activity);

		// create timelost based on actual time
		List<String> timeSlots = new ArrayList<String>();
		java.text.DateFormat format = DateFormat.getTimeFormat(this);
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		for (int h = hour; h < 25; h++) {
			timeSlots.add(format.format(new Time(h, 0, 0)));
		}
		// Set<String> timeslots = prefs.getStringSet("epg.timeslots",
		// defaults);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(),
				timeSlots.toArray(new String[timeSlots.size()]));

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.epgnow_list_title);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
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
			return true;
		}
		case R.id.mi_channels: {
			Intent intent = new Intent(getBaseContext(),
					ChannelListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_epg_timeline: {
			Intent intent = new Intent(getBaseContext(),
					EPGTimelineActivity.class);
			startActivity(intent);
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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private final String[] timeslots;

		public SectionsPagerAdapter(FragmentManager fm, String[] timeslots) {
			super(fm);
			this.timeslots = timeslots;
		}

		@Override
		public Fragment getItem(int position) {
			// When the given tab is selected, show the tab contents in the
			// container view.
			EPGListFragment fragment = new EPGListFragment();
			Bundle args = new Bundle();
			args.putString(EPGListFragment.ARG_TIME_SLOT, timeslots[position]);
			fragment.setArguments(args);

			return fragment;
		}

		@Override
		public int getCount() {
			return timeslots.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return timeslots[position];
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class EPGListFragment extends ListFragment implements
			HTSListener {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_TIME_SLOT = "time_slot";

		private EPGTimeListAdapter prAdapter;

		public EPGListFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			String timeSlot = getArguments().getString(ARG_TIME_SLOT);
			java.text.DateFormat format = DateFormat
					.getTimeFormat(getActivity());
			Date date;
			try {
				date = format.parse(timeSlot);
				Calendar cal = Calendar.getInstance();
				cal.clear(Calendar.MINUTE);
				cal.clear(Calendar.SECOND);
				cal.clear(Calendar.MILLISECOND);
				cal.set(Calendar.HOUR_OF_DAY, date.getHours());
				date = cal.getTime();

				TVHGuideApplication thv = (TVHGuideApplication) getActivity()
						.getApplication();
				List<Channel> list = thv.getChannels();
				prAdapter = new EPGTimeListAdapter(getActivity(), list, date);
				prAdapter.sort();
				setListAdapter(prAdapter);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void onResume() {
			super.onResume();
			TVHGuideApplication app = (TVHGuideApplication) getActivity()
					.getApplication();
			app.addListener(this);
		}

		@Override
		public void onPause() {
			TVHGuideApplication app = (TVHGuideApplication) getActivity()
					.getApplication();
			app.removeListener(this);
			super.onPause();
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			registerForContextMenu(getListView());
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.string.menu_record:
			case R.string.menu_record_cancel:
			case R.string.menu_record_remove: {
				getActivity().startService(item.getIntent());
				return true;
			}
			default: {
				return super.onContextItemSelected(item);
			}
			}
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Programme p = prAdapter.getProgrammeAt(info.position);

			menu.setHeaderTitle(p.title);

			Intent intent = new Intent(getActivity(), HTSService.class);

			MenuItem item = null;

			if (p != null) {
				if (p.recording == null) {
					intent.setAction(HTSService.ACTION_DVR_ADD);
					intent.putExtra("eventId", p.id);
					intent.putExtra("channelId", p.channel.id);
					item = menu.add(ContextMenu.NONE, R.string.menu_record,
							ContextMenu.NONE, R.string.menu_record);
				} else if (p.isRecording() || p.isScheduled()) {
					intent.setAction(HTSService.ACTION_DVR_CANCEL);
					intent.putExtra("id", p.recording.id);
					item = menu.add(ContextMenu.NONE,
							R.string.menu_record_cancel, ContextMenu.NONE,
							R.string.menu_record_cancel);
				} else {
					intent.setAction(HTSService.ACTION_DVR_DELETE);
					intent.putExtra("id", p.recording.id);
					item = menu.add(ContextMenu.NONE,
							R.string.menu_record_remove, ContextMenu.NONE,
							R.string.menu_record_remove);
				}

				item.setIntent(intent);

				item = menu.add(ContextMenu.NONE, R.string.search_hint,
						ContextMenu.NONE, R.string.search_hint);
				item.setIntent(new SearchEPGIntent(getActivity(), p.title));

				item = menu.add(ContextMenu.NONE, ContextMenu.NONE,
						ContextMenu.NONE, "IMDb");
				item.setIntent(new SearchIMDbIntent(getActivity(), p.title));
			}
		}

		@Override
		public void onMessage(String action, final Object obj) {
			if (action.equals(TVHGuideApplication.ACTION_CHANNEL_ADD)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						prAdapter.add((Channel) obj);
						prAdapter.notifyDataSetChanged();
						prAdapter.sort();
					}
				});
			} else if (action.equals(TVHGuideApplication.ACTION_CHANNEL_DELETE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						prAdapter.remove((Channel) obj);
						prAdapter.notifyDataSetChanged();
					}
				});
			} else if (action.equals(TVHGuideApplication.ACTION_CHANNEL_UPDATE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Channel channel = (Channel) obj;
						prAdapter.updateView(getListView(), channel);
					}
				});
			} else if (action.equals(TVHGuideApplication.ACTION_PROGRAMME_ADD)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Programme p = (Programme) obj;
						prAdapter.updateView(getListView(), p.channel);
					}
				});
			} else if (action
					.equals(TVHGuideApplication.ACTION_PROGRAMME_DELETE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Programme p = (Programme) obj;
						prAdapter.updateView(getListView(), p.channel);
					}
				});
			} else if (action
					.equals(TVHGuideApplication.ACTION_PROGRAMME_UPDATE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Programme p = (Programme) obj;
						prAdapter.updateView(getListView(), p.channel);
					}
				});
			} else if (action.equals(TVHGuideApplication.ACTION_DVR_UPDATE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Recording rec = (Recording) obj;
						for (Channel c : prAdapter.list) {
							for (Programme p : c.epg) {
								if (rec == p.recording) {
									prAdapter.updateView(getListView(), c);
									return;
								}
							}
						}
					}
				});
			}
		}
	}

	static class EPGTimeListAdapter extends ArrayAdapter<Channel> {

		Activity context;
		List<Channel> list;
		Date timeSlot;

		EPGTimeListAdapter(Activity context, List<Channel> list, Date timeSlot) {
			super(context, R.layout.epgnow_list_widget, list);
			this.context = context;
			this.list = list;
			this.timeSlot = timeSlot;
		}

		public void sort() {
			sort(new Comparator<Channel>() {

				public int compare(Channel x, Channel y) {
					return x.compareTo(y);
				}
			});
		}

		public Programme getProgrammeAt(int position) {
			Channel item = getItem(position);
			return getProgrammeStartingAfter(item, timeSlot);
		}

		public void updateView(ListView listView, Channel channel) {
			for (int i = 0; i < listView.getChildCount(); i++) {
				View view = listView.getChildAt(i);
				int pos = listView.getPositionForView(view);
				Channel pr = (Channel) listView.getItemAtPosition(pos);

				if (view.getTag() == null || pr == null) {
					continue;
				}

				if (channel.id != pr.id) {
					continue;
				}

				EPGTimeListViewWrapper wrapper = (EPGTimeListViewWrapper) view
						.getTag();
				wrapper.repaint(channel);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			EPGTimeListViewWrapper wrapper = null;

			if (row == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater
						.inflate(R.layout.epgnow_list_widget, null, false);

				wrapper = new EPGTimeListViewWrapper(row, timeSlot);
				row.setTag(wrapper);

			} else {
				wrapper = (EPGTimeListViewWrapper) row.getTag();
			}

			Channel channel = getItem(position);
			wrapper.repaint(channel);
			return row;
		}

	}

	/**
	 * get next program based on channel and timeslot
	 * 
	 * @return
	 */
	public static Programme getProgrammeStartingAfter(Channel channel,
			Date timeSlot) {
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
