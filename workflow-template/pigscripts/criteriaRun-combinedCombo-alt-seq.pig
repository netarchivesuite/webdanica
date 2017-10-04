
-- Load the metadata from the parsed data, which is JSON strings stored in a gzip-file
captures = LOAD '$input' USING org.archive.bacon.io.SequenceFileLoader()
             AS ( key:chararray, value:chararray );


-- Convert the JSON Strings into Pig Map Objects.

captures = FOREACH captures GENERATE key, FROMJSON( value ) AS m:[];

captures = FILTER captures BY m#'errorMessage' is null;
captures = FILTER captures BY ISNT_ROBOTSTXT(m#'url');  
captures = FILTER captures BY FILENOTFOUND(m#'code');

--captures = FOREACH captures GENERATE m#'url' as url:chararray, 
-- m#'outlinks' as links:chararray, LOWER(CONCATTEXT(m)) AS text:chararray, m#'date' AS date:chararray, HOST(m#'url') AS hostname:chararray;

captures = FOREACH captures GENERATE m#'url' as url:chararray,
 m#'outlinks' as links:chararray, CONCATTEXT(m) AS text:chararray, m#'date' AS date:chararray, HOST(m#'url') AS hostname:chararray;


--captures = FOREACH captures GENERATE CombinedCombo(url, date, text, links, hostname);
--captures = FOREACH captures GENERATE CombinedComboJsonAlt(url, date, text, links, hostname, true, '/home/test/workflow/Bynavne_Stednavn_JEI_UTF16.txt');
captures = FOREACH captures GENERATE CombinedCombo(url, date, text, links, hostname, true, 
'wordslist/danishMajorCities_UTF8.txt',
'wordslist/DanishNames_UTF8.txt',
'wordslist/foreninger_lowercased_UTF8.txt',
'wordslist/foreninger_one_word_lowercased_UTF8.txt',
'wordslist/placenames_UTF8.txt',
'wordslist/virksomheder_lowercased_UTF8.txt',
'wordslist/virksomheder_one_word_lowercased_UTF8.txt'
);

 
STORE captures INTO '$output' USING PigStorage();
