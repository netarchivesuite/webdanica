package dk.kb.webdanica.core.utils;

public class Constants {

   public static final String NODATA = "nodata";
   public static final String EMPTYLIST = "emptylist";
   /**
    * How big a buffer we use for read()/write() operations on InputStream/ OutputStream.
    */
   public static final int IO_BUFFER_SIZE = 4096;
   
   public static String getCriteriaName(Object o){
       return o.getClass().getSimpleName();
   }

}
