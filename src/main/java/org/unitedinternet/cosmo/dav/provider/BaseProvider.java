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
package org.unitedinternet.cosmo.dav.provider;

import static carldav.CarldavConstants.caldav;

import carldav.exception.resolver.ResponseUtils;
import carldav.jackrabbit.webdav.CustomDavConstants;
import carldav.jackrabbit.webdav.CustomDavPropertyName;
import carldav.jackrabbit.webdav.CustomDavPropertyNameSet;
import carldav.jackrabbit.webdav.CustomDomUtils;
import carldav.jackrabbit.webdav.CustomElementIterator;
import carldav.jackrabbit.webdav.CustomMultiStatus;
import carldav.jackrabbit.webdav.CustomReportInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.header.DepthHeader;
import org.springframework.http.MediaType;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.ContentLengthRequiredException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import carldav.jackrabbit.webdav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.report.ReportBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * A base class for implementations of <code>DavProvider</code>.
 * </p>
 *
 * @see DavProvider
 */
public abstract class BaseProvider implements DavProvider, CustomDavConstants {

    private static final MediaType APPLICATION_XML = MediaType.APPLICATION_XML;
    private static final MediaType TEXT_XML = MediaType.TEXT_XML;

    private DavResourceFactory resourceFactory;
    private int propfindType = PROPFIND_ALL_PROP;
    private CustomDavPropertyNameSet propfindProps;
    private CustomReportInfo reportInfo;

