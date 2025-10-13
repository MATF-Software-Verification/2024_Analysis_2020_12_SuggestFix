# Davanje sugestija za popravljanje kvaliteta izvornog koda

## Opis ideje

- Osnovna ideja projekta je davanje sugestija kako se u programskom jeziku *Java* određeni sintaksni konstrukti
  mogu zameniti sintaksnim konstruktima koji su poželjniji.

## Pokretanje aplikacije :hammer:

Projekat koristi Gradle sistem za kompilaciju, pa se sve potrebne biblioteke automatski preuzimaju.

### Kompilacija

```bash
# Kompilacija projekta i kreiranje JAR fajla sa svim zavisnostima
./gradlew clean jar
```

JAR fajl će biti kreiran u: `build/libs/SuggestFix-1.0-SNAPSHOT.jar`


### Argumenti kojima se kontroliše izvršavanje
```
Arguments usage: fileName [-f fileNameToAnalyze(relative path to file and name without .java)]
[-s wantedSuggestions (
i - IDENTIFIER_ASSIGNMENT,
v - VARIABLE_DEFINED_NOT_USED, 
p - PARAMETER_NOT_USED, 
r - REDUNDANT_INITIALIZATION, 
w - WHILE_TO_FOR, 
n - VARIABLE_CAN_BE_NULL, 
e - EXCEPTION_SPLIT 
c - STRING_CONCATENATION
f - NESTED_IF_TO_SINGLE_IF
t - IF_ELSE_TO_TERNARY
l - FOR_LOOP_TO_FOR_EACH
s - STRING_EQUALITY_COMPARISON
a - SAFE_CAST
)]

```
Argument -s nije obavezan i ukoliko se ne navede biće ispisane sve dostupne sugestije.

### Primeri pokretanja

```bash
# Sve sugestije za HelloWorld.java
java -jar build/libs/SuggestFix-1.0-SNAPSHOT.jar -f HelloWorld

# For-loop to for-each transformacija
java -jar build/libs/SuggestFix-1.0-SNAPSHOT.jar -f ForLoopToForEachExample -s l
```
