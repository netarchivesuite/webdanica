package dk.kb.webdanica.core.interfaces.harvesting;

import dk.netarkivet.harvester.datamodel.JobStatus;

public class NasJob {
    
    private Long jobId;
    private JobStatus status;
    private Long harvestId;

    public NasJob(Long jobId, JobStatus status, Long harvestId) {
        this.jobId = jobId;
        this.status = status;
        this.harvestId = harvestId;
    }

    public Long getJobId() {
        return jobId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Long getHarvestId() {
        return harvestId;
    }
    
    public String toString() {
        return "job w/jobId=" + getJobId() + ", status=" + getStatus() + ", harvestId=" + getHarvestId();
    }

}

