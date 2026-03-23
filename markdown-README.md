#+TITLE: Fi-Lo-Games

####### Attentie #######
Dit is een kopie van het bestand README.org omdat dit volgens de eisen van het opdracht in .md geleverd moet worden. Wil je de volledige functionaliteit van org-mode gebruiken? Open dan README.org met Emacs of een andere IDE met org-mode functionaliteit.
########################

# Inhoud
* Inleiding
* Benodigdheden (met minimale versienummers)
* Projectstructuur
* Installatie
* Overige Informatie

GitHub pagina: https://github.com/benssoon/fi-lo-games-board-game-scheduler

API Documentatie: https://app.swaggerhub.com/apis-docs/benz-2f9/Fi-Lo-Games/1.0.0?view=uiDocs

* Inleiding
De FiLoGames web-API is gebouwd met de Spring Framework en de frontend applicatie is gebouwd met React. FiLoGames is ontworpen om borspel fans andere lokale fans te vinden en daar evenementen mee te plannen. Zoek makkelijk op bestaande evenementen met een specifiek spel.
* Benodigdheden (met minimale versienummers)
** Voor de backend
- PostgreSQL
  + Minimale versie: 14.2
  + PostgreSQL is een systeem waarmee een gebruiker databases kunt creëren en beheren.
- Apache Maven
  + Minimale versie: 3.6.3
  + Apache Maven is een tool om o.a. Java applicaties te compileren en beheren en om de dependencies daarvan te beheren.
- Java
  + Minimale JDK versie: 21.0.9
  + Java is de programmeer taal waarin de applicatie geschreven is. Een installatie van Java is verplicht om Maven te kunnen gebruiken.
- pgAdmin 4 (Indien wenselijk)
  + Minimale versie: 9.2
  + pgAdmin is een applicatie waarmee je via een GUI een database kunt aamaken en data uit de database kunt aflezen en SQL queries uitvoeren. In principe is alle functionaliteit van pgAdmin ook mogelijk met PostgreSQL via de command line.
- Postman (Niet verplicht)
  + Minimale versie: 11.72.3
  + Postman is een applicatie waarmee de gebruiker HTTP requests kunt uitvoeren. Dit is vooral handig bij het testen van een web-API, zonder dat er een volledige frontend applicatie gebouwd hoeft te worden. De frontend FiLoGames applicatie kan ook gebruikt worden om de endpoints te triggeren en kan dus in plaats van Postman gebruikt worden.
** Voor de frontend
- Node.js
  + Minimale versie: 22.15.0
  + Node.js is een runtime environment waarin applicaties die in JavaScript gescreven kunnen draaien.
- nvm
  + Minimale versie: 0.40.3
  + Nvm (Node Version Manager) wordt gebruikt om Node.js te installeren.
- npm
  + Minimale versie: 10.9.2
  + Npm (Node package manager) is de pakketbeheerder voor Node.js en biedt commando's o.a. om de frontend applicatie te draaien.
* Projectstructuur
Deze applicatie bestaat uit een frontend en een backend applicatie. De frontend applicatie is geschreven in JavaScript en gebouwd met de JavaScript library React. De backend applicatie is geschreven in Java en gebouwd met de Spring Framework.
* Installatie
1. Clone deze repository naar jouw eigen systeem.
2. Installeer indien nodig PostgreSQL.
   - https://www.postgresql.org/download/
   - Selecteer jouw OS en volg de aangegeven instructies.
3. Maak een nieuwe database aan.
   - Volg eventueel de instructies in de PostgreSQL documentatie om het te installeren en om een database aan te maken:
     + https://www.postgresql.org/docs/18/tutorial-start.html
   - *Let op: afhankelijk van de setup op jouw systeem kan het zijn dat je een nieuwe rol moet aanmaken voordat je een database kunt aanmaken. Zie https://www.postgresql.org/docs/18/database-roles.html voor meer informatie.*
4. Installeer indien nodig Apache Maven.
   - https://maven.apache.org/download.cgi
   - Download de gewenste installatie package (minimaal versie 3.6.3).
   - Volg eventueel de installatie instructies op:
     + https://maven.apache.org/install.html.
5. Installeer indien nodig Java.
   - https://www.oracle.com/nl/java/technologies/downloads/#java21
   - Selecteer jouw OS en download de gewenste installatie package.
   - Volg eventueel de installatie instructies op:
     + https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html
6. Installeer indien wenselijk pgAdmin 4
   - https://www.pgadmin.org/download/
   - Selecteer de gewenste installatiemethode en volg de instructies.
7. Installeer indien wenselijk Postman
   - https://www.postman.com/downloads/
   - Als alternatief voor het downloaden en installeren van Postman kan het ook in de browser gebruikt worden, door op de download pagina op "Try the Web Version" te klikken.
8. Installeer Node.js
   - https://nodejs.org/en/download
   - Voer hier de gewenste versie (minimaal 22.15.0) en jouw OS in. Kies "using *nvm* with *npm*".
   - Voer de aangeven commando's uit.
   - Dit moet minimaal de nodige versies van nvm, npm en node.js installeeren.
