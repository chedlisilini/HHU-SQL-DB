-- 1)
-- Projekte (alle Attribute) aus, die mindestens drei Aufgaben mit hoher Priorit¨at haben

SELECT * FROM Projekt
WHERE (	SELECT COUNT(*) FROM Aufgabe 
		WHERE Projekt.ProjektID = Aufgabe.ProjektID 
		GROUP BY Vermerk HAVING lower(Vermerk) = 'hoch' )  >=3;

		
-- 2) 
-- das Projekt mit der besten Durchschnittsbewertung

SELECT 	P.*,
		avg(Bepunktung) as Average_Bepunktung
FROM Bewertung as B, Projekt as P 
WHERE B.ProjektID = P.ProjektID
GROUP BY B.ProjektID 
ORDER BY Average_Bepunktung DESC
LIMIT 1;


-- 3)
-- in alphabetischer Reihenfolge die E-Mail-Adressen genau der Nutzer aus,
-- die nochkein Projekt in Auftrag gegeben haben

SELECT 	*
FROM Nutzer as N 
WHERE "E-Mail-Adresse" 
	  NOT IN(       SELECT Kunde."E-Mail-Adresse"
					FROM Kunde, Projekt
					WHERE Kunde.Telefonnummer = Projekt.Telefonnummer)
ORDER BY "E-Mail-Adresse" ASC;
