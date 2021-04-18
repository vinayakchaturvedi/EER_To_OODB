package com.example.DM_Final;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class UnionParser {
    private final String packageName = "com.example.DM_Final.";
    public Object parse(Object object, String className) throws Exception{
        ObjectContainer db = Db4o.openFile("Demo");
        try{
        Class<?> unionClass = Class.forName(packageName + className);
        Field[] fields = unionClass.getDeclaredFields();
        List<Object> objects = db.query((Class<Object>) unionClass);
        List<Object> response = new ArrayList<>();
        for(Object temp: objects){
            boolean matchFound=true;
            for(Field field:fields){
                if(field.get(temp)==null){
                    matchFound=false;
                    break;
                }
            }
            if(matchFound) response.add(temp);
        }
            return response;
        }
        catch (Exception e){
            System.out.println(e.toString());
            return null;
        }
        finally {
            db.close();
        }


    }
}
