package dk.kb.webdanica.oldtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;

public class CleanupFrequencyData {
    
    /**
     * @param args
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, ExecutionException, TimeoutException {
        File words150 = new File("korpus/frekvens150.php");
        File adj250 = new File("korpus/frekvens250adj.php");  
        File sub250 = new File("korpus/frekvens250sub.php");
        File verb250 = new File("korpus/frekvens250verb.php");
        /*
        Metadata metadata = new Metadata();
        WriteOutContentHandler handler = new WriteOutContentHandler();
        
        new HtmlParser().parse(
                        HtmlParserTest.class.getResourceAsStream(path),
                        new BoilerpipeContentHandler(handler),  metadata, new ParseContext());
                
                String content = handler.toString();
        */        
        // BoilerPipe! 
        System.out.println(getboiled(words150));
        
        
    }

    transient private static ExecutorService timeoutExecutor = Executors.newSingleThreadExecutor();
    private static <T> T runWithTimeout(long timeout, TimeUnit timeUnit,
            Callable<T> callable) throws InterruptedException, ExecutionException, TimeoutException {
        Future<T> future = timeoutExecutor.submit(callable);
        try {
            return future.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            future.cancel(true);
            try {
                Thread.sleep(2000); // give it 2 seconds to try to cancel
            } catch (InterruptedException f) {
            }
            // We don't actually expect the cancel to work since the parsing
            // library doesn't respond to interrupt. If we try to reuse this
            // executor it will keep timing out waiting for the job ahead of
            // it to finish. Create a new executor instead.
            timeoutExecutor.shutdownNow();
            timeoutExecutor = Executors.newSingleThreadExecutor();
            throw e;
        }
    }

    static String getboiled(File file) throws FileNotFoundException, InterruptedException, ExecutionException, TimeoutException {
        final org.xml.sax.InputSource inputSource = new org.xml.sax.InputSource(
                new FileInputStream(file));
        String boiled = runWithTimeout(60, TimeUnit.SECONDS, new Callable<String>() {
            // dummy var to help in debugging so we can see what url the thing is spinning on
            //private ArchiveRecordProxy rec = record; 

            @Override
            public String call() throws BoilerpipeProcessingException {
                return de.l3s.boilerpipe.extractors.DefaultExtractor.INSTANCE.getText( 
                        inputSource );                                             }

        });
        return boiled;
    }
    
     
        
                
    }

