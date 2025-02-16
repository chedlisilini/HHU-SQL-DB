PRAGMA auto_vacuum = 1;
PRAGMA encoding = "UTF-8";
PRAGMA foreign_keys = 1;
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;

DROP TABLE IF EXISTS Bewertung;
DROP TABLE IF EXISTS Optionaler_Text;
DROP TABLE IF EXISTS Aufgabe;
DROP TABLE IF EXISTS Mentor;
DROP TABLE IF EXISTS Arbeitet_An;
DROP TABLE IF EXISTS Fachlicher_Kompetenz;
DROP TABLE IF EXISTS Spezialist_Hat_Fachlicher_Kompetenz;
DROP TABLE IF EXISTS Entwickler;
DROP TABLE IF EXISTS Beherrscht;
DROP TABLE IF EXISTS Optionale_Alias;
DROP TABLE IF EXISTS Designer_Hat_Alias;
DROP TABLE IF EXISTS Weitere_Kompetenz;
DROP TABLE IF EXISTS Designer_Hat_Weitere_Kompetenz;
DROP TABLE IF EXISTS Nutzer;
DROP TABLE IF EXISTS Kunde;
DROP TABLE IF EXISTS Projekt;
DROP TABLE IF EXISTS Spezialist;
DROP TABLE IF EXISTS Programmiersprache;
DROP TABLE IF EXISTS Designer;
DROP TABLE IF EXISTS Projektleiter;



