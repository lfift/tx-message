package com.ift.txmessage.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.OracleTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AuthMyBatisPlusGenerator {

    private static final String datasourcesUrl = "jdbc:oracle:thin:@192.168.0.202:1521/kfdb.zt";
    private static final String datasourcesUsername = "gsywjz";
    private static final String datasourcesPassword = "zenithinfo";
    private static final String datasourcesDriver = "oracle.jdbc.OracleDriver";


    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入" + tip + "：");
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        //代码生成器
        AutoGenerator autoGenerator = new AutoGenerator();

        //全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        final String projectPath = System.getProperty("user.dir");
        globalConfig.setOutputDir(projectPath + "/src/main/java");
        globalConfig.setAuthor("liufei");
//        globalConfig.setOpen(false);
        //是否覆盖文件
        globalConfig.setFileOverride(true);
        //不需要ActiveRecord特性的请改为false
        globalConfig.setActiveRecord(false);
        //XML 二级缓存
        globalConfig.setEnableCache(false);
        //XML ResultMap
        globalConfig.setBaseResultMap(true);
        //XML columList
        globalConfig.setBaseColumnList(false);
        globalConfig.setDateType(DateType.ONLY_DATE);
        globalConfig.setDateType(DateType.TIME_PACK);
        autoGenerator.setGlobalConfig(globalConfig);

        //数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.ORACLE);
        //类型转换
        dataSourceConfig.setTypeConvert(new OracleTypeConvert() {
            @Override
            public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                return super.processTypeConvert(globalConfig, fieldType);
            }
        });

        dataSourceConfig.setUrl(datasourcesUrl);
        dataSourceConfig.setDriverName(datasourcesDriver);
        dataSourceConfig.setUsername(datasourcesUsername);
        dataSourceConfig.setPassword(datasourcesPassword);
        autoGenerator.setDataSource(dataSourceConfig);

        //包配置
        final PackageConfig packageConfig = new PackageConfig();
        //基础包名
        packageConfig.setParent("com.ift.txmessage");
        //模块名
        //packageConfig.setModuleName(scanner("模块名"));
        packageConfig.setService("service");
        packageConfig.setServiceImpl("service.impl");
        packageConfig.setEntity("entity");
        packageConfig.setController("controller");
        packageConfig.setMapper("mapper");
        packageConfig.setXml("mapper");
        autoGenerator.setPackageInfo(packageConfig);

        // 自定义配置
        InjectionConfig injectionConfig = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig("/templates/mapper.xml.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输入文件名称
                return projectPath + "/src/main/resources/mapper/"
                        + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        injectionConfig.setFileOutConfigList(focList);
        autoGenerator.setCfg(injectionConfig);
        autoGenerator.setTemplate(new TemplateConfig().setXml(null));
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(false);
        strategy.setRestControllerStyle(true);
        strategy.setCapitalMode(true);
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setEntityTableFieldAnnotationEnable(true);
        strategy.setEntityLombokModel(true);
        List<TableFill> tableFillList = new ArrayList<>();
        tableFillList.add(new TableFill("DELETED", FieldFill.INSERT));
        tableFillList.add(new TableFill("CREATE_TIME", FieldFill.INSERT));
        tableFillList.add(new TableFill("UPDATE_TIME", FieldFill.INSERT));
        tableFillList.add(new TableFill("UPDATE_TIME", FieldFill.UPDATE));
        strategy.setTableFillList(tableFillList);
//        strategy.setTablePrefix("n_");
        strategy.setLogicDeleteFieldName("DELETED");
        String tableNames = scanner("表名(多个用;分开)").toUpperCase();
        strategy.setInclude(tableNames.split(";"));
        autoGenerator.setStrategy(strategy);
        autoGenerator.setTemplateEngine(new VelocityTemplateEngine());
        autoGenerator.execute();
    }
}
