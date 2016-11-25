package dk.kb.webdanica.core.interfaces.harvesting;

public class HarvestError {
	private SingleSeedHarvest hr;
	private String errMsg;

	public HarvestError(SingleSeedHarvest h, String errMsg) {
		this.hr = h;
		this.errMsg = errMsg;
	}
	
	public SingleSeedHarvest getHarvest() {
		return this.hr;
	}
	
	public String getError() {
		return this.errMsg;
	}
	
}
