package dk.kb.webdanica.core.criteria;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import dk.kb.webdanica.core.utils.TextUtils;


/**
 * List of frequent words used to investigate potential danish websites.
 *
 */
public class Words {
    /** List taken from korpus website. */
    public static String[] frequent150words = new String[] {
            "og", "i", "at", "det", "er", "en", "til", "af", "på", "for", 
            "der", "den", "med", "de", "ikke", "som", "har", "et", "jeg", "om", 
            "var", "så", "han", "men", "kan", "vi", "fra", "sig", "man", "skal", 
            "ved", "vil", "også", "hun", "eller", "være", "blev", "havde", "efter", "over", 
            "hvor", "ud", "da", "nu", "du", "år", "når", "op", "kunne", "selv", 
            "meget", "hvis", "sin", "alle", "noget", "siger", "to", "mange", "dem", "hvad", 
            "bliver", "få", "mig", "mere", "her", "hans", "have", "deres", "andre", "ind", 
            "godt", "kun", "må", "end", "været", "ham", "mod", "skulle", "jo", "under", 
            "går", "denne", "helt", "store", "ville", "dag", "hele", "blive", "kr", "får", 
            "sammen", "os", "lidt", "sagde", "uden", "kommer", "nogle", "første", "fik", 
            "alt", 
            "se", "mellem", "danske", "min", "lige", "kom", "gang", "fordi", "flere", 
            "nok", "nye", "siden",  "ingen", "dette", "derfor", "anden", "blevet", "danmark", 
            "sidste", "før", "tid", "samme", "sådan", "måske", "tre", "ned", "igen", "sit", 
            "andet", "både", "gøre", "hos", "lille", "ny", "bare", "gå", "gør", "sine", 
            "gik", "ja", "tilbage", "mens", "stor", "komme", "sige"
    };
    //6d
    public static String[] frequent150wordsNov = new String[] {
        "og", "det", "er", "en", "til", "af", "på", 
        "der", "den", "med", "de", "ikke", "som", "har", "et", "jeg", "om", 
        "var", "så", "han", "kan", "vi", "fra", "sig", "skal", 
        "ved", "vil", "også", "hun", "eller", "være", "blev", "havde", "efter", 
        "hvor", "ud", "da", "nu", "du", "år", "når", "kunne", "selv", 
        "meget", "hvis",  "alle", "noget", "siger", "mange", "dem", "hvad", 
        "bliver", "få", "mig", "mere", "her", "hans", "deres", "andre", "ind", 
        "godt", "kun", "må", "været", "ham", "skulle", "jo", 
        "går", "denne", "helt", "ville", "dag", "hele", "blive", "kr", "får", 
        "sammen", "os", "lidt", "sagde", "uden", "kommer", "nogle", "første", "fik", 
        "alt", 
        "mellem", "danske", "lige", "kom", "gang", "fordi", "flere", 
        "nok", "nye", "siden", "ingen", "dette", "derfor", "anden", "blevet", "danmark", 
        "sidste", "før", "tid", "samme", "sådan", "måske", "tre", "ned", "igen", 
        "andet", "både", "gøre", "hos", "lille", "ny", "bare", "gå", "gør", "sine", 
        "gik", "ja", "tilbage", "mens", "stor", "komme", "sige"
};
    // C3b:
    // Coded means på => paa, være => vaere
    // ø => oe/o
    // Contents merged from frequent150words,frequent250adjs,frequent250subs, frequent250verbs
    public static String[] frequentwordsWithDanishLettersCoded = new String[] {
        "paa", "saa", "ogsaa", "vaere", "aar", "naar",  
         "faa","maa", "vaeret", "gaar", "faar", 
        "forste", "foerste", "for","foer", "saadan", 
        "maaske", "baade", "gore", "goere", "gaa", "goer", "gor",
        
        // Adjektiver
        "forst", "foerst",
        "hoj","hoej", "staerk", "naeste", "oekonomisk","okonomisk", "oevrig", "ovrig", "svaer", 
        
        "naer", "saerlig",  "haard", "daarlig", "roed","rod", "taet", 
        "noedvendig","nodvendig",  "saadan", "faa",
        
        "aaben", "faelles",  "europaeisk", 
        "saakaldt", "spaendende",  
         "faerdig", "vaesentlig",  
        "afgoerende","afgorende", "nuvaerende","dod", "gron","doed", "groen", 
        "sjaelden", "paen", 
         
        "blaa", "noedt", "bloed","nodt", "blod", 
        "foreloebig", "forelobig","fuldstaendig", 
        "opmaerksom", "hoejre","hojre",  
        "soed","sod", "militaer",  
        "populaer", "aabenbar", "traet", 
        "aarlig",  
        "selvstaendig",  "vaerd",  
         "maerkelig",  
        "los", "overst","loes", "oeverst",  
        "berømt", "paagaeldende",  
        "udmaerket", "aegte",  
        "tilstraekkelig", "vaerre",  
        "tor","toer",  
        "moerk", "moerk","afhaengig",  "usaedvanlig", 
        // navneord
        "aar",  "kobenhavn","koebenhavn",  
        "maade",  "maaned", "oeje", "oje", "haand", "omraade", 
        "spoergsmaal", "raekke", "loeb","lob", 
        "hjaelp", "tilfaelde", "ojeblik", "mode","oejeblik", "moede", 
        "son", "jorgen", "soen", "joergen","foraeldre", "laege", 
          
        "maal",  "aarhus",  "raad", "direktoer", "direktor", "forsoeg", "doer","forsog", "dor",  
        "soendag", "soeren","sondag", "soren", "kaerlighed", "undersoegelse","undersogelse",   
        "loerdag", "oekonomi", "lordag", "okonomi","gengaeld",  
        "praesident", "stoette", "loesning","moeller", "stotte", "losning","moller","glaede", 
        
        // Verber
        "vaere", "faa", "gaa", 
        "goe", "go", "maatte", "staa", "saette",  
        "fortaelle", "hoere","hore", "laegge", 
        "foelge", "koere", "folge", "kore","taenke", 
        "oenske","onske", "slaa", "foere", "spoerge","fore", "sporge",  
        "vaelge", "foele","fole", "traekke", "forstaa", "gaelde", 
        "naa", "proeve", "koebe", "prove", "kobe", "laese",  
        "saelge", "hjaelpe", "moede", "forsoege","mode", "forsoge", "laere", "fortsaette", 
        "kraeve", "soege","soge", "taelle",  
        "maerke", "aendre", 
        "naevne", "bestaa", "baere", 
        "haenge", "loebe","lobe",  
        "haabe", "foregaa", "loese","lose", 
        "gennemfoere","gennemfore", "aabne",  
        "daekke", "oege","oge", "undgaa", "opstaa", 
        "indgaa",  "opnaa", "foede","fode", "raabe", "doe", "behoeve","do", "behove", 
        "foreslaa", "traede", "soerge","sorge",  
        "stoette", "toe","stotte", "to", "kaempe", 
        "draebe", "undersoege","undersoge", "glaede",  
        "indfoere", "besoege","indfore", "besoge", 
        "loefte", "lofte", "praesentere", "ødelaegge",  
        "braende", "praege", "udfoere", "udfore", "udsaette",  
        "bevaege", "udgoere", "moedes","udgore", "modes", "traenge", 
        "afsloere", "medfoere","afslore", "medfore", "begraense",         
};
    //3g minus for
    public static String[] frequentwordsWithDanishLettersCodedNov = new String[] {
        "paa", "saa", "ogsaa", "vaere", "aar", "naar",  
         "faa","maa", "vaeret", "gaar", "faar", 
        "forste", "foerste", "foer", "saadan", 
        "maaske", "baade", "gore", "goere", "gaa", "goer", "gor",
        
        // Adjektiver
        "forst", "foerst",
        "hoj","hoej", "staerk", "naeste", "oekonomisk","okonomisk", "oevrig", "ovrig", "svaer", 
        
        "naer", "saerlig",  "haard", "daarlig", "roed","rod", "taet", 
        "noedvendig","nodvendig",  "saadan", "faa",
        
        "aaben", "faelles",  "europaeisk", 
        "saakaldt", "spaendende",  
         "faerdig", "vaesentlig",  
        "afgoerende","afgorende", "nuvaerende","dod", "gron","doed", "groen", 
        "sjaelden", "paen", 
         
        "blaa", "noedt", "bloed","nodt", "blod", 
        "foreloebig", "forelobig","fuldstaendig", 
        "opmaerksom", "hoejre","hojre",  
        "soed","sod", "militaer",  
        "populaer", "aabenbar", "traet", 
        "aarlig",  
        "selvstaendig",  "vaerd",  
         "maerkelig",  
        "los", "overst","loes", "oeverst",  
        "berømt", "paagaeldende",  
        "udmaerket", "aegte",  
        "tilstraekkelig", "vaerre",  
        "tor","toer",  
        "moerk", "moerk","afhaengig",  "usaedvanlig", 
        // navneord
        "aar",  "kobenhavn","koebenhavn",  
        "maade",  "maaned", "oeje", "oje", "haand", "omraade", 
        "spoergsmaal", "raekke", "loeb","lob", 
        "hjaelp", "tilfaelde", "ojeblik", "mode","oejeblik", "moede", 
        "son", "jorgen", "soen", "joergen","foraeldre", "laege", 
          
        "maal",  "aarhus",  "raad", "direktoer", "direktor", "forsoeg", "doer","forsog", "dor",  
        "soendag", "soeren","sondag", "soren", "kaerlighed", "undersoegelse","undersogelse",   
        "loerdag", "oekonomi", "lordag", "okonomi","gengaeld",  
        "praesident", "stoette", "loesning","moeller", "stotte", "losning","moller","glaede", 
        
        // Verber
        "vaere", "faa", "gaa", 
        "goe", "go", "maatte", "staa", "saette",  
        "fortaelle", "hoere","hore", "laegge", 
        "foelge", "koere", "folge", "kore","taenke", 
        "oenske","onske", "slaa", "foere", "spoerge","fore", "sporge",  
        "vaelge", "foele","fole", "traekke", "forstaa", "gaelde", 
        "naa", "proeve", "koebe", "prove", "kobe", "laese",  
        "saelge", "hjaelpe", "moede", "forsoege","mode", "forsoge", "laere", "fortsaette", 
        "kraeve", "soege","soge", "taelle",  
        "maerke", "aendre", 
        "naevne", "bestaa", "baere", 
        "haenge", "loebe","lobe",  
        "haabe", "foregaa", "loese","lose", 
        "gennemfoere","gennemfore", "aabne",  
        "daekke", "oege","oge", "undgaa", "opstaa", 
        "indgaa",  "opnaa", "foede","fode", "raabe", "doe", "behoeve","do", "behove", 
        "foreslaa", "traede", "soerge","sorge",  
        "stoette", "toe","stotte", "to", "kaempe", 
        "draebe", "undersoege","undersoge", "glaede",  
        "indfoere", "besoege","indfore", "besoge", 
        "loefte", "lofte", "praesentere", "ødelaegge",  
        "braende", "praege", "udfoere", "udfore", "udsaette",  
        "bevaege", "udgoere", "moedes","udgore", "modes", "traenge", 
        "afsloere", "medfoere","afslore", "medfore", "begraense",         
    };
    public final static Set<WordPattern> patternsFrequentwordsWithDanishLettersCodedNov = WordPattern.getCompiledPatterns(frequentwordsWithDanishLettersCodedNov);
    public final static Set<WordPattern> patternsFrequentwordsWithDanishLettersCodedNovNoCase = WordPattern.getCompiledPatternsNoCase(frequentwordsWithDanishLettersCodedNov);

