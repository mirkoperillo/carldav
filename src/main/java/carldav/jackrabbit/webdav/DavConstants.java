package carldav.jackrabbit.webdav;

import javax.xml.namespace.QName;

/**
 * <code>DavConstants</code> provide constants for request and response headers,
 * XML elements and property names defined by
 * <a href="http://www.webdav.org/specs/rfc2518.html">RFC 2518</a>. In addition,
 * common date formats (creation date and modification time) are included.
 */
public interface DavConstants {

	// -------------------------------------------------------< Depth Header >---
	String HEADER_DEPTH = "Depth";
	int DEPTH_INFINITY = Integer.MAX_VALUE;
	int DEPTH_0 = 0;
	int DEPTH_1 = 1;
	String DEPTH_INFINITY_S = "infinity";

	// ---< XML Element, Attribute Names >---------------------------------------
	String XML_ALLPROP = "allprop";
	String XML_COLLECTION = "collection";
	String XML_HREF = "href";
	String XML_MULTISTATUS = "multistatus";
	String XML_PROP = "prop";
	String XML_PROPFIND = "propfind";
	String XML_PROPNAME = "propname";
	String XML_PROPSTAT = "propstat";
	String XML_RESPONSE = "response";

	QName ALLPROP = new QName("DAV:", XML_ALLPROP, "D");
	QName PROPNAME = new QName("DAV:", XML_PROPNAME, "D");
	QName HREF = new QName("DAV:", XML_HREF, "D");

	// -------------------------------------------------< PropFind Constants >---
	int PROPFIND_BY_PROPERTY = 0;
	int PROPFIND_ALL_PROP = 1;
	int PROPFIND_PROPERTY_NAMES = 2;
	int PROPFIND_ALL_PROP_INCLUDE = 3; // RFC 4918, Section 9.1
}
