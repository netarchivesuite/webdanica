package dk.kb.webdanica.core.interfaces.harvesting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.UnknownID;
import dk.netarkivet.common.utils.ExceptionUtils;
import dk.netarkivet.harvester.datamodel.DBSpecifics;
import dk.netarkivet.harvester.datamodel.HarvestDBConnection;
import dk.netarkivet.harvester.datamodel.JobStatus;
import dk.netarkivet.harvester.webinterface.HarvestStatusQuery;
import dk.netarkivet.harvester.webinterface.HarvestStatusQuery.SORT_ORDER;

public class HarvestStatusQueryBuilder {

    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(HarvestStatusQueryBuilder.class);

    /**
     * Internal utility class to build a SQL query using a prepared statement.
     */
    /** The sql string. */
    private String sqlString;
    // from java.sql.Types
    /** list of parameter classes. */
    private LinkedList<Class<?>> paramClasses = new LinkedList<Class<?>>();
    /** list of parameter values. */
    private LinkedList<Object> paramValues = new LinkedList<Object>();

    /**
     * Constructor.
     */
    HarvestStatusQueryBuilder() {
        super();
    }

    @Override public String toString() {
        return sqlString;
    }

    /**
     * @param sqlString the sqlString to set
     */
    void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    /**
     * Add the given class and given value to the list of paramClasses and paramValues respectively.
     *
     * @param clazz a given class.
     * @param value a given value
     */
    void addParameter(Class<?> clazz, Object value) {
        paramClasses.addLast(clazz);
        paramValues.addLast(value);
    }

