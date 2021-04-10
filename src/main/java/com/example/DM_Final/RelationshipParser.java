package com.example.DM_Final;

import com.db4o.ObjectSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationshipParser {

    private final String packageName = "com.example.DM_Final.";
    public List<Object> parser(String request) throws Exception{

        String[] relationshipQuery = request.split("&");
        Map<String, Object> map = new HashMap<>();
        for (String s : relationshipQuery) {
            map.put(s.split("=")[0], s.split("=")[1]);
        }
        String relatedClassName = map.get("relatedClass").toString();
        Class<?> clazz = Class.forName(packageName + relatedClassName);
        String[] relatedIds = map.get("relatedId").toString().substring(1,map.get("relatedId").toString().length()-1).split(",");

        DynamicMappingParser dynamicMappingParser = new DynamicMappingParser();
        List<Object> listOfRelatedObject = new ArrayList<Object>();
        for(String id:relatedIds){
            Object relatedObject = clazz.newInstance();
            relatedObject.getClass().getDeclaredFields()[0].set(relatedObject,Integer.valueOf(id));
            relatedObject = dynamicMappingParser.retrieve(relatedObject);
            listOfRelatedObject.add(relatedObject);
        }


        return listOfRelatedObject;
    }

}

