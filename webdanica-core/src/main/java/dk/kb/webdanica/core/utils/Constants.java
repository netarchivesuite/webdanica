package dk.kb.webdanica.core.utils;

public class Constants {

   public static final String NODATA = "nodata";
   public static final String EMPTYLIST = "emptylist";
   
   public static String getCriteriaName(Object o){
       return o.getClass().getSimpleName();
   }

}
