
-- Load the metadata from the parsed data, which is JSON strings stored in a gzip-file
captures = LOAD '$input' 
             AS ( key:chararray, value:chararray );


-- Convert the JSON Strings into Pig Map Objects.

captures = FOREACH captures GENERATE key, FROMJSON( value ) AS m:[];

captures = FILTER captures BY m#'errorMessage' is null;
captures = FILTER captures BY ISNT_ROBOTSTXT(m#'url');  
captures = FILTER captures BY FILENOTFOUND(m#'code');


captures = FOREACH captures GENERATE m#'url' AS urlS:chararray, LOWER(m#'url') as url_lower:chararray, 
 m#'outlinks' as links:chararray, LOWER(CONCATTEXT(m)) AS text:chararray, m#'date' AS date:chararray, HOST(urlS) AS hostname:chararray;

--captures = FOREACH captures GENERATE urlS, Combo(urlS, date, text, links, hostname);
--captures = FOREACH captures GENERATE Combo(url, date, text, links, hostname);

-- SHOWSIZE(text), C1(text), C1a(links), 
-- C2(text), C3a(text), C3b(text), C3c(url_lower),
-- C4(text), C5a(text), C5b(text), 
-- C6a(text), C6b(text), C6c(url_lower),
-- C7a(text), C7b(url_lower), C7c(text), C7d(url_lower), C7e(text), C7f(url_lower),
-- C8a(text), C8b(url_lower), C9a(text), C9b(text), C9c(url_lower), C9d(text),
-- C10a(text), C10b(text), C15(HOST(url_lower)), 
-- C17(links); 
--C18(HOST(url_lower));
 
STORE captures INTO '$output' USING PigStorage();
