package com.example.DM_Final;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class InheritanceParser {
    private final String packageName = "com.example.DM_Final.";
    public void parse(Object object,String className,String request) throws ClassNotFoundException, IllegalAccessException {

        String[] inheritanceQuery = request.split("&");
        Map<String, Object> parentMapFieldValues = new HashMap<>();

        for (String s : inheritanceQuery) {
            parentMapFieldValues.put(s.split("=")[0], s.split("=")[1]);
        }

        Class<?> childClass = Class.forName(packageName + className);
        Class<?> parentClass =childClass.getSuperclass();
        Field[] parentFields = parentClass.getDeclaredFields();

        for (Field field : parentFields) {
            if (parentMapFieldValues.containsKey(field.getName())) {

                if (field.getType().toString().equals("int")) {
                    field.set(object, Integer.valueOf(parentMapFieldValues.get(field.getName()).toString()));
                } else if (field.getType().toString().equals("double")) {
                    field.set(object, Double.valueOf(parentMapFieldValues.get(field.getName()).toString()));
                } else {
                    field.set(object, parentMapFieldValues.get(field.getName()));
                }
            }
        }

    }
}
