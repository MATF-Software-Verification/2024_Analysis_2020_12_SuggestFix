# SuggestFix - Nadogradnja sistema za davanje sugestija za poboljšanje kvaliteta Java koda

**Autor:** Marko Radosavljevic (1010/2024)

**Napomena:** Ovaj dokument opisuje nadogradnju postojećeg SuggestFix projekta. Osnovni projekat je već sadržao 8 tipova sugestija, a ova nadogradnja dodaje 5 novih sugestija fokusiranih na čitljivost i bezbednost koda.

---

## 1. OPIS PROBLEMA

### 1.1 Motivacija

Kvalitet izvornog koda direktno utiče na održivost, čitljivost i pouzdanost softvera. Java programeri često pišu kod koji je funkcionalno ispravan ali ne koristi najbolje prakse jezika, što može dovesti do:
- Smanjene čitljivosti i razumljivosti koda
- Potencijalnih runtime grešaka
- Otežanog održavanja i refaktorisanja
- Lošijih performansi

### 1.2 Cilj projekta

SuggestFix sistem automatski analizira Java kod i identifikuje sintaksne konstrukcije koje mogu biti zamenjene efikasnijim, čitljivijim ili sigurnijim alternativama. Za svaku detektovanu konstrukciju, sistem prikazuje trenutni kod i predlaže poboljšanu verziju.

### 1.3 Obuhvat nadogradnje

Osnovni projekat je već sadržao sugestije za:
- Spajanje definicije i inicijalizacije promenljivih
- Detekciju neiskorišćenih promenljivih i parametara  
- Uklanjanje redundantne inicijalizacije polja
- Transformaciju while petlje u for petlju
- Korišćenje Optional umesto vraćanja null vrednosti
- Razdvajanje višestrukih izuzetaka
- Zamenu konkatenacije stringova sa StringBuilder
- Poboljšanje identifikatora

**Ova nadogradnja dodaje 5 novih tipova sugestija:**

| Oznaka | Tip sugestije | Fokus |
|--------|---------------|-------|
| **f** | NESTED_IF_TO_SINGLE_IF | Kombinovanje ugnježdenih if uslova |
| **t** | IF_ELSE_TO_TERNARY | Zamena if-else sa ternarnim operatorom |
| **l** | FOR_LOOP_TO_FOR_EACH | Transformacija for petlje u for-each |
| **s** | STRING_EQUALITY_COMPARISON | Korišćenje .equals() umesto == |
| **a** | SAFE_CAST | Dodavanje instanceof provere pre kastovanja |

---

## 2. ARHITEKTURA SISTEMA

### 2.1 Tehnološka osnova

Sistem koristi **JavaParser biblioteku** za parsiranje Java koda u Apstraktno Sintaksno Stablo (AST). Umesto tekstualne analize ili regex-a, AST pristup omogućava semantičko razumevanje strukture koda nezavisno od formatiranja.

**Ključna prednost AST pristupa:** Sistem razume kontekst - može razlikovati `==` operator za poređenje objekata od `==` u aritmetičkim izrazima, može navigirati kroz parent/child odnose u kodu, i precizno identifikovati sintaksne konstrukcije.

### 2.2 Osnovni moduli

#### Main modul
Ulazna tačka aplikacije koja parsira argumente komandne linije (`-f` za fajl, `-s` za selekciju sugestija), validira fajl i pokreće analizu.

#### ASTUtil
Wrapper oko JavaParser biblioteke koji parsira Java fajl u CompilationUnit (root AST čvor) i pokreće visitor za obilazak stabla.

#### ASTVisitor - Srce analize
Implementira Visitor pattern (nasleđuje `VoidVisitorAdapter<Void>` iz JavaParser-a). Override-uje metode za posećivanje različitih tipova AST čvorova:

- **visit(BlockStmt)** - Analizira blokove koda
  - Priprema: Prikuplja String promenljive za kasniju analizu
  - Detekcija: Identifikuje nested if, if-else-return konstrukcije, for petlje
  
