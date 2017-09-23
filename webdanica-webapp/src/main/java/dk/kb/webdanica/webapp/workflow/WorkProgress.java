package dk.kb.webdanica.webapp.workflow;

/**
 * Class used by the WorkThreadAbstract class 
 * to contain progress of the WorkflowWorkThreads.
 */
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
