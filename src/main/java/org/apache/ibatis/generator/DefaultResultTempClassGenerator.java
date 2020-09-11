package org.apache.ibatis.generator;

import org.apache.ibatis.mapping.MappedStatement;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyou on 2020/6/5.
 * tempclass生成逻辑
 */
public class DefaultResultTempClassGenerator implements ResultTempClassGenerator{

    private MappedStatement mappedStatement;
    private List<String> columnNames=new ArrayList<>();
    private List<String> classNames=new ArrayList<>();
    private List<Map<String,Object>> resultList=new ArrayList<>();;//List<Map<String,Object>>形式保存查询结果集
    private Map<String,String> fieldClassMap=new HashMap<String,String>();//关联字段与Java类类型
    private StringBuffer tempclass=new StringBuffer();//临时类StringBuffer对象
    private String packageStatement;//生成import语句固定格式
    private String classname;//临时对象名称

    /**
     *
     * @param columnNames   sql查询的列名集合
     * @param classNames    sql查询的列名映射的java类型
     * @param mappedStatement
     */
    public DefaultResultTempClassGenerator(List<String> columnNames,List<String> classNames,MappedStatement mappedStatement){
        this.mappedStatement=mappedStatement;
        this.columnNames=columnNames;
        this.classNames=classNames;
        //关联查询字段名称和对应的Java类型
        mapFieldsAndClasses(columnNames,classNames);
    }

    /**
    //给resultList赋值
    private void addresultSet(){
        int columncount=0;
        try {
            ResultSetMetaData metaData=resultSet.getMetaData();
            columncount=metaData.getColumnCount();
            for(int i=1;i<columncount;i++){
                Map<String,Object> map=new HashMap<>();
                String columnName=metaData.getColumnName(i);
                Object value=resultSet.getObject(columnName);
                map.put(columnName,value);
                this.resultList.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     **/

    //关联查询字段名称和对应的Java类型
    private void mapFieldsAndClasses(List<String> columnNames,List<String> classNames){
        for(int i=0;i<columnNames.size();i++){
            fieldClassMap.put(columnNames.get(i),classNames.get(i));
        }
        /**
        Map<String,Object> result=resultList.get(0);
        for(String field:result.keySet()){
            //获取字段值类型
            String fieldtypeName=result.get(field).getClass().getTypeName();
            fieldClassMap.put(field,fieldtypeName);
        }
         **/
    }

    //生成类文件内容
    @Override
    public void generate() {
        generatePackageStatement();
        generateImportStatement();
        generateClassTitle();
        generateFields();
        generatorGetter();
        generatorSetter();
        generateClassEnd();
    }

    private void generatePackageStatement(){
        tempclass.append(appendPackageStatement());
    }

    private String appendPackageStatement(){
        this.packageStatement="package "+mappedStatement.gettempPackage()+".temp;\r\n";
        return this.packageStatement;
    }

    private void generateImportStatement(){
        tempclass.append(appendImportStatement());
    }

    private String appendImportStatement(){
        String importStatement="";
        for(int i=0;i<this.classNames.size();i++){
            importStatement=importStatement+"import "+this.classNames.get(i)+";\r\n";
        }
        return importStatement;
    }

    private void generateClassTitle(){
        tempclass.append(appendClassTitle());
    }

    private String appendClassTitle(){
        this.classname=getLastSplit(mappedStatement.getId(),"\\.");
        String ClassTitle="public class "+this.classname+"{\r\n";
        return ClassTitle;
    }

    private void generateFields(){
        for(Map.Entry<String,String> entry: fieldClassMap.entrySet()){
            tempclass.append(appendField(entry.getKey(),entry.getValue()));
        }
    }

    private String appendField(String fieldname,String fieldtype){
        String fieldstatement="private "+getLastSplit(fieldtype,"\\.")+" "+fieldname+";\r\n";
        fieldstatement=fieldstatement+"\r\n";
        return fieldstatement;
    }

    private void generatorGetter(){
        for(Map.Entry<String,String> entry: fieldClassMap.entrySet()){
                tempclass.append(appendGetter(entry.getKey(),entry.getValue()));
        }
    }

    private String appendGetter(String fieldname,String fieldtype){
        String getterstatement="public "+getLastSplit(fieldtype,"\\.")+" get"+fieldname+" (){\r\n";
        getterstatement=getterstatement+"       return this."+fieldname+";\r\n";
        getterstatement=getterstatement+"}\r\n";
        return getterstatement;
    }

    private void generatorSetter(){
        for(Map.Entry<String,String> entry: fieldClassMap.entrySet()){
                tempclass.append(appendSetter(entry.getKey(),entry.getValue()));
        }
    }

    private String appendSetter(String fieldname,String fieldtype){
        String setterstatement="public void set"+fieldname+" ("+getLastSplit(fieldtype,"\\.")+" "+fieldname+"){\r\n";
        setterstatement=setterstatement+"       this."+fieldname+"="+fieldname+";\r\n";
        setterstatement=setterstatement+"}\r\n";
        return setterstatement;
    }

    private void generateClassEnd(){
        tempclass.append(appendClassEnd());
    }

    private String appendClassEnd(){
        String end="}";
        return end;
    }

    /**
     * 根据分隔符切字符串，获取最后一个分割的字符串
     * @param string
     * @param regex
     * @return
     */
    private String getLastSplit(String string,String regex){
        String stringArray[]=string.split(regex);
        return stringArray[stringArray.length-1];
    }

    public List<Map<String, Object>> getResultList() {
        return resultList;
    }

    public Map<String, String> getFieldClassMap() {
        return fieldClassMap;
    }

    public StringBuffer getTempclass() {
        return tempclass;
    }

    public String getClassname() {
        return classname;
    }
}
