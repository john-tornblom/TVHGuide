package org.tvheadend.tvhguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tvheadend.tvhguide.htsp.HTSListener;
import org.tvheadend.tvhguide.htsp.HTSService;
import org.tvheadend.tvhguide.intent.SearchEPGIntent;
import org.tvheadend.tvhguide.intent.SearchIMDbIntent;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.ChannelTag;
import org.tvheadend.tvhguide.model.Programme;
import org.tvheadend.tvhguide.model.Recording;
import org.tvheadend.tvhguide.ui.HorizontalListView;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * 
 * @author mike toggweiler
 * 
 */
public class EPGTimelineActivity extends ListActivity implements HTSListener {

	private EPGTimelineAdapter adapter;

	private AlertDialog tagDialog;
	private ArrayAdapter<ChannelTag> tagAdapter;

	private List<OnEPGScrollListener> mListeners = Collections
			.synchronizedList(new ArrayList<EPGTimelineActivity.OnEPGScrollListener>());

	private int mLastEPGScrollPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new EPGTimelineAdapter(this, new ArrayList<Channel>());
		setListAdapter(adapter);

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
						populateChannelList();
					}
				});

		tagDialog = builder.create();
	}

	private void setCurrentTag(ChannelTag t) {
		if (getActionBar() != null) {

			if (t == null) {
				getActionBar().setTitle(R.string.pr_all_channels);
				getActionBar().setIcon(R.drawable.logo_72);
			} else {
				getActionBar().setTitle(t.name);
				getActionBar().setIcon(R.drawable.logo_72);

				if (t.iconBitmap != null) {
					getActionBar().setIcon(
							new BitmapDrawable(getResources(), t.iconBitmap));
				} else {
					getActionBar().setIcon(R.drawable.logo_72);
				}
			}
		}
	}

	public void populateChannelList() {
		TVHGuideApplication app = (TVHGuideApplication) getApplication();
		ChannelTag currentTag = app.getCurrentTag();
		adapter.clear();
		// mListeners.clear();

		adapter.add(new DummyChannel());
		for (Channel ch : app.getChannels()) {
			if (currentTag == null || ch.hasTag(currentTag.id)) {
				adapter.add(ch);
			}
		}

		adapter.sort();
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		if (info == null) {
			return;
		}

		HorizontalListView hv = (HorizontalListView) v;
		Programme p = (Programme) hv.getItemAtPosition(info.position);

		if (p == null) {
			return;
		}

		menu.setHeaderTitle(p.title);

		Intent intent = new Intent(this, HTSService.class);

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
				item = menu.add(ContextMenu.NONE, R.string.menu_record_cancel,
						ContextMenu.NONE, R.string.menu_record_cancel);
			} else {
				intent.setAction(HTSService.ACTION_DVR_DELETE);
				intent.putExtra("id", p.recording.id);
				item = menu.add(ContextMenu.NONE, R.string.menu_record_remove,
						ContextMenu.NONE, R.string.menu_record_remove);
			}

			item.setIntent(intent);

			item = menu.add(ContextMenu.NONE, R.string.search_hint,
					ContextMenu.NONE, R.string.search_hint);
			item.setIntent(new SearchEPGIntent(this, p.title));

			item = menu.add(ContextMenu.NONE, ContextMenu.NONE,
					ContextMenu.NONE, "IMDb");
			item.setIntent(new SearchIMDbIntent(this, p.title));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.string.menu_record:
		case R.string.menu_record_cancel:
		case R.string.menu_record_remove: {
			startService(item.getIntent());
			return true;
		}
		default: {
			return super.onContextItemSelected(item);
		}
		}
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
		case R.id.mi_epg_prime: {
			Intent intent = new Intent(getBaseContext(),
					EPGPrimeTimeListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.mi_epg_list: {
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

	@Override
	protected void onResume() {
		super.onResume();
		TVHGuideApplication app = (TVHGuideApplication) getApplication();
		app.addListener(this);

		setLoading(app.isLoading());
	}

	@Override
	protected void onPause() {
		super.onPause();
		TVHGuideApplication app = (TVHGuideApplication) getApplication();
		app.removeListener(this);
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
			runOnUiThread(new Runnable() {

				public void run() {
					adapter.add((Channel) obj);
					adapter.notifyDataSetChanged();
					adapter.sort();
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_CHANNEL_DELETE)) {
			runOnUiThread(new Runnable() {

				public void run() {
					adapter.remove((Channel) obj);
					adapter.notifyDataSetChanged();
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_CHANNEL_UPDATE)) {
			runOnUiThread(new Runnable() {

				public void run() {
					Channel channel = (Channel) obj;
					adapter.updateView(getListView(), channel);
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_TAG_ADD)) {
			runOnUiThread(new Runnable() {

				public void run() {
					ChannelTag tag = (ChannelTag) obj;
					tagAdapter.add(tag);
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_TAG_DELETE)) {
			runOnUiThread(new Runnable() {

				public void run() {
					ChannelTag tag = (ChannelTag) obj;
					tagAdapter.remove(tag);
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_PROGRAMME_ADD)) {
			runOnUiThread(new Runnable() {

				public void run() {
					Programme p = (Programme) obj;
					try {
						adapter.updateView(getListView(), p.channel);
					} catch (Exception e) {
					}
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_PROGRAMME_DELETE)) {
			runOnUiThread(new Runnable() {

				public void run() {
					Programme p = (Programme) obj;
					adapter.updateView(getListView(), p.channel);
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_PROGRAMME_UPDATE)) {
			runOnUiThread(new Runnable() {

				public void run() {
					Programme p = (Programme) obj;
					adapter.updateView(getListView(), p.channel);
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_DVR_UPDATE)) {
			runOnUiThread(new Runnable() {

				public void run() {
					Recording rec = (Recording) obj;
					for (Channel c : adapter.list) {
						for (Programme p : c.epg) {
							if (rec == p.recording) {
								adapter.updateView(getListView(), c);
								return;
							}
						}
					}
				}
			});
		} else if (action.equals(TVHGuideApplication.ACTION_TAG_UPDATE)) {
			// NOP
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

			populateChannelList();
		}
	}

	public void notifyOnScoll(int scrollTo) {
		mLastEPGScrollPosition = scrollTo;
		for (OnEPGScrollListener listener : mListeners) {
			listener.scrollTo(scrollTo);
		}
	}

	public int getLastEPGScrollPosition() {
		return mLastEPGScrollPosition;
	}

	public void notifyOnFling(float velocityX) {
		for (OnEPGScrollListener listener : mListeners) {
			listener.flingBy(velocityX);
		}
	}

	public void registerOnScrollListener(OnEPGScrollListener listener) {
		mListeners.add(listener);
	}

	public void unregisterOnScrollListener(OnEPGScrollListener listener) {
		mListeners.remove(listener);
	}

	public static interface OnEPGScrollListener {

		public void scrollTo(int scrollTo);

		public void flingBy(float velocityX);
	}

	public static class DummyChannel extends Channel {
		public DummyChannel() {
			id = 0;
			number = -1;
		}
	}
}
