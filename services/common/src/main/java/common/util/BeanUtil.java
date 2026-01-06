package common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Bean 转换工具类
 * 
 * 功能：
 * - Bean 对象转换
 * - 对象转 Map
 * - Map 转对象
 * - 属性复制
 */
public class BeanUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 对象转 Map（浅拷贝）
     * 
     * @param bean Bean 对象
     * @return Map 集合
     */
    public static Map<String, Object> toMap(Object bean) {
        if (bean == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = bean.getClass();

        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(bean));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("转换失败", e);
        }

        return map;
    }

    /**
     * Map 转对象
     * 
     * @param map   Map 集合
     * @param clazz 目标类
     * @param <T>   目标类型
     * @return 转换后的对象
     */
    public static <T> T toBean(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        try {
            T bean = clazz.getDeclaredConstructor().newInstance();
            Class<?> beanClass = bean.getClass();
            Field[] fields = beanClass.getDeclaredFields();

            for (Field field : fields) {
                if (map.containsKey(field.getName())) {
                    field.setAccessible(true);
                    field.set(bean, map.get(field.getName()));
                }
            }

            return bean;
        } catch (Exception e) {
            throw new RuntimeException("转换失败", e);
        }
    }

    /**
     * 对象复制（浅拷贝）
     * 
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }

        try {
            Class<?> sourceClass = source.getClass();
            Class<?> targetClass = target.getClass();

            Field[] sourceFields = sourceClass.getDeclaredFields();
            for (Field sourceField : sourceFields) {
                try {
                    Field targetField = targetClass.getDeclaredField(sourceField.getName());
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);

                    Object value = sourceField.get(source);
                    targetField.set(target, value);
                } catch (NoSuchFieldException e) {
                    // 目标对象中不存在该字段，跳过
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("复制失败", e);
        }
    }

    /**
     * 对象转换（转换相同属性）
     * 
     * @param source 源对象
     * @param clazz  目标类
     * @param <T>    目标类型
     * @return 转换后的对象
     */
    public static <T> T convert(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }

        try {
            T target = clazz.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("转换失败", e);
        }
    }

    /**
     * 对象转 JSON 字符串
     * 
     * @param bean Bean 对象
     * @return JSON 字符串
     */
    public static String toJson(Object bean) {
        if (bean == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(bean);
        } catch (Exception e) {
            throw new RuntimeException("转换失败", e);
        }
    }

    /**
     * JSON 字符串转对象
     * 
     * @param json  JSON 字符串
     * @param clazz 目标类
     * @param <T>   目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("转换失败", e);
        }
    }

    /**
     * 列表对象转换
     * 
     * @param sourceList 源列表
     * @param clazz      目标类
     * @param <T>        目标类型
     * @return 转换后的列表
     */
    public static <T> List<T> convertList(List<?> sourceList, Class<T> clazz) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> targetList = new ArrayList<>();
        for (Object source : sourceList) {
            targetList.add(convert(source, clazz));
        }

        return targetList;
    }

    /**
     * 获取对象的属性值
     * 
     * @param bean      Bean 对象
     * @param fieldName 属性名
     * @return 属性值
     */
    public static Object getFieldValue(Object bean, String fieldName) {
        if (bean == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }

        try {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(bean);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 设置对象的属性值
     * 
     * @param bean      Bean 对象
     * @param fieldName 属性名
     * @param value     属性值
     */
    public static void setFieldValue(Object bean, String fieldName, Object value) {
        if (bean == null || fieldName == null || fieldName.isEmpty()) {
            return;
        }

        try {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(bean, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("设置属性失败", e);
        }
    }

    /**
     * 判断对象是否为空（所有属性都为 null）
     * 
     * @param bean Bean 对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object bean) {
        if (bean == null) {
            return true;
        }

        try {
            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.get(bean) != null) {
                    return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    /**
     * 对象克隆（深拷贝，通过 JSON 序列化）
     * 
     * @param source 源对象
     * @param <T>    对象类型
     * @return 克隆后的对象
     */
    public static <T> T deepClone(T source) {
        if (source == null) {
            return null;
        }

        try {
            String json = toJson(source);
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) source.getClass();
            return fromJson(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("克隆失败", e);
        }
    }

    /**
     * 比较两个对象的属性值是否相同
     * 
     * @param obj1 对象 1
     * @param obj2 对象 2
     * @return 是否相同
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (!obj1.getClass().equals(obj2.getClass())) {
            return false;
        }

        try {
            Field[] fields = obj1.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);

                if (!Objects.equals(value1, value2)) {
                    return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    /**
     * 过滤 Map 中的空值
     * 
     * @param map 原 Map
     * @return 过滤后的 Map
     */
    public static Map<String, Object> filterNullValues(Map<String, Object> map) {
        if (map == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        map.forEach((key, value) -> {
            if (value != null) {
                result.put(key, value);
            }
        });

        return result;
    }
}
