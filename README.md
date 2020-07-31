# saf-converter
Simple universal converter on Java, with available for expansion. And conversions are show as table in code.


For example:

| x  | String  | Long  | Enum  |
|---|---|---|---|
| String  | self  | parseLong  | parseEnum  |
| Long | Object::toString  | self  | selectEnumByOrdinal  |
| Enum | Object::toString  | Enum::ordinal  | self  |
