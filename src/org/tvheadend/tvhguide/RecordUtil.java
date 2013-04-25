package org.tvheadend.tvhguide;

import org.tvheadend.tvhguide.model.Recording;

import android.widget.ImageView;

public class RecordUtil {
	public static void applyRecording(Recording rec, ImageView state) {
		if (rec == null) {
			state.setImageDrawable(null);
		} else if (rec.error != null) {
			state.setImageResource(R.drawable.ic_error_small);
		} else if ("completed".equals(rec.state)) {
			state.setImageResource(R.drawable.ic_success_small);
		} else if ("invalid".equals(rec.state)) {
			state.setImageResource(R.drawable.ic_error_small);
		} else if ("missed".equals(rec.state)) {
			state.setImageResource(R.drawable.ic_error_small);
		} else if ("recording".equals(rec.state)) {
			state.setImageResource(R.drawable.ic_rec_small);
		} else if ("scheduled".equals(rec.state)) {
			state.setImageResource(R.drawable.ic_schedule_small);
		} else {
			state.setImageDrawable(null);
		}
	}
}