- **visit(MethodDeclaration)** - Analizira deklaracije metoda
  - Prikuplja String parametre za analizu equality comparison

- **visit(FieldDeclaration)** - Analizira polja klase
  - Prikuplja String polja za analizu equality comparison

- **visit(BinaryExpr)** - Analizira binarne izraze
  - Detektuje `==` operator na String tipovima

- **visit(CastExpr)** - Analizira cast izraze
  - Detektuje kastovanje bez instanceof provere

Visitor koristi flag checking da izvršava samo aktivne sugestije, optimizujući performanse kada korisnik bira samo određene tipove analiza.

#### Suggestion moduli
Svaka sugestija je implementirana kao zasebna klasa sa specifičnom logikom za detekciju antipaterna. Modularna struktura omogućava lako dodavanje novih sugestija bez menjanja postojećih.

#### SuggestionUtil
Centralni repozitorijum svih detektovanih sugestija. Sortira sugestije po tipu, formatira ih prema specific šablonima i generiše finalni izveštaj sa color coding-om za terminal.

### 2.3 Tok izvršavanja

1. **Inicijalizacija:** Parsiranje argumenata i mapiranje u tipove sugestija
2. **Parsiranje:** Kreiranje AST stabla iz Java fajla
3. **Analiza:** Visitor obilazi AST stablo i poziva relevantne Suggestion module
4. **Prikazivanje:** Formatiranje i ispis svih detektovanih sugestija

Ključna karakteristika je **jedan prolaz kroz stablo** - sve sugestije se detektuju u jednom obilasu AST-a, što optimizuje performanse.

---

## 3. OPIS NOVIH SUGESTIJA

### 3.1 NESTED_IF_TO_SINGLE_IF - Kombinovanje ugnježdenih if-ova

**Cilj:** Smanjenje nivoa indentacije i povećanje čitljivosti kombinovanjem ugnježdenih if naredbi koje nemaju else grane.

**Šta detektuje:**
Pattern gde spoljašnji if sadrži samo jedan unutrašnji if, oba bez else grana. Ovakva struktura se može pojednostaviti korišćenjem logičkog `&&` operatora.

**Primer:**
```java
// Pre:
if (user != null) {
    if (user.isActive()) {
        processUser(user);
    }
}

// Posle:
if ((user != null) && (user.isActive())) {
    processUser(user);
}
```

**Analiza:**
Metoda `checkAndSuggestNestedIfToSingleIf` prolazi kroz sve statement-e u bloku i za svaki if proverava da li njegov then blok sadrži tačno jedan statement koji je takođe if bez else grane. Ako su uslovi ispunjeni, generiše se sugestija sa kombinovanim uslovom.

**Prednosti transformacije:**
- Smanjena indentacija (jedan nivo umesto dva)
- Jasniji logički uslov (eksplicitno prikazuje AND relaciju)
- Lakše čitanje koda

### 3.2 IF_ELSE_TO_TERNARY - Ternarni operator za return

**Cilj:** Zamena jednostavnih if-else konstrukcija sa return naredbama ternarnim operatorom radi konciznosti.

**Šta detektuje:**
If-else blokove gde obe grane sadrže samo return naredbu sa vrednošću. Ovo je najčešći i najbezbedniji slučaj za primenu ternarnog operatora.

**Primer:**
```java
// Pre:
if (age >= 18) {
    return "Adult";
} else {
    return "Minor";
}

// Posle:
return age >= 18 ? "Adult" : "Minor";
```

**Analiza:**
Metoda `checkAndSuggestIfElseToTernary` analizira if naredbe koje imaju else granu. Proverava da li obe grane sadrže tačno jedan statement koji je return naredba, i ako jesu, generiše ternarni operator.

**Prednosti transformacije:**
- Drastično kraći kod (sa 5 linija na 1)
- Jasnije pokazuje da je u pitanju uslovni return
- Idiomatski Java stil za proste uslovne returnove

### 3.3 FOR_LOOP_TO_FOR_EACH - Enhanced for petlja

**Cilj:** Transformacija tradicionalnih for petlji koje iteriraju kroz kolekcije u moderniju for-each sintaksu.