9. Pas de applicatie configuratie aan voor jouw database  
   - Open [[file:backend/fi-lo-games/src/main/resources/application.properties][application.properties]].
   - Zorg dat er een regel is met deze code:
     + ~spring.datasource.url=jdbc:postgresql://localhost:5432/fi-lo-games~
     + Zorg dat de naam van jouw database overeenkomt met de naam aan het eind van die regel.
     + De standaard naam die de applicatie verwacht is "fi-lo-games". Mocht je jouw database een andere naam willen geven, pas die naam dan hier aan.
   - Zorg dat jouw gebruikersnaam voor PostgreSQL overeenkomt met de in dit bestand aangegeven gebruikersnaam:
     + ~spring.datasource.username=<jouw-postgres-gebruikersnaam>~
     + Vervang <jouw-postgres-gebruikersnaam> met de gebruikersnaam die je hebt aangemaakt bij het instellen van PostgreSQL.
   - Zorg dat jouw wachtwoord voor PostgreSQL overeenkomt met het in dit bestand aangegeven wachtwoord:
     + Maak indien wenselijk een omgevingsvariabele aan met de naam POSTGRESQL_PASSWORD.
       * ~spring.datasource.password=${POSTGRESQL_PASSWORD}~
       * Stel de waarde van POSTGRESQL_PASSWORD in op het wachtwoord dat je hebt aangemaakt bij het instellen van PostgreSQL. In bash is dat bijvoorbeeld met de commando ~export POSTGRESQL_PASSWORD:"<jouw-wachtwoord>"~
       * Zorg dat de variabele permanent is aangemaakt (bijvoorbeeld om het in je ~.bashrc~ op te slaan, bij het gebruik van Linux).
     + Mocht je geen omgevingsvariabele hebben aangemaakt:
       * ~spring.datasource.password=<jouw-postgres-wachtwoord>~
       * Vervang dan <jouw-postgres-wachtwoord> met het wachtwoord dat je hebt aangemaakt bij het instellen van PostgreSQL.
10. Voeg een map voor uploads toe.
    - Maak een map ergens op jouw systeem met een naam die jij kiest.
    - Maak een omgevingsvariabele aan met de naam FILO_IMAGES en geef het als waarde de path naar de net aangemaakte map.
    - Zorg dat de variabele permanent is aangemaakt (bijvoorbeeld om het in je ~.bashrc~ op te slaan, bij het gebruik van Linux).
11. Draai de backend applicatie.
    - In een shell sessie, navigeer naar de backend root map.
      + bijvoorbeeld met de commando ~cd <repository-root-map>/backend/fi-lo-games~
      + link naar de map: [[backend/fi-lo-games][<repository-root-map>/backend/fi-lo-games]]
    - Voer de commando ~mvn spring-boot:run~ uit.
    - Als er geen tekst meer gelogd wordt en de laatste regel eindigt met "Started FiLoGamesApplication in 5.924 seconds (process running for 6.216)" of iets vergelijkbaars, dan is de applicatie succesvol gestart.
12. Draai de frontend applicatie.
    - In een andere shell sessie, navigeer naar de frontend root map.
      + bijvoorbeeld met de commando ~cd <repository-root-map>/frontend~
      + link naar de map: [[frontend][<repository-root-map>/frontend]]
    - Voer de commando ~npm install~ uit.
    - Voer de commando ~npm run dev~ uit.
    - Als de regel ~➜  Local:   http://localhost:5173/~ verschijnt, dan is de applicatie succesvol gestart. Dat adres is het adres waar de applicatie gedraaid wordt.
    - Voer het adres in de browser in of klik er direct op op de applicatie in de browser te openen.
      
* Overige Informatie
** Tests
De tests in de applicatie kunnen met Apache Maven gedraaid worden. Dit kan door vanuit de [[backend/fi-lo-games/][backend root map]] de commando ~mvn test~ uit te voeren.
** Standaardgebruikers
Er zijn een aantal standaard gebruikers al aangemaakt in [[backend/fi-lo-games/src/main/resources/data.sql][data.sql]]. Hierbij een overzicht van hun inloggegevens en gekoppelde rollen (authorisatie niveaus):
- ben
  + gebruikersnaam: ben
  + wachtwoord: 1234
  + rollen:
    * ROLE_ADMIN
    * ROLE_USER
- ellen
  + gebruikersnaam: ellen
  + wachtwoord: 1234
  + rollen:
    * ROLE_USER
- bob
  + gebruikersnaam: bob
  + wachtwoord: 1234
  + rollen:
    * ROLE_USER
- test_admin
  + gebruikersnaam: test_admin
  + wachtwoord: 1234
  + rollen:
    * ROLE_ADMIN
- test_user
  + gebruikersnaam: test_user
  + wachtwoord: 1234
  + rollen:
    * ROLE_USER
- test_none
  + gebruikersnaam: test_none
  + wachtwoord: 1234
  + rollen: geen
