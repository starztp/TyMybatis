package org.apache.ibatis.generator;

/**
 * Created by tianyou on 2020/6/5.
 */
public interface ResultTempClassGenerator {

    public void generate();

    public StringBuffer getTempclass();

    public String getClassname();
}
