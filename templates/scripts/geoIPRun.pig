
-- Load the metadata from the parsed data, which is JSON strings stored in a Hadoop Sequence file

DEFINE ISNT_ROBOTSTXT 	dk.kb.webdanica.criteria.IsNotRobotsTxt();

captures = LOAD '$input' USING org.archive.bacon.io.SequenceFileLoader()
             AS ( key:chararray, value:chararray );

-- Convert the JSON Strings into Pig Map Objects.

captures = FOREACH captures GENERATE key, FROMJSON( value ) AS m:[];

--DESCRIBE captures;
captures = FILTER captures BY m#'errorMessage' is null;
captures = FILTER captures BY ISNT_ROBOTSTXT(m#'url');  
captures = FILTER captures BY FILENOTFOUND(m#'code');

captures = FOREACH captures GENERATE m#'url' AS url:chararray, LOWER(m#'url') as url_lower:chararray, 
 m#'outlinks' as links:chararray, LOWER(CONCATTEXT(m)) AS text:chararray;


captures = FOREACH captures GENERATE url, SHOWSIZE(text), C18(HOST(url_lower));

DUMP captures; 

