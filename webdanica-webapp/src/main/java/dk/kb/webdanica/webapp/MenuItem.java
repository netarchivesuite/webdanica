package dk.kb.webdanica.webapp;

import java.util.Locale;

import dk.kb.webdanica.core.datamodel.Status;
import dk.netarkivet.common.utils.I18n;

public class MenuItem {
	
	private int status;
	private Locale loc;
	private I18n i18n;
	private Long count;

	public MenuItem(int status, Long count, Locale loc, I18n i18n) {
		this.status = status;
		this.loc = loc;
		this.i18n = i18n;
		this.count = count;
	}
	

	public int getOrdinalState() {
	    return status;
    }

	public String getShortHeaderDescription() {
		String translationLabel = Status.getInternationalizationHeaderLabel(status);
	    return i18n.getString(loc, translationLabel);
    }

	public String getLongHeaderDescription() {
		String translationLabel = Status.getInternationalizationDescriptionLabel(status);
	    return i18n.getString(loc, translationLabel);
    }

	public Long getCount() {
	    return count;
    }
	
}
