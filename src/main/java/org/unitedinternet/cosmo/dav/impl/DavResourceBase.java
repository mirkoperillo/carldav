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
package org.unitedinternet.cosmo.dav.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.version.DeltaVResource;
import org.apache.jackrabbit.webdav.version.OptionsInfo;
import org.apache.jackrabbit.webdav.version.OptionsResponse;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.PreconditionFailedException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.AddressbookHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarHomeSet;
import org.unitedinternet.cosmo.dav.property.PrincipalUrl;
import org.unitedinternet.cosmo.dav.property.SupportedReportSet;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;

/**
 * <p>
 * Base class for implementations of <code>WebDavResource</code>
 * which provides behavior common to all resources.
 * </p>
 * <p>
 * This class declares the following live properties:
 * </p>
 * <ul>
 * <li> DAV:supported-report-set </li>
 * <li> DAV:current-user-privilege-set </li>
 * </ul>
 * <p>
 * This class does not declare any reports.
 * </p>
 * 
 * @see WebDavResource
 */
public abstract class DavResourceBase implements ExtendedDavConstants, DeltaVResource{

    protected static final EntityConverter converter = new EntityConverter();
    
    @Override
    public void move(final DavResource destination) throws DavException {

        throw new UnsupportedOperationException();
    }

    private final HashSet<DavPropertyName> liveProperties = new HashSet<>(10);
    private final Set<ReportType> reportTypes = new HashSet<>(10);

    private DavResourceLocator locator;
    private DavResourceFactory factory;
    private DavPropertySet properties;
    private boolean initialized;

    public DavResourceBase(DavResourceLocator locator,
                           DavResourceFactory factory)
        throws CosmoDavException {
        registerLiveProperty(SUPPORTEDREPORTSET);
        this.locator = locator;
        this.factory = factory;
        this.properties = new DavPropertySet();
        this.initialized = false;
    }

    // WebDavResource methods

    public String getComplianceClass() {
        return WebDavResource.COMPLIANCE_CLASS;
    }

    public org.apache.jackrabbit.webdav.DavResourceLocator getLocator() {
        return null;
    }

    public String getResourcePath() {
        return locator.getPath();
    }

    public String getHref() {
        return locator.getHref(isCollection());
    }

    public void spool(OutputContext outputContext)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public DavPropertyName[] getPropertyNames() {
        loadProperties();
        return properties.getPropertyNames();
    }

    public org.apache.jackrabbit.webdav.property.DavProperty<?>
        getProperty(DavPropertyName name) {
        loadProperties();
        return properties.get(name);
    }

    @Deprecated
    public DavPropertySet getProperties() {
        loadProperties();
        return properties;
    }

    public Map<String, WebDavProperty> getWebDavProperties() {
        final DavPropertySet properties = getProperties();
        final DavPropertyName[] propertyNames = properties.getPropertyNames();
        final Map<String, WebDavProperty> sorted = new TreeMap<>();

        for (final DavPropertyName propertyName : propertyNames) {
            sorted.put(propertyName.getName(), (WebDavProperty) properties.get(propertyName));
        }

        return sorted;
    }

    public void setProperty(org.apache.jackrabbit.webdav.property.DavProperty<?> property) throws DavException {
        throw new UnsupportedOperationException();
    }

    public void removeProperty(DavPropertyName propertyName) throws DavException {
        throw new UnsupportedOperationException();

    }

    public MultiStatusResponse alterProperties(List<? extends PropEntry> changeList) throws DavException{
        throw new UnsupportedOperationException();
    }

    public boolean isLockable(Type type,
                              Scope scope) {
        // nothing is lockable at the moment
        return false;
    }

