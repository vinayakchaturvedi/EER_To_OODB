package com.example.DM_Final;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CloneObject {
    public Object clone(Object object) throws Exception {

        Field[] declaredFields = object.getClass().getDeclaredFields();
        String lastField=declaredFields[declaredFields.length-1].getType().getSimpleName();
        if(lastField.equals("int")||lastField.equals("double")||lastField.equals("String")){
            return object;
        }
        Object clonedObject = object.getClass().newInstance();

        for (Field field : declaredFields) {
            if(field.equals(declaredFields[declaredFields.length-1])) continue;
            field.set(clonedObject, field.get(object));

        }
        if(declaredFields[declaredFields.length-1].get(object)==null){
            return clonedObject;
        }
        if(declaredFields[declaredFields.length-1].get(object).getClass().toString().equals("class java.util.ArrayList")){
            List<Object> listofrelatedObjects= (List<Object>) declaredFields[declaredFields.length-1].get(object);
            List<Object> listOfClonedRelatedObject = new ArrayList<>();
            for(Object relatedObject:listofrelatedObjects){
                Object clonedRelatedObject = relatedObject.getClass().newInstance();
                Field[] relatedDeclaredFields = relatedObject.getClass().getDeclaredFields();

                for (Field field : relatedDeclaredFields) {
                    if(field.equals(relatedDeclaredFields[relatedDeclaredFields.length-1])) continue;
                    field.set(clonedRelatedObject, field.get(relatedObject));
                }
                listOfClonedRelatedObject.add(clonedRelatedObject);
            }
            declaredFields[declaredFields.length-1].set(clonedObject,listOfClonedRelatedObject);
            return  clonedObject;
        }
        Object clonedRelatedObject = declaredFields[declaredFields.length-1].get(object).getClass().newInstance();
        Object relatedObject = declaredFields[declaredFields.length-1].get(object);
        Field[] relatedDeclaredFields = relatedObject.getClass().getDeclaredFields();

        for (Field field : relatedDeclaredFields) {
            if(field.equals(relatedDeclaredFields[relatedDeclaredFields.length-1])) continue;
            field.set(clonedRelatedObject, field.get(relatedObject));

        }

        declaredFields[declaredFields.length-1].set(clonedObject, clonedRelatedObject);

        return clonedObject;
    }
}
