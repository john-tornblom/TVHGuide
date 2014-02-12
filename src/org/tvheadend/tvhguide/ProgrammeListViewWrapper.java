package org.tvheadend.tvhguide;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.tvheadend.tvhguide.R.string;
import org.tvheadend.tvhguide.model.Programme;
import org.tvheadend.tvhguide.model.SeriesInfo;

import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class ProgrammeListViewWrapper {

	TextView title;
	TextView time;
	TextView seriesInfo;
	TextView date;
	TextView description;
	ImageView state;
	private final View base;
	private SparseArray<String> contentTypes;

	public ProgrammeListViewWrapper(View base) {
		this.base = base;
		title = (TextView) base.findViewById(R.id.pr_title);
		description = (TextView) base.findViewById(R.id.pr_desc);
		seriesInfo = (TextView) base.findViewById(R.id.pr_series_info);

		time = (TextView) base.findViewById(R.id.pr_time);
		date = (TextView) base.findViewById(R.id.pr_date);

		state = (ImageView) base.findViewById(R.id.pr_state);

		contentTypes = TVHGuideApplication.getContentTypes(base.getResources());

	}

	public void repaint(Programme p) {
		title.setText(p.title);

		RecordUtil.applyRecording(p.recording, state);

		title.invalidate();

		if (seriesInfo != null) {
			String s = buildSeriesInfoString(base, p.seriesInfo);
			if (s.length() == 0) {
				s = contentTypes.get(p.contentType);
			}

			seriesInfo.setText(s);
			seriesInfo.invalidate();
		}

		if (description != null) {
			if (p.description.length() > 0) {
				description.setText(p.description);
				description.setVisibility(TextView.VISIBLE);
			} else {
				description.setText("");
				description.setVisibility(TextView.GONE);
			}
			description.invalidate();
		}

		if (date != null) {
			if (DateUtils.isToday(p.start.getTime())) {
				date.setText(base.getResources().getString(R.string.today));
			} else if (p.start.getTime() < System.currentTimeMillis() + 1000
					* 60 * 60 * 24 * 2
					&& p.start.getTime() > System.currentTimeMillis() - 1000
							* 60 * 60 * 24 * 2) {
				date.setText(DateUtils.getRelativeTimeSpanString(
						p.start.getTime(), System.currentTimeMillis(),
						DateUtils.DAY_IN_MILLIS));
			} else if (p.start.getTime() < System.currentTimeMillis() + 1000
					* 60 * 60 * 24 * 6
					&& p.start.getTime() > System.currentTimeMillis() - 1000
							* 60 * 60 * 24 * 2) {
				date.setText(new SimpleDateFormat("EEEE", Locale.getDefault())
						.format(p.start.getTime()));
			} else {
				date.setText(DateFormat.getDateFormat(date.getContext())
						.format(p.start));
			}

			date.invalidate();
		}

		if (time != null) {
			time.setText(DateFormat.getTimeFormat(time.getContext()).format(
					p.start)
					+ " - "
					+ DateFormat.getTimeFormat(time.getContext())
							.format(p.stop));
			time.invalidate();
		}
	}

	public String buildSeriesInfoString(View context, SeriesInfo info) {
		if (info.onScreen != null && info.onScreen.length() > 0)
			return info.onScreen;

		String s = "";
		String season = context.getResources().getString(string.pr_season);
		String episode = context.getResources().getString(string.pr_episode);
		String part = context.getResources().getString(string.pr_part);

		if (info.onScreen.length() > 0) {
			return info.onScreen;
		}

		if (info.seasonNumber > 0) {
			if (s.length() > 0)
				s += ", ";
			s += String.format("%s %02d",
					season.toLowerCase(Locale.getDefault()), info.seasonNumber);
		}
		if (info.episodeNumber > 0) {
			if (s.length() > 0)
				s += ", ";
			s += String.format("%s %02d",
					episode.toLowerCase(Locale.getDefault()),
					info.episodeNumber);
		}
		if (info.partNumber > 0) {
			if (s.length() > 0)
				s += ", ";
			s += String.format("%s %d", part.toLowerCase(Locale.getDefault()),
					info.partNumber);
		}

		if (s.length() > 0) {
			s = s.substring(0, 1).toUpperCase(Locale.getDefault())
					+ s.substring(1);
		}

		return s;
	}

}