    /**
     * Prepare a statement for the database that uses the sqlString, and the paramClasses, and paramValues. Only
     * Integer, Long, String, and Date values accepted.
     *
     * @param c an Open connection to the harvestDatabase
     * @return the prepared statement
     * @throws SQLException If unable to prepare the statement
     * @throws UnknownID If one of the parameter classes is unexpected
     */
    PreparedStatement getPopulatedStatement(Connection c) throws SQLException {
        PreparedStatement stm = c.prepareStatement(sqlString);

        Iterator<Class<?>> pClasses = paramClasses.iterator();
        Iterator<Object> pValues = paramValues.iterator();
        int pIndex = 0;
        while (pClasses.hasNext()) {
            pIndex++;
            Class<?> pClass = pClasses.next();
            Object pVal = pValues.next();

            if (Integer.class.equals(pClass)) {
                stm.setInt(pIndex, (Integer) pVal);
            } else if (Long.class.equals(pClass)) {
                stm.setLong(pIndex, (Long) pVal);
            } else if (String.class.equals(pClass)) {
                stm.setString(pIndex, (String) pVal);
            } else if (java.sql.Date.class.equals(pClass)) {
                stm.setDate(pIndex, (java.sql.Date) pVal);
            } else {
                throw new UnknownID("Unexpected parameter class " + pClass);
            }
        }
        return stm;
    }

    
    /**
     * Builds a query to fetch jobs according to selection criteria.
     *
     * @param query the selection criteria.
     * @param count build a count query instead of selecting columns.
     * @return the proper SQL query.
     */
    public static HarvestStatusQueryBuilder buildSqlQuery(HarvestStatusQuery query, boolean count) {
        HarvestStatusQueryBuilder sq = new HarvestStatusQueryBuilder();
        
        StringBuffer sql = new StringBuffer("SELECT");
        if (count) {
            sql.append(" count(*)");
        } else {
            sql.append(" jobs.job_id, status, jobs.harvest_id,");
            sql.append(" harvestdefinitions.name, harvest_num,");
            sql.append(" harvest_errors, upload_errors, orderxml,");
            sql.append(" num_configs, submitteddate, creationdate, startdate, enddate,");
            sql.append(" resubmitted_as_job");
        }
        sql.append(" FROM jobs, harvestdefinitions ");
        sql.append(" WHERE harvestdefinitions.harvest_id = jobs.harvest_id ");

        JobStatus[] jobStatuses = query.getSelectedJobStatuses();
        if (jobStatuses.length > 0) {
            if (jobStatuses.length == 1) {
                int statusOrdinal = jobStatuses[0].ordinal();
                sql.append(" AND status = ?");
                sq.addParameter(Integer.class, statusOrdinal);
            } else {
                sql.append("AND (status = ");
                sql.append(jobStatuses[0].ordinal());
                for (int i = 1; i < jobStatuses.length; i++) {
                    sql.append(" OR status = ?");
                    sq.addParameter(Integer.class, jobStatuses[i].ordinal());
                }
                sql.append(")");
            }
        }

        String harvestName = query.getHarvestName();
        boolean caseSensitiveHarvestName = query.getCaseSensitiveHarvestName();
        if (!harvestName.isEmpty()) {
            if (caseSensitiveHarvestName) {
                if (harvestName.indexOf(HarvestStatusQuery.HARVEST_NAME_WILDCARD) == -1) {
                    // No wildcard, exact match
                    sql.append(" AND harvestdefinitions.name = ?");
                    sq.addParameter(String.class, harvestName);
                } else {
                    String harvestNamePattern = harvestName.replaceAll("\\*", "%");
                    sql.append(" AND harvestdefinitions.name LIKE ?");
                    sq.addParameter(String.class, harvestNamePattern);
                }
            } else {
                harvestName = harvestName.toUpperCase();
                if (harvestName.indexOf(HarvestStatusQuery.HARVEST_NAME_WILDCARD) == -1) {
                    // No wildcard, exact match
                    sql.append(" AND UPPER(harvestdefinitions.name) = ?");
                    sq.addParameter(String.class, harvestName);
                } else {
                    String harvestNamePattern = harvestName.replaceAll("\\*", "%");
                    sql.append(" AND UPPER(harvestdefinitions.name)  LIKE ?");
                    sq.addParameter(String.class, harvestNamePattern);
                }
            }
        }

        Long harvestRun = query.getHarvestRunNumber();
        if (harvestRun != null) {
            sql.append(" AND jobs.harvest_num = ?");
            log.debug("Added harvest run number param {}.", harvestRun);
            sq.addParameter(Long.class, harvestRun);
        }

        Long harvestId = query.getHarvestId();
        if (harvestId != null) {
            sql.append(" AND harvestdefinitions.harvest_id = ?");
            log.debug("Added harvest_id param {}.", harvestId);
            sq.addParameter(Long.class, harvestId);
        }

        long startDate = query.getStartDate();
        if (startDate != HarvestStatusQuery.DATE_NONE) {
            sql.append(" AND startdate >= ?");
            sq.addParameter(java.sql.Date.class, new java.sql.Date(startDate));
        }

        long endDate = query.getEndDate();
        if (endDate != HarvestStatusQuery.DATE_NONE) {
            sql.append(" AND enddate < ?");
            // end date must be set +1 day at midnight
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(endDate);
            cal.roll(Calendar.DAY_OF_YEAR, 1);
            sq.addParameter(java.sql.Date.class, new java.sql.Date(cal.getTimeInMillis()));
        }
        
        List<String> jobIdRangeIds = query.getPartialJobIdRangeAsList(false);
        List<String> jobIdRanges = query.getPartialJobIdRangeAsList(true);
        if (!jobIdRangeIds.isEmpty()) {
            String comma = "";
            sql.append(" AND (jobs.job_id IN (");
            for(String id : jobIdRangeIds) {
                //id
                sql.append(comma);
                comma = ",";
                sql.append("?");
                sq.addParameter(Long.class, Long.parseLong(id));
            }
            sql.append(") ");

            
        }
        if(!jobIdRanges.isEmpty()) {
            String andOr = "AND";
            if (!jobIdRangeIds.isEmpty()) {
                andOr = "OR";
            }
            
            for(String range : jobIdRanges) {
                String[] r = range.split("-");
                sql.append(" "+andOr+" jobs.job_id BETWEEN ? AND ? ");
                sq.addParameter(Long.class, Long.parseLong(r[0]));
                sq.addParameter(Long.class, Long.parseLong(r[1]));
            }
        }
        if (!jobIdRangeIds.isEmpty()) {
            sql.append(")");
        }

        if (!count) {
            sql.append(" ORDER BY jobs.job_id");
            if (!query.isSortAscending()) {
                sql.append(" " + SORT_ORDER.DESC.name());
            } else {
                sql.append(" " + SORT_ORDER.ASC.name());
            }

            long pagesize = query.getPageSize();
            if (pagesize != HarvestStatusQuery.PAGE_SIZE_NONE) {
                sql.append(" "
                        + DBSpecifics.getInstance().getOrderByLimitAndOffsetSubClause(pagesize,
                                (query.getStartPageIndex() - 1) * pagesize));
            }
        }

        sq.setSqlString(sql.toString());
        System.out.println("sql-query: " + sql.toString());
        return sq;
    }
    
