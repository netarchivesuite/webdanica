package dk.kb.webdanica.core.criteria;

import java.util.HashSet;
import java.util.Set;

public class FrequentWords {
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
    // Updated with new set of words from STHU November 2016
    public static String[] frequentwordsWithDanishLettersCoded = new String[] {
        "paa", "saa", "ogsaa", "vaere", "aar", "naar",  
         "faa","maa", "vaeret", "gaar", "faar", 
        "forste", "foerste","foer", "saadan", 
        "maaske", "baade", "goere", "gaa", "goer",
        
        // Adjektiver
        "forst", "foerst",
        "hoj","hoej", "staerk", "naeste", "oekonomisk","okonomisk", "oevrig", "ovrig", "svaer", 
        
        "naer", "saerlig",  "haard", "daarlig", "roed", "taet", 
        "noedvendig","nodvendig",  "saadan", "faa",
        
        "aaben", "faelles",  "europaeisk", 
        "saakaldt", "spaendende",  
         "faerdig", "vaesentlig",  
        "afgoerende","afgorende", "nuvaerende","doed", "groen", 
        "sjaelden", "paen", 
         
        "blaa", "noedt", "bloed","nodt", 
        "foreloebig", "forelobig","fuldstaendig", 
        "opmaerksom", "hoejre","hojre",  
        "soed","sod", "militaer",  
        "populaer", "aabenbar", "traet", 
        "aarlig",  
        "selvstaendig",  "vaerd",  
         "maerkelig",  
        "overst","loes", "oeverst",  
        "berømt", "paagaeldende",  
        "udmaerket", "aegte",  
        "tilstraekkelig", "vaerre",  
        "toer",  
        "moerk", "moerk","afhaengig",  "usaedvanlig", 
        // navneord
        "aar",  "kobenhavn","koebenhavn",  
        "maade",  "maaned", "oeje", "haand", "omraade", 
        "spoergsmaal", "raekke", "loeb","lob", 
        "hjaelp", "tilfaelde", "ojeblik", "mode","oejeblik", "moede", 
        "jorgen", "soen", "joergen","foraeldre", "laege", 
          
        "maal",  "aarhus",  "raad", "direktoer", "direktor", "forsoeg", "doer","forsog",  
        "soendag", "soeren","sondag", "soren", "kaerlighed", "undersoegelse","undersogelse",   
        "loerdag", "oekonomi", "lordag", "okonomi","gengaeld",  
        "praesident", "stoette", "loesning","moeller", "stotte", "losning","moller","glaede", 
        
        // Verber
        "vaere", "faa", "gaa", 
        "goe", "go", "maatte", "staa", "saette",  
        "fortaelle", "hoere", "laegge", 
        "foelge", "koere", "folge", "kore","taenke", 
        "oenske","onske", "slaa", "foere", "spoerge","fore", "sporge",  
        "vaelge", "foele","fole", "traekke", "forstaa", "gaelde", 
        "naa", "proeve", "koebe", "prove", "kobe", "laese",  
        "saelge", "hjaelpe", "moede", "forsoege","mode", "forsoge", "laere", "fortsaette", 
        "kraeve", "soege", "taelle",  
        "maerke", "aendre", 
        "naevne", "bestaa", "baere", 
        "haenge", "loebe",  
        "haabe", "foregaa", "loese", 
        "gennemfoere","gennemfore", "aabne",  
        "daekke", "oege","oge", "undgaa", "opstaa", 
        "indgaa",  "opnaa", "foede", "raabe", "doe", "behoeve", "behove", 
        "foreslaa", "traede", "soerge","sorge",  
        "stoette", "toe","stotte", "kaempe", 
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
    
    
}