    // C3e+f: NEW FIELDS - restricted version of above
    // Coded means på => paa, være => vaere
    // ø => oe/o
    // Contents merged from frequent150words,frequent250adjs,frequent250subs, frequent250verbs
    public static String[] frequentwordsWithDanishLettersCodedNew = new String[] {
        "paa", "saa", "ogsaa", "vaere", "aar", "naar",  
         "faa","maa", "vaeret", "gaar", "faar", 
        "forste", "foerste", "foer", "saadan", 
        "maaske", "baade", "goere", "gaa", 
        // Adjektiver
        "forst", "foerst",
        "hoj","hoej", "staerk", "naeste", "oekonomisk","okonomisk", "oevrig", "ovrig", "svaer", 
        
        "naer", "saerlig",  "haard", "daarlig", "roed","taet", 
        "noedvendig","nodvendig",  "saadan", "faa",
        
        "aaben", "faelles",  "europaeisk", 
        "saakaldt", "spaendende",  
         "faerdig", "vaesentlig",  
        "afgoerende","afgorende", "nuvaerende","gron","doed", "groen", 
        "sjaelden", "paen", 
         
        "blaa", "noedt", "bloed","nodt", "blod", 
        "foreloebig", "forelobig","fuldstaendig", 
        "opmaerksom", "hoejre","hojre",  
        "soed","militaer",  
        "populaer", "aabenbar", "traet", 
        "aarlig",  
        "selvstaendig",  "vaerd",  
         "maerkelig",  
        "overst","loes", "oeverst",  
        "beroemt", "paagaeldende",  
        "udmaerket", "aegte",  
        "tilstraekkelig", "vaerre",  
        "moerk","afhaengig",  "usaedvanlig", 
        // navneord
        "aar",  "kobenhavn","koebenhavn",  
        "maade",  "maaned", "oeje", "oje", "haand", "omraade", 
        "spoergsmaal", "raekke", "loeb", "hjaelp", "tilfaelde", "ojeblik", "oejeblik", "moede", //eld exluded "mode" 
        "jorgen", "soen", "joergen","foraeldre", "laege", 
          
        "maal",  "aarhus",  "raad", "direktoer", "direktor", "forsoeg", "forsog", 
        "soendag", "soeren","sondag", "soren", "kaerlighed", "undersoegelse","undersogelse",   
        "loerdag", "oekonomi", "lordag", "okonomi","gengaeld",  
        "praesident", "stoette", "loesning","moeller", "stotte", "losning","moller","glaede", 
        
        // Verber
        "vaere", "faa", "gaa", 
        "maatte", "staa", "saette",  
        "fortaelle", "hoere","laegge", 
        "foelge", "koere", "folge", "kore","taenke", 
        "oenske","onske", "slaa", "foere", "spoerge","fore", "sporge",  
        "vaelge", "foele","fole", "traekke", "forstaa", "gaelde", 
        "naa", "proeve", "koebe", "kobe", "laese",  
        "saelge", "hjaelpe", "moede", "forsoege","forsoge", "laere", "fortsaette", 
        "kraeve", "soege","soge", "taelle",  
        "maerke", "aendre", 
        "naevne", "bestaa", "baere", 
        "haenge", "loebe", "haabe", "foregaa", "loese",
        "gennemfoere","gennemfore", "aabne",  
        "daekke", "oege","undgaa", "opstaa", 
        "indgaa",  "opnaa", "foede","fode", "raabe", "behoeve", "behove", 
        "foreslaa", "traede", "soerge","sorge",  
        "stoette", "stotte", "kaempe", 
        "draebe", "undersoege","undersoge", "glaede",  
        "indfoere", "besoege","indfore", "besoge", 
        "loefte", "lofte", "praesentere", "ødelaegge",  
        "braende", "praege", "udfoere", "udfore", "udsaette",  
        "bevaege", "udgoere", "moedes","udgore", "traenge", 
        "afsloere", "medfoere","afslore", "medfore", "begraense",         
};
    
    
    /** List taken from korpus website. */
    public static final String[] frequent250adjs = new String[] {
            "meget", "god", "mange", "stor", "hel", "ny", "lille", "dansk", "lidt", "først", 
            "lang", "gammel", "sidst", "samme", "egen", "sen", "høj", "ung", "tidlig", "rigtig", 
            "klar", "forskellig", "enkelt", "politisk", "hurtig", "kort", "stærk", "næste", "vigtig", "økonomisk", 
            "øvrig", "mulig", "svær", "fast", "nær", "let", "særlig", "faktisk", "ene", "eneste", 
            "amerikansk", "fri", "ren", "sikker", "fuld", "offentlig", "hård", "halv", "dårlig", "sort", 
            "rød", "tæt", "almindelig", "fin", "nødvendig", "international", "vis", "hvid", "sådan", "få", 
            "åben", "tysk", "glad", "direkte", "flot", "smuk", "speciel", "dyb", "privat", "fælles", 
            "kendt", "død", "bestemt", "grøn", "normal", "social", "enig", "fransk", "europæisk", "alvorlig", 
            "såkaldt", "varm", "personlig", "lokal", "bred", "kold", "svensk", "lav", "spændende", "ekstra", 
            "kraftig", "naturlig", "utrolig", "daglig", "yderlig", "indre", "dejlig", "færdig", "væsentlig", "ond", 
            "afgørende", "positiv", "voksen", "billig", "konservativ", "gal", "bange", "moderne", "frisk", "nuværende", 
            "voldsom", "rig", "eventuel", "nem", "tung", "sjælden", "radikal", "selve", "teknisk", "pæn", 
            "dyr", "anderledes", "tydelig", "slem", "vanskelig", "tilsyneladende", "nordisk", "tredje", "aktiv", "umiddelbar", 
            "blå", "fremmest", "rimelig", "forkert", "nødt", "langsom", "svag", "blød", "betydelig", "syg", 
            "farlig", "formentlig", "vild", "total", "foreløbig", "umulig", "levende", "absolut", "fremmed", "fuldstændig", 
            "venstre", "effektiv", "tynd", "sjov", "opmærksom", "rolig", "engelsk", "højre", "professionel", "praktisk", 
            "interessant", "reel", "konkret", "modsat", "historisk", "grov", "sød", "generel", "militær", "tilsvarende", 
            "udenlandsk", "national", "borgerlig", "britisk", "populær", "åbenbar", "gul", "oprindelig", "yderst", "træt", 
            "årlig", "aktuel", "vred", "traditionel", "menneskelig", "skarp", "central", "parat", "tyk", "egentlig", 
            "kvindelig", "sovjetisk", "selvstændig", "fysisk", "officiel", "tilfreds", "værd", "adskillig", "typisk", "enorm", 
            "norsk", "simpel", "gift", "socialdemokratisk", "lykkelig", "italiensk", "klassisk", "fantastisk", "mærkelig", "sund", 
            "løs", "samtlige", "venlig", "øverst", "bekendt", "vestlig", "fornuftig", "tom", "klog", "demokratisk", 
            "berømt", "dygtig", "ordentlig", "russisk", "nylig", "endelig", "pågældende", "gratis", "japansk", "evig", 
            "kommunal", "fattig", "forleden", "travl", "seksuel", "kongelig", "dobbelt", "udmærket", "ægte", "bevidst", 
            "tilstrækkelig", "forsigtig", "værre", "ret", "virkelig", "velkommen", "grundig", "tør", "psykisk", "ukendt", 
            "mørk", "omfattende", "afhængig", "rar", "frivillig", "usædvanlig", "hellig", "kulturel" 

    };
    //6d
    public static final String[] frequent250adjsNov = new String[] {
        "meget", "mange", "stor", "hel", "ny", "lille", "dansk", "lidt", "først", 
        "lang", "gammel", "sidst", "samme", "egen", "sen", "høj", "ung", "tidlig", "rigtig", 
        "klar", "forskellig", "enkelt", "politisk", "hurtig", "kort", "stærk", "næste", "vigtig", "økonomisk", 
        "øvrig", "mulig", "svær", "fast", "nær", "let", "særlig", "faktisk", "ene", "eneste", 
        "amerikansk", "fri", "ren", "sikker", "fuld", "offentlig", "hård", "halv", "dårlig",
        "rød", "tæt", "almindelig", "fin", "nødvendig", "vis", "hvid", "sådan", "få", 
        "åben", "tysk", "direkte", "flot", "smuk", "speciel", "dyb", "privat", "fælles", 
        "kendt", "død", "bestemt", "grøn", "normal", "social", "enig", "fransk", "europæisk", "alvorlig", 
        "såkaldt", "varm", "personlig", "lokal", "bred", "kold", "svensk", "lav", "spændende", "ekstra", 
        "kraftig", "naturlig", "utrolig", "daglig", "yderlig", "indre", "dejlig", "færdig", "væsentlig", "ond", 
        "afgørende", "positiv", "voksen", "billig", "konservativ", "gal", "bange", "moderne", "frisk", "nuværende", 
        "voldsom", "rig", "eventuel", "nem", "tung", "sjælden", "radikal", "selve", "teknisk", "pæn", 
        "dyr", "anderledes", "tydelig", "slem", "vanskelig", "tilsyneladende", "nordisk", "tredje", "aktiv", "umiddelbar", 
        "blå", "fremmest", "rimelig", "forkert", "nødt", "langsom", "svag", "blød", "betydelig", "syg", 
        "farlig", "formentlig", "vild", "foreløbig", "umulig", "levende", "absolut", "fremmed", "fuldstændig", 
        "venstre", "effektiv", "tynd", "sjov", "opmærksom", "rolig", "engelsk", "højre", "praktisk", 
        "interessant", "reel", "konkret", "modsat", "historisk", "grov", "sød", "generel", "militær", "tilsvarende", 
        "udenlandsk", "national", "borgerlig", "britisk", "populær", "åbenbar", "gul", "oprindelig", "yderst", "træt", 
        "årlig", "aktuel", "vred", "menneskelig", "skarp", "parat", "tyk", "egentlig", 
        "kvindelig", "sovjetisk", "selvstændig", "fysisk", "tilfreds", "værd", "adskillig", "typisk", "enorm", 
        "norsk", "gift", "socialdemokratisk", "lykkelig", "italiensk", "klassisk", "fantastisk", "mærkelig", "sund", 
        "løs", "samtlige", "venlig", "øverst", "bekendt", "vestlig", "fornuftig", "klog", "demokratisk", 
        "berømt", "dygtig", "ordentlig", "russisk", "nylig", "endelig", "pågældende", "gratis", "japansk", "evig", 
        "kommunal", "fattig", "forleden", "travl", "seksuel", "kongelig", "dobbelt", "udmærket", "ægte", "bevidst", 
        "tilstrækkelig", "forsigtig", "værre", "ret", "virkelig", "velkommen", "grundig", "tør", "psykisk", "ukendt", 
        "mørk", "omfattende", "afhængig", "rar", "frivillig", "usædvanlig", "hellig", "kulturel" 

    };

    /** List taken from korpus website. */
    public static final String[] frequent250subs = new String[] {
            "år", "dag", "krone", "kr", "tid", "gang", "danmark", "mand", "barn", "land", "sted", 
            "menneske", "side", "del", "liv", "eksempel", "københavn", "verden", "folk", "problem", "vej", 
            "sag", "kvinde", "million", "uge", "arbejde", "måde", "par", "penge", "forhold", "mulighed", 
            "procent", "grund", "by", "regering", "ting", "måned", "kommune", "time", "hus", "foto", 
            "bil", "øje", "ord", "bog", "hånd", "herr(e)", "område", "mor", "vand", "plads", 
            "familie", "spørgsmål", "pige", "aften", "række", "fader", "billede", "løb", "fald", "politi", 
            "historie", "gud", "film", "tale", "peter", "ret", "nummer", "parti", "forbindelse", "skole", 
            "hjælp", "kamp", "gruppe", "udvikling", "tv", "forslag", "stykke", "medlem", "virksomhed", "form", 
            "situation", "dansker", "usa", "navn", "erik", "nielsen", "slags", "person", "brug", "resultat", 
            "lov", "tilfælde", "minut", "regel", "øjeblik", "formand", "møde", "folketing", "jensen", "ide", 
            "opgave", "søn", "jørgen", "ven", "hansen", "krav", "musik", "forældre", "samarbejde", "læge", 
            "morgen", "plan", "nat", "stat", "lars", "leder", "lys", "poul", "krig", "samfund", 
            "mål", "dreng", "jens", "jord(en)", "ole", "europa", "teater", "fremtid", "svar", "masse", 
            "kilometer", "hovede", "lejlighed", "politiker", "kraft", "kilo(gram)", "prise", "andersen", 
            "interesse", "socialdemokrati", 
            "virkelighed", "mad", "ef", "per", "århus", "krop", "niels", "tidspunkt", "bord", "stemme", 
            "rolle", "grad", "råd", "nej", "direktør", "befolkning", "mening", "forsøg", "dør", "henrik", 
            "søndag", "antal", "klasse", "forfatter", "valg", "rest", "søren", "dyr", "michael", "udtryk", 
            "kærlighed", "grænse", "periode", "hensyn", "undersøgelse", "elev", "ben", "oplysning", "centimeter", "han", 
            "tur", "hold", "selskab", "john", "behov", "behandling", "projekt", "aftale", "blad", "debat", 
            "lørdag", "kone", "økonomi", "sommer", "gengæld", "stof", "magt", "kirke", "jan", "tvivl", 
            "tyskland", "lyst", "meter", "beslutning", "program", "avis", "system", "larsen", "marked", "præsident", 
            "ansigt", "betydning", "job", "skyld", "luft", "sygdom", "natur", "linje", "politik", "baggrund", 
            "gade", "konge", "medarbejder", "flertal", "sf", "brev", "uddannelse", "chance", "sprog", "datter", 
            "sol", "sang", "støtte", "hund", "kunst", "art", "tal", "løsning", "stilling", "ansvar", 
            "møller", "stand", "fredag", "kunde", "svend", "fordel", "holdning", "mandag", "start", "glæde", 
            "oplevelse", "have", "pris", "firma", "rasmussen", "pedersen", "sverige", "scene", "radio", "vare", 
            "spil", "ende"
    };
    //6d and names
    public static final String[] frequent250subsNov = new String[] {
        "år", "dag", "krone", "kr", "tid", "danmark", "mand", "barn", "sted", 
        "menneske", "side", "liv", "eksempel", "københavn", "verden","vej", 
        "sag", "kvinde",  "uge", "arbejde", "måde",  "penge", "forhold", "mulighed", 
        "procent", "grund", "regering", "ting", "måned", "kommune", "time", "hus",
        "bil", "øje", "ord", "bog", "hånd", "herr(e)", "område", "mor", "vand", "plads", 
        "spørgsmål", "pige", "aften", "række", "billede", "løb", "fald", "politi", 
        "historie", "gud", "tale", "ret", "nummer", "parti", "forbindelse", "skole", 
        "hjælp", "kamp", "gruppe", "udvikling", "forslag", "stykke", "medlem", "virksomhed", "form", 
        "situation", "dansker", "navn", "slags", "person", "brug", "resultat", 
        "lov", "tilfælde", "minut", "regel", "øjeblik", "formand", "møde", "folketing", "ide", 
        "opgave", "søn", "jørgen", "ven", "hansen", "krav", "musik", "forældre", "samarbejde", "læge", 
        "leder", "lys", "krig", "samfund", 
        "mål", "dreng","jord(en)", "teater", "fremtid", "svar",
        "hovede", "lejlighed", "kraft", "kilo(gram)",
        "interesse", "socialdemokrati", 
        "virkelighed", "ef", "århus", "krop", "tidspunkt", "bord", "stemme", 
        "rolle", "råd", "nej", "direktør", "befolkning", "mening", "forsøg", "dør", 
        "søndag", "antal", "klasse", "forfatter", "valg", "rest", "dyr", "udtryk", 
        "kærlighed", "grænse", "periode", "hensyn", "undersøgelse", "elev", "oplysning", "han", 
        "tur", "selskab", "john", "behov", "behandling", "projekt", "aftale", "blad", 
        "lørdag", "kone", "økonomi", "sommer", "gengæld", "stof", "magt", "kirke", "tvivl", 
        "tyskland", "lyst", "beslutning", "marked", "præsident", 
        "ansigt", "betydning", "skyld", "luft", "sygdom", "linje", "baggrund", 
        "gade", "konge", "medarbejder", "flertal", "brev", "uddannelse", "chance", "sprog", "datter", 
        "sang", "støtte", "tal", "løsning", "stilling", "ansvar", 
        "møller", "fredag", "kunde", "svend", "fordel", "holdning", "mandag", "start", "glæde", 
        "oplevelse", "pris", "vare", "spil"
    };
    
