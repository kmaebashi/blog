package com.kmaebashi.jsonparser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

public class ClassMapper {
    private ClassMapper() {}

    public static String toJson(Object obj) {
        JsonElement elem = toJsonElement(obj);
        return elem.stringify();
    }

    public static JsonElement toJsonElement(Object obj) {
        if (obj instanceof Integer intObj) {
            return JsonValue.createIntValue(intObj.intValue());
        } else if (obj instanceof Double doubleObj) {
            return JsonValue.createRealValue(doubleObj.doubleValue());
        } else if (obj instanceof Float floatObj) {
            return JsonValue.createRealValue(floatObj.floatValue());
        } else if (obj instanceof Boolean boolObj) {
            return JsonValue.createBooleanValue(boolObj.booleanValue());
        } else if (obj instanceof String strObj) {
            return JsonValue.createStringValue(strObj);
        } else if (obj == null) {
            return JsonValue.createNullValue();
        } else if (obj instanceof int[] array) {
            List<JsonElement> elems = new ArrayList<>();
            for (var elem : array) {
                elems.add(toJsonElement(elem));
            }
            return JsonArray.newInstance(elems);
        } else if (obj instanceof float[] array) {
            List<JsonElement> elems = new ArrayList<>();
            for (var elem : array) {
                elems.add(toJsonElement(elem));
            }
            return JsonArray.newInstance(elems);
        } else if (obj instanceof double[] array) {
            List<JsonElement> elems = new ArrayList<>();
            for (var elem : array) {
                elems.add(toJsonElement(elem));
            }
            return JsonArray.newInstance(elems);
        } else if (obj instanceof boolean[] array) {
            List<JsonElement> elems = new ArrayList<>();
            for (var elem : array) {
                elems.add(toJsonElement(elem));
            }
            return JsonArray.newInstance(elems);
        } else if (obj instanceof Object[] array) {
            List<JsonElement> elems = new ArrayList<>();
            for (var elem : array) {
                elems.add(toJsonElement(elem));
            }
            return JsonArray.newInstance(elems);
        } else if (obj instanceof List<?> list) {
            List<JsonElement> elems = new ArrayList<>();
            for (var elem : list) {
                elems.add(toJsonElement(elem));
            }
            return JsonArray.newInstance(elems);
        } else {
            Map<String, JsonElement> map = new LinkedHashMap<>();
            for (Field field : obj.getClass().getDeclaredFields()) {
                JsonIgnore ji = field.getAnnotation(JsonIgnore.class);
                if (ji != null) {
                    continue;
                }
                int modifier = field.getModifiers();
                if (!Modifier.isPublic(modifier)) {
                    continue;
                }
                try {
                    map.put(field.getName(), toJsonElement(field.get(obj)));
                } catch (IllegalAccessException ex) {
                    assert false : "publicであることを確認しているのでここには来ないはず";
                }
            }
            return JsonObject.newInstance(map);
        }
    }

