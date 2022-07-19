# Android-App des Forschungsprojektes
Ergänzend zu der forgestellten [Installationsanleitung für das Knüppelzähler Forschungsprojekt](https://github.com/jobdk/openfaas-function/blob/master/readme.md)
dient diese Datei als Guide mit dem folgenden Inhalt:

# Inhalt
1. Screens und Funktionen
2. Schnittstellen
3. Installation der App

---
# Screens/Dialoge und Funktionen
1. Home-Screen
2. Add-Screen
3. More Options Dialog
4. Details Screen
5. Row-Mapping Dialog
6. CSV-Export Dialog

## Home-Screen
Auf dem Home-Screen der Android-App stehen die folgenden Funktionen zur Verfügung:
1. <b>Hinzufügen</b> neuer Einträge (Siehe <b>Add-Screen</b>)
2. <b>Interaktion (Öffnen, Löschen, CSV-Export)</b> mit vorhandenen Einträgen (Siehe <b>More Options Dialog</b>)
3. <b>Suche</b> von Einträgen mit einer einfachen Textsuche

<img src="documentation/HomeScreen.png" width="200"/>

## Add-Screen
Auf den Add-Screen hat der Nutzer die Möglichkeit, neue Aufnahmen von Knüppelbildern an die OpenFaaS-Funktionen zu versenden. <br>
Dafür sind die folgenden Informationen erforderlich:
1. Titel des neu zu erstellenden Eintrags (Über ein einfaches Textfeld zur Identifikation)
2. Ein Bild des Knüppelhaufens

![AddScreen](documentation/AddScreen.png)

Für die <b>Bildauswahl</b> stehen zwei Optionen zur Verfügung:
1. Ein neues Bild aufnehmen mit der <b>Kamera</b> des Mobilen Endgerätes
2. Ein Bild aus dem <b>Dateisystem</b> des Mobilen Endgerätes hereinladen

Nach der Auswahl eines Bildes erscheint dieses in der Mitte des Bildschirms. Dem Nutzer stehen nun die folgenden Optionen zur Verfügung:
1. Mit dem <b>Add</b>-Button wird das Bild lediglich <b>lokal</b> gespeichert.
2. Mit dem <b>Evaluate</b>-Button wird das Bild an eine OpenFaaS-Funktion zur Auswertung versendet. Zusätzlich wird es lokal gespeichert.


#### ! Wichtig !
Damit die Algorithmen der App reibungslos funktionieren können ist es sinnvoll, dass die Balkenbilder möglichst parrallel zum Boden aufgenommen werden.<br>
Andernfalls kann es passieren, dass die Knüppel einer falschen <b>Zeile</b> zugeordnet werden und somit unerwartete Ergebnisse auftreten.


## More Options Dialog
Dieser Dialog stellt einige Interaktionsmöglichkeiten für bereits erstellte Einträge zur Verfügung:

![MoreOptionsDialog](documentation/MoreOptionsDialog.png)

1. <b>Öffnen</b> - Navigiert den Nutzer zum <b>Details-Screen</b> des Eintrags
2. <b>Änderungen hochladen</b> - Die lokal vorgenommenen Änderungen an den <b>Persister</b> senden
3. <b>CSV-Export</b> - Die Knüppel-Positionen und Beschriftungen als CSV exportieren (Siehe <b>CSV-Export Dialog</b>)
4. <b>Löschen</b> - Löscht den Eintrag aus der lokalen Datenbank


## Details-Screen
In diesem Screen kann der Nutzer manuelle Anpassungen der Knüppel-Positionen und Beschriftungen vornehmen.<br>
![DetailsScreen](documentation/DetailsScreen.png)

In dem unteren Bereich des Screens stehen Optionen für die visuelle Hervorhebung der Boxen zur verfügung. <br>
Dabei kann der Nutzer die <b>Farbstärke (Opacity)</b> und <b>Breite</b> des Randes der erkannten Knüppel anpassen wodurch Lücken im Knüppelhaufen leicht auszumachen sind <br>

![DetailsScreenLücken](documentation/DetailsScreenLücken.png)

Für die Interaktion mit dem Bild stehen dem Nutzer einige Möglichkeiten zur Verfügung:
* <b>Click</b> auf einen Knüppel - Selektiert den Knüppel und hebt diesen Farblich hervor.<br>
  Selektierte Knüppel können gelöscht oder dessen Beschriftung angepasst werden.
* <b>Long Click</b> auf einen Knüppel - Startet ein Drag and Drop mit dem Knüppel.<br>
  Dieser kann in einen beliebigen Bereich des Bildes verschoben werden.
* <b>Long Click</b> auf eine freie Stelle - Erstellt einen neuen Knüppel-Eintrag mit leerer Beschriftung auf der interagierten Fläche.

![DetailsScreenSelected](documentation/DetailsScreenSelected.png)

Mit dem Click auf das Burger Menu unten rechts in der Ecke, kann der Nutzer zum <b>Row-Mapping Dialog</b> navigieren.


## Row-Mapping Dialog
Mit diesem Dialog ist es möglich, die Knüppel-Positionen und Beschriftungen in Zusammenfassung darzustellen.<br>
Dabei werden die Knüppel in der von NDW spezifizierten Form formatiert und angezeigt.<br>
Die folgende Abbildung stellt diesen Sachverhalt dar:

![Row-MappingDialog](documentation/RowMappingDialog.png)

## CSV-Export Dialog
Mit dem CSV-Export Dialog ist es möglich, die Knüppel in der vom Row-Mapping Dialog angezeigten Form als CSV zu exportieren. <br>
Für den Export dieser Informationen werden dabei lediglich die folgenden Informationen benötigt:
1. Der <b>Name</b> der Datei (Standardwert ist der Name des Eintrags)
2. Checkbox (True/False), ob ein <b>Header</b> mit Spaltenbeschriftungen der Datei hinzugefügt werden soll.

![CsvExportDialog](documentation/CsvExportDialog.png)

---
# Schnittstellen
Die OpenFaaS-Funktionen werden auf einer VM der Hoschule Furtwangen gehostet und sind zum Stand des Forschungsprojektes ausschließlich über das hochschulinterne <b>VPN</b> erreichbar.

## barextractorfunction
**Adresse**: http://141.28.73.147:8080/function/barextractorfunction

**Request-Body:**<br>
Sendet ein Bild an die <b>OpenFaaS-barextractorfunction</b> in dem folgenden Format:

```json
{
  "name": "Knüppelhaufen",
  "time": 1220227200,
  "extension": "png",
  "image": "<Base64 codiertes Bild>",
  "result": null
}
```
**Response-Body:**<br>
Gibt den Text und die Koordinaten der erkannten Knüppel zurück:

```json
[
  {
    "caption": "GM",
    "left": 129.60374069213867,
    "top": 68.19547271728516,
    "right": 198.8857421875,
    "bottom": 139.75296020507812
  },
  {
    "caption": "GM",
    "left": 69.86802673339844,
    "top": 0.5825424194335938,
    "right": 139.07223510742188,
    "bottom": 70.78595733642578
  }
  ...
]
```

## persistresultfunction
**Adresse**: http://141.28.73.147:8080/function/persistresultfunction

**Body**:<br>
Sendet ein angepasstes Ergebnis an die <b>OpenFaaS-persistresultfunction</b> in dem folgenden Format:
```json
{
  "name": "Knüppelhaufen",
  "time": 1220227200,
  "extension": null,
  "image": null,
  "result": [
    {
      "caption": "GM",
      "left": 129.60374069213867,
      "top": 68.19547271728516,
      "right": 198.8857421875,
      "bottom": 139.75296020507812
    },
    {
      "caption": "GM",
      "left": 69.86802673339844,
      "top": 0.5825424194335938,
      "right": 139.07223510742188,
      "bottom": 70.78595733642578
    }
    ...
  ]
}
```
**Response:**<br>
Gibt zurück, ob der Upload erfolgreich war in Form eines <b>HTTP-Statuscodes</b>.

---
# Installation der App

## Debug-Modus
Die Installation der Android App auf einem Mobilen Endgerät erfolgt zum Stand des Forschungsprojektes ausschließlich über die IDE Android Studio im Debug-Modus.<br>
Hierfür müssen die Entwickleroptionen des Android Gerätes aktiviert werden. Die Applikation kann nun mit <b>USB-Debugging</b> oder über <b>WLAN</b> installiert werden. <br>
Ein Artikel für die Installation einer App auf einem physischen Endgerät im <b>Debug-Modus</b> kann [hier](https://developer.android.com/studio/run/device) gefunden werden.

## Produktiv-Modus
Um eine Android-App im Produktiv-Modus nutzen zu können, muss eine <b>APK</b> aus dieser erstellt werden. Dadurch ist es sehr einfach möglich, die App auf mehrere Engeräte zu verteilen.
Da für die Erstellung einer <b>APK</b> einige sensible Daten festgelegt werden müssen, wurde diese im Rahmen des Forschungsprojektes nicht erstellt. <br>
Einen Leitfaden für die Erstellung einer solchen <b>APK</b> kann [hier](https://developer.android.com/studio/publish) gefunden werden. 
  