    /** List taken from korpus website. */
    public static String[] frequent250verbs = new String[] {
            "være", "have", "kunne", "blive", "skulle", "ville", "få", "sige", "komme", "gå", 
            "gø", "se", "måtte", "tage", "give", "stå", "finde", "holde", "sætte", "vise", 
            "bruge", "ligge", "vide", "tro", "mene", "ske", "fortælle", "høre", "lave", "lægge", 
            "skrive", "sidde", "lade", "spille", "synes", "følge", "begynde", "køre", "stille", "tænke", 
            "kalde", "ønske", "slå", "leve", "føre", "burde", "spørge", "arbejde", "skabe", "sende", 
            "falde", "kende", "vælge", "føle", "betyde", "trække", "samle", "vente", "forstå", "gælde", 
            "betale", "nå", "prøve", "rejse", "købe", "bo", "tale", "læse", "svare", "vende", 
            "vinde", "sælge", "hjælpe", "møde", "forsøge", "lære", "hedde", "findes", "fortsætte", "stige", 
            "lyde", "kræve", "passe", "sikre", "klare", "koste", "starte", "søge", "tælle", "bryde", 
            "bygge", "lukke", "mærke", "virke", "opleve", "bringe", "handle", "ændre", "ligne", "bede", 
            "dreje", "nævne", "udvikle", "bestå", "flytte", "elske", "spise", "regne", "mangle", "bære", 
            "ansætte", "udsende", "fylde", "hænge", "løbe", "forklare", "lykkes", "ringe", "vokse", "deltage", 
            "håbe", "lide", "modtage", "oplyse", "foregå", "løse", "danne", "huske", "foretage", "hente", 
            "skyldes", "slippe", "forsvinde", "glemme", "kigge", "miste", "gennemføre", "åbne", "fjerne", "forlade", 
            "snakke", "slutte", "dække", "tjene", "anvende", "øge", "rette", "undgå", "gribe", "opstå", 
            "tabe", "behandle", "ende", "indgå", "kaste", "ramme", "interessere", "styre", "skifte", "beslutte", 
            "opnå", "fungere", "føde", "råbe", "ses", "betragte", "dø", "true", "behøve", "tegne", 
            "foreslå", "indeholde", "opdage", "skyde", "drikke", "forvente", "optage", "træde", "sørge", "afvise", 
            "dele", "benytte", "støtte", "tvinge", "skaffe", "stikke", "stoppe", "beskrive", "tø", "kæmpe", 
            "drive", "dræbe", "undersøge", "glæde", "melde", "omfatte", "bestemme", "konstatere", "tilbyde", "understrege", 
            "blande", "vedtage", "indføre", "placere", "synge", "spare", "besøge", "sove", "lytte", "eksistere", 
            "yde", "acceptere", "løfte", "præsentere", "udtrykke", "byde", "diskutere", "ødelægge", "bevare", "presse", 
            "fremstille", "brænde", "overtage", "præge", "udføre", "ryge", "udsætte", "springe", "love", "flyve", 
            "erkende", "udtale", "opgive", "bevæge", "udgøre", "pege", "mødes", "trænge", "smide", "afsløre", 
            "forestille", "smile", "dukke", "medføre", "tillade", "vurdere", "producere", "sejle", "skjule", "begrænse", 
            "etablere", "koge", "lede", "frygte", "overveje", "tilføje", "stemme", "skære", "træffe"
    };
    //6d
    public static String[] frequent250verbsNov = new String[] {
        "være", "kunne", "blive", "skulle", "ville", "få", "sige", "komme", "gå", 
        "gø",  "måtte", "tage", "stå", "finde", "holde", "sætte", "vise", 
        "bruge", "ligge", "vide", "tro", "mene", "ske", "fortælle", "høre", "lave", "lægge", 
        "skrive", "sidde", "lade", "spille", "synes", "følge", "begynde", "køre", "stille", "tænke", 
        "kalde", "ønske", "slå", "leve", "føre", "burde", "spørge", "arbejde", "skabe", "sende", 
        "falde", "kende", "vælge", "føle", "betyde", "trække", "samle", "vente", "forstå", "gælde", 
        "betale", "nå", "prøve", "rejse", "købe", "læse", "svare", "vende", 
        "vinde", "sælge", "hjælpe", "møde", "forsøge", "lære", "hedde", "findes", "fortsætte", "stige", 
        "lyde", "kræve", "passe", "sikre", "klare", "koste", "starte", "søge", "tælle", "bryde", 
        "bygge", "lukke", "mærke", "virke", "opleve", "bringe", "handle", "ændre", "ligne", "bede", 
        "dreje", "nævne", "udvikle", "bestå", "flytte", "elske", "spise", "regne", "mangle", "bære", 
        "ansætte", "udsende", "fylde", "hænge", "løbe", "forklare", "lykkes", "ringe", "vokse", "deltage", 
        "håbe", "lide", "modtage", "oplyse", "foregå", "løse", "danne", "huske", "foretage", "hente", 
        "skyldes", "slippe", "forsvinde", "glemme", "kigge", "miste", "gennemføre", "åbne", "fjerne", "forlade", 
        "snakke", "slutte", "dække", "tjene", "anvende", "øge", "rette", "undgå", "gribe", "opstå", 
        "tabe", "behandle", "indgå", "kaste", "ramme", "interessere", "styre", "skifte", "beslutte", 
        "opnå", "fungere", "føde", "råbe", "ses", "betragte", "dø", "behøve", "tegne", 
        "foreslå", "indeholde", "opdage", "skyde", "drikke", "forvente", "optage", "træde", "sørge", "afvise", 
        "dele", "benytte", "støtte", "tvinge", "skaffe", "stikke", "stoppe", "beskrive", "tø", "kæmpe", 
        "dræbe", "undersøge", "glæde", "melde", "omfatte", "bestemme", "konstatere", "tilbyde", "understrege", 
        "blande", "vedtage", "indføre", "placere", "synge", "besøge", "sove", "lytte", "eksistere", 
        "yde", "acceptere", "løfte", "præsentere", "udtrykke", "byde", "diskutere", "ødelægge", "bevare", "presse", 
        "fremstille", "brænde", "overtage", "præge", "udføre", "ryge", "udsætte", "springe", "flyve", 
        "erkende", "udtale", "opgive", "bevæge", "udgøre", "pege", "mødes", "trænge", "smide", "afsløre", 
        "forestille", "smile", "dukke", "medføre", "tillade", "vurdere", "producere", "sejle", "skjule", "begrænse", 
        "etablere", "koge", "lede", "frygte", "overveje", "tilføje", "stemme", "skære", "træffe"
    };
    // Used for C5a
    public static String[] especiallyNormalDanishWords = new String[] {
            "af", "nu", "lige", "nej", "hvad" 
    }; 
 // Used for C5b
    public static String[] notDanishWords = new String[] {
            "av", "meg", "hva", "nei", 
            // More words added (see DB-138)
            "deg", "meg", "noe", "noen", "dere", "ut", "igjen", "oss", "gjør", "gjøre", 
            "ta", "tar", "opp", "seg", "bli", "takk", "aldri", "hei", "tilbake", "kanskje", "trenger", "mye",
            "unnskyld", "rett", "sånn", "liker", "greit", "finne", "mann", "fikk", "gikk", "hadde", "enn", "tok", "veldig"
    };
    
    // Danske stednavneendelser. Fra sitet Navn.ku.dk/stednavne/hvorgamle
    // og http://da.wikipedia.org/wiki/Danske_stednavne#Typiske_efterled_i_bebyggelsesnavne
    //Used for C7a
    public final static String[] placenamesuffixes = new String[] {"løse", "inge", "hem", "um", "lev", "sted", 
    "by", "toft", "torp", "tved", "rød", "bøl", "bølle", "bjerg", "høj", "dal", 
    "bæk", "å", "sund", "vig", "sig", "holm", "næs", "holt", "lund", "skov", 
    "ved", "bakke dige", "hule", "kilde", "kær", "odde",
    
    "ager", "balle", "borg", "bo", "dale", "holte", "lt", "lte", 
    "høje", "øv", "øje", "ie", "ing", "ker", "køb", "købing", "leje", "lunde", 
    "løkke", "løk", "lse", "mark", "marke", "nse", "ns", "rud", "rød", "rod", 
    "rum", "tofte", "trup", "drup", "rup", "rp", "tved", "vad ", "ved", "ør", 
    "øre", "er",  "lose","loese", "rod", "roed", "bol", "boel", "bolle", "boelle", "hoj", "hoej", 
    "baek", "aa", "naes", "kaer", "hoje", "hoeje", "ov", "oev", 
    "oje", "oeje",  "kob", "koeb", "kobing", "koebing",  
    "lokke","loekke", "lok","loekke", "or", "oer", "oere", "ore"};

    //De 45 største byer i Danmark ifølge Wikipedia:
    //Used for C7a??
    public final static String[] danishMajorCities = new String[] {
    "københavn","kobenhavn", "koebenhavn", "århus", "aarhus", "odense", "aalborg", 
    "frederiksberg", "esbjerg", 
    "gentofte", "gladsaxe", "randers ", "kolding", "horsens", 
     "lyngby-taarbæk","lyngby-taarbaek", 
    "vejle", "hvidovre", "roskilde", "helsingør", "helsingor", "helsingoer", "herning", 
    "silkeborg", 
    "næstved","naestved", "greve strand", "tårnby","taarnby", "fredericia", "ballerup", 
    "rødovre", "roedovre","rodovre", "viborg", "køge","koege","koge", "holstebro", 
    "brøndby", "broendby","brondby", 
    "taastrup", "tåstrup", "slagelse", "hillerød", "hilleroed", "hillerod", "albertslund", 
    "sønderborg", "soenderborg","sonderborg", 
    "svendborg", "herlev", "holbæk", "holbaek",
    "hjørring", "hjoerring","hjorring", "hørsholm", "hoersholm", "horsholm",
    "frederikshavn ", "glostrup", "haderslev", 
    "nørresundby", "norresundby", "noerresundby",  "ringsted ", 
    "ølstykke-Stenløse", "olstykke-stenlose", "oelstykke-stenloese","skive"
    };
    public final static Set<WordPattern> patternsdanishMajorCities = WordPattern.getCompiledPatterns(danishMajorCities);
 
    //C7g -koge
    public final static String[] danishMajorCitiesNov = new String[] {
    "københavn","kobenhavn", "koebenhavn", "århus", "aarhus", "odense", "aalborg", 
    "frederiksberg", "esbjerg", 
    "gentofte", "gladsaxe", "randers ", "kolding", "horsens", 
     "lyngby-taarbæk","lyngby-taarbaek", 
    "vejle", "hvidovre", "roskilde", "helsingør", "helsingor", "helsingoer", "herning", 
    "silkeborg", 
    "næstved","naestved", "greve strand", "tårnby","taarnby", "fredericia", "ballerup", 
    "rødovre", "roedovre","rodovre", "viborg", "køge","koege","holstebro", 
    "brøndby", "broendby","brondby", 
    "taastrup", "tåstrup", "slagelse", "hillerød", "hilleroed", "hillerod", "albertslund", 
    "sønderborg", "soenderborg","sonderborg", 
    "svendborg", "herlev", "holbæk", "holbaek",
    "hjørring", "hjoerring","hjorring", "hørsholm", "hoersholm", "horsholm",
    "frederikshavn ", "glostrup", "haderslev", 
    "nørresundby", "norresundby", "noerresundby",  "ringsted ", 
    "ølstykke-Stenløse", "olstykke-stenlose", "oelstykke-stenloese","skive"
    };
    public final static Set<WordPattern> patternsdanishMajorCitiesNov = WordPattern.getCompiledPatterns(danishMajorCitiesNov);
    public final static Set<WordPattern> patternsdanishMajorCitiesNovNoCase = WordPattern.getCompiledPatternsNoCase(danishMajorCitiesNov);
    
    //C7e 
    public final static String[] CapitalCountryTranslated = new String[] {
            "copenhagen", "denmark", // Engelsk
            "kopenhagen", "dänemark", // Tysk
            "copenhague", "danemark", // Fransk
            "köpenhamn", "danmark", // Svensk
            "kööpenhamina", "tanska", //Finsk
            "copenaghen", "danimarca", //Italiensk
            "kopenhagen", "denemarken", //Hollandsk
            "copenhague", "dinamarca",   //Spansk
            "kopenhag", "danimarka", //Tyrkisk
            "kobanheegan", "denmark", //Somali
            "copenhaga", "danemarca", //Rumænsk
            "koppenhága", "dánia", //Ungarsk
            "kopenhaageni", "taani", //Estisk
            "kopenhaga", "w kopenhadze", "dania", // Polsk
            "kodaň", "dánsko" //tjekkisk/slovakisk
    };
    //7h
    public final static String[] CapitalCountryTranslatedNov = new String[] {
        //"copenhagen", "denmark", // Engelsk
        "kopenhagen", "dänemark", // Tysk
        "copenhague", "danemark", // Fransk
        "köpenhamn", //"danmark", // Svensk
        "kööpenhamina", "tanska", //Finsk
        "copenaghen", "danimarca", //Italiensk
        "kopenhagen", "denemarken", //Hollandsk
        "copenhague", "dinamarca",   //Spansk
        "kopenhag", "danimarka", //Tyrkisk
        "kobanheegan", "denmark", //Somali
        "copenhaga", "danemarca", //Rumænsk
        "koppenhága", "dánia", //Ungarsk
        "kopenhaageni", "taani", //Estisk
        "kopenhaga", "w kopenhadze", "dania", // Polsk
        "kodaň", "dánsko" //tjekkisk/slovakisk
    };
    public final static Set<WordPattern> patternsCapitalCountryTranslatedNov = WordPattern.getCompiledPatterns(CapitalCountryTranslatedNov);
    public final static Set<WordPattern> patternsCapitalCountryTranslatedNovNoCase = WordPattern.getCompiledPatternsNoCase(CapitalCountryTranslatedNov);
    
    public final static String[] virksomheder_lowercased_1_word_Nov2 = new String[]{
    	"alectia", "alk-abelló", "ambu", "americapital", "aqualife", "arkitema", "banknordik", "bech-bruun", "br-energy", "bioporto", "brokersclub", "carlsberg",  "cbrain", 
    	 "chemometec","coloplast", "comendo", "cowex", "cowi","danfoss", "danisco", "dantax", "dsb", "dantherm", "deltaq", "dfds", "dfds", "dissing+weitling", "dsv", "eas", "fdb", 
    	 "ecco", "egetaepper", "egmont", "enalyzer", "erria", "exiqon", "expedit", "dlf-trifolium", "fastpasscorp", "firstfarms", "flsmidth", "fluegger", 
    	 "falck", "g4s", "genmab", "gronlandsbanken", "grundfos", "microskin", "neurosearch",  "intermail", "iqnovate", "jeudan", "kreditbanken", "jobindex", "klimainvest", 
    	 "matas", "mols-linien", "nordicom", "novozymes", "nykredit", "pandora", "pbs","pharmacosmos",  "sanistal", "sato",  "rias", "roblon", "plesner",
    	 "ramboll",  "simcorp", "skako",  "topdanmark", "tivoli", "sydbank", "tdc","ssbv-rovsing", "rtx",  "totalbanken", "trifork", "tryg", "tuborg", "topotarget",  
    	 "torm", "velux", "vestas", "zentropa",  "winlogic", "wirtek", "welltec", "widex"
    };

