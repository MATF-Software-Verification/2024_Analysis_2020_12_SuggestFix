# Davanje sugestija za popravljanje kvaliteta izvornog koda

## Opis ideje

- Osnovna ideja projekta je davanje sugestija kako se u programskom jeziku *Java* određeni sintaksni konstrukti
mogu zameniti sintaksnim konstruktima koji su poželjniji.

## Pokretanje aplikacije :hammer:

- Za kompilaciju je preporučeno razvojno okruženje IntelliJIDEA. Projekat koristi Gradle sistem za kompilaciju, pa se sve potrebne biblioteke automatski preuzimaju. Projekat se kompilira i pokreće kao standardni IntelliJIDEA projekat.

- Izvršiva verzija programa je .jar datoteka, za čije izvršavanje je potrebna Java virtuelna mašina. Najnovija verzija može biti preuzeta sa strane u [`Releases`](https://github.com/MATF-Software-Verification/2020_12_SuggestFix/releases) sekciji.

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
)]

```
Argument -s nije obavezan i ukoliko se ne navede biće ispisane sve dostupne sugestije.

- Primer pokretanja (HelloWorld.java fajl nalazi se u istom folderu kao i .jar datoteka):
```
java -jar SuggestFix.jar -f HelloWorld
```

## Primeri upotrebe

- Primeri korišćenja programa mogu se pronaći u [`Wiki`](https://github.com/MATF-Software-Verification/2020_12_SuggestFix/wiki) stranicama.

## Članovi tima

- [Aleksandar Jakovljević](https://github.com/AlexJakovljevic) 
- [Boris Karanović](https://github.com/bozzano101)
- [Marko Veljković](https://github.com/bataVeljko)
- [Olivera Popović](https://github.com/popovic-olivera)
