# Davanje sugestija za popravljanje kvaliteta izvornog koda

## Opis ideje

- Osnovna ideja projekta je davanje sugestija kako se u programskom jeziku *Java* određeni sintaksni konstrukti
  mogu zameniti sintaksnim konstruktima koji su poželjniji.

## Pokretanje aplikacije :hammer:

- Za kompilaciju je preporučeno razvojno okruženje IntelliJIDEA. Projekat koristi Gradle sistem za kompilaciju, pa se sve potrebne biblioteke automatski preuzimaju. Projekat se kompilira i pokreće kao standardni IntelliJIDEA projekat.

-  Argumenti kojima se kontroliše izvršavanje:
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
)]

```
Argument -s nije obavezan i ukoliko se ne navede biće ispisane sve dostupne sugestije.

- Primer pokretanja (HelloWorld.java fajl nalazi se u istom folderu kao i .jar datoteka):
```
java -jar SuggestFix.jar -f HelloWorld
```


