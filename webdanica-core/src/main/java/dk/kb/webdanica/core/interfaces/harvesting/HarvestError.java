package dk.kb.webdanica.core.interfaces.harvesting;

public class HarvestError {
	private HarvestReport hr;
	private String errMsg;

	public HarvestError(HarvestReport hr, String errMsg) {
		this.hr = hr;
		this.errMsg = errMsg;
	}
	
	public HarvestReport getReport() {
		return this.hr;
	}
	
	public String getError() {
		return this.errMsg;
	}
	
}