**Šta detektuje:**
For petlje koje prate standardni pattern iteracije: počinju od 0, idu do `collection.size()` ili `array.length`, inkrementiraju indeks za 1, i u telu pristupaju elementima samo preko tog indeksa.

**Primer:**
```java
// Pre:
for (int i = 0; i < students.size(); i++) {
    System.out.println(students.get(i));
}

// Posle:
for (Object student : students) {
    System.out.println(student);
}
```

**Analiza:**
Najkompleksnija od novih sugestija. Metoda `changeForLoopToForEach` analizira:
1. **Inicijalizaciju:** Da li počinje od 0
2. **Uslov:** Da li je `< size()` ili `< length`
3. **Update:** Da li je prosti inkrement
4. **Telo petlje:** Da li se indeks koristi samo za pristup elementima

Kritične provere uključuju detekciju da li se kolekcija menja u petlji (što bi uzrokovalo `ConcurrentModificationException` u for-each) i da li se indeks koristi za nešto drugo osim pristupa elementima.

**Prednosti transformacije:**
- Mnogo čitljiviji kod
- Eliminacija mogućnosti za off-by-one greške
- Kraći i jasniji kod
- Moderan Java stil

### 3.4 STRING_EQUALITY_COMPARISON - Korišćenje .equals()

**Cilj:** Prevencija česte greške poređenja stringova sa `==` operatorom koji poredi reference, ne sadržaj.

**Problem:**
U Java-i, `==` poredi reference objekata. Za stringove ovo može "slučajno raditi" zbog načina na koji string funkcioniše, ali je nepouzdano i česta je greška:
```java
String s1 = new String("test");
String s2 = new String("test");
s1 == s2;        // false - različite reference
s1.equals(s2);   // true - isti sadržaj
```

**Šta detektuje:**
Korišćenje `==` ili `!=` operatora na String tipovima. Sistem koristi dvofazni pristup:
1. **Prikupljanje podataka:** Pamti sve String promenljive, parametre i polja u HashMap
2. **Detekcija:** Analizira binarne izraze i koristi HashMap za brzu proveru tipova

**Primer:**
```java
// Pre:
if (input == "admin") { ... }

// Posle:
if ("admin".equals(input)) { ... }
```

**Analiza:**
Metoda `checkStringEqualityComparison` proverava sve binarne izraze sa `==` ili `!=` operatorima. Ignoriše null provere (koje su validne sa `==`). Za detektovane String poređenja, generiše `.equals()` poziv, sa heuristikom da literal poziva metodu radi bezbednosti rada sa promenljivama koje su null.

**Prednosti transformacije:**
- Ispravno semantičko poređenje sadržaja
- Prevencija teških bugova
- Null-safe pattern kada je literal caller

### 3.5 SAFE_CAST - Prevencija ClassCastException

**Cilj:** Dodavanje `instanceof` provere pre kastovanja objekta kako bi se sprečila `ClassCastException` u runtime-u.

**Šta detektuje:**
Kastovanje objekata (ne primitivnih tipova) bez eksplicitne instanceof provere. Sistem razlikuje tri vrste cast operacija:
1. **Deklaracije promenljivih** - koristi pattern matching sintaksu (Java 16+)
2. **Dodele na lokalne promenljive** - koristi pattern matching sintaksu
3. **Pozive metoda** - koristi klasičnu instanceof sintaksu

**Primeri:**

**Deklaracija promenljive:**
```java
// Pre:
String str = (String) obj;

// Posle:
if (obj instanceof String str) {
    // TODO: Move all usages of 'str' here
}
```

**Poziv metode:**
```java
// Pre:
processString((String) obj);

// Posle:
if (obj instanceof String) {
    processString((String) obj);
}
```

**Analiza:**
Metoda `checkForUnsafeCast` filtrira primitivne kastove i problematične slučajeve:
- **Klasna polja** - ne sugeriše transformaciju jer bi ograničila doseg polja
- **Lokalne promenljive** - sugeriše sa TODO komentarom
- **Pozivi metoda** - sugeriše klasičnu instanceof sintaksu

