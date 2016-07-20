
-- Load the metadata from the parsed data, which is JSON strings stored in a Hadoop Sequence file

captures = LOAD '$input' USING org.archive.bacon.io.SequenceFileLoader()
             AS ( key:chararray, value:chararray );

--captures = LOAD '/home/hadoop/IA-WIDE-005-sample/SEQ/WIDE-20120804104316-07354.warc.gz'
--           USING org.archive.bacon.io.SequenceFileLoader()
--           AS ( key:chararray, value:chararray );

DESCRIBE captures;

-- Convert the JSON Strings into Pig Map Objects.

captures = FOREACH captures GENERATE key, FROMJSON( value ) AS m:[];

DESCRIBE captures;
captures = FILTER captures BY m#'errorMessage' is null;

captures = FOREACH captures GENERATE m#'url' AS url:chararray, LOWER(m#'url') as url_lower:chararray, 
 LOWER(m#'boiled') AS boiled, m#'outlinks' as links:chararray;

analysis = FOREACH captures GENERATE url, C1(boiled), C2(boiled), C3a(boiled), C3b(boiled), C3c(url_lower),
C4(boiled), C5a(boiled), C5b(boiled), 
C6a(boiled), C6b(boiled), C6c(url_lower),
C7a(boiled), C7b(url_lower), C7c(boiled), C7d(url_lower), C7e(boiled), C7f(url_lower),
C8a(boiled), C8b(url_lower), C9a(boiled), C9b(boiled), C9c(url_lower),
C10a(boiled), C10b(boiled), C15(HOST(url_lower)), 
--C17(links), 
C18(HOST(url_lower));
-- m#'outlinks' AS links:chararray;
-- m#'digest' as digest:chararray
--	LANGDETECT( m#'boiled' );
--DESCRIBE captures;

DUMP analysis;  



		