    public final static String[] virksomheder_lowercased_1_word_Nov3 = new String[]{
    	"alectia", "alk-abelló", "ambu", "americapital", "aqualife", "arkitema", "banknordik", "bech-bruun", "br-energy", "bioporto", "brokersclub", "carlsberg",  "cbrain", 
    	 "chemometec","coloplast", "comendo", "cowex", "cowi","danfoss", "danisco", "dantax", "dsb", "dantherm", "deltaq", "dfds", "dissing+weitling", "dsv", "eas", "fdb", 
    	 "ecco", "egetaepper", "egmont", "enalyzer", "erria", "exiqon", "expedit", "dlf-trifolium", "fastpasscorp", "firstfarms", "flsmidth", "fluegger", 
    	 "falck", "g4s", "genmab", "gronlandsbanken", "grundfos", "microskin", "neurosearch",  "intermail", "iqnovate", "jeudan", "kreditbanken", "jobindex", "klimainvest", 
    	 "matas", "mols-linien", "nordicom", "novozymes", "nykredit", "pbs","pharmacosmos",  "sanistal", "sato",  "rias", "roblon", "plesner",
    	 "ramboll",  "simcorp", "skako",  "topdanmark", "sydbank", "tdc","ssbv-rovsing", "rtx",  "totalbanken", "trifork", "tryg", "tuborg", "topotarget",  
    	 "velux", "vestas", "zentropa",  "winlogic", "wirtek", "welltec", "widex"
    };

    public final static String[] virksomheder_lowercased_2_words_Nov2 = new String[]{
    "2up gaming", "a. p. moller-maersk group", "african capital partners holding", 
    "aller media", "alm brand", "andersen & martini", "anglo african minerals", "ap moeller - maersk", "aqualeap technologies ltd", 
    "arkil holding", "arla foods", "arp-hansen hotel group", "asgaard group", "asia pacific gold mining investment ltd", "astra resources", "athena it-group", "atlantic airways", "atlantic petroleum", 
    "auriga industries", "axon global", "balux brands", "bang & olufsen", "bavarian nordic",  "belgrave resources", 
    "bispebjerg kollegiet", "bjarke ingels group", "block 42", "blue vision", "boconcept holding", "brd klee", "brodrene hartmann", "brondbyernes if fodbold", 
    "brødrene hartmann", "c. f. møller architects", "cassona se","celebrity brands", "chr hansen holding", "chr. hansen", "city odds capital", 
    "cobe architects", "cold fall corp", "creek project investments", "d/s norden", "dalhoff larsen & horneman", 
    "dampskibsselskabet norden", "dampskibsselskabet torm", "danish agro", "danish crown", "dansk supermarked group", "danske andelskassers bank", "danske bank", 
    "djurslands bank", "dk co", "dong energy", 
    "dxs international", "dynamic systems holdings inc", "east asiatic co ltd", "efb elite",  "egnsinvest ejd. tyskland", 
    "ei invest nordisk retail turku oy", "electrum mining resources", "eligere investments", "esoft systems", "eurocap investments", "euroinvestor.com", 
    "fast ejendom danmark", "fcm holding", "fe bording",  "flsmidth & co", 
    "fodboldalliancen ac horsens", "formuepleje epikur", "formuepleje merkur", "formuepleje optimum", "formuepleje pareto", "formuepleje penta", "fortune graphite inc", "fragrant prosperity", 
    "fynske bank",
    "gabriel holding", "gc mining",  "german high street properties", "global mineral resources corp", "glunz & jensen intl", "gn resound", "gn store nord", "go green group ltd", "gold horizons mining", 
    "greentech energy systems", "griffin iv berlin", "group 4 securicor", "gyldendalske boghandel", "h lundbeck", "h+h international", "haldor topsoe", "harboes bryggeri", 
    "hci hamilton capital", "hempel group", "henning larsen architects", "herrington teddy bear corp", "hojgaard holdings", "house of amber", "hvidbjerg bank", "ic companys", "imc exploration group", 
    "incor holdings", 
    "international western petroleum corp", "invest resources", "io interactive", "iqx ltd","jensen & moller invest", "jet time",  
    "jorgensen engineering", "jutlander bank", "jyske bank", "kif handbold elite", "kilimanjaro capital", "kobenhavns lufthavne", 
    "københavns lufthavne", "lambda td software", "lan & spar bank", "land & leisure", "lego group", "lm glasfiber", "lollands bank", "lottoarena entertainment", "lundgaard & tranberg", 
    "maghreb24 television inc", "magical production", "man oil group", "martin light", "maxi vision", "medical prognosis institute", "mega village systems", 
    "minerals mining corp", "monberg & thorsen", "mons bank", "monterey integrative retirement systems", "motivideo systems",  "neg micon", "netbooster holding", 
    "new freedom", "newcap holding", "nexacon energy inc", "nkt holding", "nordea bank", "nordfyns bank", "nordic shipholding", "nordisk film", 
    "nordjyske bank", "norresundby bank", "north media", "northwest oil & gas trading co inc", "novo nordisk", "ntr holding",  "optima worldwide group", "or holding inc",     
    "ossur hf", "ostjydsk bank", "parken sport & entertainment",  "per aarsleff", "pg alluvial mining", "pharma nord",  "phase one", 
    "post danmark", "pre owned cars", "prime office", "questus global capital market", "rapid nutrition", "re-cap b", "rella holding",
    "ringkjoebing landbobank", "rockwool international", "royal copenhagen", "royal unibrew", "salling bank",
    "saxo bank", "scandinavian airlines system", "scandinavian brake systems", "scandinavian private equity", "schmidt hammer lassen", "schouw & co", "silkeborg if invest",     
    "skandinavisk tobakskompagni", 
    "skjern bank", "smallcap danmark", "smartguy group", "southern cross resource group", "sp group", "spar nord bank", "strategic investments", "sumo resources", 
    "svejsemaskinefabrikken migatronic",  "tera hyper networks", "terma a", "thorco shipping", "tk development",
    "topsil semiconductor matls", "travelmarket.com", "tricolor sport", "united international enterprises", 
    "united shipping & trading company", "universal health solutions", "us oil and gas", "vejle boldklub holding", "veloxis pharmaceuticals", "vestas wind systems", "vestjysk bank", "viborg handbold klub", 
    "victor international", "victoria properties", "william demant", "zealand pharma", "aalborg boldspilklub", 
    "aarhus elite"
    };


// Edited by STHU on November 24, 2016
public final static String[] foreninger_lowercased = new String[] {
    "aab support club", "aalborg kunstpavillon", "aarhus økologiske fødevarefællesskab", 
    "acab (bornholm)", "adoption og samfund", "afrika kontakt", "akademisk arkitektforening", 
    "aktive kvinder i danmark", "aktive lyttere og seere", "aldrig mere krig", 
    "andelsboligforeningernes fællesrepræsentation", "andelssamfundet i hjortshøj", 
    "anima (forening)", "antropologforeningen i danmark", "antroposofisk selskab", 
    "arbejde adler", "arbejderbevægelsens internationale forum", "arbejderforeningen af 1860", 
    "arbejdernes andels boligforening (københavn)", "arbejdernes kunstforening", "arsenal denmark", 
    "astma-allergi forbundet", "astronomisk selskab", "ateistisk selskab", "athenæum (læseselskab)", 
    "avalon (forening)", "bedre byggeskik", "bifrost (rollespil)", "black wolves", "blue knights", 
    "bornholms passagerforening", "brancheforeningen for industriel automation", "bryggerlavet i den gamle by", 
    "brøndby support", "busbevarelsesgruppen danmark", "centralasiatisk selskab", "colitis-crohn foreningen", 
    "coop amba", "danes worldwide", "danish sail training association", "danmarks afholdsforening", 
    "danmarks agrarforening", "danmarks apotekerforening", "danmarks blinde", "danmarks bløderforening", 
    "danmarks farmaceutiske selskab", "danmarks film akademi", "danmarks folkelige broderier", 
    "danmarks forskningsbiblioteksforening", "danmarks gymnastik forbund", "danmarks lungeforening", 
    "danmarks naturfredningsforening", "danmarks privatskoleforening", "danmarks rejsebureau forening", 
    "danmarks rygerforening", "danmarks sløjdlærerforening", "dansk adelsforening", "dansk aktionærforening", 
    "dansk amatør teater samvirke", "dansk amatør-orkesterforbund", "dansk amerikansk fodbold forbund", 
    "dansk annoncørforening", "dansk antijødisk liga", "dansk arbejdsgiverforening", "dansk arkitektforening", 
    "dansk bibliofilklub", "dansk blindesamfund", "dansk botanisk forening", "dansk broder orden", 
    "dansk ejendomsmæglerforening", "dansk eksportforening (stiftet 1895)", "dansk elbil komite", 
    "dansk epilepsiforening", "dansk etnografisk forening", "dansk farmacihistorisk fond", "dansk folkeforening", 
    "dansk folkeoplysnings samråd", "dansk forening for ludomaner og pårørende", "dansk forfatterforening", 
    "dansk forum", "dansk fotografisk forening", "dansk fredsforening", "dansk friskoleforening", 
    "dansk geologisk forening", "dansk historisk fællesråd", "dansk ingeniørforening", 
    "dansk international bosætningsservice", "dansk it", "dansk jernbane-klub", "dansk komponist forening", 
    "dansk kvindesamfund", "dansk land og strandjagt", "dansk landsforening for laryngectomerede", 
    "dansk matematisk forening", "dansk muslimsk union", "dansk ornitologisk forening", "dansk pen", 
    "dansk santalmission", "dansk selskab for otolaryngologi - hoved & halskirurgi", 
    "dansk selskab for teoretisk statistik", "dansk skoleidræt", "dansk skovforening", "dansk sløjdforening", 
    "dansk sløjdlærerforening", "dansk sløjdlærersamfund", "dansk sommelier forening", "dansk spare-selskab", 
    "dansk søvn-apnø forening", "dansk teknologihistorisk selskab", "dansk vampyr selskab", "dansk vandrelaug", 
    "dansk vegetarforening", "dansk zionistforbund", "dansk-cubansk forening", "dansk-skaansk forening", 
    "dansk-tysk forening", "dansk-tysk selskab", "danske anlægsgartnere", "danske arkitekters landsforbund", 
    "danske arkitektvirksomheder", "danske gymnasieelevers sammenslutning", "danske gymnastik- & idrætsforeninger", 
    "danske malermestre", "danske mediers arbejdsgiverforening", "danske naturister", "danske sportsjournalister", 
    "danske torpare", "danske ølentusiaster", "danske, frie og uafhængige murere", "de berejstes klub", 
    "de blå/hvide engle", "de danske forsvarsbrødre for fredericia og omegn", "de danske vaabenbrødre", 
    "de samvirkende danske forsvarsbroderselskaber", "de studerendes erhvervskontakt", "demokratiske muslimer", 
    "den danske dommerforening", "den danske forening", "den danske historiske forening", 
    "den danske købstadsforening", "den danske publicistklub", "den danske radeerforening", 
    "den danske sommerskole", "den fri architektforening", "den frie biavlerforening, læsø", 
    "den liberale erhvervsklub", "den photographiske forening", "den republikanske grundlovsbevægelse", 
    "det danske gastronomiske akademi", "det danske haveselskab", "det danske haveselskab � øerne", 
    "det danske kriminalakademi", "det danske sprog- og litteraturselskab", "det grønlandske selskab", 
    "det grønne crew", "det jydske haveselskab", "det kongelige danske haveselskab", 
    "det kongelige danske landhusholdningsselskab", "det kongelige kjøbenhavnske skydeselskab og danske broderskab", 
    "det krigsvidenskabelige selskab", "det norske selskab", "det ny samfund", 
    "det skandinaviske litteraturselskab", "det tekniske selskab", "det udenrigspolitiske selskab", 
    "det unge grænseværn", "dhs - foreningen de historie studerende", "di service", "dis danmark", 
    "divisionsforeningen håndbold", "dykkehistorisk selskab", "dykkerklubben narhvalen", "dyrenes beskyttelse", 
    "døk alumni", "ejendomsforeningen danmark", "energiforum danmark", "ensomme gamles værn", 
    "erhvervslejernes landsorganisation", "erhvervsskolernes elev-organisation", "esbjerg fredsbevægelse", 
    "esperantoforeningen for danmark", "europa 2000", "eventyrernes klub", "f.c. københavn fan club", 
    "finanssektorens forening til støtte af et sundt og konkurrencedygtigt erhvervsliv", 
    "forening for boghaandværk", "foreningen af 1888 til understøttelse af dannebrogsridderes efterladte", 
    "foreningen af 3. december 1892", "foreningen af danske spiludviklere", 
    "foreningen af danske teaterjournalister", "foreningen af danske transportcentre", 
    "foreningen af katolske børnehaver i danmark", "foreningen af katolske skoler i danmark", 
    "foreningen af kommuner i københavns amt", "foreningen af kristne friskoler", 
    "foreningen af små teatre i danmark", "foreningen af speciallæger", "foreningen dansk arbejde", 
    "foreningen for dansk kunst", "foreningen for kønsforskning i danmark", 
    "foreningen for undersøgende journalistik", "foreningen grønlandske børn", 
    "foreningen herberger langs hærvejen", "foreningen kollegienet odense", "foreningen materiel design", 
    "foreningen oprør", "foreningen skånsk fremtid", 
    "foreningen til fremskaffelse af boliger for ældre og enlige", "foreningen til gamle bygningers bevaring", 
    "foreningen til søfartens fremme", "foreningen til udgivelse af danmarks adels aarbog", 
    "forsvars- & aerospaceindustrien i danmark", "frie grundskolers fællesråd", "frit danmark (forening)", 
    "frit oplysningsforbund", "fædrelandets forsvar", "fællesrepræsentationen for dansk industri og haandværk", 
    "gate 21", "gesten lokalråd", "gifted children danmark", "glentevejs antennelaug", "global contact", 
    "grundlovskomiteen 2003 vedr. irak-krigen", "gymnasieskolernes rektorforening", "h.c. andersen-samfundet", 
    "herfølge support", "historisk samfund for fyn", "historisk-topografisk selskab for lyngby-taarbæk kommune", 
    "horserød-stutthof foreningen", "humanistisk samfund", "håbefulde unge forfattere", 
    "immun defekt foreningen", "industriens arbejdsgivere", "ingeniørforeningen i danmark", 
    "instituttet for fremtidsforskning", "isps danmark", "it-politisk forening", "itek (di)", 
    "jydsk racekatte klub", "jyllands forfattere", "jyllinge sejl og motorbådsforening", 
    "jyllinge sejl- og motorbådsforening", "jysk børneforsorg fredehjem", "jysk folkeforening", 
    "jysk selskab for historie", "kalk- og teglværksforeningen af 1893", "kammermusikforeningen af 1868", 
    "kemisk forening", "kirkelig forening for den indre mission i danmark", 
    "kolding borgerlige skydeselskab af 1785", "kommunale tjenestemænd og overenskomstansatte", 
    "kongelig dansk aeroklub", "kongelig dansk automobil klub", "konservative gymnasiaster", 
    "kredsen mars og merkur danmark", "kræftens bekæmpelse", "kubologisk sportsforening", 
    "kulturforum danaustria", "kunst på arbejdspladsen", "kunstnerforeningen af 18. november", 
    "kvindelig læseforening", "kvinder i fysik", "køge amatørscene", "køge support", 
    "lambda (forening)", "landbrugernes sammenslutning", "landsforeningen af beskikkede advokater", 
    "landsforeningen bedre psykiatri", "landsforeningen danske folkedansere", 
    "landsforeningen for bygnings- og landskabskultur", "landsforeningen for bæredygtigt landbrug", 
    "landsforeningen krim", "landsforeningen sind", "landsorganisationen af arbejdsledige", 
    "landsorganisationen danske fugleforeninger", "landssammenslutningen af handelsskoleelever", 
    "landssammenslutningen af handelsskoleelever region nord", "lejernes lo", "lgbt danmark", 
    "liberalt oplysnings forbund", "litteraturkritikernes lav", "livgardens gamle tambourer", 
    "lokale pengeinstitutter", "luthersk mission", "lærerstuderendes landskreds", 
    "læreruddannelsens sløjdlærerforening", "løvfald i/s", "maritimt center danmark", 
    "mellemfolkeligt samvirke", "miqësia - dansk-albansk forening", "morgendagens heltinder", 
    "morsø kunstforening", "muslimernes fællesråd", "muslimsk ungdom i danmark", "nationaløkonomisk forening", 
    "nepenthes (miljøforening)", "netværket af ungdomsråd", "noah (dansk miljøorganisation)", 
    "nordisk copyright bureau", "nordisk jernbane-klub", "nordsjællands astronomi forening", 
    "næstved amatørscene", "næstved model racing club", "nørrebro lokalhistoriske forening og arkiv", 
    "nørrebros beboeraktion", "olympic amager idrætsforening", "ordenshistorisk selskab", 
    "organisationen danske arkiver", "organisationen til oplysning om atomkraft", "patriotisk selskab", 
    "poetklub århus", "poetry slam cph.", "polyteknisk flyvegruppe", "polyteknisk forening", 
    "pornofrit miljø", "praktiserende lægers organisation", "ptsd foreningen i danmark", 
    "ptu - landsforeningen af polio-, trafik- og ulykkesskadede", "radiohistorisk forening ringsted", 
    "randers flyveklub", "rejsearrangører i danmark", "reklamer ja tak", "ren energi oplysning, reo", 
    "ridder rune og hans lystige svende", "ringen (germanofil organisation)", 
    "rådet for international konfliktløsning", "samfundet for dansk genealogi og personalhistorie", 
    "sammenslutningen af danske erhvervsbiavlere", "sammenslutningen af danske småøer", 
    "seksualpolitisk forum", "selskabet for borgerdyd", "selskabet for dansk memorabilitet", 
    "selskabet for dekorativ kunst", "selskabet for efterslægten", "selskabet for frihed og kultur", 
    "selskabet for kirkelig kunst", "selskabet for naturlærens udbredelse", "selskabet for psykisk forskning", 
    "selskabet for trykkefrihedens rette brug", "selskabet til forskning i arbejderbevægelsens historie", 
    "selskabet til udgivelse af danske mindesmærker", "seminariernes musiklærerforening", 
    "silkeborg boldklubs venner", "sjældne diagnoser", "skandinavisk ufo information", "skive idræts-forbund", 
    "skuespillerforeningen af 1879", "slagtehal 3", "sløjdforeningen af 1902", 
    "sløjdlærernes fællesrepræsentation", "socialistisk standpunkt", "societa dante alighieri", 
    "somali community", "sorø antenneforening", "sporvejshistorisk selskab", "stomiforeningen copa", 
    "stop islamiseringen af danmark", "storlogen af danmark", "støtte til soldater og pårørende", 
    "støttekomiteen for tibet", "svenstrup godthåb idrætsforening", "sydslesvigsk udvalg af 5. maj 1945", 
    "tandsundhed uden grænser", "the network (forening)", "tivolis venner", "tuba (forening)", 
    "tørring-uldum folkedansere", "vandreforeningen fodslaw", "vedvarendeenergi (forening)", 
    "venskabsforeningen danmark - den demokratiske folkerepublik korea", "venøsund færgelaug", 
    "vesterbro komponistforening", "vikingeklubben jomsborg", "visens venner i danmark", 
    "vorupør fiskeriforening", "yngre læger", "århus sangskriver værksted", "ærø natur- og energiskole", 
    "øjenforeningen værn om synet"
};


