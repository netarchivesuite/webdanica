package dk.kb.webdanica.criteria;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

//import dk.netarkivet.common.exceptions.IOFailure;

public class LinksBase {

    /** The basedir for the database itself. */
    private File databaseBaseDir;
    
    /** The name of the database. */
    private static final String LINKS_DATABASE_NAME = "LINKS";
    
    /** The name of the class database. */
    private static final String CLASS_DATABASE_NAME = "CLASS";
    
    /** The Database environment. */
    private Environment env;
    /** The Database itself */
    private Database linksDB;
    
    /** The linksbase instance. */
    private static LinksBase instance;
    
    
    /** The class DB. */
    private Database classDB;
    
    /** The Berkeley DB binder for the data object and keyObject in our database, 
      * i.e. Url and Long, respectively. 
      **/
   private EntryBinding objectBinding;
   private EntryBinding keyBinding;

    
    public LinksBase(File databasedir) throws Exception {
        this.databaseBaseDir = databasedir;
        initdatabase();
    }

    private void initdatabase() throws EnvironmentLockedException, DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

        Transaction nullTransaction = null;
        
        env = new Environment(this.databaseBaseDir, envConfig);
        linksDB = env.openDatabase(nullTransaction, LINKS_DATABASE_NAME, dbConfig);
        
        // Open the database that stores your class information.
        classDB = env.openDatabase(nullTransaction, CLASS_DATABASE_NAME, dbConfig);
        StoredClassCatalog classCatalog = new StoredClassCatalog(classDB);
        
        // Create the binding
        objectBinding = new SerialBinding(classCatalog, Long.class);
        keyBinding = new SerialBinding(classCatalog, String.class);
    }

    /**
     * Get Frequency for a given Url
     * @param Url A given URL
     * @return null, if Url not found in database, otherwise the frequency stored with the given Url.
     * @throws Exception 
     */
    public Long getFrequency(String Url) throws Exception {
        Transaction nullTransaction = null;
        LockMode nullLockMode = null;
        DatabaseEntry key = new DatabaseEntry();
        keyBinding.objectToEntry(Url, key);
        DatabaseEntry data = new DatabaseEntry();
        OperationStatus status = null;
        try {
            status = linksDB.get(nullTransaction, key, data, nullLockMode);
        } catch (DatabaseException e) {
            throw new Exception(
                    "Could not retrieve a frequency for the URL '" + Url, e);
        }
        Long urlfrequency = null;
        if (status == OperationStatus.SUCCESS) {
            urlfrequency = (Long) objectBinding.entryToObject(data);
        }
        return urlfrequency;
    }
    
    public boolean hasUrl(String Url) throws Exception {
        return (getFrequency(Url) != null);
    }

    
    /**
     * 
     * @param url A given url related to a frequency
     * @param frequency How often does this url occur
     */
    public void put(String uuid, Long frequency) throws Exception {
        Transaction nullTransaction = null;
        DatabaseEntry theKey = new DatabaseEntry();
        DatabaseEntry theData = new DatabaseEntry(); 
        keyBinding.objectToEntry(uuid, theKey);
        objectBinding.objectToEntry(frequency, theData);

        try {
            linksDB.put(nullTransaction, theKey, theData);
        } catch (DatabaseException e) {
            throw new Exception("Database exception occuring during ingest", e);
        }
    }  

    
    /** 
     * Write the contents of the database to the given file.
     * @param outputFile The outputfile whereto the data is written.
     * @param writeOnlyFilenames If true, we only write the filenames to the files, 
     * not the checksums
     * @throws IOException If unable to write to file for some reason
     */
    private void dumpDatabaseToFile(File tempFile, boolean writeOnlyFilenames, LinksBase oldDb) 
            throws IOException, Exception {
        Cursor cursor = null;
        File resultFile = tempFile;

        FileWriter fw = new FileWriter(resultFile);
        try { 
            cursor = linksDB.openCursor(null, null);

            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
                OperationStatus.SUCCESS) {
                
                while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
                        OperationStatus.SUCCESS) {
                    String keyString = (String) keyBinding.entryToObject(foundKey);
                    Long dataString = (Long) objectBinding.entryToObject(foundData);
                    if (oldDb == null || !oldDb.hasUrl(keyString)) {
                        if (writeOnlyFilenames){
                            //System.out.println("key: " + keyString);
                            fw.append(keyString);
                        } else {
                            fw.append(keyString); fw.append(","); 
                            fw.append(dataString.toString());
                        }
                        fw.append('\n'); // end with newline
                    }
                }
            }
            fw.flush();
        } catch (DatabaseException de) {
            throw new IOException("Error accessing database." + de);
        } finally {
            if (fw != null) {
                IOUtils.closeQuietly(fw);
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                    //log.warn("Database error occurred when closing the cursor: " + e);
                }
            }
        }
    }
    
    
    public File getAllSeeds(LinksBase relativeTo) throws IOException{
        File tempFile = null;
        try {
            tempFile = File.createTempFile("allseeds", "tmp");
        } catch (IOException e) {
            throw new IOException(e.toString());
        }
        
        try {
            dumpDatabaseToFile(tempFile, true, relativeTo);
        } catch (IOException e) {
            throw new IOException("Error during the getAllFilenames operation: " + e);
        } catch (Exception e) {
            throw new IOException("Error during the getAllFilenames operation: " + e);
        }

       
        return tempFile;
    }

    public File getAllSeedsAndTheirFrequencies()  throws IOException{
        File tempFile = null;
        try {
            tempFile = File.createTempFile("allseedsandfrequencies", "tmp");
        } catch (IOException e) {
            throw new IOException(e.toString());
        }
        
        try {
            dumpDatabaseToFile(tempFile, false, null);
        } catch (IOException e) {
            throw new IOException("Error during the operation: " + e);
        } catch (Exception e) {
            throw new IOException("Error during the operation: " + e);
        }
  
        return tempFile;
    }
    
    /**
     * Close the databases and set the instance to null. 
     */
    public void cleanup() {
        if (linksDB != null) {
            try {
                linksDB.close();
            } catch (DatabaseException e) {
                //log.warn("Unable to close links database. The error was :", e);
            }
        }
        if (classDB != null) {
            try {
                classDB.close();
            } catch (DatabaseException e) {
                //log.warn("Unable to close class database. The error was :", e);
            }
        }
    }
    
    public static synchronized LinksBase getInstance() {
        if (instance == null) {
            if (System.getenv(C16.LinkDatabaseHomeKey) != null){
                File basedir = new File(System.getenv(C16.LinkDatabaseHomeKey));
                try {
                    if (!basedir.isDirectory()) {
                        String errMsg = "The basedir '" + basedir.getAbsolutePath() + "' given by the env-key: " + C16.LinkDatabaseHomeKey;
                        System.err.println(errMsg);
                        throw new Exception(errMsg);
                        
                    }
                    instance = new LinksBase(basedir);   
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                
            }
        }
        return instance;
        
    }
    
}
