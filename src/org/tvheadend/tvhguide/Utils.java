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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.tvheadend.tvhguide.R.string;
import org.tvheadend.tvhguide.htsp.HTSService;
import org.tvheadend.tvhguide.model.Channel;
import org.tvheadend.tvhguide.model.Connection;
import org.tvheadend.tvhguide.model.Program;
import org.tvheadend.tvhguide.model.Recording;
import org.tvheadend.tvhguide.model.SeriesInfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Utils {
    
    // The currently selected tag. This allows showing the same amount of
    // channels (with this tag only) in the channel list and program guide
    // screens.
    private static int channelTagId = 0;

    // Constants required for the date calculation
    private static final int twoDays = 1000 * 3600 * 24 * 2;
    private static final int sixDays = 1000 * 3600 * 24 * 6;
    
    // This is the width in pixels from the icon in the program_guide_list.xml
    // We need to subtract this value from the window width to get the real
    // usable width. The same values is also used in the
    // ProgramGuideListFragment class.
    private final static int LAYOUT_ICON_OFFSET = 66;

    /**
     * 
     * @param context
     * @return
     */
    public static int getThemeId(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean theme = prefs.getBoolean("lightThemePref", true);
        return (theme ? R.style.CustomTheme_Light : R.style.CustomTheme);
    }

    /**
     * 
     * @param context
     * @return
     */
    public static boolean showChannelIcons(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean showIcons = prefs.getBoolean("showIconPref", true);
        return showIcons;
    }
    
    /**
     * 
     * @param info
     * @return
     */
    public static String buildSeriesInfoString(final Context context, final SeriesInfo info) {
        
        if (info.onScreen != null && info.onScreen.length() > 0)
            return info.onScreen;

        String s = "";
        final String season = context.getResources().getString(string.season);
        final String episode = context.getResources().getString(string.episode);
        final String part = context.getResources().getString(string.part);
        
        if (info.onScreen.length() > 0) {
            return info.onScreen;
        }
        
        if (info.seasonNumber > 0) {
            if (s.length() > 0)
                s += ", ";
            s += String.format("%s %02d", season.toLowerCase(Locale.getDefault()), info.seasonNumber);
        }
        if (info.episodeNumber > 0) {
            if (s.length() > 0)
                s += ", ";
            s += String.format("%s %02d", episode.toLowerCase(Locale.getDefault()), info.episodeNumber);
        }
        if (info.partNumber > 0) {
            if (s.length() > 0)
                s += ", ";
            s += String.format("%s %d", part.toLowerCase(Locale.getDefault()), info.partNumber);
        }

        if (s.length() > 0) {
            s = s.substring(0,1).toUpperCase(Locale.getDefault()) + s.substring(1);
        }
        
        return s;
    }
    
    /**
     * TODO: return different error codes
     * 
     * @param context
     * @param force
     */
    public static void connect(final Context context, final boolean force) {
        Intent intent = null;
        
        // Get the currently selected connection
        Connection conn = DatabaseHelper.getInstance().getSelectedConnection();
        // If we got one connection, get the values
        if (conn != null) {
            // Create an intent and pass on the connection details
            intent = new Intent(context, HTSService.class);
            intent.setAction(HTSService.ACTION_CONNECT);
            intent.putExtra("hostname", conn.address);
            intent.putExtra("port", conn.port);
            intent.putExtra("username", conn.username);
            intent.putExtra("password", conn.password);
            intent.putExtra("force", force);
        }

        // Start the service with given action and data
        if (intent != null) {
            context.startService(intent);
        }
    }

    /**
     * 
     * @param context
     * @param id
     */
    public static void removeProgram(final Context context, final Recording rec) {

        final Intent intent = new Intent(context, HTSService.class);
        intent.setAction(HTSService.ACTION_DVR_DELETE);
        intent.putExtra("id", rec.id);
        
        // Show a confirmation dialog before deleting the recording
        new AlertDialog.Builder(context)
        .setTitle(R.string.menu_record_remove)
        .setMessage(context.getString(R.string.delete_recording, rec.title))
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                context.startService(intent);
            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // NOP
            }
        }).show();
    }

    /**
     * 
     * @param context
     * @param id
     */
    public static void cancelProgram(final Context context, final long id) {
        Intent intent = new Intent(context, HTSService.class);
        intent.setAction(HTSService.ACTION_DVR_CANCEL);
        intent.putExtra("id", id);
        context.startService(intent);
    }

    /**
     * 
     * @param context
     * @param id
     * @param channelId
     */
    public static void recordProgram(final Context context, final long id, final long channelId) {
        Intent intent = new Intent(context, HTSService.class);
        intent.setAction(HTSService.ACTION_DVR_ADD);
        intent.putExtra("eventId", id);
        intent.putExtra("channelId", channelId);
        context.startService(intent);
    }

    /**
     * 
     * @param menu
     * @param program
     */
    public static void setProgramMenu(final Menu menu, final Program program) {
        
        MenuItem recordMenuItem = menu.findItem(R.id.menu_record);
        MenuItem recordCancelMenuItem = menu.findItem(R.id.menu_record_cancel);
        MenuItem recordRemoveMenuItem = menu.findItem(R.id.menu_record_remove);
        MenuItem playMenuItem = menu.findItem(R.id.menu_play);
        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        
        // Disable these menus as a default
        searchMenuItem.setVisible(false);
        
        if (program.recording == null) {
            // Show the record menu
            playMenuItem.setVisible(false);
            recordCancelMenuItem.setVisible(false);
            recordRemoveMenuItem.setVisible(false);
        }
        else if (program.isRecording()) {
            // Show the cancel menu
            recordMenuItem.setVisible(false);
            recordRemoveMenuItem.setVisible(false);
        }
        else if (program.isScheduled()) {
            // Show the cancel and play menu
            playMenuItem.setVisible(false);
            recordMenuItem.setVisible(false);
            recordRemoveMenuItem.setVisible(false);
        }
        else {
            // Show the delete menu
            recordMenuItem.setVisible(false);
            recordCancelMenuItem.setVisible(false);
        }
    }

    /**
     * 
     * @param menu
     * @param rec
     */
    public static void setRecordingMenu(final Menu menu, final Recording rec) {

        // Get the menu items so they can be shown 
        // or hidden depending on the recording state
        MenuItem recordMenuItem = menu.findItem(R.id.menu_record);
        MenuItem recordCancelMenuItem = menu.findItem(R.id.menu_record_cancel);
        MenuItem recordRemoveMenuItem = menu.findItem(R.id.menu_record_remove);
        MenuItem playMenuItem = menu.findItem(R.id.menu_play);
        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);

        // Disable these menus as a default
        searchMenuItem.setVisible(false);
        
        if (rec.isRecording() || rec.isScheduled()) {
            // Show the cancel menu
            recordMenuItem.setVisible(false);
            recordRemoveMenuItem.setVisible(false);
            playMenuItem.setVisible(false);
        }
        else {
            // Show the delete and play menu
            recordMenuItem.setVisible(false);
            recordCancelMenuItem.setVisible(false);
        }
    }

    /**
     * 
     * @param state
     * @param recording
     */
    public static void setState(ImageView state, final Recording recording) {

        if (state == null)
            return;

        // If no recording was given hide the state icon
        if (recording == null) {
            state.setImageDrawable(null);
            state.setVisibility(ImageView.GONE);
        }
        else {
            // Show the state icon and set the correct image
            state.setVisibility(ImageView.VISIBLE);

            if (recording.error != null) {
                state.setImageResource(R.drawable.ic_error_small);
            }
            else if ("completed".equals(recording.state)) {
                state.setImageResource(R.drawable.ic_success_small);
            }
            else if ("invalid".equals(recording.state)) {
                state.setImageResource(R.drawable.ic_error_small);
            }
            else if ("missed".equals(recording.state)) {
                state.setImageResource(R.drawable.ic_error_small);
            }
            else if ("recording".equals(recording.state)) {
                state.setImageResource(R.drawable.ic_rec_small);
            }
            else if ("scheduled".equals(recording.state)) {
                state.setImageResource(R.drawable.ic_schedule_small);
            }
            else {
                state.setImageDrawable(null);
                state.setVisibility(ImageView.GONE);
            }
        }
    }

    /**
     * 
     * @param duration
     * @param start
     * @param stop
     */
    public static void setDuration(TextView duration, final Date start, final Date stop) {
        
        if (duration != null) {
            // Get the start and end times so we can show them
            // and calculate the duration. Then show the duration in minutes
            final double durationTime = ((stop.getTime() - start.getTime()) / 1000 / 60);
            final String s = duration.getContext().getString(R.string.minutes, (int) durationTime);

            duration.setText(duration.getContext().getString(R.string.minutes, (int) durationTime));
            duration.setVisibility((s.length() > 0) ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 
     * @param time
     * @param start
     * @param stop
     */
    public static void setTime(TextView time, final Date start, final Date stop) {
        
        if (time != null) {
            final String startTime = DateFormat.getTimeFormat(time.getContext()).format(start);
            final String endTime = DateFormat.getTimeFormat(time.getContext()).format(stop); 
            time.setText(startTime + " - " + endTime);
        }
    }

    /**
     * 
     * @param date
     * @param start
     */
    public static void setDate(TextView date, final Date start) {

        if (date == null)
            return;

        String dateText = "";
        if (DateUtils.isToday(start.getTime())) {
            // Show the string today
            dateText = date.getContext().getString(R.string.today);
        }
        else if (start.getTime() < System.currentTimeMillis() + twoDays
                && start.getTime() > System.currentTimeMillis() - twoDays) {
            // Show a string like "42 minutes ago"
            dateText = DateUtils.getRelativeTimeSpanString(start.getTime(), System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS).toString();
        }
        else if (start.getTime() < System.currentTimeMillis() + sixDays
                && start.getTime() > System.currentTimeMillis() - twoDays) {
            // Show the day of the week, like Monday or Tuesday
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.US);
            dateText = sdf.format(start.getTime());
        }
        else {
            // Show the regular date format like 31.07.2013
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
            dateText = sdf.format(start.getTime());
        }

        // Translate the day strings
        if (dateText.equals("today")) {
            date.setText(R.string.today);
        }
        else if (dateText.equals("tomorrow")) {
            date.setText(R.string.tomorrow);
        }
        else if (dateText.equals("in 2 days")) {
            date.setText(R.string.in_2_days);
        }
        else if (dateText.equals("Monday")) {
            date.setText(R.string.monday);
        }
        else if (dateText.equals("Tuesday")) {
            date.setText(R.string.tuesday);
        }
        else if (dateText.equals("Wednesday")) {
            date.setText(R.string.wednesday);
        }
        else if (dateText.equals("Thursday")) {
            date.setText(R.string.thursday);
        }
        else if (dateText.equals("Friday")) {
            date.setText(R.string.friday);
        }
        else if (dateText.equals("Saturday")) {
            date.setText(R.string.saturday);
        }
        else if (dateText.equals("Sunday")) {
            date.setText(R.string.sunday);
        }
        else {
            date.setText(dateText);
        }
    }

    /**
     * 
     * @param seriesInfoLabel
     * @param seriesInfo
     * @param si
     */
    public static void setSeriesInfo(TextView seriesInfoLabel, TextView seriesInfo, final SeriesInfo si) {

        if (seriesInfo != null) {
            final String s = Utils.buildSeriesInfoString(seriesInfo.getContext(), si);
            
            seriesInfo.setText(s);
            seriesInfo.setVisibility((s.length() > 0) ? View.VISIBLE : View.GONE);
            
            if (seriesInfoLabel != null) {
                seriesInfoLabel.setVisibility((s.length() > 0) ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * 
     * @param contentTypeLabel
     * @param contentType
     * @param ct
     */
    public static void setContentType(TextView contentTypeLabel, TextView contentType, final int ct) {
        
        if (contentType != null) {
            
            final SparseArray<String> ctl = TVHGuideApplication.getContentTypes(contentType.getContext());
            final String type = ctl.get(ct, "");
            
            contentType.setText(type);
            contentType.setVisibility((type.length() > 0) ? View.VISIBLE : View.GONE);
            
            if (contentTypeLabel != null) {
                contentTypeLabel.setVisibility((type.length() > 0) ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * 
     * @param icon
     * @param channel
     * @param ch
     */
    public static void setChannelIcon(ImageView icon, final TextView channel, final Channel ch) {

        if (icon != null) {
            // Get the setting if the channel icon shall be shown or not
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(icon.getContext());
            final boolean showIcons = prefs.getBoolean("showIconPref", false);
    
            icon.setImageBitmap((ch != null) ? ch.iconBitmap : null);
            icon.setVisibility(showIcons ? ImageView.VISIBLE : ImageView.GONE);
        }

        if (channel != null) {
            channel.setText((ch != null) ? ch.name : "");
        }
    }

    /**
     * 
     * @param descriptionLabel
     * @param description
     * @param desc
     */
    public static void setDescription(TextView descriptionLabel, TextView description, final String desc) {
        
        if (description != null) {
            description.setText(desc);
            description.setVisibility((desc.length() > 0) ? View.VISIBLE : View.GONE);
            
            if (descriptionLabel != null) {
                descriptionLabel.setVisibility((desc.length() > 0) ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * This method is only used when the startActivityForResult is called. It
     * returns a smaller integer instead of the passed one to avoid a
     * segmentation fault. This only happens on android versions before 4.X.X
     * 
     * @param code
     * @return
     */
    public static int getResultCode(final int code) {

        // When the startActivityForResult method is called with the regular
        // integer then the
        // java.lang.IllegalArgumentException: Can only use lower 16 bits for
        // resultCode
        // The code value must be lower than 0xffff.
        if (code == R.id.menu_settings) {
            return 219;
        } else if (code == R.id.menu_connections) {
            return 221;
        } else {
            return 0;
        }
    }
    
    /**
     * 
     * @param progress
     * @param start
     * @param stop
     * @param showEmpty
     */
    public static void setProgress(final ProgressBar progress, final Date start, final Date stop) {
        
        // Get the start and end times to calculate the progress.
        double durationTime = (stop.getTime() - start.getTime());
        double elapsedTime = new Date().getTime() - start.getTime();
        
        // Show the progress as a percentage
        double percent = 0;
        if (durationTime > 0)
            percent = elapsedTime / durationTime;

        if (progress != null) {
            progress.setProgress((int) Math.floor(percent * 100));
            progress.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 
     * @param progressText
     * @param start
     * @param stop
     */
    public static void setProgressText(final TextView progressText, final Date start, final Date stop) {
        
        // Get the start and end times to calculate the progress.
        final double durationTime = (stop.getTime() - start.getTime());
        final double elapsedTime = new Date().getTime() - start.getTime();
        
        // Show the progress as a percentage
        double percent = 0;
        if (durationTime > 0)
            percent = elapsedTime / durationTime;

        int progress = (int) Math.floor(percent * 100);
        if (progressText != null) {
            if (progress > 0) {
                progressText.setText(progressText.getResources().getString(R.string.progress, progress));
                progressText.setVisibility(View.VISIBLE);
            } else {
                progressText.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * 
     * @param context
     * @param numberOfProgramsToLoad
     * @param channel
     */
    public static void loadMorePrograms(final Context context, final int numberOfProgramsToLoad, final Channel channel) {
        
        Iterator<Program> it = channel.epg.iterator();
        Program p = null;
        long nextId = 0;

        while (it.hasNext()) {
            p = it.next();
            // Check if there is a next program available or if the current
            // program has an id for the next one
            if (p.id != nextId && nextId != 0) {
                break;
            }
            // Get the next id of the program so we can check in
            // the next iteration if this program is the last one.
            nextId = p.nextId;
        }

        if (p == null) {
            return;
        }
        // In case the while loop was not entered get the next id 
        // or if there is none the current id if the program.
        if (nextId == 0) {
            nextId = p.nextId;
        }
        if (nextId == 0) {
            nextId = p.id;
        }

        // Set the required information and start the service command.
        Intent intent = new Intent(context, HTSService.class);
        intent.setAction(HTSService.ACTION_GET_EVENTS);
        intent.putExtra("eventId", nextId);
        intent.putExtra("channelId", channel.id);
        intent.putExtra("count", numberOfProgramsToLoad);
        context.startService(intent);
    }

    /**
     * Calculates the available display width of one minute in pixels. This
     * depends how wide the screen is and how many hours shall be shown in one
     * screen.
     * 
     * @param context
     * @param tabIndex
     * @param hoursToShow
     * @return
     */
    public static float getPixelsPerMinute(FragmentActivity context, int tabIndex, int hoursToShow) {
        // Get the usable width. Subtract the icon width if its visible.
        DisplayMetrics displaymetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float displayWidth = displaymetrics.widthPixels - ((tabIndex == 0) ? LAYOUT_ICON_OFFSET : 0);
        float pixelsPerMinute = ((float) displayWidth / (60.0f * (float) hoursToShow));

        return pixelsPerMinute;
    }

    public static int getChannelTagId() {
        return channelTagId;
    }

    public static void setChannelTagId(int channelTagId) {
        Utils.channelTagId = channelTagId;
    }
}