    public BaseProvider(DavResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    // DavProvider methods
    /**
     * 
     * {@inheritDoc}
     */
    public void get(HttpServletRequest request,
                    HttpServletResponse response,
                    WebDavResource resource)
        throws CosmoDavException, IOException {
        spool(request, response, resource, true);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public void head(HttpServletRequest request,
                     HttpServletResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException {
        spool(request, response, resource, false);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void propfind(HttpServletRequest request,
                         HttpServletResponse response,
                         WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        int depth = getDepth(request);
        if (depth != DEPTH_0 && ! resource.isCollection()){
            throw new BadRequestException("Depth must be 0 for non-collection resources");
        }

        CustomDavPropertyNameSet props = getPropFindProperties(request);
        int type = getPropFindType(request);
        CustomMultiStatus ms = new CustomMultiStatus();
        ms.addResourceProperties(resource, props, type, depth);

        ResponseUtils.sendXmlResponse(response, ms, 207);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void delete(HttpServletRequest request,
                       HttpServletResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            response.setStatus(204);
            return;
        }
        checkNoRequestBody(request);

        int depth = getDepth(request);
        if (depth != DEPTH_INFINITY){
            throw new BadRequestException("Depth for DELETE must be infinity");
        }

        resource.getParent().removeMember2(resource);
        response.setStatus(204);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void report(HttpServletRequest request,
                       HttpServletResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        try {
            CustomReportInfo info = getReportInfo(request);
            if (info == null){
                if(resource.isCollection()){
                    return;
                } else {
                    throw new BadRequestException("REPORT requires entity body");
                }
            }

            ((ReportBase) resource.getReport(info)).run(response);
        } catch (CosmoDavException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CosmoDavException(exception);
        }
    }

    protected void spool(HttpServletRequest request,
                         HttpServletResponse response,
                         WebDavResource resource,
                         boolean withEntity)
        throws CosmoDavException, IOException {
        if (! resource.exists()){
            throw new NotFoundException();
        }
        checkNoRequestBody(request);
        resource.writeHead(response);
        if(withEntity) {
            resource.writeBody(response);
        }
        response.flushBuffer();
    }
    /**
     * 
     * @param request 
     * @return InputContext 
     * @throws CosmoDavException 
     * @throws IOException 
     */
    protected DavInputContext createInputContext(final HttpServletRequest request)
        throws CosmoDavException, IOException {
        String xfer = request.getHeader("Transfer-Encoding");
        boolean chunked = xfer != null && xfer.equals("chunked");
        if (xfer != null && ! chunked){
            throw new BadRequestException("Unknown Transfer-Encoding " + xfer);
        }
        if (chunked && request.getContentLength() <= 0){
            throw new ContentLengthRequiredException();
        }

        InputStream in = request.getContentLength() > 0 || chunked ?
            request.getInputStream() : null;
        return new DavInputContext(request, in);
    }

    protected void checkNoRequestBody(HttpServletRequest request) throws CosmoDavException {
        boolean hasBody = getRequestDocument(request) != null;
        if (hasBody){
            throw new UnsupportedMediaTypeException("Body not expected for method " + request.getMethod());
        }
    }

    protected int getDepth(final HttpServletRequest request) {
        return DepthHeader.parse(request, DEPTH_INFINITY).getDepth();
    }

    private CustomReportInfo getReportInfo(final HttpServletRequest request) throws CosmoDavException {
        if (reportInfo == null) {
            reportInfo = parseReportRequest(request);
        }
        return reportInfo;
    }

    private CustomReportInfo parseReportRequest(final HttpServletRequest request) throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument(request);
        if (requestDocument == null) { // reports with no bodies are supported
            // for collections
            return null;
        }

        try {
            return new CustomReportInfo(requestDocument.getDocumentElement(), getDepth(request));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private int getPropFindType(final HttpServletRequest request) throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest(request);
        }
        return propfindType;
    }

    private CustomDavPropertyNameSet getPropFindProperties(final HttpServletRequest request) throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest(request);
        }
        return propfindProps;
    }

    private void parsePropFindRequest(final HttpServletRequest request) throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument(request);

        if (requestDocument == null) {
            // treat as allprop
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new CustomDavPropertyNameSet();
            return;
        }

        Element root = requestDocument.getDocumentElement();
        if (!CustomDomUtils.matches(root, XML_PROPFIND, caldav(XML_PROPFIND))) {
            throw new BadRequestException("Expected " + XML_PROPFIND
                    + " root element");
        }

        Element prop = CustomDomUtils.getChildElement(root, caldav(XML_PROP));
        if (prop != null) {
            propfindType = PROPFIND_BY_PROPERTY;
            propfindProps = new CustomDavPropertyNameSet(prop);
            return;
        }

        if (CustomDomUtils.getChildElement(root, caldav(XML_PROPNAME)) != null) {
            propfindType = PROPFIND_PROPERTY_NAMES;
            propfindProps = new CustomDavPropertyNameSet();
            return;
        }

        if (CustomDomUtils.getChildElement(root, caldav(XML_ALLPROP)) != null) {
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new CustomDavPropertyNameSet();

            Element include = CustomDomUtils.getChildElement(root, caldav("include"));
            if (include != null) {
                CustomElementIterator included = CustomDomUtils.getChildren(include);
                while (included.hasNext()) {
                    CustomDavPropertyName name = CustomDavPropertyName
                            .createFromXml(included.nextElement());
                    propfindProps.add(name);
                }
            }

            return;
        }

        throw new BadRequestException("Expected one of " + XML_PROP + ", "
                + XML_PROPNAME + ", or " + XML_ALLPROP + " as child of "
                + XML_PROPFIND);
    }

    private Document getSafeRequestDocument(final HttpServletRequest request) {
        if (StringUtils.isBlank(request.getContentType())) {
            throw new BadRequestException("No Content-Type specified");
        }

        final MediaType mediaType = MediaType.valueOf(request.getContentType());
        if (!(mediaType.isCompatibleWith(APPLICATION_XML) || mediaType.isCompatibleWith(TEXT_XML))) {
            throw new UnsupportedMediaTypeException("Expected Content-Type " + APPLICATION_XML + " or " + TEXT_XML);
        }

        return getRequestDocument(request);
    }

    private Document getRequestDocument(final HttpServletRequest request) {
        Document requestDocument = null;
        /*
        Don't attempt to parse the body if the content length header is 0.
        NOTE: a value of -1 indicates that the length is unknown, thus we have
        to parse the body. Note that http1.1 request using chunked transfer
        coding will therefore not be detected here.
        */
        if (request.getContentLength() == 0) {
            return requestDocument;
        }
        // try to parse the request body
        try {
            InputStream in = request.getInputStream();
            if (in != null) {
                // use a buffered input stream to find out whether there actually
                // is a request body
                InputStream bin = new BufferedInputStream(in);
                bin.mark(1);
                boolean isEmpty = -1 == bin.read();
                bin.reset();
                if (!isEmpty) {
                    requestDocument = CustomDomUtils.parseDocument(bin);
                }
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
        return requestDocument;
    }

    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }
}