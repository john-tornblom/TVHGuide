/*
 *  Copyright (C) 2013 Robert Siebert
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
package org.tvheadend.tvhguide;

import java.util.ArrayList;
import java.util.List;

import org.tvheadend.tvhguide.adapter.ConnectionListAdapter;
import org.tvheadend.tvhguide.model.Connection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SettingsManageConnectionsActivity extends ActionBarActivity {

    private ActionBar actionBar = null;
    private ConnectionListAdapter connAdapter;
    private List<Connection> connList;
    private ListView connListView;
    protected int prevPosition;
    
    @Override
    public void onCreate(Bundle icicle) {
        
        // Apply the specified theme
        setTheme(Utils.getThemeId(this));
        
        super.onCreate(icicle);
        setContentView(R.layout.list_layout);
        
        // Setup the action bar and show the title
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.pref_manage_connections);
        
        connList = new ArrayList<Connection>();
        connAdapter = new ConnectionListAdapter(this, connList);
        connListView = (ListView) findViewById(R.id.item_list);
        connListView.setAdapter(connAdapter);
        registerForContextMenu(connListView);
        
        connListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setConnectionActive(connAdapter.getItem(position));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        showConnections();
    }

    private void showConnections() {
        connList.clear();
        
        List<Connection> cl = DatabaseHelper.getInstance().getConnections();
        if (cl != null && cl.size() > 0) {
            for (int i = 0; i < cl.size(); ++i) {
                connList.add(cl.get(i));
            }
        }
        
        connAdapter.sort();
        connAdapter.notifyDataSetChanged();
        actionBar.setSubtitle(connAdapter.getCount() + " " + getString(R.string.pref_connections));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        
        // Get the currently selected program from the list
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Connection c = connAdapter.getItem(info.position);
        
        switch (item.getItemId()) {
        case R.id.menu_set_active:
            
            setConnectionActive(c);
            return true;

        case R.id.menu_set_not_active:
            c.selected = false;
            DatabaseHelper.getInstance().updateConnection(c);
            showConnections();
            return true;

        case R.id.menu_edit:
            Intent intent = new Intent(this, SettingsAddConnectionActivity.class);
            intent.putExtra("id", c.id);
            startActivity(intent);
            return true;

        case R.id.menu_delete:
            
            // Show confirmation dialog to cancel 
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.delete_connection, c.name));
            builder.setTitle(getString(R.string.menu_delete));

            // Define the action of the yes button
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (DatabaseHelper.getInstance().removeConnection(c.id)) {
                        connAdapter.remove(c);
                        connAdapter.notifyDataSetChanged();
                        connAdapter.sort();
                        actionBar.setSubtitle(connAdapter.getCount() + " " + getString(R.string.pref_connections));
                    }
                }
            });
            // Define the action of the no button
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;

        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.connection_menu, menu);

        // Get the currently selected program from the list
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Connection connection = connAdapter.getItem(info.position);

        // Show or hide the activate / deactivate menu item
        if (connection.selected) {
            menu.getItem(0).setVisible(false);
        } else {
            menu.getItem(1).setVisible(false);
        }

        // Set the title of the context menu and show or hide
        // the menu items depending on the connection
        menu.setHeaderTitle(getString(R.string.connection_options, connection.name));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.preference_connections, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;

        case R.id.menu_add:
            Intent intent = new Intent(this, SettingsAddConnectionActivity.class);
            startActivity(intent);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("reconnect", true);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void setConnectionActive(Connection c) {
        // Switch the selection status
        c.selected = (c.selected) ? false : true;
        
        // Set the previous connection to unselected 
        // if the selected one is set as selected
        if (c.selected) {
            Connection previousConn = DatabaseHelper.getInstance().getSelectedConnection();
            if (previousConn != null) {
                previousConn.selected = false;
                DatabaseHelper.getInstance().updateConnection(previousConn);
            }
        };
        
        // Update the currently selected connection and refresh the display
        DatabaseHelper.getInstance().updateConnection(c);
        showConnections();
    }
}
