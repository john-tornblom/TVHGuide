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

import android.app.Application;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.me.tvhguide.model.Channel;
import org.me.tvhguide.model.ChannelTag;
import org.me.tvhguide.model.Programme;
import org.me.tvhguide.htsp.HTSListener;
import org.me.tvhguide.model.Recording;

/**
 *
 * @author john-tornblom
 */
public class TVHGuideApplication extends Application {

    public static final String ACTION_CHANNEL_ADD = "org.me.tvhguide.CHANNEL_ADD";
    public static final String ACTION_CHANNEL_DELETE = "org.me.tvhguide.CHANNEL_DELETE";
    public static final String ACTION_CHANNEL_UPDATE = "org.me.tvhguide.CHANNEL_UPDATE";
    public static final String ACTION_TAG_ADD = "org.me.tvhguide.TAG_ADD";
    public static final String ACTION_TAG_DELETE = "org.me.tvhguide.TAG_DELETE";
    public static final String ACTION_TAG_UPDATE = "org.me.tvhguide.TAG_UPDATE";
    public static final String ACTION_DVR_ADD = "org.me.tvhguide.DVR_ADD";
    public static final String ACTION_DVR_DELETE = "org.me.tvhguide.DVR_DELETE";
    public static final String ACTION_DVR_UPDATE = "org.me.tvhguide.DVR_UPDATE";
    public static final String ACTION_SIGNAL_STATUS = "org.me.tvhguide.SIGNAL_STATUS";
    public static final String ACTION_LOADING = "org.me.tvhguide.LOADING";
    public static final String ACTION_RESULT_FOUND = "org.me.tvhguide.RESULT_FOUND";
    private final List<HTSListener> listeners = new ArrayList<HTSListener>();
    private final List<ChannelTag> tags = Collections.synchronizedList(new ArrayList<ChannelTag>());
    private final List<Channel> channels = Collections.synchronizedList(new ArrayList<Channel>());
    private final List<Recording> recordings = Collections.synchronizedList(new ArrayList<Recording>());
    private final List<Programme> searchResult = Collections.synchronizedList(new ArrayList<Programme>());
    private volatile boolean loading = false;

    public void addListener(HTSListener l) {
        listeners.add(l);
    }

    public void removeListener(HTSListener l) {
        listeners.remove(l);
    }

    private void broadcastMessage(String action, Object obj) {
        synchronized (listeners) {
            for (HTSListener l : listeners) {
                l.onMessage(action, obj);
            }
        }
    }

    public List<ChannelTag> getChannelTags() {
        return tags;
    }

    public void addChannelTag(ChannelTag tag) {
        tags.add(tag);

        if (!loading) {
            broadcastMessage(ACTION_TAG_ADD, tag);
        }
    }

    public void removeChannelTag(ChannelTag tag) {
        tags.remove(tag);

        if (!loading) {
            broadcastMessage(ACTION_TAG_DELETE, tag);
        }
    }

    public void removeChannelTag(long id) {
        for (ChannelTag tag : getChannelTags()) {
            if (tag.id == id) {
                removeChannelTag(tag);
                return;
            }
        }
    }

    public void addChannel(Channel channel) {
        channels.add(channel);

        if (!loading) {
            broadcastMessage(ACTION_CHANNEL_ADD, channel);
        }
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void removeChannel(Channel channel) {
        channels.remove(channel);

        if (!loading) {
            broadcastMessage(ACTION_CHANNEL_DELETE, channel);
        }
    }

    public Channel getChannel(long id) {
        for (Channel ch : getChannels()) {
            if (ch.id == id) {
                return ch;
            }
        }
        return null;
    }

    public void removeChannel(long id) {
        for (Channel ch : getChannels()) {
            if (ch.id == id) {
                removeChannel(ch);
                return;
            }
        }
    }

    public void updateChannel(Channel ch) {
        if (!loading) {
            broadcastMessage(ACTION_CHANNEL_UPDATE, ch);
        }
    }

    public void addRecording(Recording rec) {
        recordings.add(rec);

        if (!loading) {
            broadcastMessage(ACTION_DVR_ADD, rec);
        }
    }

    public List<Recording> getRecordings() {
        return recordings;
    }

    public void removeRecording(Recording rec) {
        recordings.remove(rec);

        if (!loading) {
            broadcastMessage(ACTION_DVR_DELETE, rec);
        }
    }

    public Recording getRecording(long id) {
        for (Recording rec : getRecordings()) {
            if (rec.id == id) {
                return rec;
            }
        }
        return null;
    }

    public void removeRecording(long id) {
        for (Recording rec : getRecordings()) {
            if (rec.id == id) {
                removeRecording(rec);
                return;
            }
        }
    }

    public void updateRecording(Recording rec) {
        if (!loading) {
            broadcastMessage(ACTION_DVR_UPDATE, rec);
        }
    }

    public void setLoading(boolean b) {
        if (loading != b) {
            broadcastMessage(ACTION_LOADING, b);
        }
        loading = b;
    }
    
    public void clearSearchResult() {
    	searchResult.clear(); 
    }
    
    public void addSearchResult(Programme pr) {
		searchResult.add(pr);
		
        if (!loading) {
            broadcastMessage(ACTION_RESULT_FOUND, pr);
        }
	}

    public void clearAll() {
    	searchResult.clear();
        tags.clear();
        recordings.clear();

        for (Channel ch : channels) {
            ch.epg.clear();
            ch.recordings.clear();
        }
        channels.clear();
    }
}
