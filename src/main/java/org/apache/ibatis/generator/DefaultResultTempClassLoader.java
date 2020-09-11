package org.apache.ibatis.generator;

import org.apache.ibatis.mapping.MappedStatement;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;

/**
 * Created by tianyou on 2020/6/9.
 * 负责生成临时的.java和.class文件并放到指定位置
 */
public class DefaultResultTempClassLoader extends ClassLoader {

    private ResultTempClassGenerator generator;
    private File javaFileDir;
    private String javaFile;
    private File classFileDir;
    private String ClassFile;//编译后的.class文件路径
    private String entityClass;//临时类文件内容
    private MappedStatement mappedStatement;
    private final String temp="/temp/";

    public DefaultResultTempClassLoader(ResultTempClassGenerator generator, MappedStatement mappedStatement){
        this.generator=generator;
        this.entityClass=generator.getTempclass().toString();
        this.mappedStatement=mappedStatement;
        try {
            //生成.java文件路径
            generateJavaFilePath();
            //生成.class文件路径
            generateClassFilePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取新生成的ORM类实例
    public Object newTempInstance() throws Exception {
        File javaFile=generateEntityFile();
        compileJavaFile(javaFile);
        copyClassFile();
        return newClassInstance();
    }


    /**
     * 生成编译后的.class文件输出路径
     */
    private void generateClassFilePath(){
        //File thisfiledir=new File(DefaultResultTempClassLoader.class.getResource("").getPath());
        //log.info("thisfiledir:"+thisfiledir);
        URL url = this.getClass().getClassLoader()
                .getResource(mappedStatement.gettempPackage().replaceAll("\\.","/"));
        File classFileDir = new File(url.getFile()+temp);
        if(!classFileDir.exists()){
            classFileDir.mkdirs();
            try {
                classFileDir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.classFileDir=classFileDir;
        this.ClassFile=classFileDir.getPath()+"/"+generator.getClassname()+".class";
        //log.info("ClassFile:"+ClassFile);
    }

    /**
     * 生成.java文件路径
     */
    private void generateJavaFilePath() throws IOException {
        //String thisPackageName=DefaultResultTempClassLoader.class.getPackage().getName();
        //包路径转文件路径
        //String thisPackagePath=thisPackageName.replace(".","\\");
        //File thisPackageDir=new File(thisPackagePath);
        String userdir=System.getProperty("user.dir")+"/src/main/java/";
        //URL url = this.getClass().getClassLoader()
                //.getResource(userdir+mappedStatement.gettempPackage().replaceAll("\\.","/"));
        File javaFileDir = new File(userdir+mappedStatement.gettempPackage().replaceAll("\\.","/")+temp);
        if(!javaFileDir.exists()){
            javaFileDir.mkdirs();
            javaFileDir.createNewFile();
        }
        //String classpath=Class.class.getClass().getResource("/").getPath();
        //log.info("userdir："+userdir);
        //log.info("classpath:"+classpath);
        //log.info("javaFileDir:"+javaFileDir);
        this.javaFileDir=javaFileDir;
        this.javaFile=javaFileDir.getPath()+"/"+generator.getClassname()+".java";
        //log.info("javaFile:"+javaFile);
    }

    /**
     * 生成临时实体类.java文件
     * @return
     */
    private File generateEntityFile() throws IOException {
        File tempJavadir=new File(this.javaFileDir.getPath());
        //log.info("tempJavadirpath:"+tempJavadir.getPath());
        if(!tempJavadir.exists()){
            tempJavadir.mkdirs();
            tempJavadir.createNewFile();
        }
        File tempJavafile=new File(this.javaFile);

        FileWriter writer=new FileWriter(tempJavafile);
        writer.write(this.entityClass);
        //刷新流对象中的缓冲中的数据
        //将数据刷到目的地中
        writer.flush();
        //关闭流资源，但是关闭之前会刷新一次内部的缓冲中的数据。
        //将数据刷到目的地中去。
        //和flush区别：flush 刷新后，流可以继续使用，close刷新后，会将流关闭。
        writer.close();
        return tempJavafile;
    }

    /**
     * 编译.java文件，并生成.class文件
     * @param javafile
     * @throws IOException
     */
    private void compileJavaFile(File javafile) throws IOException {
        JavaCompiler compiler= ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager=compiler.getStandardFileManager(null,null,null);
        Iterable iterable=manager.getJavaFileObjects(javafile);
        JavaCompiler.CompilationTask task=compiler.getTask(null,manager,null,null,null,iterable);
        task.call();
        manager.close();
        //编译完成后删除java文件
        //javafile.delete();
    }

    /**
     * 将.java文件编译后生成的.class文件放到target文件夹下的同等目录结构
     */
    private void copyClassFile(){
        File classfile=new File(javaFileDir.getPath(),generator.getClassname()+".class");
        try {
            FileUtil.copy(classfile,new File(this.ClassFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将编译好的.class文件加载到JVM中
     */
    private Object newClassInstance() throws Exception {
        Class ProxyClass=findClass(generator.getClassname());
        return ProxyClass.newInstance();
    }

    @Override
    /**
     * 根据类名编译生成Class对象
     * @param name
     * @return
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        //Class.forName(generator.getClassname()).newInstance();
        String classname= null;
        try {

            classname = Class.forName(mappedStatement.gettempPackage()+".temp."+generator.getClassname()).newInstance().getClass().getPackage().getName()+"."+name;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        File classfile=new File(ClassFile);
        //log.info("classname:"+classname);
        FileInputStream inputStream=null;
        ByteArrayOutputStream outputStream=null;
        try {
            inputStream=new FileInputStream(classfile);
            outputStream=new ByteArrayOutputStream();
            byte[] buff=new byte[1024];
            int len;
            while ((len=inputStream.read(buff))!=-1){
                outputStream.write(buff,0,len);
            }
            return defineClass(classname,outputStream.toByteArray(),0, outputStream.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File getJavaFileDir() {
        return javaFileDir;
    }

    public String getJavaFile() {
        return javaFile;
    }

    public File getClassFileDir() {
        return classFileDir;
    }

    public String getClassFile() {
        return ClassFile;
    }
}