    public final static Set<WordPattern> patternsForeninger_lowercased = WordPattern.getCompiledPatterns(foreninger_lowercased);
    public final static Set<WordPattern> patternsForeninger_lowercasedNoCase = WordPattern.getCompiledPatternsNoCase(foreninger_lowercased);
    
    public final static String[] foreninger_lowercased_1_word_Nov2 = new String[] {
    	"4h","akb", "afholdsbevægelsen", "atlantsammenslutningen", "augustforeningen", "arkivforeningen", "asfaltindustrien", "assurandør-societetet", "bigruppen", "blus",
    	"beredskabsforbundet", "bibliotekslederforeningen", 
    	"blågården", "bogstaveligheden", "bolsjefabrikken", "brugsforening", "busfronten", "byggesocietetet", "børnehjælpsdagen", "citybugowners", "domea", "doss", "danmission", 
    	"diabetesforeningen", "dialogos", "dyreværnet", "eksportforeningen", 
        "fadb", "flik",  "fdm", "fiduso", "fiskeringen", "forbrugerrådet", "forbrugsforeningen",
        "fædrelandets forsvar", "fællesrepræsentationen for dansk industri og haandværk", "gigtforeningen", "gjaestebud", "gramex", "hedeselskabet", 
        "guldsmedelauget", "harmonien", "haveselskabet", "hiv-danmark", "hjerneskadeforeningen", "hjerteforeningen", "haandværkerforeningen", "høreforeningen", "industriforeningen", "ingeniør-sammenslutningen", 
        "kirkeasyl", "klimabevægelsen", "knæleren", "koda", "kogræsserselskab", "kommunekredit", "kulturkapellet", "koncertforeningen",  "kunstforeningen", "kunstnersamfundet",
        "latinlærerforeningen", "leda",  "dfk",
        "dansker-ligaen", "ligeværd", "litteraturselskabet", "managementrådgiverne",
        "militærnægterforeningen", "miljøorganisation", "modersmål-selskabet", "modersmålskredsen", "musoc", "musikforeningen", "muskelsvindfonden",
        "nyreforeningen", "næstor", "ok-klubben", "oktoberforeningen", 
        "patientforeningen", "patientforsikringen", "realdania",  "ripen", "ripensersamfundet","scleroseforeningen", "sehat", "skamlingsbankeselskabet", 
        "skyttebladet", "skyttesagen", "slesvig-ligaen",  "smokenhagen", "spastikerforeningen", "sprogforeningen", "sslug", "søe-lieutenant-selskabet", "søofficers-foreningen",
        "træskibs-sammenslutningen", "ungdomsråd", "unf", 
        "run4kids", 
    	"xet",   "aakjærselskabet", "øjlug", "ølakademiet",  
    };
    
    public final static String[] foreninger_lowercased_2_words_Nov2 = new String[] {
        "acab (bornholm)", "dansk adelsforening", "adoption og samfund", "danmarks afholdsforening", "afrika kontakt", 
        "danmarks agrarforening", "akademisk arkitektforening",  "dansk aktionærforening", "aktive kvinder i danmark", "aktive lyttere og seere", "aldrig mere krig", "dansk amatør teater samvirke", 
        "dansk amatør-orkesterforbund", "dansk amerikansk fodbold forbund", 
        "andelsboligforeningernes fællesrepræsentation", "andelssamfundet i hjortshøj", "anima (forening)", "danske anlægsgartnere", "dansk annoncørforening", "dansk antijødisk liga", 
        "antropologforeningen i danmark", "antroposofisk selskab", "danmarks apotekerforening", "arbejde adler", 
        "arbejderbevægelsens internationale forum", "arbejderforeningen af 1860", "arbejdernes andels boligforening (københavn)", "arbejdernes kunstforening", "dansk arbejdsgiverforening", 
        "danske arkitekters landsforbund", "dansk arkitektforening", "danske arkitektvirksomheder", "arsenal denmark",         
        "astma-allergi forbundet", "astronomisk selskab", "ateistisk selskab", "athenæum (læseselskab)",  "avalon (forening)", 
        "bedre byggeskik", "dansk bibliofilklub", "bifrost (rollespil)", "black wolves", "danmarks blinde", "blue knights", 
        "de blå/hvide engle", "danmarks bløderforening", "bornholms passagerforening", "dansk botanisk forening", "brancheforeningen for industriel automation", "dansk broder orden",
        "bryggerlavet i den gamle by", "brøndby support", "busbevarelsesgruppen danmark", "centralasiatisk selskab",  "colitis-crohn foreningen", "coop amba", 
        "danes worldwide", "dansk blindesamfund", "dansk-cubansk forening", "den danske forening", "de berejstes klub", "de samvirkende danske forsvarsbroderselskaber", "de studerendes erhvervskontakt", 
        "demokratiske muslimer", "den fri architektforening", 
        "den frie biavlerforening, læsø", "den liberale erhvervsklub", "det kongelige danske haveselskab", "det kongelige danske landhusholdningsselskab", 
        "det kongelige kjøbenhavnske skydeselskab og danske broderskab", "det norske selskab", "det skandinaviske litteraturselskab", "det tekniske selskab", "det unge grænseværn", 
        "dhs - foreningen de historie studerende", "di service",  "dis danmark", "divisionsforeningen håndbold",  "den danske dommerforening",  
        "dværgeforeningen", "dykkehistorisk selskab", "dykkerklubben narhvalen", "dyrenes beskyttelse", "døk alumni", "ejendomsforeningen danmark", 
        "dansk ejendomsmæglerforening", "dansk eksportforening (stiftet 1895)", 
        "dansk elbil komite", "energiforum danmark", "ensomme gamles værn", "dansk epilepsiforening", "erhvervslejernes landsorganisation", "erhvervsskolernes elev-organisation", 
        "esbjerg fredsbevægelse", "esperantoforeningen for danmark", "dansk etnografisk forening", "europa 2000", 
        "eventyrernes klub", "f.c. københavn fan club", 
        "danmarks farmaceutiske selskab", "dansk farmacihistorisk fond","danmarks film akademi", "finanssektorens forening til støtte af et sundt og konkurrencedygtigt erhvervsliv", 
        "dansk folkeforening", "danmarks folkelige broderier", "dansk folkeoplysnings samråd", 
        "forening for boghaandværk", "dansk forening for ludomaner og pårørende", "foreningen af 1888 til understøttelse af dannebrogsridderes efterladte", "foreningen af 3. december 1892", 
        "foreningen af danske spiludviklere", "foreningen af danske teaterjournalister", "foreningen af danske transportcentre", "foreningen af katolske børnehaver i danmark",
        "foreningen af katolske skoler i danmark", "foreningen af kommuner i københavns amt", "foreningen af kristne friskoler", "foreningen af små teatre i danmark", 
        "foreningen af speciallæger", "foreningen dansk arbejde", 
        "foreningen for dansk kunst", "foreningen for kønsforskning i danmark", "foreningen for undersøgende journalistik", "foreningen grønlandske børn", "foreningen herberger langs hærvejen", 
        "foreningen kollegienet odense", "foreningen materiel design", "foreningen oprør", "foreningen skånsk fremtid", "foreningen til fremskaffelse af boliger for ældre og enlige", 
        "foreningen til gamle bygningers bevaring", "foreningen til søfartens fremme", "foreningen til udgivelse af danmarks adels aarbog", "dansk forfatterforening", 
        "forfatterlandsholdet", "fuhu", "fængselslærerforeningen", "galebevægelsen", 
        "danmarks forskningsbiblioteksforening", "forstyrret.dk", "forsvars- & aerospaceindustrien i danmark", "de danske forsvarsbrødre for fredericia og omegn", "dansk forum", 
        "dansk fotografisk forening", "dansk fredsforening", "frie grundskolers fællesråd", "danske, frie og uafhængige murere", "friluftsrådet", "dansk friskoleforening", "frit danmark (forening)", "frit oplysningsforbund", "frøsamlerne", "fsbbolig", 
        "det danske gastronomiske akademi", "gate 21", "dansk geologisk forening", "gesten lokalråd", "gifted children danmark", 
        "glentevejs antennelaug", "global contact", "grundlovskomiteen 2003 vedr. irak-krigen", "grundlovsværneforeningen", "grænseforeningen", "det grønlandske selskab", "det grønne crew", 
        "danske gymnasieelevers sammenslutning", "gymnasieskolernes rektorforening", "danmarks gymnastik forbund", "danske gymnastik- & idrætsforeninger", 
        "h.c. andersen-samfundet", "det danske haveselskab", "det danske haveselskab & øerne",  
        "herfølge support", "dansk historisk fællesråd", "historisk samfund for fyn", "historisk-topografisk selskab for lyngby-taarbæk kommune", "den danske historiske forening", 
        "horserød-stutthof foreningen", "humanistisk samfund", "håbefulde unge forfattere", "immun defekt foreningen", "industriens arbejdsgivere", 
        "dansk ingeniørforening", "ingeniørforeningen i danmark", 
        "instituttet for fremtidsforskning", "dansk international bosætningsservice", "isps danmark", "dansk it", "it-politisk forening", "itek (di)", 
        "dansk jernbane-klub", "jydsk racekatte klub", "det jydske haveselskab", "jyllands forfattere", 
        "jyllinge sejl og motorbådsforening", "jyllinge sejl- og motorbådsforening", "jysk børneforsorg fredehjem", "jysk folkeforening", "jysk selskab for historie", 
        "kalk- og teglværksforeningen af 1893", "kammermusikforeningen af 1868", "kemisk forening", 
        "kirkelig forening for den indre mission i danmark", 
        "kolding borgerlige skydeselskab af 1785", "kommunale tjenestemænd og overenskomstansatte", "dansk komponist forening", 
        "kongelig dansk aeroklub", "kongelig dansk automobil klub", "konservative gymnasiaster", "kredsen mars og merkur danmark", "det krigsvidenskabelige selskab", 
        "det danske kriminalakademi", "kræftens bekæmpelse", "kubologisk sportsforening", "kulturforum danaustria", 
        "kunst på arbejdspladsen", "kunstnerforeningen af 18. november", 
        "kvindelig læseforening", "kvinder i fysik", "dansk kvindesamfund", "den danske købstadsforening", "køge amatørscene", 
        "køge support", "lambda (forening)", "dansk land og strandjagt", "landbrugernes sammenslutning", "dansk landsforening for laryngectomerede", 
        "landsforeningen af beskikkede advokater", "landsforeningen bedre psykiatri", "landsforeningen danske folkedansere", "landsforeningen for bygnings- og landskabskultur", 
        "landsforeningen for bæredygtigt landbrug", 
        "landsforeningen krim", "landsforeningen sind", "landsorganisationen af arbejdsledige", "landsorganisationen danske fugleforeninger", "landssammenslutningen af handelsskoleelever", 
        "landssammenslutningen af handelsskoleelever region nord", "lejernes lo", 
        "lgbt danmark", "liberalt oplysnings forbund", "litteraturkritikernes lav", 
        "livgardens gamle tambourer", "ljud", "lki.dk", "lokale pengeinstitutter", 
        "danmarks lungeforening", "luthersk mission", "lærerstuderendes landskreds", "læreruddannelsens sløjdlærerforening", "løvfald i/s", "majoratsforeningen", "danske malermestre", 
        "maritimt center danmark", "dansk matematisk forening", "danske mediers arbejdsgiverforening", "mellemfolkeligt samvirke", "miqësia - dansk-albansk forening", "ateistisk selskab", 
        "morgendagens heltinder", "morsø kunstforening", 
        "muslimernes fællesråd", "muslimsk ungdom i danmark", "dansk muslimsk union", "nationaløkonomisk forening", "danmarks naturfredningsforening", "danske naturister", 
        "nepenthes (miljøforening)", "netværket af ungdomsråd", "noah (dansk miljøorganisation)", "nordisk copyright bureau", "nordisk jernbane-klub", "nordsjællands astronomi forening", 
        "det ny samfund",  "næstved amatørscene", 
        "næstved model racing club", "nørrebro lokalhistoriske forening og arkiv", "nørrebros beboeraktion", 
        "olympic amager idrætsforening", "ordenshistorisk selskab", "organisationen danske arkiver", "organisationen til oplysning om atomkraft", "dansk ornitologisk forening", 
        "patriotisk selskab", "dansk pen", "den photographiske forening", "poetklub århus", "poetry slam cph.", "polyteknisk flyvegruppe", "polyteknisk forening", "pornofrit miljø", 
        "praktiserende lægers organisation", "danmarks privatskoleforening", "ptsd foreningen i danmark", "ptu - landsforeningen af polio-, trafik- og ulykkesskadede", 
        "den danske publicistklub", "den danske radeerforening", "radiohistorisk forening ringsted", "randers flyveklub", "rejsearrangører i danmark",
        "danmarks rejsebureau forening", "reklamer ja tak", "rekylkorps", "ren energi oplysning, reo", "den republikanske grundlovsbevægelse", 
        "ridder rune og hans lystige svende", "ringen (germanofil organisation)",  
        "danmarks rygerforening", "rådet for international konfliktløsning", "danish sail training association", 
        "danmarks-samfundet", "samfundet for dansk genealogi og personalhistorie", "sammenslutningen af danske erhvervsbiavlere", "sammenslutningen af danske småøer", "dansk santalmission", 
        "seksualpolitisk forum", "dansk selskab for otolaryngologi - hoved & halskirurgi", "dansk selskab for teoretisk statistik", "selskabet for borgerdyd", "selskabet for dansk memorabilitet",
        "selskabet for dekorativ kunst", "selskabet for efterslægten", "selskabet for frihed og kultur", 
        "selskabet for kirkelig kunst", "selskabet for naturlærens udbredelse", "selskabet for psykisk forskning", "selskabet for trykkefrihedens rette brug", 
        "selskabet til forskning i arbejderbevægelsens historie", "selskabet til udgivelse af danske mindesmærker", "seminariernes musiklærerforening", 
        "silkeborg boldklubs venner", "sjældne diagnoser", 
        "skandinavisk ufo information", "skatteborgerforeningen", "skive idræts-forbund", "dansk skoleidræt", "dansk skovforening", "skuespillerforeningen af 1879", 
        "dansk-skaansk forening", "slagtehal 3", 
        "dansk sløjdforening", "sløjdforeningen af 1902", "danmarks sløjdlærerforening", "dansk sløjdlærerforening", "sløjdlærernes fællesrepræsentation", "dansk sløjdlærersamfund", 
        "socialistisk standpunkt", "societa dante alighieri", "somali community", "dansk sommelier forening", "den danske sommerskole", "sorø antenneforening", "dansk spare-selskab", 
        "danske sportsjournalister", "sporvejshistorisk selskab",
        "det danske sprog- og litteraturselskab", "stomiforeningen copa", "stop islamiseringen af danmark", "storlogen af danmark", "støtte til soldater og pårørende", "støttekomiteen for tibet", 
        "svenstrup godthåb idrætsforening", "sydslesvigsk udvalg af 5. maj 1945", 
        "dansk søvn-apnø forening", "tandsundhed uden grænser", "dansk teknologihistorisk selskab", "the network (forening)", "tivolis venner", "danske torpare", "trekkies.dk", 
        "tuba (forening)", "dansk-tysk forening", "dansk-tysk selskab", "tågekammeret", "tørring-uldum folkedansere", "det udenrigspolitiske selskab", "dansk vampyr selskab",
        "vandreforeningen fodslaw", "dansk vandrelaug", "vardensersamfundet", "vederfølner", "vedvarendeenergi (forening)", "dansk vegetarforening", "venskabsforeningen danmark - den demokratiske folkerepublik korea", "venøsund færgelaug", "vesterbro komponistforening", "vikingeklubben jomsborg", 
        "visens venner i danmark", "vorupør fiskeriforening", "de danske vaabenbrødre", "yngre læger",  "dansk zionistforbund", "aab support club",  
        "aalborg kunstpavillon", 
        "århus sangskriver værksted", "aarhus økologiske fødevarefællesskab", "ærø natur- og energiskole", "øjenforeningen værn om synet", "ølejrbevægelsen", "danske ølentusiaster" 
    };
    
    
    // Danske foreninger
    // "Danske foreninger (fra Wikipedia - i alt 515 stk) 
    // (http://da.wikipedia.org/wiki/Kategori:Foreninger_fra_Danmark)"
    