    public boolean hasLock(Type type,
                           Scope scope) {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    public ActiveLock getLock(Type type,
                              Scope scope) {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    public ActiveLock[] getLocks() {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    public ActiveLock lock(LockInfo reqLockInfo)
        throws DavException {
        // nothing is lockable at the moment
        throw new PreconditionFailedException("Resource not lockable");
    }

    public ActiveLock refreshLock(LockInfo reqLockInfo,
                                  String lockToken)
        throws DavException {
        // nothing is lockable at the moment
        throw new PreconditionFailedException("Resource not lockable");
    }

    public void unlock(String lockToken)
        throws DavException {
        // nothing is lockable at the moment
        throw new PreconditionFailedException("Resource not lockable");
    }

    public void addLockManager(LockManager lockmgr) {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    public org.apache.jackrabbit.webdav.DavResourceFactory getFactory() {
        return null;
    }

    public org.apache.jackrabbit.webdav.DavSession getSession() {
        return null;
    }

    @Override
    public void copy(final DavResource destination, final boolean shallow) throws DavException {
        throw new UnsupportedOperationException();
    }

    // WebDavResource methods

    public MultiStatusResponse
        updateProperties(DavPropertySet setProperties,
                         DavPropertyNameSet removePropertyNames)
        throws CosmoDavException {
        if (! exists()) {
            throw new NotFoundException();
        }

        MultiStatusResponse msr = new MultiStatusResponse(getHref(), null);

        ArrayList<DavPropertyName> df = new ArrayList<DavPropertyName>();
        CosmoDavException error = null;
        DavPropertyName failed = null;

        org.apache.jackrabbit.webdav.property.DavProperty<?> property = null;
        for (DavPropertyIterator i=setProperties.iterator(); i.hasNext();) {
            try {
                property = i.nextProperty();
                setResourceProperty((WebDavProperty)property, false);
                df.add(property.getName());
                msr.add(property.getName(), 200);
            } catch (CosmoDavException e) {
                // we can only report one error message in the
                // responsedescription, so even if multiple properties would
                // fail, we return 424 for the second and subsequent failures
                // as well
                if (error == null) {
                    error = e;
                    failed = property.getName();
                } else {
                    df.add(property.getName());
                }
            }
        }

        DavPropertyName name = null;
        for (DavPropertyNameIterator i=removePropertyNames.iterator();
             i.hasNext();) {
            try {
                name = (DavPropertyName) i.next();
                removeResourceProperty(name);
                df.add(name);
                msr.add(name, 200);
            } catch (CosmoDavException e) {
                // we can only report one error message in the
                // responsedescription, so even if multiple properties would
                // fail, we return 424 for the second and subsequent failures
                // as well
                if (error == null) {
                    error = e;
                    failed = name;
                } else {
                    df.add(name);
                }
            }
        }

        if (error != null) {
            // replace the other response with a new one, since we have to
            // change the response code for each of the properties that would
            // have been set successfully
            msr = new MultiStatusResponse(getHref(), error.getMessage());
            for (DavPropertyName n : df) {
                msr.add(n, 424);
            }
            msr.add(failed, error.getErrorCode());
        }

        return msr;
    }

    public Report getReport(ReportInfo reportInfo)
        throws CosmoDavException {
        if (! exists()) {
            throw new NotFoundException();
        }

        if (! isSupportedReport(reportInfo)) {
            throw new UnprocessableEntityException("Unknown report " + reportInfo.getReportName());
        }

        try {
        	return ReportType.getType(reportInfo).createReport(this, reportInfo);
        } catch (DavException e){
            if (e instanceof CosmoDavException) {
                throw (CosmoDavException) e;
            }
            throw new CosmoDavException(e);
        }
    }

    public DavResourceFactory getResourceFactory() {
        return factory;
    }

    public DavResourceLocator getResourceLocator() {
        return locator;
    }

    // our methods

    protected CosmoSecurityManager getSecurityManager() {
        return factory.getSecurityManager();
    }

    /**
     * Determines whether or not the report indicated by the given
     * report info is supported by this collection.
     */
    protected boolean isSupportedReport(ReportInfo info) {
        for (Iterator<ReportType> i=getReportTypes().iterator(); i.hasNext();) {
            if (i.next().isRequestedReportType(info)) {
                return true;
            }
        }
        return false;
    }

    protected Set<ReportType> getReportTypes() {
     return reportTypes;
    }

    /**
     * <p>
     * Registers the name of a live property.
     * </p>
     * <p>
     * Typically used in subclass static initializers to add to the set
     * of live properties for the resource.
     * </p>
     */
    protected void registerLiveProperty(DavPropertyName name) {
        liveProperties.add(name);
    }

    /**
     * Returns the set of resource types for this resource.
     */
    protected abstract Set<QName> getResourceTypes();

    /**
     * Determines whether or not the given property name identifies a
     * live property.
     * 
     * If the server understands the semantic meaning of a property
     * (probably because the property is defined in a DAV-related
     * specification somewhere), then the property is defined as
     * "live". Live properties are typically explicitly represented in the
     * object model.
     *
     * If the server does not know anything specific about the
     * property (usually because it was defined by a particular
     * client), then it is known as a "dead" property.
     */
    protected boolean isLiveProperty(DavPropertyName name) {
        return liveProperties.contains(name);
    }

    /**
     * Calls {@link #loadLiveProperties()} and {@link #loadDeadProperties()}
     * to load the resource's properties from its backing state.
     */
    protected void loadProperties() {
        if (initialized) {
            return;
        }

        properties.add(new SupportedReportSet(getReportTypes()));

        loadLiveProperties(properties);

        initialized = true;
    }    

    protected void setResourceProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        DavPropertyName name = property.getName();
        if (name.equals(SUPPORTEDREPORTSET)) {
            throw new ProtectedPropertyModificationException(name);
        }

        if (isLiveProperty(property.getName())) {
            setLiveProperty(property, create);
        }
        
        properties.add(property);
    }

    protected void removeResourceProperty(DavPropertyName name)
        throws CosmoDavException {
        if (name.equals(SUPPORTEDREPORTSET)) {
            throw new ProtectedPropertyModificationException(name);
        }

        if (isLiveProperty(name)) {
            removeLiveProperty(name);
        }

        properties.remove(name);
    }

    /**
     * Loads the live DAV properties for the resource.
     */
    protected abstract void loadLiveProperties(DavPropertySet properties);

    /**
     * Sets a live DAV property on the resource on resource initialization.
     *
     * @param property the property to set
     *
     * @throws CosmoDavException if the property is protected
     * or if a null value is specified for a property that does not
     * accept them or if an invalid value is specified
     */
    protected void setLiveProperty(WebDavProperty property, boolean create) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }

    /**
     * Removes a live DAV property from the resource.
     *
     * @param name the name of the property to remove
     *
     * @throws CosmoDavException if the property is protected
     */
    protected void removeLiveProperty(DavPropertyName name) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(name);
    }

    /**
     * Sets a dead DAV property on the resource.
     *
     * @param property the property to set
     *
     * @throws CosmoDavException if a null value is specified for a property that
     * does not accept them or if an invalid value is specified
     */
    protected void setDeadProperty(WebDavProperty property) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }

    public OptionsResponse getOptionResponse(OptionsInfo optionsInfo){
    	return null;
    }

    @Override
    public void addWorkspace(org.apache.jackrabbit.webdav.DavResource workspace) {
    	
    }
    
    @Override
    public WebDavResource[] getReferenceResources(DavPropertyName hrefPropertyName) {
    	return new WebDavResource[]{};
    }

    protected void generateHrefIfNecessary(final PrintWriter writer, final WebDavProperty prop, final String text) {
        if(instanceOf(prop)) {
            writer.write("<a href=\"");
        }

        writer.write(StringEscapeUtils.escapeHtml(text));

        if(instanceOf(prop)) {
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(text));
            writer.write("</a>\n");
        }
    }

    protected boolean instanceOf(final WebDavProperty prop) {
        return prop instanceof AddressbookHomeSet || prop instanceof CalendarHomeSet || prop instanceof PrincipalUrl;
    }
}
