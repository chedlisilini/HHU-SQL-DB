---Fragen: FLOAT Datentyp
---Kritische Punkte UNIQUE
---PK still alo FP
---Nur Fremdschlüssel wird kaskadiert?



------------------------------------------------
INSERT INTO Nutzer 
VALUES ('kunde1@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('kunde2@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Projektleiter1@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Projektleiter2@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Spezialist1@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Spezialist2@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Entwickler1@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Entwickler2@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Designer1@db.de' , 'adD89d');
INSERT INTO Nutzer 
VALUES ('Designer2@db.de' , 'adD89d');
-------------------------------------------------------------

INSERT INTO Kunde 
VALUES ('Kunde1@db.de','00123456789');
INSERT INTO Kunde 
VALUES ('Kunde2@db.de','01123456789');

--------------------------------------------------------------
INSERT INTO Projektleiter 
VALUES ('Projektleiter1@db.de', 7900);
INSERT INTO Projektleiter 
VALUES ('Projektleiter2@db.de', 8500);

-------------------------------------------------------------------
INSERT INTO Projekt 
VALUES (1, 'DB', '2022-12-30','00123456789','Projektleiter1@db.de');

INSERT INTO Projekt
VALUES ( 2, 'Propra2', '2022-12-31','01123456789','Projektleiter2@db.de');

INSERT INTO Projekt
VALUES ( 3, 'Test Projekt', '2022-12-16','00123456789','Projektleiter2@db.de');

-------------------------------------------------------------------
INSERT INTO Bewertung 
VALUES (1, 9,'01123456789',1);
INSERT INTO Bewertung 
VALUES (2, 8,'01123456789',1);
INSERT INTO Bewertung 
VALUES (3, 9,'01123456789',1);

INSERT INTO Bewertung 
VALUES (4, 7,'00123456789',1);
INSERT INTO Bewertung 
VALUES (5, 7,'00123456789',2);

INSERT INTO Bewertung 
VALUES (6, 9,'01123456789',2);


-------------------------------------------------------------------------
INSERT INTO Optionaler_Text 
VALUES (1,'good' , 1);

INSERT INTO Optionaler_Text 
VALUES (2,'well done' , 5);

--------------------------------------------------------------------------
--DELETE FROM Aufgabe
INSERT INTO Aufgabe 
VALUES (1,'Aufgabe01','2022-12-30','hoch','done', 1);

INSERT INTO Aufgabe 
VALUES (2,'Aufgabe02','2022-12-30','hoch' ,'waiting', 1);

INSERT INTO Aufgabe 
VALUES (3,'Aufgabe03','2022-12-30','hoch','waiting', 1);

INSERT INTO Aufgabe 
VALUES (4,'Aufgabe01','2022-12-30','hoch' ,'done', 2);

INSERT INTO Aufgabe 
VALUES (5,'Aufgabe02','2022-12-31','hoch', 'done', 2);

INSERT INTO Aufgabe 
VALUES (6,'Aufgabe03','2022-12-31','hoch' ,'waiting', 2);

INSERT INTO Aufgabe 
VALUES (7,'Aufgabe01','2022-12-31','hoch' ,'test', 3);

--------------------------------------------------------------------------
INSERT INTO Spezialist 
VALUES ('Spezialist1@db.de','verfuegbar');

INSERT INTO Spezialist 
VALUES ('Spezialist2@db.de','verfuegbar');

INSERT INTO Spezialist 
VALUES ('Designer1@db.de','verfuegbar');

INSERT INTO Spezialist 
VALUES ('Designer2@db.de','verfuegbar');

INSERT INTO Spezialist 
VALUES ('Entwickler2@db.de','verfuegbar');

INSERT INTO Spezialist 
VALUES ('Entwickler1@db.de','verfuegbar');

-------------------------------------------------------------------------

INSERT INTO Mentor 
VALUES ('Spezialist1@db.de','Spezialist2@db.de');
INSERT INTO Mentor 
VALUES ('Entwickler1@db.de','Entwickler2@db.de');

----------------------------------------------------------------------------
INSERT INTO Arbeitet_An 
VALUES ('Spezialist1@db.de',1);
INSERT INTO Arbeitet_An 
VALUES ('Entwickler1@db.de',1);
INSERT INTO Arbeitet_An 
VALUES ('Designer1@db.de',1);

INSERT INTO Arbeitet_An 
VALUES ('Spezialist1@db.de',2);
INSERT INTO Arbeitet_An 
VALUES ('Entwickler1@db.de',2);
INSERT INTO Arbeitet_An 
VALUES ('Designer2@db.de',2);
----------------------------------------------------------------------------

INSERT INTO Fachlicher_Kompetenz 
VALUES (1,'SoftwareEntwicklung');
INSERT INTO Fachlicher_Kompetenz 
VALUES (2,'Datenbaken');
INSERT INTO Fachlicher_Kompetenz 
VALUES (3,'Visualisirung');
INSERT INTO Fachlicher_Kompetenz 
VALUES (4,'DesignPrinciples');
INSERT INTO Fachlicher_Kompetenz 
VALUES (5,'Creativity');

----------------------------------------------------------------------------
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (4,'Spezialist2@db.de');
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (1,'Spezialist1@db.de');
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (1,'Entwickler1@db.de');
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (2,'Entwickler2@db.de');
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (4,'Designer1@db.de');
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (5,'Designer1@db.de');
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (4,'Designer2@db.de');
INSERT INTO Spezialist_Hat_Fachlicher_Kompetenz 
VALUES (5,'Designer2@db.de');

----------------------------------------------------------------------------

INSERT INTO Entwickler 
VALUES ('Entwickler1@db.de','aabaa123');
INSERT INTO Entwickler 
VALUES ('Entwickler2@db.de','bbabb123');

----------------------------------------------------------------------------

INSERT INTO Programmiersprache 
VALUES (1,'Java');
INSERT INTO Programmiersprache 
VALUES (2,'Sql');
INSERT INTO Programmiersprache 
VALUES (3,'Python');
INSERT INTO Programmiersprache 
VALUES (4,'Html');

----------------------------------------------------------------------------

INSERT INTO Beherrscht 
VALUES ('aabaa123',1,3);
INSERT INTO Beherrscht 
VALUES ('aabaa123',4,3);
INSERT INTO Beherrscht 
VALUES ('bbabb123',2,3);
INSERT INTO Beherrscht 
VALUES ('bbabb123',3,2);

----------------------------------------------------------------------------

INSERT INTO Designer 
VALUES ('Designer1@db.de', 'Digital');
INSERT INTO Designer 
VALUES ('Designer2@db.de','Grafik');

----------------------------------------------------------------------------

INSERT INTO Optionale_Alias 
VALUES (1, 'Alias1');
INSERT INTO Optionale_Alias 
VALUES (2,'Alias2');
----------------------------------------------------------------------------

INSERT INTO Designer_Hat_Alias 
VALUES ('Designer1@db.de',1);
INSERT INTO Designer_Hat_Alias 
VALUES ('Designer2@db.de',2);
----------------------------------------------------------------------------

INSERT INTO Weitere_Kompetenz 
VALUES (1,'Presentieren');
INSERT INTO Weitere_Kompetenz 
VALUES (2,'Kommunucation');
INSERT INTO Weitere_Kompetenz 
VALUES (3,'Problem Solving');

----------------------------------------------------------------------------
INSERT INTO Designer_Hat_Weitere_Kompetenz 
VALUES (1,'Designer1@db.de');
INSERT INTO Designer_Hat_Weitere_Kompetenz 
VALUES (1,'Designer2@db.de');
INSERT INTO Designer_Hat_Weitere_Kompetenz 
VALUES (2,'Designer1@db.de');
INSERT INTO Designer_Hat_Weitere_Kompetenz
VALUES (3,'Designer2@db.de');
------------------------------------------------------------------------------













SELECT
    B.BewertungID,
    B.Bepunktung,
    T.text
FROM Bewertung as B, Projekt as P
LEFT JOIN Optionaler_Text as T
ON T.BewertungID = B.BewertungID
WHERE B.ProjektID= P.ProjektID



SELECT
S.ROWID,
S.Verfugbarkeitsstatus,
S."E-Mail-Adresse",
N.Passwort
FROM  Arbeitet_An as A, Spezialist as S, Nutzer as N
WHERE A."E-Mail-Adresse" = S."E-Mail-Adresse"
AND  A."E-Mail-Adresse" = N."E-Mail-Adresse"
AND A.ProjektID = ? ;


INSERT INTO Programmiersprache
VALUES (5, 'C');


SELECT * FROM Programmiersprache
WHERE Name = 'Sql' ;

SELECT "E-Mail-Adresse", 'KUNDE' FROM Kunde
WHERE "E-Mail-Adresse" = ?
UNION
SELECT "E-Mail-Adresse", 'PROJEKTLEITER' FROM Projektleiter
WHERE "E-Mail-Adresse" = ?;


UPDATE Bewertung
SET  Bepunktung = ?
WHERE ROWID = ? ;


UPDATE Optionaler_Text
SET  Text = ?
WHERE BewertungID = ? ;


SELECT ROWID, * FROM Nutzer

