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
package org.unitedinternet.cosmo.dav;

/**
 * An exception indicating that a dav resource has not been modified since it
 * was last requested.
 */
public class NotModifiedException extends CosmoDavException {

	/**
	 * Constructor.
	 */
	public NotModifiedException() {
		super(304);
	}

	public boolean hasContent() {
		return false;
	}
}
