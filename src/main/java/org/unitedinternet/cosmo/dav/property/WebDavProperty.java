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
package org.unitedinternet.cosmo.dav.property;

import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.property.DavPropertyName;
import carldav.jackrabbit.webdav.property.PropEntry;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;

/**
 * <p>
 * Extends the jcr-server WebDavProperty interface.
 * </p>
 */
public interface WebDavProperty<T> extends PropEntry, ExtendedDavConstants {

	/**
	 * <p>
	 * Returns the text content of the property value as a string. The string is
	 * calculated by concatening the text and character data content of every
	 * element in the value.
	 * </p>
	 */
	String getValueText();

	/**
	 * Returns the name of this property
	 *
	 * @return the name of this property
	 */
	DavPropertyName getName();

	/**
	 * Returns the value of this property
	 *
	 * @return the value of this property
	 */
	T getValue();

	/**
	 * Return <code>true</code> if this property should be suppressed in a
	 * PROPFIND/{@link DavConstants#PROPFIND_ALL_PROP DAV:allprop} response. See RFC
	 * 4918, Section 9.1.
	 *
	 * @return true, if this property should be suppressed in a PROPFIND/allprop
	 *         response
	 */
	boolean isInvisibleInAllprop();
}
