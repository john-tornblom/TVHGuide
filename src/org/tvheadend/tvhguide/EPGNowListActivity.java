package org.tvheadend.tvhguide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.tvheadend.tvhguide.model.Programme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class EPGNowListActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean theme = prefs.getBoolean("lightThemePref", false);
		setTheme(theme ? R.style.CustomTheme_Light : R.style.CustomTheme);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.epgnow_list_widget);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_tab_now)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_tab_next)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_tab_2000)
				.setTabListener(this));

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
		getMenuInflater().inflate(R.menu.epgnow_list_widget, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		EPGListFragment fragment = new EPGListFragment();
		Bundle args = new Bundle();
		args.putInt(EPGListFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
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
		public static final String ARG_SECTION_NUMBER = "section_number";

		private ProgrammeListAdapter prAdapter;

		public EPGListFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			List<Programme> prList = new ArrayList<Programme>();
			prAdapter = new ProgrammeListAdapter(getActivity(), prList);
			prAdapter.sort();
			setListAdapter(prAdapter);
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

	static class ProgrammeListAdapter extends ArrayAdapter<Programme> {

		Activity context;
		List<Programme> list;

		ProgrammeListAdapter(Activity context, List<Programme> list) {
			super(context, R.layout.epgnow_list_widget, list);
			this.context = context;
			this.list = list;
		}

		public void sort() {
			sort(new Comparator<Programme>() {

				public int compare(Programme x, Programme y) {
					return x.compareTo(y);
				}
			});
		}

		public void updateView(ListView listView, Programme programme) {
			for (int i = 0; i < listView.getChildCount(); i++) {
				View view = listView.getChildAt(i);
				int pos = listView.getPositionForView(view);
				Programme pr = (Programme) listView.getItemAtPosition(pos);

				if (view.getTag() == null || pr == null) {
					continue;
				}

				if (programme.id != pr.id) {
					continue;
				}

				// ViewWarpper wrapper = (ViewWarpper) view.getTag();
				// wrapper.repaint(programme);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			// ViewWarpper wrapper = null;

			if (row == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater
						.inflate(R.layout.epgnow_list_widget, null, false);

				// wrapper = new ViewWarpper(row);
				// row.setTag(wrapper);

			} else {
				// wrapper = (ViewWarpper) row.getTag();
			}

			Programme p = getItem(position);
			// wrapper.repaint(p);
			return row;
		}
	}
}
