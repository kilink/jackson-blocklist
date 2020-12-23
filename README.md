# jackson-blocklist

## usage

Imagine you have a JPA Entity or some class you never want to inadvertently serialize directly.

```java
@Entity
class Author {
    @Id
    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public getName() {
        return name;
    }
}
```

To prevent Jackson from serializing any `Entity` annotated classes, a `BlocklistModule` can be configured and registered as follows:

```java
Module module = BlocklistModule.builder()
    .annotations(Entity.class) // All Entity annotated classes are blocked
    .build();
ObjectMapper mapper = new ObjectMapper()
    .registerModule(module);
```
The module can also be scoped to a specific class or package:

```java
Module module = BlocklistModule.builder()
    .classes(Author.class) // Author is explicitly blocked
    .packages("net.kilink.example") // All classes from net.kilink.example are blocked
    .build();
```
Attempting to serialize a blocked class will cause a `JsonMappingException` to be thrown with a message similar to "Attempted to serialize disallowed class Foo".
