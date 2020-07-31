package saf.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;


/**
 * Класс для преобразований типов
 */

@SuppressWarnings("unchecked")
public enum Converter {
    INSTANCE_DEFAULT(DateTimeFormatter.ISO_LOCAL_DATE_TIME, DateTimeFormatter.ISO_LOCAL_DATE, ZoneId.systemDefault());

    private final DateTimeFormatter dateTimeFormatter;
    private final DateTimeFormatter dateFormatter;
    private final ZoneId zoneId;

    private static final Logger LOG = Logger.getLogger(Converter.class.getName());
    private static final List<Class<? extends Number>> NUMBERS = Arrays.asList(Long.class, Integer.class, Double.class, BigDecimal.class);
    private final Map<Class<?>, Map<Class<?>, Function<Object, Object>>> table = new HashMap<>();

    private final List<Class<?>> classes;

    private <T extends Serializable> void put(Class<T> clazz, Function<T, Object>... fns) {
        Map<Class<?>, Function<Object, Object>> map = new HashMap<>();
        addRow(clazz, map, fns);
        table.put(clazz, map);
    }

    /**
     * Добавить строку преобразований в таблицу
     */
    private <T> void addRow(Class<T> cls, Map<Class<?>, Function<Object, Object>> map, Function<T, Object>... fns) {
        Objects.requireNonNull(cls);
        for (int clsIdx = 0; clsIdx < fns.length; ++clsIdx) {
            final int crt = clsIdx;
            Class<?> cls2 = classes.get(clsIdx);
            map.put(cls2, (Object o) ->
                    convertObject(fns, crt, cls, cls2, (T) o)
            );
        }
    }

    /**
     * Для отладки, точку останова ставить здесь
     *
     * @param fns         функция преобразования
     * @param clsIndex    порядковый номер в списке классов (номер столбца)
     * @param targetClass класс целевой
     * @param sourceClass класс исходный
     * @param value       обхект для преобразования
     * @param <T>         тип целевого класса
     * @return преобразованный объект
     */
    private <T> Object convertObject(Function<T, Object>[] fns, int clsIndex, Class<T> targetClass, Class<?> sourceClass, T value) {
        assert value instanceof Serializable;
        return fns[clsIndex].apply(value);
    }


    /**
     * Добавить как другой уже добавленный тип otherType
     */
    public <T extends Serializable, O extends Serializable> void putAsOtherType(Class<T> clazz, Function<O, T> fromType, Class<O> otherType, Function<T, O> toType) {
        Map<Class<?>, Function<Object, Object>> map = new HashMap<>();
        Map<Class<?>, Function<Object, Object>> otherFns = table.get(otherType);

        for (Class<?> cls : classes) {
            // clazz -> otherType -> cls
            map.put(cls, o ->
                    otherFns.get(cls).apply(toType.apply((T) o))
            );

            // cls -> otherType -> clazz
            table.get(cls).put(clazz, o ->
                    fromType.apply((O) table.get(cls).get(otherType).apply(o))
            );
        }

        //otherType -> clazz
        otherFns.put(clazz, o -> fromType.apply((O) o));

        //clazz -> clazz
        map.put(clazz, ConverterPrimitive::selfStatic);

        //clazz -> classes
        table.put(clazz, map);

        //classes += clazz
        classes.add(clazz);
        assert check();
    }

    private boolean check() {
        int size = table.size();
        for (Map.Entry<Class<?>, Map<Class<?>, Function<Object, Object>>> entry : table.entrySet()) {
            if (entry.getValue().size() != size) {
                LOG.warning(
                        MessageFormat.format("Found row is different size [{0}, {1}, {2}]", entry.getKey(), entry.getValue().size(), size)
                );
                return false;
            }
        }
        return true;
    }

    /**
     * Добавить тип как {@link String}
     */
    public <T extends Serializable> void putAsString(Class<T> clazz, Function<String, T> fromStr) {
        putAsOtherType(clazz, fromStr, String.class, Object::toString);
    }

    /**
     * Вставить в таблицу преобразования перечисление
     *
     * @param clazz            тип перечисления
     * @param findEnumFunction функция поиска по числовому значению
     */
    public <T extends Enum> void putAsEnum(Class<T> clazz, Function<Enum, T> findEnumFunction, Function<Number, T> numberToEnumFunction) {
        putAsOtherType(clazz, findEnumFunction, Enum.class, o -> o);
        table.get(String.class).put(clazz, o -> Enum.valueOf(clazz, (String) o));
        for (Class<? extends Number> cls : NUMBERS) {
            table.get(cls).put(clazz, (Function) numberToEnumFunction);
        }
        table.get(Boolean.class).put(clazz, o -> clazz.getEnumConstants()[((Boolean) o) ? 1 : 0]);
        assert check();
    }

    /**
     * Вставить в таблицу преобразования перечисление
     *
     * @param clazz тип перечисления
     */
    public <T extends Enum> void putAsEnum(Class<T> clazz) {
        Function<Number, T> find = o -> clazz.getEnumConstants()[o.intValue()];
        putAsEnum(clazz, o -> find.apply(o.ordinal()), find);
        assert check();
    }

