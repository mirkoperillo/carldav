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
package org.unitedinternet.cosmo.calendar.query;

import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.xml.DomUtils;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * Represents the CALDAV:text-match element. From sec 9.6.5:
 * 
 * Name: text-match
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Specifies a substring match on a property or parameter value.
 * 
 * Definition:
 * 
 * <!ELEMENT text-match (#PCDATA)> PCDATA value: string
 * 
 * <!ATTLIST text-match collation CDATA "i;ascii-casemap" negate-condition (yes
 * | no) "no">
 */
public class TextMatchFilter implements DavConstants, CaldavConstants {

	private static final String COLLATION_IASCII = "i;ascii-casemap";
	public static final String COLLATION_OCTET = "i;octet";

	private boolean isNegateCondition;
	private String collation;
	private String value;

	public TextMatchFilter(String value) {
		this.value = value;
	}

	/**
	 * Construct a TextMatchFilter object from a DOM Element
	 * 
	 * @param element The dom element.
	 * @throws ParseException - if something is wrong this exception is thrown.
	 */
	public TextMatchFilter(Element element) throws ParseException {
		// Element data is string to match
		// TODO: do we need to do this replacing??
		value = DomUtils.getTextTrim(element).replaceAll("'", "''");

		// Check attribute for collation
		collation = DomUtils.getAttribute(element, ATTR_CALDAV_COLLATION);

		String negateCondition = DomUtils.getAttribute(element, ATTR_CALDAV_NEGATE_CONDITION);

		if (VALUE_YES.equals(negateCondition)) {
			isNegateCondition = true;
		} else {
			isNegateCondition = false;
		}
	}

	public void setCollation(String collation) {
		this.collation = collation;
	}

	public boolean isNegateCondition() {
		return isNegateCondition;
	}

	public void setNegateCondition(boolean isNegateCondition) {
		this.isNegateCondition = isNegateCondition;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns true if the collation is a caseless collation, meaning case should be
	 * ingored when matching text. The default collation is 'i;ascii-casemap', which
	 * is considered a caseless collation. On the other hand, 'i;octet' is not
	 * caseless.
	 * 
	 * @return true if the collation is a caseless collation
	 */
	public boolean isCaseless() {
		return (collation == null || COLLATION_IASCII.equals(collation));
	}

	/**
	 * Validates if collation is supported.
	 */
	public void validate() {
		if (collation != null && !CalendarUtils.isSupportedCollation(collation)) {
			throw new UnsupportedCollationException();
		}
	}
}
