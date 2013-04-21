package org.tvheadend.tvhguide;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.Programme;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

class EPGTimeListAdapter extends ArrayAdapter<Channel> {

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
		return ((EPGTimeListActivity) getContext()).getProgrammeStartingAfter(
				item, timeSlot);
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
			row = inflater.inflate(R.layout.epgnow_list_widget, null, false);

			wrapper = new EPGTimeListViewWrapper(context, row, timeSlot);
			row.setTag(wrapper);

		} else {
			wrapper = (EPGTimeListViewWrapper) row.getTag();
		}

		Channel channel = getItem(position);
		wrapper.repaint(channel);
		return row;
	}

}