    public static <T> T toObject(String json, Class<T> targetClass)
            throws JsonParseException, JsonDeserializeException, TypeMismatchException,
                NoSuchMethodException, NoSuchFieldException,
                InstantiationException, IllegalAccessException, InvocationTargetException {
        JsonElement jsonElement = JsonParser.parse(json);
        return toObject(jsonElement, targetClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(JsonElement jsonElement, Class<T> targetClass)
            throws TypeMismatchException, JsonDeserializeException, NoSuchMethodException, NoSuchFieldException,
                InstantiationException, IllegalAccessException, InvocationTargetException {
        if (!targetClass.isPrimitive() && jsonIsNull(jsonElement)) {
            return null;
        }
        if (targetClass == int.class || targetClass == Integer.class) {
            if (jsonElement instanceof JsonValue value) {
                if (value.getType() == JsonValueType.INT) {
                    return (T)Integer.valueOf(((JsonValue)jsonElement).getInt());
                }
            }
            throw new TypeMismatchException(jsonElement, "整数型");
        } else if (targetClass == float.class || targetClass == Float.class) {
            if (jsonElement instanceof JsonValue value) {
                if (value.getType() == JsonValueType.REAL) {
                    return (T)Float.valueOf((float)((JsonValue)jsonElement).getReal());
                } else if (value.getType() == JsonValueType.INT) {
                    return (T)Float.valueOf((float)((JsonValue)jsonElement).getInt());
                }
            }
            throw new TypeMismatchException(jsonElement, "(単精度の)実数型");
        } else if (targetClass == double.class || targetClass == Double.class) {
            if (jsonElement instanceof JsonValue value) {
                if (value.getType() == JsonValueType.REAL) {
                    return (T)Double.valueOf(((JsonValue)jsonElement).getReal());
                } else if (value.getType() == JsonValueType.INT) {
                    return (T)Double.valueOf(((JsonValue)jsonElement).getInt());
                }
            }
            throw new TypeMismatchException(jsonElement, "実数型");
        } else if (targetClass == boolean.class || targetClass == Boolean.class) {
            if (jsonElement instanceof JsonValue value && value.getType() == JsonValueType.BOOLEAN) {
                return (T)Boolean.valueOf(((JsonValue)jsonElement).getBoolean());
            } else {
                throw new TypeMismatchException(jsonElement, "ブーリアン型");
            }
        } else if (targetClass == String.class) {
            if (jsonElement instanceof JsonValue value && value.getType() == JsonValueType.STRING) {
                return (T) ((JsonValue) jsonElement).getString();
            } else {
                throw new TypeMismatchException(jsonElement, "文字列型");
            }
        } else if (targetClass.isArray()) {
            if (jsonElement instanceof JsonArray array) {
                List<JsonElement> list = array.getArray();
                return (T) createArray(list, targetClass);
            } else {
                throw new TypeMismatchException(jsonElement, "配列");
            }
        } else if (List.class.isAssignableFrom(targetClass)) {
            throw new JsonDeserializeException("Listコレクションへの変換はサポートしていません。",
                                               jsonElement.getLineNumber());
        } else {
            if (jsonElement instanceof JsonObject obj) {
                return (T)createObject(obj, targetClass);
            } else {
                throw new TypeMismatchException(jsonElement, "オブジェクト");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object createArray(List<JsonElement> list, Class targetClass)
            throws TypeMismatchException, JsonDeserializeException,
                NoSuchMethodException, NoSuchFieldException,
                InstantiationException, IllegalAccessException, InvocationTargetException {
        Class elemClass = targetClass.getComponentType();

        if (elemClass == int.class) {
            int[] array = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                if (((JsonValue) list.get(i)).getType() != JsonValueType.INT) {
                    throw new TypeMismatchException(list.get(i), "int");
                }
                array[i] = ((JsonValue) list.get(i)).getInt();
            }
            return array;
        } else if (elemClass == float.class) {
            float[] array = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                if (((JsonValue) list.get(i)).getType() == JsonValueType.REAL) {
                    array[i] = (float)((JsonValue)list.get(i)).getReal();
                } else if (((JsonValue) list.get(i)).getType() == JsonValueType.INT) {
                    array[i] = (float)((JsonValue)list.get(i)).getInt();
                } else {
                    throw new TypeMismatchException(list.get(i), "float");
                }
            }
            return array;
        } else if (elemClass == double.class) {
            double[] array = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                if (((JsonValue) list.get(i)).getType() == JsonValueType.REAL) {
                    array[i] = ((JsonValue)list.get(i)).getReal();
                } else if (((JsonValue) list.get(i)).getType() == JsonValueType.INT) {
                    array[i] = ((JsonValue)list.get(i)).getInt();
                } else {
                    throw new TypeMismatchException(list.get(i), "double");
                }
            }
            return array;
        } else if (elemClass == boolean.class) {
            boolean[] array = new boolean[list.size()];
            for (int i = 0; i < list.size(); i++) {
                if (((JsonValue) list.get(i)).getType() != JsonValueType.BOOLEAN) {
                    throw new TypeMismatchException(list.get(i), "boolean");
                }
                array[i] = ((JsonValue)list.get(i)).getBoolean();
            }
            return array;
        } else {
            Object[] array = (Object[])Array.newInstance(elemClass, list.size());
            for (int i = 0; i < list.size(); i++) {
                array[i] = toObject(list.get(i), elemClass);
            }
            return array;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object createObject(JsonObject jsonObject, Class targetClass)
            throws TypeMismatchException, JsonDeserializeException,
                    NoSuchMethodException, InstantiationException,
                    IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        Object targetObj = targetClass.getDeclaredConstructor().newInstance();

        for (String key: jsonObject.getMap().keySet()) {
            JsonElement jsonElem = jsonObject.getMap().get(key);
            Field field = targetClass.getDeclaredField(key);
            Object value = toObject(jsonElem, field.getType());
            field.set(targetObj, value);
        }

        return targetObj;
    }

    private static boolean jsonIsNull(JsonElement elem) {
        return elem instanceof JsonValue value && value.getType() == JsonValueType.NULL;
    }
}