    /**
     * Аналогично this.convert только с преобразованием
     */
    public <T> T convertType(Class<T> to, Object o, Object defaultValue) {
        return (T) convert(to, o, defaultValue);
    }

    /**
     * Аналогично this.convert только с преобразованием
     */
    public <T> T convertType(Class<T> to, Object o) {
        return (T) convert(to, o, null);
    }

    /**
     * то же самое, что и {@link #convert(Class, Object, Object)}. Третий параметр = null
     *
     * @param to целевой тип
     * @param o  экземпляр для конвертиации
     * @return преобразованный объект в целевом типе
     */
    public Object convert(Class<?> to, Object o) {
        return convert(to, o, null);
    }

    /**
     * Сконвертировать экземпляр класса в указанный тип
     *
     * @param to           целевой тип
     * @param o            экземпляр для конвертиации
     * @param defaultValue значение по умолчанию
     * @return преобразованный объект в целевом типе
     */
    public Object convert(Class<?> to, Object o, Object defaultValue) {
        if (o == null) {
            return defaultValue;
        }
        try {
            Map<Class<?>, Function<Object, Object>> targets = table.get(o.getClass());
            if (targets == null) {
                throw new IllegalArgumentException("Targets not found for [" + to + "]");
            }

            Function<Object, Object> converterFunc = targets.get(to);
            if (converterFunc == null) {
                throw new IllegalArgumentException("Convert function for target class [" + to + "] from class [" + o.getClass() + "]");
            }
            return converterFunc.apply(o);
        } catch (Exception e) {
            throw new IllegalArgumentException("Class convert exception on to-class [" + to.getName()
                    + "], from-class [" + o.getClass().getName() + "], object [" + o + "]", e);
        }
    }

    /**
     * Получить функцию конвертации для классов from -> to
     */
    public Function<Object, Object> getConverter(Class<?> from, Class<?> to) {
        return o -> {
            try {
                return table.get(from).get(to).apply(o);
            } catch (Exception e) {
                throw new IllegalArgumentException("Class convert exception on to-class [" + to.getName() + "], from-class [" + from.getName() + "], object [" + o + "]", e);
            }
        };
    }

    ///вызов этого метода гарантирует вызов конструктора
    public void fakeInit() {
        ///skip
    }


    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Заполнить значениями по умолчанию
     * @param dateTimeFormatter
     * @param zoneId
     */
    Converter(DateTimeFormatter dateTimeFormatter, DateTimeFormatter dateFormatter, ZoneId zoneId) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.dateFormatter = dateFormatter;
        this.zoneId = zoneId;

        ConverterPrimitive p = new ConverterPrimitive(dateTimeFormatter, dateFormatter, zoneId);

        /*
          Таблица преобразований
         */
        classes = new CopyOnWriteArrayList<>(Arrays.asList(
                                    String.class,       Long.class,         BigDecimal.class,   Double.class,           Integer.class,
                Boolean.class,      LocalDate.class,    Enum.class,         LocalTime.class,    LocalDateTime.class
        ));

        put(String.class,           p::self,            Long::parseLong,    BigDecimal::new,    Double::parseDouble,    Integer::parseInt,
                p::toBoolean,       p::toLocalDate,     p::empty,           LocalTime::parse,   LocalDateTime::parse);

        put(Long.class,             Object::toString,   p::self,            BigDecimal::new,    Number::doubleValue,    Long::intValue,
                p::fromNumber,      p::toLocalDate,     p::empty,           p::fromNum,         p::ldtFromNum);

        put(BigDecimal.class,       Object::toString,   Number::longValue,  p::self,            Number::doubleValue,    Number::intValue,
                p::fromNumber,      p::empty,           p::empty,           p::fromNum,         p::ldtFromNum);

        put(Double.class,           Object::toString,   Number::longValue,  BigDecimal::new,    p::self,                Number::intValue,
                p::fromNumber,      p::empty,           p::empty,           p::fromNum,         p::ldtFromNum);

        put(Integer.class,          Object::toString,   Number::longValue,  BigDecimal::new,    Number::doubleValue,    p::self,
                p::fromNumber,      p::empty,           p::empty,           p::fromNum,         p::ldtFromNum);

        put(Boolean.class,          Object::toString,   p::toLong,          p::toNumber,        p::toDouble,            p::toNumber,
                p::self,            p::empty,           p::empty,           p::empty,           p::empty);

        put(LocalDate.class,        p::formatLD,        p::toLong,          p::empty,           p::empty,               p::empty,
                p::empty,           p::self,            p::empty,           p::empty,           p::toLocalDateTime);

        put(Enum.class,             Object::toString,   p::toLong,          p::toDecimal,       p::toDouble,            p::toInteger,
                p::toBoolean,       p::empty,           p::self,            p::empty,           p::empty);

        put(LocalTime.class,        Object::toString,   p::toLong,          p::toDecimal,       p::toDouble,            LocalTime::toNanoOfDay,
                p::toBoolean,       p::empty,           p::empty,           p::self,            LocalDateTime::from);

        put(LocalDateTime.class,    p::formatLDT,       p::empty,           p::toDecimal,       p::empty,               p::empty,
                p::empty,           LocalDate::from,    p::empty,           LocalTime::from,    p::self);

    }
}