    String[] foreninger = new String[]{
            "4H", "ACAB (Bornholm)", "Dansk Adelsforening", "Adoption og Samfund", "Afholdsbevægelsen", "Danmarks Afholdsforening", "Afrika Kontakt", 
            "Danmarks Agrarforening", "Akademisk Arkitektforening", "AKB", "Dansk Aktionærforening", "Aktive Kvinder i Danmark", "Aktive Lyttere og Seere", "Aldrig Mere Krig", "Dansk Amatør Teater Samvirke", "Dansk Amatør-Orkesterforbund", "Dansk Amerikansk Fodbold Forbund", 
            "Andelsboligforeningernes Fællesrepræsentation", "Andelssamfundet i Hjortshøj", "Anima (forening)", "Danske Anlægsgartnere", "Dansk Annoncørforening", "Dansk Antijødisk Liga", "Antropologforeningen i Danmark", "Antroposofisk Selskab", "Danmarks Apotekerforening", "Arbejde Adler", 
            "Arbejderbevægelsens Internationale Forum", "Arbejderforeningen af 1860", "Arbejdernes Andels Boligforening (København)", "Arbejdernes Kunstforening", "Dansk Arbejdsgiverforening", "Danske Arkitekters Landsforbund", "Dansk Arkitektforening", "Danske Arkitektvirksomheder", "Arkivforeningen", "Arsenal Denmark", 
            "Asfaltindustrien", "Assurandør-Societetet", "Astma-Allergi Forbundet", "Astronomisk Selskab", "Ateistisk Selskab", "Athenæum (læseselskab)", "Atlantsammenslutningen", "Augustforeningen", "Avalon (forening)", "Axis", 
            "Bedre Byggeskik", "Beredskabsforbundet", "Dansk Bibliofilklub", "Bibliotekslederforeningen", "Bifrost (rollespil)", "Bigruppen", "Black Wolves", "Danmarks Blinde", "Blue Knights", "BLUS", 
            "De Blå/Hvide Engle", "Blågården", "Danmarks Bløderforening", "Bogstaveligheden", "Bolsjefabrikken", "Bornholms Passagerforening", "Dansk Botanisk Forening", "Brancheforeningen for Industriel Automation", "Dansk Broder Orden", "Brugsforening", 
            "Bryggerlavet i Den Gamle By", "Brøndby Support", "Busbevarelsesgruppen Danmark", "Busfronten", "Byggesocietetet", "Børnehjælpsdagen", "Centralasiatisk Selskab", "Citybugowners", "Colitis-Crohn Foreningen", "Coop amba", 
            "Danes Worldwide", "Danmission", "Dansk Blindesamfund", "Dansk-Cubansk Forening", "Den Danske Forening", "De Berejstes Klub", "De samvirkende danske Forsvarsbroderselskaber", "De Studerendes Erhvervskontakt", "Demokratiske Muslimer", "Den fri Architektforening", 
            "Den frie Biavlerforening, Læsø", "Den Liberale Erhvervsklub", "Det kongelige danske Haveselskab", "Det Kongelige Danske Landhusholdningsselskab", "Det Kongelige Kjøbenhavnske Skydeselskab og Danske Broderskab", "Det Norske Selskab", "Det skandinaviske Litteraturselskab", "Det tekniske Selskab", "Det Unge Grænseværn", "DFK", 
            "DHS - Foreningen De Historie Studerende", "DI Service", "Diabetesforeningen", "Dialogos", "DIS Danmark", "Divisionsforeningen Håndbold", "Domea", "Den Danske Dommerforening", "DOSS", "DUT", 
            "Dværgeforeningen", "Dykkehistorisk Selskab", "Dykkerklubben Narhvalen", "Dyrenes Beskyttelse", "Dyreværnet", "DøK Alumni", "Ejendomsforeningen Danmark", "Dansk Ejendomsmæglerforening", "Dansk Eksportforening (stiftet 1895)", "Eksportforeningen", 
            "Dansk Elbil Komite", "Energiforum Danmark", "Ensomme Gamles Værn", "Dansk Epilepsiforening", "Erhvervslejernes Landsorganisation", "Erhvervsskolernes Elev-Organisation", "Esbjerg Fredsbevægelse", "Esperantoforeningen for Danmark", "Dansk Etnografisk Forening", "Europa 2000", 
            "Eventyrernes Klub", "F.C. København Fan Club", "FaDB", "Danmarks Farmaceutiske Selskab", "Dansk Farmacihistorisk Fond", "FDM", "Fiduso", "Danmarks Film Akademi", "Finanssektorens forening til støtte af et sundt og konkurrencedygtigt erhvervsliv", "Fiskeringen", 
            "FLIK", "Dansk Folkeforening", "Danmarks Folkelige Broderier", "Dansk Folkeoplysnings Samråd", "Forbrugerrådet", "Forbrugsforeningen", "Forening for Boghaandværk", "Dansk Forening for Ludomaner og Pårørende", "Foreningen af 1888 til understøttelse af Dannebrogsridderes efterladte", "Foreningen af 3. December 1892", 
            "Foreningen Af Danske Spiludviklere", "Foreningen af Danske Teaterjournalister", "Foreningen af Danske Transportcentre", "Foreningen af katolske børnehaver i Danmark", "Foreningen af Katolske Skoler i Danmark", "Foreningen af Kommuner i Københavns Amt", "Foreningen af Kristne Friskoler", "Foreningen Af Små Teatre i Danmark", "Foreningen af Speciallæger", "Foreningen Dansk Arbejde", 
            "Foreningen for Dansk Kunst", "Foreningen for Kønsforskning i Danmark", "Foreningen for Undersøgende Journalistik", "Foreningen Grønlandske Børn", "Foreningen Herberger langs Hærvejen", "Foreningen Kollegienet Odense", "Foreningen Materiel Design", "Foreningen Oprør", "Foreningen Skånsk Fremtid", "Foreningen til Fremskaffelse af Boliger for ældre og Enlige", 
            "Foreningen til Gamle Bygningers Bevaring", "Foreningen til Søfartens Fremme", "Foreningen til Udgivelse af Danmarks Adels Aarbog", "Dansk Forfatterforening", "Forfatterlandsholdet", "Danmarks Forskningsbiblioteksforening", "Forstyrret.dk", "Forsvars- & Aerospaceindustrien i Danmark", "De Danske Forsvarsbrødre for Fredericia og Omegn", "Dansk Forum", 
            "Dansk Fotografisk Forening", "Dansk Fredsforening", "Frie Grundskolers Fællesråd", "Danske, Frie og Uafhængige Murere", "Friluftsrådet", "Dansk Friskoleforening", "Frit Danmark (forening)", "Frit Oplysningsforbund", "Frøsamlerne", "FSBbolig", 
            "FUHU", "Fædrelandets Forsvar", "Fællesrepræsentationen for dansk Industri og Haandværk", "Fængselslærerforeningen", "Galebevægelsen", "Det Danske Gastronomiske Akademi", "Gate 21", "Dansk Geologisk Forening", "Gesten Lokalråd", "Gifted Children Danmark", 
            "Gigtforeningen", "Gjaestebud", "Glentevejs Antennelaug", "Global Contact", "Gramex", "Grundlovskomiteen 2003 vedr. Irak-krigen", "Grundlovsværneforeningen", "Grænseforeningen", "Det grønlandske Selskab", "Det Grønne Crew", 
            "Guldsmedelauget", "Danske Gymnasieelevers Sammenslutning", "Gymnasieskolernes Rektorforening", "Danmarks Gymnastik Forbund", "Danske Gymnastik- & Idrætsforeninger", "H.C. Andersen-Samfundet", "Harmonien", "Det Danske Haveselskab", "Det Danske Haveselskab  øerne", "Haveselskabet", 
            "Hedeselskabet", "Herfølge Support", "Dansk Historisk Fællesråd", "Historisk Samfund for Fyn", "Historisk-topografisk Selskab for Lyngby-Taarbæk Kommune", "Den danske historiske Forening", "Hiv-Danmark", "Hjerneskadeforeningen", "Hjerteforeningen", "Horserød-Stutthof Foreningen", 
            "Humanistisk Samfund", "Håbefulde Unge Forfattere", "Haandværkerforeningen", "Høreforeningen", "Immun Defekt Foreningen", "Industriens Arbejdsgivere", "Industriforeningen", "Ingeniør-Sammenslutningen", "Dansk Ingeniørforening", "Ingeniørforeningen i Danmark", 
            "Instituttet for Fremtidsforskning", "Dansk International Bosætningsservice", "ISPS Danmark", "DANSK IT", "IT-Politisk Forening", "ITEK (DI)", "Dansk Jernbane-Klub", "Jydsk Racekatte Klub", "Det Jydske Haveselskab", "Jyllands Forfattere", 
            "Jyllinge Sejl og Motorbådsforening", "Jyllinge Sejl- og Motorbådsforening", "Jysk børneforsorg Fredehjem", "Jysk Folkeforening", "Jysk Selskab for Historie", "Kalk- og Teglværksforeningen af 1893", "Kammermusikforeningen af 1868", "Kemisk Forening", "Kirkeasyl", "Kirkelig Forening for den Indre Mission i Danmark", 
            "KL", "KLID", "Klimabevægelsen", "Knæleren", "Koda", "Kogræsserselskab", "Kolding borgerlige skydeselskab af 1785", "Kommunale Tjenestemænd og Overenskomstansatte", "Kommunekredit", "Dansk Komponist Forening", 
            "Koncertforeningen", "Kongelig Dansk Aeroklub", "Kongelig Dansk Automobil Klub", "Konservative Gymnasiaster", "Kredsen Mars og Merkur Danmark", "Det Krigsvidenskabelige Selskab", "Det Danske Kriminalakademi", "Kræftens Bekæmpelse", "Kubologisk Sportsforening", "Kulturforum DanAustria", 
            "Kulturkapellet", "Kunst på arbejdspladsen", "Kunstforeningen", "Kunstnerforeningen af 18. november", "Kunstnersamfundet", "Kvindelig Læseforening", "Kvinder i Fysik", "Dansk Kvindesamfund", "Den danske Købstadsforening", "Køge Amatørscene", 
            "Køge Support", "Lambda (forening)", "Dansk Land og Strandjagt", "Landbrugernes Sammenslutning", "Dansk Landsforening for Laryngectomerede", "Landsforeningen af Beskikkede Advokater", "Landsforeningen Bedre Psykiatri", "Landsforeningen Danske Folkedansere", "Landsforeningen for Bygnings- og Landskabskultur", "Landsforeningen for Bæredygtigt Landbrug", 
            "Landsforeningen KRIM", "Landsforeningen SIND", "Landsorganisationen af Arbejdsledige", "Landsorganisationen Danske Fugleforeninger", "Landssammenslutningen af Handelsskoleelever", "Landssammenslutningen af handelsskoleelever region nord", "Latinlærerforeningen", "LEDA", "Lejernes LO", "LEV", 
            "LGBT Danmark", "Liberalt Oplysnings Forbund", "Dansker-Ligaen", "Ligeværd", "Litteraturkritikernes Lav", "Litteraturselskabet", "Livgardens Gamle Tambourer", "LJUD", "Lki.dk", "Lokale Pengeinstitutter", 
            "Danmarks Lungeforening", "Luthersk Mission", "Lærerstuderendes Landskreds", "Læreruddannelsens Sløjdlærerforening", "Løvfald I/S", "Majoratsforeningen", "Danske Malermestre", "Managementrådgiverne", "Maritimt Center Danmark", "MASK", 
            "Dansk Matematisk Forening", "Danske Mediers Arbejdsgiverforening", "Mellemfolkeligt Samvirke", "Militærnægterforeningen", "Miljøorganisation", "Miqësia - Dansk-Albansk Forening", "Ateistisk Selskab", "Modersmål-Selskabet", "Modersmålskredsen", "Morgendagens Heltinder", 
            "Morsø Kunstforening", "Musikforeningen", "Muskelsvindfonden", "Muslimernes Fællesråd", "Muslimsk Ungdom i Danmark", "Dansk Muslimsk Union", "MuSoc", "Nationaløkonomisk Forening", "Danmarks Naturfredningsforening", "Danske Naturister", 
            "Nepenthes (miljøforening)", "Netværket af Ungdomsråd", "NOAH (dansk miljøorganisation)", "Nordisk Copyright Bureau", "Nordisk Jernbane-Klub", "Nordsjællands Astronomi Forening", "Det Ny Samfund", "Nyreforeningen", "Næstor", "Næstved Amatørscene", 
            "Næstved Model Racing Club", "Nørrebro Lokalhistoriske Forening og Arkiv", "Nørrebros Beboeraktion", "OK-Klubben", "Oktoberforeningen", "Olympic Amager Idrætsforening", "Ordenshistorisk Selskab", "Organisationen Danske Arkiver", "Organisationen til Oplysning om Atomkraft", "Dansk Ornitologisk Forening", 
            "Patientforeningen", "Patientforsikringen", "Patriotisk Selskab", "Dansk PEN", "Den photographiske Forening", "Poetklub Århus", "Poetry Slam Cph.", "Polyteknisk Flyvegruppe", "Polyteknisk Forening", "Pornofrit Miljø", 
            "Praktiserende Lægers Organisation", "Danmarks Privatskoleforening", "PTSD foreningen i Danmark", "PTU - Landsforeningen af Polio-, Trafik- og Ulykkesskadede", "Den Danske Publicistklub", "Den Danske Radeerforening", "Radiohistorisk Forening Ringsted", "Randers Flyveklub", "Realdania", "Rejsearrangører i Danmark", 
            "Danmarks Rejsebureau Forening", "Reklamer ja tak", "Rekylkorps", "Ren Energi Oplysning, REO", "Den Republikanske Grundlovsbevægelse", "REVY", "Ridder Rune og hans lystige svende", "Ringen (germanofil organisation)", "Ripen", "Ripensersamfundet", 
            "Run4kids", "Danmarks Rygerforening", "Rådet for International Konfliktløsning", "Danish Sail Training Association", "SALA", "Danmarks-Samfundet", "Samfundet for dansk genealogi og Personalhistorie", "Sammenslutningen af Danske Erhvervsbiavlere", "Sammenslutningen af danske småøer", "Dansk Santalmission", 
            "Scleroseforeningen", "Sehat", "Seksualpolitisk Forum", "Dansk Selskab for Otolaryngologi - Hoved & Halskirurgi", "Dansk Selskab for Teoretisk Statistik", "Selskabet for Borgerdyd", "Selskabet for Dansk Memorabilitet", "Selskabet for dekorativ Kunst", "Selskabet for Efterslægten", "Selskabet for Frihed og Kultur", 
            "Selskabet for Kirkelig Kunst", "Selskabet for Naturlærens Udbredelse", "Selskabet for Psykisk Forskning", "Selskabet for Trykkefrihedens rette Brug", "Selskabet til Forskning i Arbejderbevægelsens Historie", "Selskabet til Udgivelse af danske Mindesmærker", "Seminariernes Musiklærerforening", "Silkeborg Boldklubs Venner", "Sjældne Diagnoser", "Skamlingsbankeselskabet", 
            "Skandinavisk UFO Information", "Skatteborgerforeningen", "Skive Idræts-Forbund", "Dansk Skoleidræt", "Dansk Skovforening", "Skuespillerforeningen af 1879", "Skyttebladet", "Skyttesagen", "Dansk-Skaansk Forening", "Slagtehal 3", 
            "Slesvig-Ligaen", "Dansk Sløjdforening", "Sløjdforeningen af 1902", "Danmarks Sløjdlærerforening", "Dansk Sløjdlærerforening", "Sløjdlærernes Fællesrepræsentation", "Dansk Sløjdlærersamfund", "SMID", "SMil", "Smokenhagen", 
            "Socialistisk Standpunkt", "Societa Dante Alighieri", "Somali Community", "Dansk Sommelier Forening", "Den Danske Sommerskole", "Sorø Antenneforening", "Dansk Spare-Selskab", "Spastikerforeningen", "Danske Sportsjournalister", "Sporvejshistorisk Selskab", 
            "Det Danske Sprog- og Litteraturselskab", "Sprogforeningen", "SSLUG", "Stomiforeningen COPA", "Stop Islamiseringen af Danmark", "Storlogen af Danmark", "Støtte Til Soldater Og Pårørende", "Støttekomiteen for Tibet", "Svenstrup Godthåb Idrætsforening", "Sydslesvigsk Udvalg af 5. maj 1945", 
            "Søe-Lieutenant-Selskabet", "Søofficers-Foreningen", "Dansk Søvn-Apnø Forening", "Tandsundhed uden Grænser", "Dansk Teknologihistorisk Selskab", "TENEN", "The Network (forening)", "Tivolis Venner", "Danske Torpare", "Trekkies.dk", 
            "Træskibs-sammenslutningen", "TUBA (forening)", "Dansk-Tysk Forening", "Dansk-Tysk Selskab", "Tågekammeret", "Tørring-Uldum Folkedansere", "Det Udenrigspolitiske Selskab", "UNF", "Ungdomsråd", "Dansk Vampyr Selskab", 
            "Vandreforeningen Fodslaw", "Dansk Vandrelaug", "Vardensersamfundet", "Vederfølner", "VedvarendeEnergi (forening)", "Dansk Vegetarforening", "Venskabsforeningen Danmark - Den Demokratiske Folkerepublik Korea", "Venøsund Færgelaug", "Vesterbro Komponistforening", "Vikingeklubben Jomsborg", 
            "Visens Venner i Danmark", "Vorupør Fiskeriforening", "De danske Vaabenbrødre", "Xet", "Yngre Læger", "Ynk", "Dansk Zionistforbund", "AaB Support Club", "Aakjærselskabet", "Aalborg Kunstpavillon", 
            "Århus Sangskriver Værksted", "Aarhus økologiske Fødevarefællesskab", "ærø Natur- og Energiskole", "Øjenforeningen Værn om Synet", "ØJLUG", "Ølakademiet", "Ølejrbevægelsen", "Danske ølentusiaster"
    };
   
    
    
    
    // Danske virksomhede
    // Her er 320 danske virksomheder (konsolideret liste uden dubletter)"
    String[] virksomheder = new String[]{
            "2UP Gaming", "A. P. Moller-Maersk Group", "African Capital Partners Holding", "Alectia", 
            "ALK-Abelló", "Aller Media", "Alm Brand", "Ambu", "Americapital", "Andersen & Martini ", "Anglo African Minerals ", "AP Moeller - Maersk ", "Aqualeap Technologies Ltd ", "Aqualife ", 
            "Arkil Holding ", "Arkitema", "Arla Foods", "Arp-Hansen Hotel Group", "Asgaard Group ", "Asia Pacific Gold Mining Investment Ltd ", "Astra Resources  ", "Athena IT-Group ", "Atlantic Airways ", "Atlantic Petroleum ", 
            "Auriga Industries", "Axon Global", "Balux Brands", "Bang & Olufsen", "BankNordik", "Bavarian Nordic", "Bech-Bruun", "Belgrave Resources ", "Bestseller", "Bioporto  ", 
            "Bispebjerg Kollegiet", "Bjarke Ingels Group", "Block 42  ", "Blue Vision ", "BoConcept Holding ", "BRD Klee ", "BR-Energy   ", "Brodrene Hartmann ", "Brokersclub  ", "Brondbyernes IF Fodbold ", 
            "Brødrene Hartmann", "C. F. Møller Architects", "Carlsberg ", "Cassona SE  ", "cBrain ", "Celebrity Brands  ", "Chemometec  ", "Chr Hansen Holding ", "Chr. Hansen", "City Odds Capital ", 
            "COBE Architects", "Cold Fall Corp ", "Coloplast ", "Columbus ", "Comendo ", "COWEX", "COWI", "Creek Project Investments ", "D/S Norden ", "Dalhoff Larsen & Horneman ", 
            "Dampskibsselskabet Norden", "Dampskibsselskabet TORM", "Danfoss", "Danisco", "Danish Agro", "Danish Crown", "Dansk Supermarked Group", "Danske Andelskassers Bank ", "Danske Bank", "Dantax ", 
            "Dantherm ", "Deltaq ", "DFDS", "Dfds ", "Dissing+Weitling", "Djurslands Bank ", "DK Co ", "DLF-Trifolium", "DONG Energy", "DSB", 
            "DSV ", "DXS International  ", "Dynamic Systems Holdings Inc ", "EAS", "East Asiatic Co Ltd  ", "ECCO", "EfB Elite  ", "Egetaepper ", "Egmont", "EgnsINVEST Ejd. Tyskland ", 
            "ei invest nordisk retail Turku Oy ", "Electrum Mining Resources   ", "Eligere Investments  ", "Enalyzer ", "Erria ", "Esoft Systems ", "Eurocap Investments ", "Euroinvestor.com ", "Exiqon ", "Expedit ", 
            "Falck", "Fast Ejendom Danmark ", "FastPassCorp  ", "FCM Holding ", "FDB", "FE Bording ", "FirstFarms ", "FLSmidth", "FLSmidth & Co ", "Fluegger ", 
            "Fodboldalliancen AC Horsens ", "Formuepleje Epikur  ", "Formuepleje Merkur ", "Formuepleje Optimum ", "Formuepleje Pareto ", "Formuepleje Penta ", "Fortune Graphite Inc ", "Fragrant Prosperity ", "Fynske Bank ", "G4S   ", 
            "Gabriel Holding ", "GC Mining  ", "Genmab  ", "German High Street Properties ", "Global Mineral Resources Corp ", "Glunz & Jensen Intl ", "GN ReSound", "GN Store Nord", "Go Green Group Ltd ", "Gold Horizons Mining ", 
            "Greentech Energy Systems ", "Griffin IV Berlin ", "Gronlandsbanken ", "Group 4 Securicor", "Grundfos", "Gyldendalske Boghandel  ", "H Lundbeck ", "H+H International ", "Haldor Topsoe", "Harboes Bryggeri ", 
            "HCI Hamilton Capital ", "Hempel Group", "Henning Larsen Architects", "Herrington Teddy Bear Corp ", "Hojgaard Holdings   ", "House of Amber", "Hvidbjerg Bank ", "IC Companys ", "IMC Exploration Group ", "Incor Holdings ", 
            "InterMail ", "International Western Petroleum Corp ", "Invest Resources ", "IO Interactive", "IQnovate  ", "IQX Ltd ", "ISS", "Jensen & Moller Invest ", "Jet Time", "Jeudan ", 
            "Jobindex", "Jorgensen Engineering", "Jutlander Bank ", "JYSK", "Jyske Bank ", "KIF Handbold Elite ", "Kilimanjaro Capital ", "KlimaInvest  ", "Kobenhavns Lufthavne ", "Kreditbanken ", 
            "Københavns Lufthavne", "Lambda TD Software ", "Lan & Spar Bank ", "Land & Leisure ", "LEGO", "Lego Group", "LM Glasfiber", "Lollands Bank ", "Lottoarena Entertainment ", "Lundgaard & Tranberg", 
            "Luxor ", "Maghreb24 Television Inc ", "Magical Production ", "Man Oil Group ", "Martin Light", "Matas ", "Maxi Vision  ", "Medical Prognosis Institute ", "Mega Village Systems ", "Mermaid ", 
            "Microskin ", "Minerals Mining Corp ", "Mols-Linien  ", "Monberg & Thorsen ", "Mons Bank ", "Monterey Integrative Retirement Systems ", "Motivideo Systems ", "NAME ", "NEG Micon", "NetBooster Holding ", 
            "NeuroSearch ", "New Freedom ", "Newcap Holding ", "Nexacon Energy Inc ", "NKT Holding ", "Nordea Bank ", "Nordfyns Bank ", "Nordic Shipholding ", "Nordicom  ", "Nordisk Film", 
            "Nordjyske Bank ", "Norresundby Bank ", "North Media ", "Northwest Oil & Gas Trading Co Inc ", "Novo Nordisk", "Novozymes ", "NTR Holding ", "Nykredit", "Optima Worldwide Group ", "OR Holding Inc ", 
            "Ossur HF  ", "Ostjydsk Bank ", "Pandora", "Parken Sport & Entertainment ", "PBS ", "PER Aarsleff ", "PG Alluvial Mining ", "Pharma Nord", "Pharmacosmos", "Phase One", 
            "Plesner", "Post Danmark", "Pre Owned Cars ", "Prime Office ", "Questus Global Capital Market ", "Ramboll", "Rapid Nutrition  ", "Re-Cap B ", "Rella Holding ", "Rias ", 
            "Ringkjoebing Landbobank ", "Roblon ", "Rockwool International ", "Royal Copenhagen", "Royal Unibrew", "RTX ", "Salling Bank ", "Sanistal ", "SAS ", "Sato ", 
            "Saxo Bank", "Scandinavian Airlines System ", "Scandinavian Brake Systems ", "Scandinavian Private Equity", "Schmidt hammer lassen", "Schouw & Co ", "Silkeborg IF Invest ", "SimCorp ", "SKAKO ", "Skandinavisk Tobakskompagni", 
            "Skjern Bank ", "SmallCap Danmark ", "SmartGuy Group ", "Solar ", "Southern Cross Resource Group ", "SP Group ", "Spar Nord Bank ", "SSBV-Rovsing ", "Strategic Investments ", "Sumo Resources ", 
            "Svejsemaskinefabrikken Migatronic ", "Sydbank ", "TDC ", "Tera Hyper Networks ", "Terma A", "Thorco Shipping", "Tiger", "Tivoli ", "TK Development ", "Topdanmark ", 
            "TopoTarget ", "Topsil Semiconductor Matls ", "Torm ", "Totalbanken ", "Travelmarket.com ", "Tricolor Sport ", "Trifork ", "Tryg ", "Tuborg", "United International Enterprises ", 
            "United Shipping & Trading Company", "Universal Health Solutions ", "US Oil and Gas ", "Vejle Boldklub Holding ", "Veloxis Pharmaceuticals ", "VELUX", "Vestas", "Vestas Wind Systems ", "Vestjysk Bank ", "Viborg Handbold Klub ", 
            "Victor International ", "Victoria Properties ", "Welltec", "Widex", "William Demant", "WinLogic ", "Wirtek ", "Zealand Pharma ", "Zentropa", "Aalborg Boldspilklub", 
            "Aarhus Elite"
    };
    
