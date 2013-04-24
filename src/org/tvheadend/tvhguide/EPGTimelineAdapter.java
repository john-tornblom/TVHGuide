package org.tvheadend.tvhguide;

import java.util.Comparator;
import java.util.List;

import org.tvheadend.tvhguide.model.Channel;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

class EPGTimelineAdapter extends ArrayAdapter<Channel> {

	private final EPGTimelineActivity context;
	final List<Channel> list;

	EPGTimelineAdapter(EPGTimelineActivity context, List<Channel> list) {
		super(context, R.layout.epgtimeline_widget, list);
		this.context = context;
		this.list = list;
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
			row = inflater.inflate(R.layout.epgtimeline_widget, null, false);
			row.requestLayout();
			wrapper = new EPGTimelineViewWrapper(context, row);
			context.registerOnScrollListener(wrapper);
			row.setTag(wrapper);

		} else {
			wrapper = (EPGTimelineViewWrapper) row.getTag();
		}

		wrapper.repaint(ch);
		return row;
	}

	@Override
	public void clear() {

		super.clear();
	}
}