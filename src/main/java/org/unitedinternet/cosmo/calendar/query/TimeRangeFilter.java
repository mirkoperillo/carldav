/*
 * Copyright 2006-2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.calendar.query;

import carldav.jackrabbit.webdav.xml.DomUtils;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * Represents the CALDAV:time-range element. From sec 9.8:
 * 
 * Name: time-range
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Specifies a time range to limit the set of calendar components
 * returned by the server.
 * 
 * Definition:
 * 
 * <!ELEMENT time-range EMPTY>
 * 
 * <!ATTLIST time-range start CDATA #IMPLIED end CDATA #IMPLIED> start value: an
 * iCalendar "date with UTC time" end value: an iCalendar "date with UTC time"
 */
public class TimeRangeFilter implements CaldavConstants {

	private Period period = null;

	private VTimeZone timezone = null;

	private DateTime dstart, dend;

	/**
	 * Constructor.
	 * 
	 * @param period The period.
	 */
	public TimeRangeFilter(Period period) {
		setPeriod(period);
	}

	/**
	 * Construct a TimeRangeFilter object from a DOM Element
	 * 
	 * @param element The DOM Element.
	 * @throws ParseException - if something is wrong this exception is thrown.
	 */
	public TimeRangeFilter(Element element, VTimeZone timezone) throws ParseException {
		// Get start (must be present)
		String start = DomUtils.getAttribute(element, ATTR_CALDAV_START);
		if (start == null) {
			throw new ParseException("CALDAV:comp-filter time-range requires a start time", -1);
		}

		DateTime trstart = new DateTime(start);
		if (!trstart.isUtc()) {
			throw new ParseException("CALDAV:param-filter timerange start must be UTC", -1);
		}

		// Get end (must be present)
		String end = DomUtils.getAttribute(element, ATTR_CALDAV_END);
		if (end == null) {
			// add one year to date start Iphone ios7 bug
			end = addOneYearToDateStart(start);
			// throw new ParseException("CALDAV:comp-filter time-range requires an end
			// time", -1);
		}

		DateTime trend = new DateTime(end);
		if (!trend.isUtc()) {
			throw new ParseException("CALDAV:param-filter timerange end must be UTC", -1);
		}

		setPeriod(new Period(trstart, trend));
		setTimezone(timezone);
	}

	private String addOneYearToDateStart(String start) {
		String year = start.substring(0, 4);

		return Integer.parseInt(year) + 1 + start.substring(4);
	}

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
		// Get fixed start/end time
		dstart = period.getStart();
		dend = period.getEnd();
	}

	public String getUTCStart() {
		return dstart.toString();
	}

	public String getUTCEnd() {
		return dend.toString();
	}

	public VTimeZone getTimezone() {
		return timezone;
	}

	public void setTimezone(VTimeZone timezone) {
		this.timezone = timezone;
	}

}