**Prednosti transformacije:**
- Prevencija runtime exceptions
- Eksplicitna provera tipa pre kastovanja

---

## 4. KLJUČNE ARHITEKTURNE ODLUKE

### 4.1 Jedan prolaz kroz AST

Sve sugestije se detektuju u jednom prolazu kroz stablo. Parsiranje je najskuplija operacija, višestruki prolazi bi značajno usporili sistem. Kompromis je pažljivo dizajniran redosled visit() metoda - neke sugestije zahtevaju fazu pripreme za prikupljanje podataka pre glavne analize.

### 4.2 Modularna struktura

Svaka sugestija je zasebna klasa, što omogućava:
- Dodavanje novih sugestija bez menjanja postojećih
- Nezavisno testiranje svake sugestije
- Selektivno aktiviranje sugestija koje korisnik želi

### 4.3 Priprema podataka za String tipove

Pošto JavaParser bez dodatnih alata ne radi type resolution, sistem koristi HashMap za pamćenje String identifikatora u fazi pripreme.

### 4.4 Samo sugestije, ne automatske izmene

Sistem ne menja kod automatski već samo sugeriše izmene. Razlozi:
- Sigurnost - automatska transformacija može uvesti greške
- Kontrola - programer zadržava finalno odlučivanje
- Kontekst - sistem ne razume punu poslovnu logiku

---

## 5. TEHNIČKA IMPLEMENTACIJA

### 5.1 Korišćene tehnologije

- **Java 17** - Programski jezik
- **JavaParser 3.20.2** - Biblioteka za parsiranje i AST analizu
- **Gradle** - Build sistem
- **ANSI Escape Codes** - Color coding terminala

### 5.2 Pokretanje

```bash
java -jar SuggestFix.jar -f <fileName> -s <suggestions>

Primeri:
java -jar SuggestFix.jar -f ForLoopToForEachExample
java -jar SuggestFix.jar -f StringEqualityExample -s ls
java -jar SuggestFix.jar -f SafeCastExample -s ftlsa
```

Oznake: **f** (nested if), **t** (ternary), **l** (for-each), **s** (string equals), **a** (safe cast)

---

## 6. REZULTATI I EVALUACIJA

### 6.1 Prednosti sistema

- **Brzina:** Jedan prolaz kroz fajl, brza analiza
- **Modularnost:** 5 novih sugestija dodato bez menjanja postojećih 8
- **Preciznost:** AST pristup eliminise false positives od regex-a
- **Edukativnost:** Prikazivanje pre/posle koda pomaže učenju
- **Praktičnost:** CLI alat pogodan za CI/CD integraciju

### 6.2 Ograničenja

- Analiza samo jednog fajla odjednom
- Ne primenjuje automatske izmene

### 6.3 Buduća poboljšanja

Potencijalna proširenja sistema:
- Multi-file analiza celih projekata
- Integracija JavaSymbolSolver za preciznu type resolution
- Automatska primena izmena sa `--fix` flagom
- IDE plugin za IntelliJ IDEA
- JSON/HTML/Markdown izvoz rezultata

---

## 7. ZAKLJUČAK

Ova nadogradnja dodaje 5 novih sugestija koje se fokusiraju na čitljivost (nested if, ternary, for-each) i bezbednost (string equals, safe cast) Java koda. Korišćenjem AST analize i Visitor pattern-a, sistem omogućava preciznu detekciju antipaterna uz održavanje dobre performance.

Modularna arhitektura omogućila je dodavanje novih sugestija bez modifikacije postojećeg koda, pokazujući dobru ekstenzibilnost dizajna. Sistem je praktičan alat za:
- Poboljšanje kvaliteta koda
- Učenje Java best practices
- Code review automatizaciju
- Integraciju u development workflow

Iako ima ograničenja (lokalna analiza, bez type resolution), sistem postiže dobar balans između dubine analize i praktične upotrebljivosti, čineći ga korisnim alatom u svakodnevnom razvoju softvera.
