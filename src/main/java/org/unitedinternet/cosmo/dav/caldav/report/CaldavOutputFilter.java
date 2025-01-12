/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.ElementIterator;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.UnsupportedCalendarDataException;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * A utility for parsing an {@link OutputFilter} from XML.
 */
public class CaldavOutputFilter implements DavConstants, CaldavConstants, ICalendarConstants {

	/**
	 * Returns an <code>OutputFilter</code> representing the given
	 * <code>&lt;C:calendar-data/&gt;> element.
	 * 
	 * @param cdata the given calendar data.
	 * @return output filter.
	 * @throws CosmoDavException - if something is wrong this exception is thrown.
	 */
	public OutputFilter createFromXml(Element cdata) throws CosmoDavException {
		OutputFilter result = null;
		Period expand = null;
		Period limit = null;

		String contentType = DomUtils.getAttribute(cdata, CarldavConstants.c(ATTR_CALDAV_CONTENT_TYPE));
		if (contentType != null && !contentType.equals(ICALENDAR_MEDIA_TYPE)) {
			throw new UnsupportedCalendarDataException(contentType);
		}

		String version = DomUtils.getAttribute(cdata, CarldavConstants.c(ATTR_CALDAV_VERSION));
		if (version != null && !version.equals(ICALENDAR_VERSION)) {
			throw new UnsupportedCalendarDataException();
		}

		// Look at each child element of calendar-data
		for (ElementIterator iter = DomUtils.getChildren(cdata); iter.hasNext();) {

			Element child = iter.nextElement();
			if (ELEMENT_CALDAV_COMP.equals(child.getLocalName())) {

				// At the top-level of calendar-data there should only be one
				// <comp> element as VCALENDAR components are the only top-level
				// components allowed in iCalendar data
				if (result != null) {
					throw new UnsupportedOperationException("only one top-level component supported");
				}

				// Get required name attribute and verify it is VCALENDAR
				String name = DomUtils.getAttribute(child, ATTR_CALDAV_NAME);
				if (!Calendar.VCALENDAR.equals(name)) {
					throw new UnsupportedOperationException(
							"only top-level comp name " + Calendar.VCALENDAR + " supported");
				}

				// Now parse filter item
				result = parseCalendarDataComp(child);

			} else if (ELEMENT_CALDAV_EXPAND.equals(child.getLocalName())) {
				expand = parsePeriod(child);
			} else if (ELEMENT_CALDAV_LIMIT_RECURRENCE_SET.equals(child.getLocalName())) {
				limit = parsePeriod(child);
			}
		}

		// Now add any limit/expand options, creating a filter if one is not
		// already present
		if (result == null && (expand != null || limit != null)) {
			result = new OutputFilter("VCALENDAR");
			result.setAllSubComponents();
			result.setAllProperties();
		}
		if (expand != null) {
			result.setExpand(expand);
		}
		if (limit != null) {
			result.setLimit(limit);
		}

		return result;
	}

	// private methods

	private static OutputFilter parseCalendarDataComp(Element comp) {
		// Get required name attribute
		String name = DomUtils.getAttribute(comp, ATTR_CALDAV_NAME);
		if (name == null) {
			return null;
		}

		// Now create filter item
		OutputFilter result = new OutputFilter(name);

		// Look at each child element
		ElementIterator i = DomUtils.getChildren(comp);
		while (i.hasNext()) {
			Element child = i.nextElement();
			if (ELEMENT_CALDAV_ALLCOMP.equals(child.getLocalName())) {
				// Validity check
				if (result.hasSubComponentFilters()) {
					result = null;
					return null;
				}
				result.setAllSubComponents();

			} else if (ELEMENT_CALDAV_ALLPROP.equals(child.getLocalName())) {
				// Validity check
				if (result.hasPropertyFilters()) {
					result = null;
					return null;
				}
				result.setAllProperties();

			} else if (ELEMENT_CALDAV_COMP.equals(child.getLocalName())) {
				// Validity check
				if (result.isAllSubComponents()) {
					result = null;
					return null;
				}
				OutputFilter subfilter = parseCalendarDataComp(child);
				if (subfilter == null) {
					result = null;
					return null;
				} else {
					result.addSubComponent(subfilter);
				}
			} else if (ELEMENT_CALDAV_PROP.equals(child.getLocalName())) {
				// Validity check
				if (result.isAllProperties()) {
					result = null;
					return null;
				}

				// Get required name attribute
				String propname = DomUtils.getAttribute(child, ATTR_CALDAV_NAME);
				if (propname == null) {
					result = null;
					return null;
				}

				// Get optional novalue attribute
				boolean novalue = false;
				String novaluetxt = DomUtils.getAttribute(child, ATTR_CALDAV_NOVALUE);
				if (novaluetxt != null) {
					if (VALUE_YES.equals(novaluetxt)) {
						novalue = true;
					} else if (VALUE_NO.equals(novaluetxt)) {
						novalue = false;
					} else {
						result = null;
						return null;
					}
				}

				// Now add property item
				result.addProperty(propname, novalue);
			}
		}

		return result;
	}

	private static Period parsePeriod(Element node) throws CosmoDavException {
		DateTime trstart;
		DateTime trend;
		String start = DomUtils.getAttribute(node, ATTR_CALDAV_START);
		if (start == null) {
			throw new BadRequestException("Expected timerange attribute " + ATTR_CALDAV_START);
		}
		try {
			trstart = new DateTime(start);
		} catch (ParseException e) {
			throw new BadRequestException("Timerange start not parseable: " + e.getMessage());
		}
		if (!trstart.isUtc()) {
			throw new BadRequestException("Timerange start must be UTC");
		}

		String end = DomUtils.getAttribute(node, ATTR_CALDAV_END);
		if (end == null) {
			throw new BadRequestException("Expected timerange attribute " + ATTR_CALDAV_END);
		}
		try {
			trend = new DateTime(end);
		} catch (ParseException e) {
			throw new BadRequestException("Timerange end not parseable: " + e.getMessage());
		}
		if (!trend.isUtc()) {
			throw new BadRequestException("Timerange end must be UTC");
		}

		return new Period(trstart, trend);
	}
}
