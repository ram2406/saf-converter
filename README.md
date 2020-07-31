# saf-converter

Simple universal converter on Java, with available for expansion.

And conversions are show as table in code.

For example:

| x  | String  | Long  | Enum  |
|---|---|---|---|
| String  | self  | parseLong  | parseEnum  |
| Long | Object::toString  | self  | selectEnumByOrdinal  |
| Enum | Object::toString  | Enum::ordinal  | self  |

If you have your enum use that code:

```java
    @AllArgsConstructor
    @Getter
    public enum TestClient {

        JURIDICAL("client.type.caption.juridical"),
        PHYSICAL("client.type.caption.physical");

        private final String key;

    }
    
    Converter.INSTANCE_DEFAULT.putAsEnum(TestClient.class);
```

Or if you have custom type, use code like this:

```java
Converter.INSTANCE_DEFAULT.putAsOtherType(MoneyType.class, MoneyType::fromLong, Long.class, MoneyType::toLong);
```

And if you need more one converter, should add new enum constant:

```java
public enum Converter {
    INSTANCE_DEFAULT(DateTimeFormatter.ISO_LOCAL_DATE_TIME, DateTimeFormatter.ISO_LOCAL_DATE, ZoneId.systemDefault()),
    INSTANCE_CUSTOM(DateTimeFormatter.ISO_OFFSET_DATE_TIME, DateTimeFormatter.ISO_OFFSET_DATE, ZoneId.systemDefault());
```