    public static void getStatusInfo(HarvestStatusQuery query) {
        log.debug("Constructing Harveststatus based on given query.");
        PreparedStatement s = null;
        Connection c = HarvestDBConnection.get();

        try {
            // Obtain total count without limit
            // NB this will be a performance bottleneck if the table gets big
            long totalRowsCount = 0;

            final HarvestStatusQueryBuilder harvestStatusQueryBuilder = HarvestStatusQueryBuilder.buildSqlQuery(query, true);
            log.debug("Unpopulated query is {}.", harvestStatusQueryBuilder);
            s = harvestStatusQueryBuilder.getPopulatedStatement(c);
            log.debug("Query is {}.", s);
            ResultSet res = s.executeQuery();
            res.next();
            totalRowsCount = res.getLong(1);

            s = HarvestStatusQueryBuilder.buildSqlQuery(query, false).getPopulatedStatement(c);
            res = s.executeQuery();
            describeResults(res);
            //List<JobStatusInfo> jobs = makeJobStatusInfoListFromResultset(res);

            log.debug("Harveststatus constructed based on given query.");
            //return new HarvestStatus(totalRowsCount, jobs);
        } catch (SQLException e) {
            String message = "SQL error asking for job status list in database" + "\n"
                    + ExceptionUtils.getSQLExceptionCause(e);
            log.warn(message, e);
            throw new IOFailure(message, e);
        } finally {
            HarvestDBConnection.release(c);
        }
    }

    private static void describeResults(ResultSet res) throws SQLException {
        while (res.next()) {
            System.out.println("JobId = " + res.getLong(1));
            JobStatus status = JobStatus.fromOrdinal(res.getInt(2));
            long harvestDefinitionID = res.getLong(3);
            String harvestDefinition = res.getString(4);
            System.out.println("jobstatus = " + status);
            System.out.println("harvestdefinitionId = " + harvestDefinitionID);
            System.out.println("harvestDefinition = " + harvestDefinition);
        }
    }

    
//    private List<JobStatusInfo> makeJobStatusInfoListFromResultset(ResultSet res) throws SQLException {
//        List<JobStatusInfo> joblist = new ArrayList<JobStatusInfo>();
//        while (res.next()) {
//            final long jobId = res.getLong(1);
    // JobStatusInfo(long jobID, JobStatus status, long harvestDefinitionID, String harvestDefinition, int harvestNum,
    // String harvestErrors, String uploadErrors, String orderXMLname, int domainCount, Date submittedDate,
    // Date creationDate, Date startDate, Date endDate, Long resubmittedAsJobWithID)
    
//            joblist.add(new JobStatusInfo(jobId, JobStatus.fromOrdinal(res.getInt(2)), res.getLong(3),
//                    res.getString(4), res.getInt(5), res.getString(6), res.getString(7), res.getString(8), res
//                            .getInt(9), DBUtils.getDateMaybeNull(res, 10), DBUtils.getDateMaybeNull(res, 11), DBUtils
//                            .getDateMaybeNull(res, 12), DBUtils.getDateMaybeNull(res, 13), DBUtils.getLongMaybeNull(
//                            res, 14)));
//        }
//        return joblist;
//    }

    

}
