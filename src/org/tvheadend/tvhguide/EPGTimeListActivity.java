package org.tvheadend.tvhguide;

import java.sql.Time;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tvheadend.tvhguide.model.Channel;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author mike.toggweiler time based epg list. Show next starting programm by
 *         channel
 * 
 */
public class EPGTimeListActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean theme = prefs.getBoolean("lightThemePref", false);
		setTheme(theme ? R.style.CustomTheme_Light : R.style.CustomTheme);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.epgnow_list_activity);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each timeslot create tab
		HashSet<String> defaults = new HashSet<String>();
		java.text.DateFormat format = DateFormat.getTimeFormat(this);
		defaults.add(format.format(new Time(12, 0, 0)));
		defaults.add(format.format(new Time(16, 0, 0)));
		defaults.add(format.format(new Time(20, 0, 0)));
		Set<String> timeslots = prefs.getStringSet("epg.timeslots", defaults);
		for (String timeslot : timeslots) {
			actionBar.addTab(actionBar.newTab().setText(timeslot)
					.setTabListener(this));
		}

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
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		EPGListFragment fragment = new EPGListFragment();
		Bundle args = new Bundle();
		args.putString(EPGListFragment.ARG_TIME_SLOT, tab.getText().toString());
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment).commit();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class EPGListFragment extends ListFragment {
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

		// @Override
		// public View onCreateView(LayoutInflater inflater,
		// ViewGroup convertView, Bundle savedInstanceState) {
		// getArguments().getInt(ARG_SECTION_NUMBER);
		//
		// // generate own view
		// View rowview = convertView;
		// if (null == rowview) {
		// rowview = inflater.inflate(R.layout.epgnow_list_widget, null);
		// }
		//
		// return rowview;
		// }
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

}