    // A/S eller APS (C9b)
    public final static String[] aktieselskabNames =  new String[]{"a/s", "aps" };
    
    public final static String[] DanishNames = new String[] {
    "anne", "kirsten", "hanne", "mette", "anna", "helle", "susanne", "lene", 
    "maria", "marianne", 
    "inge", "karen", "lone", "bente", "camilla", "pia", "jette", "charlotte", "louise", 
    "inger", 
    "peter", "jens", "lars", "michael", "henrik", "søren", "thomas", "jan", "niels", 
    "christian", "jørgen", "martin", "hans", "anders", "morten", "jesper", "ole", "per", 
    "erik", "mads", 
    "jensen", "nielsen", "hansen", "pedersen", "andersen", "christensen", "larsen", 
    "sørensen", "rasmussen", "jørgensen", 
    "petersen", "madsen", "kristensen", "olsen", "thomsen", "christiansen", "poulsen", 
    "johansen", "møller", "knudsen"
    };
    //10c
    public final static String[] DanishNamesNov = new String[] {
    "kirsten", "hanne", "mette", "anna", "helle", "susanne", "lene", "marianne", 
    "inge", "karen", "lone", "bente", "camilla", "pia", "jette", "inger", 
    "jens", "lars", "henrik", "søren", "jan", "niels", 
    "jørgen", "hans", "anders", "morten", "jesper", "erik", "mads", 
    "jensen", "nielsen", "hansen", "pedersen", "andersen", "christensen", "larsen", 
    "sørensen", "rasmussen", "jørgensen", 
    "petersen", "madsen", "kristensen", "olsen", "thomsen", "christiansen", "poulsen", 
    "johansen", "møller", "knudsen"
    };
    
