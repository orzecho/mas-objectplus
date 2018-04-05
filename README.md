# MAS ObjectPlus

## Refaktoring na styl funkcyjny

Z przykładu przedstawionego na wykładzie:  
```java
public class ObjectPlus implements Serializable {
  private static Hashtable ekstensje = new Hashtable();
  
  public ObjectPlus() {
    Vector ekstensja = null;
    Class klasa = this.getClass();
    
    if(ekstensje.containsKey(klasa)) {
      // Ekstensja tej klasy istnieje w kolekcji ekstensji
      ekstensja = (Vector) ekstensje.get(klasa);
    }
    else {
      // Ekstensji tej klasy jeszcze nie ma -> dodaj ją
      ekstensja = new Vector();
      ekstensje.put(klasa, ekstensja);
    }
    
    ekstensja.add(this);
  }
}
```
na bardziej zwięzły, pseudo-funkcyjny styl:
```java
public abstract class ObjectPlus implements Serializable {
    @Getter
    private static Map<Class, Vector> extensions = new HashMap<>();

    public ObjectPlus() {
        extensions.entrySet().stream()
                .filter(e -> e.getKey().equals(this.getClass()))
                .findFirst()
                .map(e -> e.getValue())
                .orElseGet(this::createExtensionVector)
                .add(this);
    }

    private Vector createExtensionVector() {
        Vector vector = new Vector();
        extensions.put(this.getClass(), vector);
        return vector;
    }
}
```
Użycie generycznej HashMapy pozwala zaoszczędzić brzydkie i potencjalnie niebezpieczne casty. Użycie Lomboka pozwala na wygenerowanie gettera bez pisania trywialnego kodu.

## Zapisywanie ekstensji w bazie danych zamiast w pliku

```java
@Service
@RequiredArgsConstructor
public class ObjectPlusService {

    private final ExtensionRepository extensionRepository;
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final Try<ObjectOutputStream> objectOutputStream =
            Try.of(() -> new ObjectOutputStream(byteArrayOutputStream));

    public void saveAll() {
        ObjectPlus.getExtensions()
                .entrySet().stream()
                .map(e -> new Extension(e.getKey(), objectOutputStream.andThenTry(o -> o.writeObject(e.getValue()))
                        .andThenTry(byteArrayOutputStream::flush)
                        .map(o -> byteArrayOutputStream.toByteArray())
                        .get()))
                .forEach(extensionRepository::save);
    }

    public Map<Class, Vector> findAll() {
        return extensionRepository.findAll().stream()
                .collect(Collectors.toMap(Extension::getKlass, e -> (Vector) getObject(e)));
    }

    public void reload() {
        ObjectPlus.getExtensions().clear();
        ObjectPlus.getExtensions().putAll(findAll());
    }

    private Object getObject(Extension e) {
        return Try.of(() -> new ObjectInputStream(new ByteArrayInputStream(e.getExtensions())).readObject()).get();
    }
}
```
Jest to rozwiązanie poniekąd pośrednie, pomiędzy prostą serializacją do pliku, a normalnym przechowywaniem zmapowanych danych o klasach w relacyjnej bazie danych. W bazie znajduje się tylko jedna tabela z dwoma istotnymi dla nas kolumnami: klasą i listą ekstensji. Otrzymujemy więc nieco większą granularność niż w przykładzie z wykładu, bo możemy pytać bazę o ekstensje konkretnych klas, nie musimy koniecznie wyciągać zawsze wszystkich klas. 
Nie zachowujemy oczywiście postaci normalnej jako, że blob który przechowuje nasz zserializowany Vector przechowuje wartość zdecydowanie nieatomową. Pozwala nam za to przechowywać w nim dowolnie duże ekstensje dowolnej klasy.
Do komunikacji z bazą danych użyto Hibernate'a i springowych repozytoriów. Na uwagę zasługuje też użycie monady Try (z biblioteki vavr, dawniej javaslang) do obsługi wyjątków, których obsługa w standardowej Javie pozostawia trochę do życzenia. Co prawda w podanym przykładzie nie robimy z informacją o błędach nic szczególnego, ale łatwo można taki kod dopisać, bez potrzeby używania niezręcznych try-cache'y.
