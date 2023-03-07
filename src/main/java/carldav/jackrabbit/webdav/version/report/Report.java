package carldav.jackrabbit.webdav.version.report;

import org.unitedinternet.cosmo.dav.WebDavResource;

public interface Report {

	ReportType getType();

	/**
	 * Set the <code>WebDavResource</code> for which this report was requested and
	 * the <code>ReportInfo</code> as specified by the REPORT request body, that
	 * defines the details for this report.<br>
	 * Please note that this methods should perform basic validation checks in order
	 * to prevent exceptional situations during the xml serialization.
	 *
	 * @param resource
	 * @param info
	 */
	void init(WebDavResource resource, ReportInfo info);
}