    public final static String[] DanishNamesNov2 = new String[] {
    "kirsten", "hanne", "mette", "anna", "helle", "susanne", "lene", "marianne", 
    "inge", "karen", "bente", "camilla", "pia", "jette", "inger", 
    "jens", "lars", "henrik", "søren", "niels", 
    "jørgen", "hans", "anders", "morten", "jesper", "erik", "mads", 
    "jensen", "nielsen", "hansen", "pedersen", "andersen", "christensen", "larsen", 
    "sørensen", "rasmussen", "jørgensen", 
    "petersen", "madsen", "kristensen", "olsen", "thomsen", "christiansen", "poulsen", 
    "johansen", "møller", "knudsen"
    };

    public final static String[] DanishNamesNov3 = new String[] {
    "kirsten", "hanne", "mette", "helle", "susanne", "lene", "marianne", 
    "inge", "karen", "bente", "camilla", "pia", "jette", "inger", 
    "jens", "lars", "henrik", "søren", "niels", 
    "jørgen", "morten", "jesper", "erik", "mads", 
    "jensen", "nielsen", "hansen", "pedersen", "andersen", "christensen", "larsen", 
    "sørensen", "rasmussen", "jørgensen", 
    "petersen", "madsen", "kristensen", "olsen", "thomsen", "christiansen", "poulsen", 
    "johansen", "møller", "knudsen"
    };

    public final static Set<WordPattern> patternsDanishNamesNov = WordPattern.getCompiledPatterns(DanishNamesNov);
    public final static Set<WordPattern> patternsDanishNamesNovNoCase = WordPattern.getCompiledPatternsNoCase(DanishNamesNov);
    
    public static final String[] englishWordsEndingOnSen = new String[]{"arisen", "chosen"};
    
    
    
    public static Set<String> getFrequentDanishWords() {
        Set<String> words = new HashSet<String>();
        words.addAll(java.util.Arrays.asList(frequent150words));
        words.addAll(java.util.Arrays.asList(frequent250adjs));
        words.addAll(java.util.Arrays.asList(frequent250verbs));
        words.addAll(java.util.Arrays.asList(frequent250subs));
        return words;
    }
    
    public static Set<String> getFrequentDanishWordsNov() {
        Set<String> words = new HashSet<String>();
        words.addAll(java.util.Arrays.asList(frequent150wordsNov));
        words.addAll(java.util.Arrays.asList(frequent250adjsNov));
        words.addAll(java.util.Arrays.asList(frequent250verbsNov));
        words.addAll(java.util.Arrays.asList(frequent250subsNov));
        return words;
    }
    public final static Set<WordPattern> patternsFrequentDanishWordsNov = WordPattern.getCompiledPatterns(getFrequentDanishWordsNov());
    public final static Set<WordPattern> patternsFrequentDanishWordsNovNoCase = WordPattern.getCompiledPatternsNoCase(getFrequentDanishWordsNov());
    
    public static Set<String> getBadSenWords() {
        Set<String> words = new HashSet<String>();
        words.addAll(java.util.Arrays.asList(englishWordsEndingOnSen));
        return words;
    }
    
    public final static String[] virksomheder_one_word_lowercased = new String[]{
    	"alectia", "ambu", "americapital", "aqualife", "arkitema", "banknordik", 
    	"bestseller", "bioporto", "brokersclub", "carlsberg", "cbrain", "chemometec", 
    	"coloplast", "columbus", "comendo", "cowex", "cowi", "danfoss", "danisco", 
    	"dantax", "dantherm", "deltaq", "dfds", "dfds", "dsb", "dsv", "eas", "ecco", 
    	"egetaepper", "egmont", "enalyzer", "erria", "exiqon", "expedit", "falck", 
    	"fastpasscorp", "fdb", "firstfarms", "flsmidth", "fluegger", "g4s", "genmab", 
    	"gronlandsbanken", "grundfos", "intermail", "iqnovate", "iss", "jeudan", 
    	"jobindex", "jysk", "klimainvest", "kreditbanken", "lego", "luxor", "matas", 
    	"mermaid", "microskin", "name", "neurosearch", "nordicom", "novozymes", 
    	"nykredit", "pandora", "pbs", "pharmacosmos", "plesner", "ramboll", "rias", 
    	"roblon", "rtx", "sanistal", "sas", "sato", "simcorp", "skako", "solar", 
    	"sydbank", "tdc", "tiger", "tivoli", "topdanmark", "topotarget", "torm", 
    	"totalbanken", "trifork", "tryg", "tuborg", "velux", "vestas", "welltec", 
    	"widex", "winlogic", "wirtek", "zentropa"
    	};
    
    // Edited by STHU, November 24,2016
    public final static String[] virksomheder_lowercased = new String[]{
        "a. p. moller-maersk group", "aalborg boldspilklub", "aarhus elite", "african capital partners holding", 
        "aller media", "alm brand", "andersen & martini", "anglo african minerals", "ap moeller - maersk", 
        "aqualeap technologies ltd", "arkil holding", "arla foods", "arp-hansen hotel group", "asgaard group", 
        "asia pacific gold mining investment ltd", "astra resources", "athena it-group", "atlantic airways", 
        "atlantic petroleum", "auriga industries", "axon global", "balux brands", "bang & olufsen", "bavarian nordic", 
        "belgrave resources", "bispebjerg kollegiet", "bjarke ingels group", "block 42", "blue vision", 
        "boconcept holding", "brd klee", "brodrene hartmann", "brondbyernes if fodbold", "brødrene hartmann", 
        "c. f. møller architects", "cassona se", "celebrity brands", "chr hansen holding", "chr. hansen", 
        "city odds capital", "cobe architects", "cold fall corp", "creek project investments", "d/s norden", 
        "dalhoff larsen & horneman", "dampskibsselskabet norden", "dampskibsselskabet torm", "danish agro", 
        "danish crown", "dansk supermarked group", "danske andelskassers bank", "danske bank", "djurslands bank", 
        "dk co", "dong energy", "dxs international", "dynamic systems holdings inc", "east asiatic co ltd", 
        "efb elite", "egnsinvest ejd. tyskland", "ei invest nordisk retail turku oy", "electrum mining resources", 
        "eligere investments", "esoft systems", "eurocap investments", "fast ejendom danmark", "fcm holding", 
        "fe bording", "flsmidth & co", "fodboldalliancen ac horsens", "formuepleje epikur", "formuepleje merkur", 
        "formuepleje optimum", "formuepleje pareto", "formuepleje penta", "fortune graphite inc", "fragrant prosperity", 
        "fynske bank", "gabriel holding", "gc mining", "german high street properties", "global mineral resources corp", 
        "glunz & jensen intl", "gn resound", "gn store nord", "go green group ltd", "gold horizons mining", 
        "greentech energy systems", "griffin iv berlin", "group 4 securicor", "gyldendalske boghandel", 
        "h lundbeck", "h+h international", "haldor topsoe", "harboes bryggeri", "hci hamilton capital", 
        "hempel group", "henning larsen architects", "herrington teddy bear corp", "hojgaard holdings", 
        "house of amber", "hvidbjerg bank", "ic companys", "imc exploration group", "incor holdings", 
        "international western petroleum corp", "invest resources", "io interactive", "iqx ltd", 
        "jensen & moller invest", "jet time", "jorgensen engineering", "jutlander bank", "jyske bank", 
        "kif handbold elite", "kilimanjaro capital", "kobenhavns lufthavne", "københavns lufthavne", 
        "lambda td software", "lan & spar bank", "land & leisure", "lego group", "lm glasfiber", "lollands bank", 
        "lottoarena entertainment", "lundgaard & tranberg", "maghreb24 television inc", "magical production", 
        "man oil group", "martin light", "maxi vision", "medical prognosis institute", "mega village systems", 
        "minerals mining corp", "monberg & thorsen", "mons bank", "monterey integrative retirement systems", 
        "motivideo systems", "neg micon", "netbooster holding", "new freedom", "newcap holding", "nexacon energy inc", 
        "nkt holding", "nordea bank", "nordfyns bank", "nordic shipholding", "nordisk film", "nordjyske bank", 
        "norresundby bank", "north media", "northwest oil & gas trading co inc", "novo nordisk", "ntr holding", 
        "optima worldwide group", "or holding inc", "ossur hf", "ostjydsk bank", "parken sport & entertainment", 
        "per aarsleff", "pg alluvial mining", "pharma nord", "phase one", "post danmark", "pre owned cars", 
        "prime office", "questus global capital market", "rapid nutrition", "re-cap b", "rella holding", 
        "ringkjoebing landbobank", "rockwool international", "royal copenhagen", "royal unibrew", "salling bank", 
        "saxo bank", "scandinavian airlines system", "scandinavian brake systems", "scandinavian private equity", 
        "schmidt hammer lassen", "schouw & co", "silkeborg if invest", "skandinavisk tobakskompagni", "skjern bank", 
        "smallcap danmark", "smartguy group", "southern cross resource group", "sp group", "spar nord bank", 
        "strategic investments", "sumo resources", "svejsemaskinefabrikken migatronic", "tera hyper networks", 
        "terma a", "thorco shipping", "tk development", "topsil semiconductor matls", "tricolor sport", 
        "united international enterprises", "united shipping & trading company", "universal health solutions", 
        "us oil and gas", "vejle boldklub holding", "veloxis pharmaceuticals", "vestas wind systems", "vestjysk bank", 
        "viborg handbold klub", "victor international", "victoria properties", "william demant", "zealand pharma"
    };
    
    public final static Set<WordPattern> patternsVirksomheder_lowercased = WordPattern.getCompiledPatterns(virksomheder_lowercased);
    public final static Set<WordPattern> patternsVirksomheder_lowercasedNoCase = WordPattern.getCompiledPatternsNoCase(virksomheder_lowercased);
    
    /**
     * Test-program that test the 150 frequent words.
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        File f1 = new File("korpus/testfile.txt");
        //runTestsOnFile(f1, frequent150words);
        runWordPatternOnFile(f1, Words.especiallyNormalDanishWords);
        runWordPatternOnFile(f1, Words.notDanishWords);
    }
    
    private static void runWordPatternOnFile(File f4, String[] words) throws IOException {
        BufferedReader fr = new BufferedReader(new FileReader(f4));
        String line;
        while ((line = fr.readLine()) != null) {
            System.out.println("Checking line " + line);
            Set<String> foundMatches = TextUtils.SearchWord(line, words);
            System.out.println("line matches: " + TextUtils.conjoin(",", foundMatches));
            /*
            for (String word: words){
                //System.out.println("Checking word " + word);
                if (line.contains(word)) {
                    System.out.println("Matches: " + word);
                }
            }*/
        }
        IOUtils.closeQuietly(fr);
    }
    
    }