CREATE TABLE Nutzer (
	"E-Mail-Adresse"	TEXT NOT NULL  COLLATE NOCASE 
						CHECK((substr("E-Mail-Adresse", INSTR("E-Mail-Adresse", '.') + 1) NOT GLOB '*[^A-Za-z]*') 
						AND (substr("E-Mail-Adresse", 1, INSTR("E-Mail-Adresse", '@') - 1) NOT GLOB '*[^A-Za-z0-9]*') 
						AND (substr("E-Mail-Adresse", INSTR("E-Mail-Adresse", '@') + 1,
						((INSTR("E-Mail-Adresse", '.') - 1) - (INSTR("E-Mail-Adresse", '@') + 1)) + 1) NOT GLOB '*[^A-Za-z0-9]*') 
						AND "E-Mail-Adresse" LIKE '%_@%_.%_'),
	"Passwort"			TEXT NOT NULL 
						CHECK ((LENGTH(Passwort) BETWEEN 4 AND 9)
						AND Passwort NOT GLOB '*[^ -~]*'
						AND Passwort GLOB '*[0-9]*[0-9]*' 
						AND Passwort GLOB '*[A-Z]*'
						AND LOWER(Passwort) NOT GLOB '*[aeiou][13579]*' 
		),
	PRIMARY KEY("E-Mail-Adresse")
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Kunde (
	"E-Mail-Adresse"	TEXT UNIQUE NOT NULL COLLATE NOCASE,
	"Telefonnummer"		VARCHAR(11) NOT NULL
						CHECK("Telefonnummer" GLOB '0*' 
						AND "Telefonnummer" NOT GLOB '*[^0-9]*' 
						AND length("Telefonnummer") = 11 ),
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Nutzer("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE,
	PRIMARY KEY("Telefonnummer")
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Projektleiter (
	"E-Mail-Adresse"	TEXT NOT NULL COLLATE NOCASE,
	"Gehalt" 			DOUBLE NOT NULL CHECK ("Gehalt" > 0 AND "Gehalt" IS ROUND(Gehalt ,2)),
	PRIMARY KEY("E-Mail-Adresse"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Nutzer("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE 
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Projekt (
	"ProjektID"				INTEGER NOT NULL,
	"Projektname" 			TEXT NOT NULL CHECK("Projektname" != '' AND "Projektname" NOT GLOB '*[^ -~]*'),
	"Projektdeadline"		DATE NOT NULL CHECK (Projektdeadline is date("Projektdeadline")),
	"Telefonnummer"			VARCHAR(11) NOT NULL COLLATE NOCASE,
	"E-Mail-Adresse"		TEXT NOT NULL COLLATE NOCASE,
	PRIMARY KEY("ProjektID"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Projektleiter("E-Mail-Adresse") ON DELETE CASCADE  ON UPDATE CASCADE,
	FOREIGN KEY("Telefonnummer") REFERENCES Kunde("Telefonnummer") ON DELETE CASCADE ON UPDATE CASCADE 
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Bewertung (
	"BewertungID"			INTEGER NOT NULL,
	"Bepunktung" 			INTEGER NOT NULL  CHECK( "Bepunktung" in (1,2,3,4,5,6,7,8,9)),
	"Telefonnummer"			VARCHAR(11) NOT NULL,
	"ProjektID"				INTEGER NOT NULL,
	
	PRIMARY KEY("BewertungID"),	
	FOREIGN KEY("Telefonnummer") REFERENCES Kunde("Telefonnummer") ON DELETE CASCADE  ON UPDATE CASCADE,
	FOREIGN KEY("ProjektID") REFERENCES Projekt("ProjektID") ON DELETE CASCADE ON UPDATE CASCADE
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Optionaler_Text (
	"Optionaler_TextID"		INTEGER NOT NULL,
	"Text" 					Text NOT NULL CHECK("Text" != '' AND "Text" NOT GLOB '*[^ -~]*'),
	"BewertungID"			INTEGER NOT NULL,
	PRIMARY KEY("Optionaler_TextID"),	
	FOREIGN KEY("BewertungID") REFERENCES Bewertung("BewertungID") ON DELETE CASCADE ON UPDATE CASCADE
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Aufgabe (
	"AufgabeID"				INTEGER NOT NULL,
	"Beschreibung" 			Text NOT NULL,
	"Deadline"				DATE NOT NULL CHECK (Deadline is date("Deadline")),
	"Vermerk"				Text NOT NULL CHECK("Vermerk" != '' AND "Vermerk" NOT GLOB '*[^ -~]*'),
	"Status"				Text NOT NULL CHECK("Status" != '' AND "Status" NOT GLOB '*[^ -~]*'),
	"ProjektID"				INTEGER NOT NULL,
	PRIMARY KEY("AufgabeID"),	
	FOREIGN KEY("ProjektID") REFERENCES Projekt("ProjektID") ON DELETE CASCADE ON UPDATE CASCADE
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Spezialist (
	"E-Mail-Adresse"			TEXT NOT NULL COLLATE NOCASE,
	"Verfugbarkeitsstatus" 		Text NOT NULL CHECK("Verfugbarkeitsstatus" != '' AND "Verfugbarkeitsstatus" NOT GLOB '*[^ -~]*'),
	PRIMARY KEY("E-Mail-Adresse"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Nutzer("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE 
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Mentor (
	"Mentor"		TEXT NOT NULL COLLATE NOCASE,
	"Mentee"		TEXT NOT NULL COLLATE NOCASE,	
	PRIMARY KEY("Mentee"),	
	FOREIGN KEY("Mentee") REFERENCES Spezialist("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY("Mentor") REFERENCES Spezialist("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE 	
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Arbeitet_An (
	"E-Mail-Adresse"		TEXT NOT NULL COLLATE NOCASE,
	"ProjektID"				INTEGER NOT NULL,	
	PRIMARY KEY("E-Mail-Adresse","ProjektID"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Spezialist("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY("ProjektID") REFERENCES Projekt("ProjektID") ON DELETE CASCADE ON UPDATE CASCADE 	
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Fachlicher_Kompetenz (
	"Fachlicher_KompetenzID"		INTEGER NOT NULL,
	"Kompetenz"						Text NOT NULL CHECK (LOWER("Kompetenz") NOT GLOB '*[^a-z]*' AND "Kompetenz" != ''),
	PRIMARY KEY("Fachlicher_KompetenzID")		
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Spezialist_Hat_Fachlicher_Kompetenz (
	"Fachlicher_KompetenzID"	TEXT NOT NULL,
	"E-Mail-Adresse"			TEXT NOT NULL COLLATE NOCASE,	
	PRIMARY KEY("Fachlicher_KompetenzID","E-Mail-Adresse"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Spezialist("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY("Fachlicher_KompetenzID") REFERENCES Fachlicher_Kompetenz ("Fachlicher_KompetenzID") ON DELETE CASCADE ON UPDATE CASCADE 	
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Entwickler (
	"E-Mail-Adresse"		TEXT NOT NULL UNIQUE COLLATE NOCASE,
	"Kurzel" 				TEXT NOT NULL COLLATE NOCASE CHECK ( length("Kurzel")=8 AND LOWER("Kurzel") glob '[a-z][a-z][a-z][a-z][a-z][0-9][0-9][0-9]'),
	PRIMARY KEY("Kurzel"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Spezialist("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE 
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Programmiersprache (
	"ProgrammierspracheID"		INTEGER NOT NULL,
	"Name"						Text NOT NULL CHECK("Name" != '' AND "Name" NOT GLOB '*[^ -~]*'),	
	PRIMARY KEY("ProgrammierspracheID")		
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Beherrscht (
	"Kurzel"					TEXT NOT NULL COLLATE NOCASE,
	"ProgrammierspracheID"		INTEGER NOT NULL,	
	"Erfahrungsstufe"			INTEGER NOT NULL CHECK("Erfahrungsstufe" IN (1,2,3) ),
	PRIMARY KEY("Kurzel","ProgrammierspracheID"),	
	FOREIGN KEY("Kurzel") REFERENCES Entwickler("Kurzel") ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY("ProgrammierspracheID") REFERENCES Programmiersprache ("ProgrammierspracheID") ON DELETE CASCADE ON UPDATE CASCADE 	
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Designer (
	"E-Mail-Adresse"		TEXT NOT NULL COLLATE NOCASE,
	"Spezifikation" 		TEXT NOT NULL CHECK("Spezifikation" != '' AND "Spezifikation" NOT GLOB '*[^ -~]*'),
	PRIMARY KEY("E-Mail-Adresse"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Spezialist("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE 
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Optionale_Alias (
	"Optionale_AliasID"		INTEGER NOT NULL,
	"Alias" 				Text NOT NULL CHECK("Alias" != '' AND "Alias" NOT GLOB '*[^ -~]*'),
	PRIMARY KEY("Optionale_AliasID")
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Designer_Hat_Alias (
	"E-Mail-Adresse"	TEXT NOT NULL COLLATE NOCASE,
	"AliasID"			INTEGER NOT NULL,	
	PRIMARY KEY("E-Mail-Adresse"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Designer("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY("AliasID") REFERENCES Optionale_Alias("Optionale_AliasID") ON DELETE CASCADE ON UPDATE CASCADE 	
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Weitere_Kompetenz (
	"KompetenzID" 			INTEGER NOT NULL, ---fehler pdf
	"Kompetenz" 			TEXT NOT NULL CHECK ( LOWER("Kompetenz") NOT GLOB '*[^a-z ]*' AND "Kompetenz" != ''),
	PRIMARY KEY("KompetenzID")
);

-----------------------------------------------------------------------------------------------------------------------------

CREATE TABLE Designer_Hat_Weitere_Kompetenz (
	"KompetenzID"				INTEGER NOT NULL,
	"E-Mail-Adresse"			TEXT NOT NULL COLLATE NOCASE,	
	PRIMARY KEY("KompetenzID","E-Mail-Adresse"),	
	FOREIGN KEY("E-Mail-Adresse") REFERENCES Designer("E-Mail-Adresse") ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY("KompetenzID") REFERENCES Weitere_Kompetenz ("KompetenzID") ON DELETE CASCADE ON UPDATE CASCADE 	
);

-----------------------------------------------------------------------------------------------------------------------------
DROP TRIGGER IF EXISTS designer_is_not_entwickler;

CREATE TRIGGER designer_is_not_entwickler BEFORE INSERT ON Entwickler
BEGIN
    SELECT
        CASE
            WHEN EXISTS(
                    SELECT * FROM Designer
                    WHERE NEW."E-Mail-Adresse" = Designer."E-Mail-Adresse"
                )
            THEN RAISE(ABORT,'Designer kann nicht Entwickler sein')
        END;
END;

-------------------------------------------------------------------------------
DROP TRIGGER IF EXISTS entwickler_is_not_designer;

CREATE TRIGGER entwickler_is_not_designer BEFORE INSERT ON Designer
BEGIN
    SELECT
        CASE
            WHEN EXISTS(
                    SELECT * FROM entwickler
                    WHERE NEW."E-Mail-Adresse" = Entwickler."E-Mail-Adresse"
                )
            THEN RAISE(ABORT,'Entwickler kann nicht Designer sein')
        END;
END;

-------------------------------------------------------------------------------

DROP TRIGGER IF EXISTS prevent_insert_bewertung;

CREATE TRIGGER prevent_insert_bewertung 
BEFORE INSERT on Bewertung
BEGIN
	SELECT CASE	
		WHEN(	SELECT count(*)
				FROM Bewertung
				WHERE NEW.Telefonnummer = Telefonnummer
					AND NEW.ProjektID = ProjektID)>=3
		THEN 	RAISE(ABORT, 'MAX 3 BEWERTUNG')
	END;
END;

-------------------------------------------------------------------------------

DROP TRIGGER IF EXISTS prevent_delete_programmiersprache;
 
CREATE TRIGGER prevent_delete_programmiersprache
BEFORE DELETE on Programmiersprache
BEGIN
	SELECT CASE	
		WHEN(	SELECT count(*)
				FROM Beherrscht
				WHERE OLD.ProgrammierspracheID = ProgrammierspracheID)>0
		THEN 	RAISE(ABORT, 'Eine Programmiersprache kann nicht gelöscht werden, solange diese von jemandem beherrscht wird.')
	END;
END;
 
-------------------------------------------------------------------------------

DROP TRIGGER IF EXISTS prevent_Mentor_on_Mentor;
 
CREATE TRIGGER prevent_Mentor_on_Mentor
BEFORE INSERT on Mentor
BEGIN
	SELECT CASE	
		WHEN (	SELECT count(*)
				FROM Mentor
				WHERE NEW."Mentee" = "Mentor")>0
		THEN 	RAISE(ABORT, 'Mentor darf nicht gementort werden')
	END;
END;
  