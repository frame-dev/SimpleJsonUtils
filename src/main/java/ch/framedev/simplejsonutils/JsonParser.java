package ch.framedev.simplejsonutils;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings({"CallToPrintStackTrace", "unchecked", "rawtypes", "unused"})
public class JsonParser {

    private boolean indent;
    private boolean debug;

    public JsonParser() {
        this.indent = false;
        this.debug = false;
    }

    public JsonParser(Flag... flags) {
        // check for flags
        for (Flag f : flags) {
            this.indent = f == Flag.USE_INDENT || f == Flag.PRETTY_PRINT;
            this.debug = f == Flag.DEBUG;
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setIndent(boolean indent) {
        this.indent = indent;
    }

    // ✅ Serialize Object to JSON
    public String serializeObject(Object object) {
        return serializeValue(object, 0);
    }

    private String serializeValue(Object value, int indentLevel) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson(value.toString()) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List) return serializeList((List<?>) value, indentLevel);
        if (value instanceof Map) return serializeMap((Map<?, ?>) value, indentLevel);
        if (isCustomClass(value.getClass())) return serializeObjectFields(value, indentLevel);
        throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getSimpleName());
    }

    private String serializeList(List<?> list, int indentLevel) {
        StringBuilder sb = new StringBuilder("[");
        String indent = this.indent ? "\n" + "  ".repeat(indentLevel + 1) : "";
        for (Object item : list) {
            sb.append(indent).append(serializeValue(item, indentLevel + 1)).append(",");
        }
        if (!list.isEmpty()) sb.setLength(sb.length() - 1);
        return sb.append(this.indent ? "\n" + "  ".repeat(indentLevel) + "]" : "]").toString();
    }

    private String serializeMap(Map<?, ?> map, int indentLevel) {
        StringBuilder sb = new StringBuilder("{");
        String indent = this.indent ? "\n" + "  ".repeat(indentLevel + 1) : "";
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(indent)
                    .append("\"").append(entry.getKey()).append("\": ")
                    .append(serializeValue(entry.getValue(), indentLevel + 1))
                    .append(",");
        }
        if (!map.isEmpty()) sb.setLength(sb.length() - 1);
        return sb.append(this.indent ? "\n" + "  ".repeat(indentLevel) + "}" : "}").toString();
    }

    private String serializeObjectFields(Object obj, int indentLevel) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        String indent = this.indent ? "\n" + "  ".repeat(indentLevel + 1) : "";
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    sb.append(indent)
                            .append("\"").append(field.getName()).append("\": ")
                            .append(serializeValue(field.get(obj), indentLevel + 1))
                            .append(",");
                } catch (Exception ignored) {
                }
            }
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        return sb.append(this.indent ? "\n" + "  ".repeat(indentLevel) + "}" : "}").toString();
    }

    // ✅ Deserialize JSON String to Java Object
    public Object deserializeObject(String json, Class<?> clazz) {
        json = json.trim();

        if (!isCustomClass(clazz)) {
            return convertValue(clazz, json);
        }

        if (List.class.isAssignableFrom(clazz)) {
            return parseJsonList(parseJsonRawList(json), Object.class);
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return parseJsonMap(json);
        }

        if (json.startsWith("{")) {
            return parseJsonObject(json, clazz);
        }

        throw new IllegalArgumentException("Invalid JSON format");
    }

    @SuppressWarnings("unchecked")
    private <T> T parseJsonObject(String json, Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T obj = constructor.newInstance();
            Map<String, Object> map = parseJsonMap(json);

            try {

                // Iterate through all declared fields in the class
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();

                    if (map.containsKey(fieldName)) {
                        Object value = map.get(fieldName);

                        // ✅ If the field is a Map, assign it directly
                        if (Map.class.isAssignableFrom(field.getType()) && value instanceof Map) {
                            field.set(obj, value);
                        }
                        // ✅ If the field is a nested Object, parse it recursively
                        else if (isCustomClass(field.getType()) && value instanceof Map) {
                            field.set(obj, parseJsonObjectMap((Map<String, Object>) value, field.getType()));
                        }
                        // ✅ Assign Lists Properly
                        else if (List.class.isAssignableFrom(field.getType()) && value instanceof List) {
                            Type genericType = field.getGenericType();
                            if (genericType instanceof ParameterizedType) {
                                Class<?> listType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
                                field.set(obj, parseJsonList((List<?>) value, listType));
                            }
                        }
                        // ✅ Handle Primitive & Simple Type Conversion
                        else {
                            field.set(obj, convertValue(field.getType(), value));
                        }

                        // ✅ Remove processed field from map to avoid duplicate processing
                        map.remove(fieldName);

                    } else {
                        System.out.println("⚠️ WARNING: No matching key found in JSON for field: " + fieldName);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON into " + clazz.getName(), e);
        }
    }

    // ✅ Convert Map<String, Object> directly into an Object
    private <T> T parseJsonObjectMap(Map<String, Object> map, Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T obj = constructor.newInstance();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    // ✅ Convert Lists Properly
                    if (List.class.isAssignableFrom(field.getType())) {
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType) {
                            Class<?> listType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
                            value = parseJsonList((List<?>) value, listType);
                        }
                    }
                    // ✅ Convert Nested Objects Properly
                    else if (isCustomClass(field.getType()) && value instanceof Map) {
                        value = parseJsonObjectMap((Map<String, Object>) value, field.getType());
                    }
                    // ✅ Convert Primitives and Simple Types
                    else {
                        value = convertValue(field.getType(), value);
                    }

                    field.set(obj, value);
                } catch (NoSuchFieldException e) {
                    System.out.println("⚠️ WARNING: No field found for key: " + fieldName);
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Map into " + clazz.getName(), e);
        }
    }

    private List<Object> parseJsonRawList(String json) {
        json = json.trim();

        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IllegalArgumentException("Invalid JSON array format: " + json);
        }

        json = json.substring(1, json.length() - 1).trim(); // Remove "[" and "]"
        List<Object> list = new ArrayList<>();
        if (json.isEmpty()) {
            return list; // Handle empty lists
        }

        boolean insideString = false;
        StringBuilder item = new StringBuilder();

        for (char c : json.toCharArray()) {
            if (c == '"') {
                insideString = !insideString; // Toggle string mode
            }

            if (c == ',' && !insideString) {
                list.add(parseJsonValue(item.toString().trim()));
                item.setLength(0);
            } else {
                item.append(c);
            }
        }

        if (item.length() > 0) {
            list.add(parseJsonValue(item.toString().trim())); // Add last item
        }

        return list;
    }

    private <T> List<T> parseJsonList(List<?> jsonList, Class<T> listType) {
        List<T> list = new ArrayList<>();
        for (Object item : jsonList) {
            if (listType == null)
                listType = (Class<T>) String.class;
            if (item == null) {
                list.add(null);
            } else if (String.class.isAssignableFrom(listType)) {
                list.add((T) item);
            } else if (isCustomClass(listType)) {
                list.add(parseJsonObject(item.toString(), listType));
            } else {
                list.add((T) convertValue(listType, item));
            }
        }
        return list;
    }

    private Map<String, Object> parseJsonMap(String json) {
        json = json.trim();
        if(debug) System.out.println("🔍 Processing JSON Map: " + json);

        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON object format: " + json);
        }

        json = json.substring(1, json.length() - 1).trim(); // Remove `{}` brackets safely
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, String> deferredProcessing = new LinkedHashMap<>(); // Stores nested JSON for later

        if (json.isEmpty()) {
            return map; // Handle empty `{}` case
        }

        boolean insideString = false;
        boolean insideObject = false;
        boolean insideList = false;
        int nestedLevel = 0;

        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean readingValue = false;
        boolean foundSeparator = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                insideString = !insideString;
            }

            if (!insideString) {
                if (c == '{') {
                    insideObject = true;
                    nestedLevel++;
                } else if (c == '}') {
                    nestedLevel--;
                    if (nestedLevel == 0) insideObject = false;
                } else if (c == '[') {
                    insideList = true;
                    nestedLevel++;
                } else if (c == ']') {
                    nestedLevel--;
                    if (nestedLevel == 0) insideList = false;
                }
            }

            if ((c == ':' && !insideString && !insideObject && !insideList)) {
                readingValue = true;
                foundSeparator = true;
            } else if ((c == ',' || i == json.length() - 1) && !insideString && nestedLevel == 0) {
                if (foundSeparator) {
                    String keyStr = key.toString().trim().replace("\"", "");
                    String valueStr = value.toString();

                    if (i == json.length() - 1 && c != ',') {
                        valueStr += c; // Append last character if needed
                    }

                    if(debug) System.out.println("📌 Key: " + keyStr + " | Value: " + (valueStr.isEmpty() ? "(missing)" : valueStr));

                    if (!keyStr.isEmpty()) {
                        if (valueStr.startsWith("{") || valueStr.startsWith("[") && (valueStr.endsWith("}") || valueStr.endsWith("|"))) {
                            deferredProcessing.put(keyStr, valueStr); // Defer processing of nested structures
                        } else {
                            map.put(keyStr, parseJsonValue(valueStr)); // Process immediately
                        }
                    }
                }

                String keyStr = key.toString().replace("\"", "").trim();
                String valueStr;
                if(!map.containsKey(keyStr) && !deferredProcessing.containsKey(keyStr)) {
                    String[] pair = keyStr.split(":");
                    keyStr = pair[0];
                    valueStr = pair[1];
                    if (i == json.length() - 1 && c != ',') {
                        valueStr += c; // Append last character if needed
                    }
                    if(debug) System.out.println("Missing key added: " + keyStr + " Value: " + valueStr);
                    map.put(keyStr, parseJsonValue(valueStr));
                }

                key.setLength(0);
                value.setLength(0);
                readingValue = false;
                foundSeparator = false;
            } else {
                if (readingValue) {
                    value.append(c);
                } else {
                    key.append(c);
                }
            }
        }

        // Process deferred nested structures
        for (Map.Entry<String, String> entry : deferredProcessing.entrySet()) {
            String keyStr = entry.getKey();
            String valueStr = entry.getValue();

            if(debug) System.out.println("⏳ Processing deferred: " + keyStr + " → " + valueStr);

            if (valueStr.startsWith("{")) {
                map.put(keyStr, parseJsonMap(valueStr));
            } else if (valueStr.startsWith("[")) {
                map.put(keyStr, parseJsonList(parseJsonRawList(valueStr), Object.class));
            }
        }

        return map;
    }

    private int findMatchingBrace(String json, int startIndex) {
        int openBraces = 1; // ✅ We start after the first '{'
        for (int i = startIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') openBraces++;
            if (c == '}') {
                openBraces--;
                if (openBraces == 0) return i; // ✅ Found matching `}`
            }
        }
        return -1; // ❌ No matching closing `}`
    }

    private Object convertValue(Class<?> type, Object value) {
        if (value == null) return null;
        String strValue = value.toString().replace("\"", "");

        // ✅ Handle Strings
        if (type == String.class) return strValue;
        strValue = strValue.trim();

        // ✅ Handle Booleans
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(strValue);

        // ✅ Handle Integer Numbers (Avoid "21.0" causing NumberFormatException)
        if ((type == int.class || type == Integer.class) && strValue.matches("-?\\d+")) {
            return Integer.parseInt(strValue);
        }
        if ((type == long.class || type == Long.class) && strValue.matches("-?\\d+")) {
            return Long.parseLong(strValue);
        }

        // ✅ Handle Floating Point Numbers (Avoid parsing issues)
        String s = strValue.endsWith(".") ? strValue + "0" : strValue;
        if ((type == double.class || type == Double.class) && strValue.matches("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?")) {
            return Double.parseDouble(s);
        }
        if ((type == float.class || type == Float.class) && strValue.matches("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?")) {
            return Float.parseFloat(s);
        }

        // ✅ Handle Enums Properly
        if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, strValue);
        }
        return value;
    }

    private Object parseJsonValue(String value) {
        value = value.trim();

        if (value.equals("null")) return null;

        // ✅ Ensure object parsing
        if (value.startsWith("{") && value.endsWith("}")) {
            return parseJsonMap(value);
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            return parseJsonList(parseJsonRawList(value), Object.class);
        }

        // ✅ Fix: Ensure proper string parsing
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }

        // ✅ Boolean Handling
        if (value.equals("true") || value.equals("false")) return Boolean.parseBoolean(value);

        // ✅ Fix: Handle Floating Point Numbers Properly
        if (value.matches("-?\\d+\\.\\d+")) {
            return Double.parseDouble(value);
        }

        // ✅ Fix: Handle Integer Numbers
        if (value.matches("-?\\d+")) {
            return Integer.parseInt(value);
        }

        // ✅ Default Case (Return as String)
        return value;
    }

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"");
    }

    private boolean isCustomClass(Class<?> clazz) {
        return !(clazz.isPrimitive() || clazz.equals(String.class) || clazz.isAssignableFrom(String.class) || Number.class.isAssignableFrom(clazz) || clazz.equals(Boolean.class));
    }

    // ✅ Save JSON to File
    public void saveToFile(File file, Object object) throws Exception {
        String json = serializeObject(object);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }

    // ✅ Load JSON from File
    public Object loadFromFile(File file, Class<?> clazz) throws Exception {
        if (!file.exists()) return null;
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        return deserializeObject(jsonBuilder.toString(), clazz);
    }
}