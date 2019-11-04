package com.bootdo.common.utils;

import com.bootdo.common.config.Constant;
import com.bootdo.common.domain.ColumnDO;
import com.bootdo.common.domain.TableDO;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @ClassName GenUtils
 * @Description 代码生成器工具类
 * @Author SJLY
 * @Date: 2019/10/28 15:09
 * @Version 1.0
 **/
public class GenUtils {
    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("templates/common/generator/domain.java.vm");
        templates.add("templates/common/generator/Dao.java.vm");
        //templates.add("templates/common/generator/Mapper.java.vm");
        templates.add("templates/common/generator/Mapper.xml.vm");
        templates.add("templates/common/generator/Service.java.vm");
        templates.add("templates/common/generator/ServiceImpl.java.vm");
        templates.add("templates/common/generator/Controller.java.vm");
        templates.add("templates/common/generator/codeEnum.java.vm");
        templates.add("templates/common/generator/converter.java.vm");
        templates.add("templates/common/generator/vo.java.vm");
        templates.add("templates/common/generator/client.java.vm");
        templates.add("templates/common/generator/entity.java.vm");
        //templates.add("templates/common/generator/query.java.vm");
//        templates.add("templates/common/generator/list.html.vm");
//        templates.add("templates/common/generator/add.html.vm");
//        templates.add("templates/common/generator/edit.html.vm");
//        templates.add("templates/common/generator/list.js.vm");
//        templates.add("templates/common/generator/add.js.vm");
//        templates.add("templates/common/generator/edit.js.vm");
        //templates.add("templates/common/generator/menu.sql.vm");
        return templates;
    }



    /**
     * @MethodName: generatorCode
     * @Description 代码生成
     * @Param: [table, columns, zip]
     * @Return: void
     * @Author: liyao
     * @Date: 2019/10/28 15:31
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns, ZipOutputStream zip) {
        // 配置信息
        Configuration config = getConfig();
        // 表信息
        TableDO tableDO = new TableDO();
        tableDO.setTableName(table.get("tableName"));
        tableDO.setComments(table.get("tableComment"));
        // 所有的列明和代值
        String allColumnName = "";
        // 表名转换成Java类名
        String className = tableToJava(tableDO.getTableName(), config.getString("tablePrefix"), config.getString("autoRemovePre"));
        tableDO.setClassName(className);
        tableDO.setClassname(org.apache.commons.lang.StringUtils.uncapitalize(className));
        // 查询实体名
        String classQueryName = className+"Query";
        // 所有的列明和代值
        String allColumnValue="";
        // 批量列表对象查询循环数据
        String allColumnValueList="";

        // 列信息
        List<ColumnDO> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnDO columnDO = new ColumnDO();
            columnDO.setColumnName(column.get("columnName"));
            columnDO.setDataType(column.get("dataType"));
            columnDO.setComments(column.get("columnComment"));
            columnDO.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnDO.getColumnName());
            columnDO.setAttrName(attrName);
            columnDO.setAttrname(org.apache.commons.lang.StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnDO.getDataType(), "unknowType");
            columnDO.setAttrType(attrType);

            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableDO.getPk() == null) {
                tableDO.setPk(columnDO);
            }

            // 所有列的拼接字符串
            allColumnName+=columnDO.getColumnName()+", ";

            allColumnName+=columnDO.getColumnName()+", ";
            switch (columnDO.getAttrname()){
                case "gmtCreate":
                    allColumnValue +="NOW(), ";
                    allColumnValueList +="NOW(), ";
                    break;
                case "gmtModified":
                    allColumnValue +="NOW(), ";
                    allColumnValueList +="NOW(), ";
                    break;
                case "is_deleted":
                    allColumnValue +="0, ";
                    allColumnValueList +="0, ";
                    break;
                default:
                    allColumnValue +="#{"+columnDO.getAttrname()+"}, ";
                    allColumnValueList +="#{"+tableDO.getClassname()+"DO."+columnDO.getAttrname()+"}, ";
                    break;
            }

            columsList.add(columnDO);
        }
        tableDO.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableDO.getPk() == null) {
            tableDO.setPk(tableDO.getColumns().get(0));
        }


        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        //封装模板数据
        Map<String, Object> map = new HashMap<>(16);
        map.put("tableName", tableDO.getTableName());
        map.put("comments", tableDO.getComments());
        map.put("pk", tableDO.getPk());
        map.put("className", tableDO.getClassName());
        map.put("classname", tableDO.getClassname());
        map.put("pathName", config.getString("packgeneratorage").substring(config.getString("package").lastIndexOf(".") + 1));
        map.put("columns", tableDO.getColumns());
        map.put("package", config.getString("package"));
        map.put("author", config.getString("author"));
        map.put("classQueryName",classQueryName);
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        map.put("allColumnName",allColumnName.substring(0,allColumnName.length() - 2));
        map.put("allColumnValue",allColumnValue.substring(0,allColumnValue.length() - 2));
        map.put("allColumnValueList",allColumnValueList.substring(0,allColumnValue.length() - 2));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableDO.getClassname(), tableDO.getClassName(), config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new BDException("渲染模板失败，表名：" + tableDO.getTableName(), e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix, String autoRemovePre) {
        if (Constant.AUTO_REOMVE_PRE.equals(autoRemovePre)) {
            tableName = tableName.substring(tableName.indexOf("_") + 1);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }

        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new BDException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String classname, String className, String packageName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        //String modulesname=config.getString("packageName");
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }

        if (template.contains("domain.java.vm")) {
            return packagePath + "domain" + File.separator + className + "DO.java";
        }

        if (template.contains("Dao.java.vm")) {
            return packagePath + "dao" + File.separator + className + "Dao.java";
        }

//		if(template.contains("Mapper.java.vm")){
//			return packagePath + "dao" + File.separator + className + "Mapper.java";
//		}

        if (template.contains("Service.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("Controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if (template.contains("Mapper.xml.vm")) {
            return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + packageName + File.separator + className + "Mapper.xml";
        }

        if (template.contains("vo.java.vm")) {
            return packagePath + "api" + File.separator + "vos" + File.separator + "vo" + File.separator + className + "VO.java";
        }

        if (template.contains("codeEnum.java.vm")) {
            return packagePath + "common" + File.separator + "enums" + File.separator + "enum" + File.separator + className + "CodeEnum.java";
        }

        if (template.contains("converter.java.vm")) {
            return packagePath + "biz" + File.separator + "converter" + File.separator +  "converter" + className + "Converter.java";
        }

        if (template.contains("client.java.vm")) {
            return packagePath + "api" + File.separator + "facade" + File.separator + "client" + File.separator + className + "Client.java";
        }

        if (template.contains("entity.java.vm")) {
            return packagePath + "dal" + File.separator + "entity" + File.separator + "entity" + File.separator + className + "DO.java";
        }
//        if (template.contains("query.java.vm")) {
//            return packagePath + "dal" + File.separator + "query" + File.separator + "query" + File.separator + className + "Query.java";
//        }

//        if (template.contains("list.html.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
//                    + packageName + File.separator + classname + File.separator + classname + ".html";
//            //				+ "modules" + File.separator + "generator" + File.separator + className.toLowerCase() + ".html";
//        }
//        if (template.contains("add.html.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
//                    + packageName + File.separator + classname + File.separator + "add.html";
//        }
//        if (template.contains("edit.html.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "templates" + File.separator
//                    + packageName + File.separator + classname + File.separator + "edit.html";
//        }
//
//        if (template.contains("list.js.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "static" + File.separator + "js" + File.separator
//                    + "appjs" + File.separator + packageName + File.separator + classname + File.separator + classname + ".js";
//            //		+ "modules" + File.separator + "generator" + File.separator + className.toLowerCase() + ".js";
//        }
//        if (template.contains("add.js.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "static" + File.separator + "js" + File.separator
//                    + "appjs" + File.separator + packageName + File.separator + classname + File.separator + "add.js";
//        }
//        if (template.contains("edit.js.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "static" + File.separator + "js" + File.separator
//                    + "appjs" + File.separator + packageName + File.separator + classname + File.separator + "edit.js";
//        }

//		if(template.contains("menu.sql.vm")){
//			return className.toLowerCase() + "_menu.sql";
//		}

        return null;
    }
}