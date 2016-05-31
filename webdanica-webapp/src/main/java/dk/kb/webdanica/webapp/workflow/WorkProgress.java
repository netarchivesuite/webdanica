/*
 * Created on 26/09/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp.workflow;

public class WorkProgress {

	public String threadName;

	public long started = System.currentTimeMillis();

	public long stopped;

	public boolean bFailed;

	public int item;

	public int items;

	public WorkProgress(String threadName, int items) {
		this.threadName = threadName;
		this.items = items;
	}

}
