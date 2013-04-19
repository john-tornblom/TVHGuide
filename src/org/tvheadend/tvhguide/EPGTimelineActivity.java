package org.tvheadend.tvhguide;

import java.util.ArrayList;

import org.tvheadend.tvhguide.htsp.HTSListener;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.ChannelTag;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author mike toggweiler
 * 
 */
public class EPGTimelineActivity extends ListActivity implements HTSListener {

	private EPGTimelineAdapter adapter;

	private AlertDialog tagDialog;
	private ArrayAdapter<ChannelTag> tagAdapter;

	private TextView tagTextView;
	private ImageView tagImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean theme = prefs.getBoolean("lightThemePref", false);
		setTheme(theme ? R.style.CustomTheme_Light : R.style.CustomTheme);

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		TVHGuideApplication tvh = (TVHGuideApplication) getApplication();
		adapter = new EPGTimelineAdapter(this, new ArrayList<Channel>());
		setListAdapter(adapter);

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
						populateChannelList();
					}
				});

		tagDialog = builder.create();
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

	public void populateChannelList() {
		TVHGuideApplication app = (TVHGuideApplication) getApplication();
		ChannelTag currentTag = app.getCurrentTag();
		adapter.clear();

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
}
