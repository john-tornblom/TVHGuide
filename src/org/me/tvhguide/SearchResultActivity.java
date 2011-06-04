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
package org.me.tvhguide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.me.tvhguide.htsp.HTSListener;
import org.me.tvhguide.htsp.HTSService;
import org.me.tvhguide.model.Programme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 *
 * @author polini
 */
public class SearchResultActivity extends ListActivity implements HTSListener {

	private ProgrammeListAdapter prAdapter;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		List<Programme> prList = new ArrayList<Programme>();
		prAdapter = new ProgrammeListAdapter(this, prList);
		prAdapter.sort();
		setListAdapter(prAdapter);
		registerForContextMenu(getListView());

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.menu_search);
		alert.setMessage(R.string.search_input);

		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(R.string.menu_search, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				Intent intent = new Intent(SearchResultActivity.this, HTSService.class);
				intent.setAction(HTSService.ACTION_EPG_QUERY);
				intent.putExtra("query", value);
				startService(intent);
			}
		});

		alert.setNegativeButton(R.string.menu_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent(getBaseContext(), ChannelListActivity.class);
				startActivity(intent);
			}
		});

		alert.show();        
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuItem item = menu.add(ContextMenu.NONE, R.string.menu_record, ContextMenu.NONE, R.string.menu_record);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Programme pr = prAdapter.getItem(info.position);

		menu.setHeaderTitle(pr.title);
		Intent intent = new Intent(SearchResultActivity.this, HTSService.class);
		intent.setAction(HTSService.ACTION_DVR_ADD);
		intent.putExtra("eventId", pr.id);
		item.setIntent(intent);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.string.menu_record: {
			startService(item.getIntent());
			return true;
		}
		default: {
			return false;
		}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		TVHGuideApplication app = (TVHGuideApplication) getApplication();
		app.addListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		TVHGuideApplication app = (TVHGuideApplication) getApplication();
		app.removeListener(this);
	}

	public void onMessage(String action, final Object obj) {
		if (action.equals(TVHGuideApplication.ACTION_RESULT_FOUND)) {
			runOnUiThread(new Runnable() {

				public void run() {
					prAdapter.add((Programme)obj);
					prAdapter.sort();
					prAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	private class ViewWrapper {

		private TextView title;
		private TextView time;
		private TextView channel;
		public final long progId;

		public ViewWrapper(View base, long progId) {
			title = (TextView) base.findViewById(R.id.pr_title);
			time = (TextView) base.findViewById(R.id.pr_time);
			channel = (TextView) base.findViewById(R.id.pr_channel);

			this.progId = progId;
		}

		public void repaint(Programme pr) {
			title.setText("");
			time.setText("");
			channel.setText("");

			title.setText(pr.title);
			title.invalidate();

			channel.setText(pr.channel.name);
			channel.invalidate();

			time.setText(
					DateFormat.getDateFormat(time.getContext()).format(pr.start)
					+ ": "
					+ DateFormat.getTimeFormat(time.getContext()).format(pr.start)
					+ " - "
					+ DateFormat.getTimeFormat(time.getContext()).format(pr.stop));

			time.invalidate();
		}
	}

	class ProgrammeListAdapter extends ArrayAdapter<Programme> {

		ProgrammeListAdapter(Activity context, List<Programme> list) {
			super(context, R.layout.sr_widget, list);
		}

		public void sort() {
			sort(new Comparator<Programme>() {

				public int compare(Programme x, Programme y) {
					return x.start.compareTo(y.start);
				}
			});
		}

		public void updateView(ListView listView, Programme pr) {
			for (int i = 0; i < listView.getChildCount(); i++) {
				View view = listView.getChildAt(i);

				if (view.getTag() == null) {
					continue;
				}
				ViewWrapper wrapper = (ViewWrapper) view.getTag();
				if (wrapper.progId == pr.id) {
					wrapper.repaint(pr);
					break;
				}
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewWrapper wrapper = null;

			Programme pr = getItem(position);
			Activity activity = (Activity) getContext();

			if (row == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				row = inflater.inflate(R.layout.sr_widget, null, false);
				row.requestLayout();
				wrapper = new ViewWrapper(row, pr.id);
				row.setTag(wrapper);

			} else {
				wrapper = (ViewWrapper) row.getTag();
			}

			wrapper.repaint(pr);
			return row;
		}
	}
}
