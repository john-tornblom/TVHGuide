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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.me.tvhguide.htsp.HTSService;
import org.me.tvhguide.model.Recording;

/**
 *
 * @author john-tornblom
 */
public class RecordingActivity extends Activity {

	Recording rec;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TVHGuideApplication app = (TVHGuideApplication) getApplication();
		rec = app.getRecording(getIntent().getLongExtra("id", 0));
		if (rec == null) {
			return;
		}

		setContentView(R.layout.rec_layout);

		TextView text = (TextView) findViewById(R.id.rec_name);
		text.setText(rec.title);

		text = (TextView) findViewById(R.id.rec_desc);
		text.setText(rec.description);

		text = (TextView) findViewById(R.id.rec_time);
		text.setText(
				DateFormat.getLongDateFormat(this).format(rec.start)
				+ "   "
				+ DateFormat.getTimeFormat(this).format(rec.start)
				+ " - "
				+ DateFormat.getTimeFormat(this).format(rec.stop));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rc_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_remove: {
                Intent intent = new Intent(getBaseContext(), HTSService.class);
                intent.setAction(HTSService.ACTION_DVR_DELETE);
                intent.putExtra("id", rec.id);
                startService(intent);

                finish();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
