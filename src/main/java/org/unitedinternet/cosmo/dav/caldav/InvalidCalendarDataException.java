/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.caldav;

import org.unitedinternet.cosmo.dav.BadRequestException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An exception indicating that a calendar resource contains missing or invalid
 * calendar data.
 */
public class InvalidCalendarDataException extends BadRequestException implements CaldavConstants {

	/**
	 * Constructor.
	 * 
	 * @param message The exception message.
	 */
	public InvalidCalendarDataException(String message) {
		super(message);
		getNamespaceContext().addNamespace(PRE_CALDAV, NS_CALDAV);
	}

	protected void writeContent(XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement(NS_CALDAV, "valid-calendar-data");
		writer.writeCharacters(getMessage());
		writer.writeEndElement();
	}
}
