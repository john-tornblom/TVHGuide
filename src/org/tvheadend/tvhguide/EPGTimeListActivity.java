package org.tvheadend.tvhguide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.tvheadend.tvhguide.htsp.HTSListener;
import org.tvheadend.tvhguide.htsp.HTSService;
import org.tvheadend.tvhguide.intent.SearchEPGIntent;
import org.tvheadend.tvhguide.intent.SearchIMDbIntent;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.ChannelTag;
import org.tvheadend.tvhguide.model.Programme;
import org.tvheadend.tvhguide.model.Recording;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author mike.toggweiler time based epg list. Show next starting programm by
 *         channel
 * 
 */
public abstract class EPGTimeListActivity extends FragmentActivity {

	private static final int DEFAULT_EPG_LIST_MAX_START_TIME = 30;

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

	private AlertDialog tagDialog;

	private int firstVibleItem = 0;
	private int top = 0;
	private boolean lock;

	private List<EPGFragmentListener> m_fragmentListeners = new ArrayList<EPGFragmentListener>();

	private ArrayAdapter<ChannelTag> tagAdapter;

	private TextView tagTextView;
	private ImageView tagImageView;
	private int m_maxStartTimeAfterTimeSlotInMinutes;

	protected abstract List<Date> createTimeSlots();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean theme = prefs.getBoolean("lightThemePref", false);
		setTheme(theme ? R.style.CustomTheme_Light : R.style.CustomTheme);

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.epgnow_list_activity);

		// create timelost based on actual time
		List<Date> timeSlots = createTimeSlots();
		m_maxStartTimeAfterTimeSlotInMinutes = prefs
				.getInt("epg.timeslots.max_start_time",
						DEFAULT_EPG_LIST_MAX_START_TIME);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(),
				timeSlots.toArray(new Date[timeSlots.size()]));

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.epgnow_list_title);
		tagTextView = (TextView) findViewById(R.id.ct_title);
		tagImageView = (ImageView) findViewById(R.id.ct_logo);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.menu_tags);

		tagAdapter = new ArrayAdapter<ChannelTag>(this,
				android.R.layout.simple_dropdown_item_1line,
				new ArrayList<ChannelTag>());

		builder.setAdapter(tagAdapter,
				new android.content.DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int pos) {
						ChannelTag tag = tagAdapter.getItem(pos);
						TVHGuideApplication app = (TVHGuideApplication) getApplication();
						app.setCurrentTag(tag);

						setCurrentTag(tag);
						notifyEPGChannelListChanged();
					}
				});

		tagDialog = builder.create();
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
		case R.id.mi_refresh: {
			// connect(true);
			// return true;
		}
		case R.id.mi_recordings: {
			Intent intent = new Intent(getBaseContext(),
					RecordingListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_epg_prime: {
			if (getClass().isAssignableFrom(EPGPrimeTimeListActivity.class)) {
				return true;
			}
			Intent intent = new Intent(getBaseContext(),
					EPGPrimeTimeListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_epg_list: {
			if (getClass().isAssignableFrom(EPGHourlyTimeListActivity.class)) {
				return true;
			}
			Intent intent = new Intent(getBaseContext(),
					EPGHourlyTimeListActivity.class);
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
			Intent intent = new Intent(getBaseContext(),
					EPGTimelineActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_search: {
			onSearchRequested();
			return true;
		}
		case R.id.mi_tags: {
			tagDialog.show();
			return true;
		}
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

		private final Date[] timeslots;

		public SectionsPagerAdapter(FragmentManager fm, Date[] timeslots) {
			super(fm);
			this.timeslots = timeslots;
		}

		@Override
		public Fragment getItem(int position) {
			// When the given tab is selected, show the tab contents in the
			// container view.
			EPGListFragment fragment = new EPGListFragment(timeslots[position]);
			Bundle args = new Bundle();
			// args.put(EPGListFragment.ARG_TIME_SLOT, );
			fragment.setArguments(args);

			return fragment;
		}

		@Override
		public int getCount() {
			return timeslots.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return DateFormat.format("E k:mm", timeslots[position]);
		}

	}

	private void registerEPGFragmentListener(EPGFragmentListener listener) {
		m_fragmentListeners.add(listener);
	}

	private void unregisterEPGFragmentListener(EPGFragmentListener listener) {
		m_fragmentListeners.remove(listener);
	}

	private void notifyEPGScrollListener(AbsListView view, int position, int top) {
		if (lock) {
			return;
		}
		try {
			lock = true;
			this.firstVibleItem = position;
			this.top = top;
			for (EPGFragmentListener listener : m_fragmentListeners) {
				listener.scrollTo(view, position, top);
			}
		} finally {
			lock = false;
		}
	}

	private void notifyEPGChannelListChanged() {
		for (EPGFragmentListener listener : m_fragmentListeners) {
			listener.populateChannelList();
		}
	}

	private void setLoading(boolean loading) {
		if (loading) {
			//
		} else {

			TVHGuideApplication app = (TVHGuideApplication) getApplication();
			tagAdapter.clear();
			for (ChannelTag t : app.getChannelTags()) {
				tagAdapter.add(t);
			}
			setCurrentTag(app.getCurrentTag());
		}
	}

	private void setCurrentTag(ChannelTag t) {
		if (t == null) {
			tagTextView.setText(R.string.pr_all_channels);
			tagImageView.setImageResource(R.drawable.logo_72);
		} else {
			tagTextView.setText(t.name);

			if (t.iconBitmap != null) {
				tagImageView.setImageBitmap(t.iconBitmap);
			} else {
				tagImageView.setImageResource(R.drawable.logo_72);
			}
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public class EPGListFragment extends ListFragment implements HTSListener,
			EPGFragmentListener {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_TIME_SLOT = "time_slot";

		private EPGTimeListAdapter prAdapter;

		private Date m_timeslot;

		private boolean mCreated;

		public EPGListFragment(Date timeslot) {
			m_timeslot = timeslot;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// String timeSlot = getArguments().getString(ARG_TIME_SLOT);
			prAdapter = new EPGTimeListAdapter(getActivity(),
					new ArrayList<Channel>(), m_timeslot);
			prAdapter.sort();
			setListAdapter(prAdapter);

			mCreated = true;
		}

		@Override
		public void onResume() {
			super.onResume();
			TVHGuideApplication app = (TVHGuideApplication) getActivity()
					.getApplication();
			app.addListener(this);

			// scroll to aquired position
			getListView().setSelectionFromTop(firstVibleItem, top);

			registerEPGFragmentListener(this);

			setLoading(app.isLoading());
		}

		@Override
		public void onPause() {
			TVHGuideApplication app = (TVHGuideApplication) getActivity()
					.getApplication();
			app.removeListener(this);

			unregisterEPGFragmentListener(this);

			super.onPause();
		}

		private void setLoading(boolean loading) {
			EPGTimeListActivity.this.setLoading(loading);
			if (loading) {
				//
			} else {
				populateChannelList();
			}
		}

		@Override
		public void populateChannelList() {
			TVHGuideApplication app = (TVHGuideApplication) getApplication();
			ChannelTag currentTag = app.getCurrentTag();
			prAdapter.clear();

			for (Channel ch : app.getChannels()) {
				if (currentTag == null || ch.hasTag(currentTag.id)) {
					prAdapter.add(ch);
				}
			}

			prAdapter.sort();
			prAdapter.notifyDataSetChanged();
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Programme p = (Programme) prAdapter.getProgrammeAt(position);

			Intent intent = new Intent(getActivity(), ProgrammeActivity.class);
			intent.putExtra("eventId", p.id);
			intent.putExtra("channelId", p.channel.id);
			startActivity(intent);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			registerForContextMenu(getListView());

			// scroll to acquired position
			getListView().setSelectionFromTop(firstVibleItem, top);

			getListView().setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					if (visibleItemCount == 0) {
						return;
					}
					View v = view.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();
					if (firstVisibleItem != EPGTimeListActivity.this.firstVibleItem
							|| top != EPGTimeListActivity.this.top) {

						notifyEPGScrollListener(view, firstVisibleItem, top);
					}
				}
			});
		}

		@Override
		public void scrollTo(AbsListView view, int position, int top) {
			if (getListView() != view) {
				getListView().setSelectionFromTop(position, top);
			}
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
			if (info == null) {
				return;
			}
			Channel channel = prAdapter.getItem(info.position);
			Programme p = prAdapter.getProgrammeAt(info.position);

			if (p == null) {
				return;
			}

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
			if (channel != null) {
				intent = new Intent(getBaseContext(),
						ProgrammeListActivity.class);
				intent.putExtra("channelId", channel.id);
				item = menu.add(ContextMenu.NONE,
						R.string.menu_show_program_list, ContextMenu.NONE,
						R.string.menu_show_program_list);
				item.setIntent(intent);
			}
		}

		@Override
		public void onMessage(String action, final Object obj) {
			if (action.equals(TVHGuideApplication.ACTION_LOADING)) {

				runOnUiThread(new Runnable() {

					public void run() {
						boolean loading = (Boolean) obj;
						setLoading(loading);
					}
				});
			} else if (action.equals(TVHGuideApplication.ACTION_CHANNEL_ADD)) {
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
						if (mCreated) {
							try {
								Channel channel = (Channel) obj;
								prAdapter.updateView(getListView(), channel);
							} catch (Exception e) {
							}
						}
					}
				});
			} else if (action.equals(TVHGuideApplication.ACTION_PROGRAMME_ADD)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Programme p = (Programme) obj;
						try {
							if (mCreated) {
								prAdapter.updateView(getListView(), p.channel);
							}
						} catch (Exception e) {
						}
					}
				});
			} else if (action
					.equals(TVHGuideApplication.ACTION_PROGRAMME_DELETE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Programme p = (Programme) obj;
						if (mCreated) {
							try {
								prAdapter.updateView(getListView(), p.channel);
							} catch (Exception e) {
							}
						}
					}
				});
			} else if (action
					.equals(TVHGuideApplication.ACTION_PROGRAMME_UPDATE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						if (mCreated) {
							try {
								Programme p = (Programme) obj;
								prAdapter.updateView(getListView(), p.channel);
							} catch (Exception e) {
							}
						}
					}
				});
			} else if (action.equals(TVHGuideApplication.ACTION_DVR_UPDATE)) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						Recording rec = (Recording) obj;
						for (Channel c : prAdapter.list) {
							for (Programme p : c.epg) {
								if (rec == p.recording) {
									if (mCreated) {
										try {
											prAdapter.updateView(getListView(),
													c);
											return;
										} catch (Exception e) {
										}
									}
								}
							}
						}
					}
				});
			}
		}
	}

	static interface EPGFragmentListener {
		public void scrollTo(AbsListView view, int position, int top);

		public void populateChannelList();
	}

	/**
	 * get next program based on channel and timeslot
	 * 
	 * @return
	 */
	public Programme getProgrammeStartingAfter(Channel channel, Date timeSlot) {
		Iterator<Programme> it = new ArrayList<Programme>(channel.epg)
				.iterator();

		Calendar cal = Calendar.getInstance();
		cal.setTime(timeSlot);
		cal.add(Calendar.MINUTE, m_maxStartTimeAfterTimeSlotInMinutes);
		Date maxStartTime = cal.getTime();

		// find first programm after timeslot
		Programme lastPr = null;
		while (it.hasNext()) {
			Programme pr = it.next();

			// first check if programm starts at given time
			if (pr.start.equals(timeSlot)) {
				return pr;
			}

			// secondly check if program starts within next 30 minutes
			// (configurable via settings)
			if (pr.start.after(timeSlot) && pr.start.before(maxStartTime)) {
				return pr;
			}

			// secondly check if last Programme is still running
			if (lastPr != null) {
				if (lastPr.stop.after(timeSlot)) {
					return lastPr;
				}
			}

			lastPr = pr;
		}
		return null;
	}

}
