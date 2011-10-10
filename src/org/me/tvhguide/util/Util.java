package org.me.tvhguide.util;

import java.util.Date;

import org.me.tvhguide.R;

import android.content.Context;
import android.text.format.DateFormat;

public class Util {

	public static String getDate(Context ctx, Date start, Date stop) {
		String retVal = "";

		if (isToday(start)) 
			retVal = ctx.getResources().getString(R.string.time_today);
		else if (isTomorrow(start))
			retVal = ctx.getResources().getString(R.string.time_tomorrow);
		else
			retVal = DateFormat.getLongDateFormat(ctx).format(start);
		
		retVal += "   " + DateFormat.getTimeFormat(ctx).format(start)
        	+ " - "
        	+ DateFormat.getTimeFormat(ctx).format(stop);
		
		return retVal;
	}
	
	public static String getDate(Context ctx, Date start) {
		String retVal = "";

		if (isToday(start)) 
			retVal = ctx.getResources().getString(R.string.time_today);
		else if (isTomorrow(start))
			retVal = ctx.getResources().getString(R.string.time_tomorrow);
		else
			retVal = DateFormat.getMediumDateFormat(ctx).format(start);
				
		return retVal;
	}

	static Boolean isToday(Date start) {
		Date compDate = new Date();
		if (compDate.getMonth() == start.getMonth())
			if (compDate.getDate() == start.getDate())
				return true;

		return false;
	}
	
	static Boolean isTomorrow(Date start) {
		Date compDate = new Date();
		compDate.setTime(compDate.getTime() + (1 * 24 * 60 * 60 * 1000));
		if (compDate.getMonth() == start.getMonth())
			if (compDate.getDate() == start.getDate())
				return true;

		return false;
	}
	
}
