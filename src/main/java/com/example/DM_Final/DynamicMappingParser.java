package com.example.DM_Final;


import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

import java.io.EOFException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicMappingParser {

    private final String packageName = "com.example.DM_Final.";

    public Object parse(String request) throws Exception {
        String[] split = request.split("@")[0].split("&");
        Map<String, Object> map = new HashMap<>();
        for (String s : split) {
            map.put(s.split("=")[0], s.split("=")[1]);
        }
        if (request.split("@").length == 1)
            return parse(map.get("className").toString(), map, "");
        else
            return parse(map.get("className").toString(), map, request.split("@")[1]);
    }

    public Object parse(String className, Map<String, Object> parameters, String relatedQuery) throws Exception {
        Class<?> clazz = Class.forName(packageName + className);
        Object object = clazz.newInstance();
        Class<?> aClass = clazz.cast(object).getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        int i = 1;

        for (Field field : declaredFields) {
            if (parameters.containsKey(field.getName())) {

                if (field.getType().toString().equals("int")) {
                    field.set(object, Integer.valueOf(parameters.get(field.getName()).toString()));
                } else if (field.getType().toString().equals("double")) {
                    field.set(object, Double.valueOf(parameters.get(field.getName()).toString()));
                } else {
                    field.set(object, parameters.get(field.getName()));
                }
            }
        }
        //relatedQuery Operations
        if (!relatedQuery.equals("")) {
            String[] split = relatedQuery.split("&");
            Map<String, Object> map = new HashMap<>();
            for (String s : split) {
                map.put(s.split("=")[0], s.split("=")[1]);
            }
            switch (map.get("queryType").toString()) {
                //RelationShip parser
                case "relationship": {
                    String relationType = map.get("relationship").toString();
                    List<Object> listofrelatedObject = new RelationshipParser().parser(relatedQuery);
                    if (listofrelatedObject == null || listofrelatedObject.size() == 0) break;
                    switch (relationType) {
                        case "oneToOne": {
                            declaredFields[declaredFields.length - 1].set(object, listofrelatedObject.get(0));
                            break;
                        }
                        case "oneToMany": {
                            Object relatedObject = listofrelatedObject.get(0);              //In One to many we'll get only 1 related object
                            Field[] relatedDeclaredFields = relatedObject.getClass().getDeclaredFields();
                            //check code
                            List<Object> listOfObject = (List<Object>) relatedDeclaredFields[relatedDeclaredFields.length - 1].get(relatedObject);
                            if (listOfObject == null) {
                                listOfObject = new ArrayList<>();
                                relatedDeclaredFields[relatedDeclaredFields.length - 1].set(relatedObject, listOfObject);
                            }
                            listOfObject.add(object);
                            declaredFields[declaredFields.length - 1].set(object, listofrelatedObject.get(0));
                            update(relatedObject);
                            Object response = cloneObject(object);
                            return response;
                        }
                        case "manyToMany": {
                            //listOfRelatedObject - many to many

                            Field[] relatedDeclaredFields = listofrelatedObject.get(0).getClass().getDeclaredFields();
                            //update student in courses list
                            for(Object relatedObject :listofrelatedObject){
                                List<Object> listOfObject = (List<Object>) relatedDeclaredFields[relatedDeclaredFields.length - 1].get(relatedObject);
                                if (listOfObject == null) {
                                    listOfObject = new ArrayList<>();
                                    relatedDeclaredFields[relatedDeclaredFields.length - 1].set(relatedObject, listOfObject);
                                }
                                listOfObject.add(object);
                            }
                            declaredFields[declaredFields.length-1].set(object,listofrelatedObject);
                            for(Object relatedObject:listofrelatedObject){
                                update((relatedObject));
                            }
                            Object response = cloneObject(object);
                            return response;
                        }
                    }
                }

                //inheritance parser
                case "inheritance":{
                    new InheritanceParser().parse(object,className,relatedQuery);
                }
            }
        }
        switch (parameters.get("operation").toString()) {
            case "insert": {
                return insert(object);
            }
            case "retrieve": {
                return retrieve(object);
            }
            case "update": {
                return update(object);
            }
            case "delete": {
                return delete(object);
            }
            case "getAll": {
                return getAll(object);
            }
        }
        return null;
    }

    public Object cloneObject(Object object) throws Exception {

        Field[] declaredFields = object.getClass().getDeclaredFields();
        String lastField=declaredFields[declaredFields.length-1].getType().toString();
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

    public Object getAll(Object object) {
        ObjectContainer db = Db4o.openFile("Demo");
        try {
            ObjectSet<?> query = db.query(object.getClass());
            return query;
        } finally {
            db.close();
        }
    }

    public String insert(Object object) {
        ObjectContainer db = null;
        try {
            if (retrieve(object) == null) {
                db = Db4o.openFile("Demo");
                db.store(object);

                return "Inserted Sucessfully";
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            if (db != null)
                db.close();
        }
        return "Error";
    }

    public String update(Object newObject) throws Exception {
        Object oldObject = retrieve(newObject);
        delete(oldObject);
        ObjectContainer db = Db4o.openFile("Demo");
        try {
            Field[] declaredFields = newObject.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.get(newObject) != null) {
                    field.set(oldObject, field.get(newObject));
                }
            }
            db.store(oldObject);
            return "Sucessfully Updated";
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            db.close();
        }
        return "Error";
    }

    public String delete(Object object) throws Exception {
        ObjectContainer db = Db4o.openFile("Demo");
        try {
            ObjectSet<?> result = db.query(object.getClass());
            for (Object o : result) {
                if (object.getClass().getDeclaredFields()[0].get(object).equals(o.getClass().getDeclaredFields()[0].get(o))) {
                    System.out.println("Found match");
                    System.out.println(o);
                    db.delete(o);

                    return "Deleted Sucessfully";
                }
            }
        } finally {
            db.close();
        }
        System.out.println("No Match found");
        return "Error";
    }

    public Object retrieve(Object object) throws Exception {
        ObjectContainer db = Db4o.openFile("Demo");
        try {
            ObjectSet<?> result = db.query(object.getClass());
            for (Object o : result) {
                if (object.getClass().getDeclaredFields()[0].get(object).equals(o.getClass().getDeclaredFields()[0].get(o))) {
                    System.out.println("Found match");
                    System.out.println(o);
                    Object response = cloneObject(o);
                    return response;
                }
            }
        } finally {
            db.close();
        }
        System.out.println("No Match found");
        return null;
    }

}


