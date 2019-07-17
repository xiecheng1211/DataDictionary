import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import entity.TableInfo;
import entity.TableProperties;
import org.apache.poi.ss.usermodel.Font;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        getDatas(getDataSourceProperties());

    }

    public static Map getDataSourceProperties() {

        String path = Main.class.getResource("").getPath();

        Properties prop = new Properties();
        Map<String, String> datasource = new HashMap<String, String>();
        try{
            InputStream in = new BufferedInputStream(new FileInputStream(path + "\\setting.properties"));
            prop.load(in);
            Iterator<String> it=prop.stringPropertyNames().iterator();
            while(it.hasNext()){
                String key=it.next();
                datasource.put(key, prop.getProperty(key));
            }
            in.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
        return datasource;
    }

    public static void getDatas(Map<String, String> dataSourceProperties){
        String url = dataSourceProperties.get("datasource.url");
        String driverName = dataSourceProperties.get("datasource.driver-class-name");
        String username =  dataSourceProperties.get("datasource.username");
        String password =  dataSourceProperties.get("datasource.password");
        String dataBaseName =  dataSourceProperties.get("datasource.name");
        String filePath = dataSourceProperties.get("file.path");
        Connection connection = null;
        try{
            Class.forName(driverName);
            connection = DriverManager.getConnection(url, username, password);
            List<String> tablesName = getTablesName(connection);
            List<TableInfo> tableInfos = new ArrayList<TableInfo>();
            for (String tableName : tablesName) {
                TableInfo tableInfo = getTableInfo(connection, tableName, dataBaseName);
                tableInfos.add(tableInfo);
            }
            writeExcel(tableInfos, filePath);

            connection.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static List<String> getTablesName(Connection connection) {
        //查询出表信息
        PreparedStatement preparedStatement = null;
        String tablesSql = "SHOW TABLES;";
        List<String> list = new ArrayList<String>();
        try {
            preparedStatement = connection.prepareStatement(tablesSql);
            ResultSet tableNamesSet = preparedStatement.executeQuery();
            while (tableNamesSet.next()) {
                String tableName = tableNamesSet.getString(1);
                System.out.println(tableName);
                list.add(tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static TableInfo getTableInfo(Connection connection, String tableName, String dataBaseName) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("a.TABLE_NAME AS 'tabelName', ");
        sb.append("b.TABLE_COMMENT AS 'tableComment', ");
        sb.append("a.COLUMN_NAME AS 'columnName', ");
        sb.append("a.COLUMN_TYPE AS 'columnType', ");
        sb.append("a.IS_NULLABLE AS 'isNullAble', ");
        sb.append("a.COLUMN_DEFAULT AS 'columnDefault', ");
        sb.append("a.COLUMN_COMMENT AS 'columnComment', ");
        sb.append("a.COLUMN_KEY AS 'columnKey' ");
        sb.append("FROM ");
        sb.append("information_schema.COLUMNS AS a  ");
        sb.append("JOIN information_schema.TABLES AS b ON a.TABLE_SCHEMA = b.TABLE_SCHEMA  ");
        sb.append("AND a.TABLE_NAME = b.TABLE_NAME ");
        sb.append("WHERE  ");
        sb.append("a.TABLE_SCHEMA = '");
        sb.append(dataBaseName);
        sb.append("' ");
        sb.append("AND a.TABLE_NAME = '");
        sb.append(tableName);
        sb.append("' ");
        PreparedStatement preparedStatement = null;
        TableInfo tableInfo = new TableInfo();
        try {
            preparedStatement = connection.prepareStatement(sb.toString());
            ResultSet properties = preparedStatement.executeQuery();
            int index = 1;

            List<TableProperties> list = new ArrayList<TableProperties>();
            while (properties.next()) {
                if (index == 1) {
                    String tabelName = properties.getString("tabelName");
                    String tableComment = properties.getString("tableComment");
                    tableInfo.setTabelName(tabelName);
                    tableInfo.setTableComment(tableComment);
                }
                TableProperties tableProperties = new TableProperties();
                String columnName = properties.getString("columnName");
                String columnType = properties.getString("columnType");
                String isNullAble = properties.getString("isNullAble");
                String columnDefault = properties.getString("columnDefault");
                String columnComment = properties.getString("columnComment");
                String columnKey = properties.getString("columnKey");

                tableProperties.setColumnName(columnName);
                tableProperties.setColumnType(columnType);
                tableProperties.setIsNullAble(isNullAble);
                tableProperties.setColumnDefault(columnDefault);
                tableProperties.setColumnComment(columnComment);
                tableProperties.setColumnKey(columnKey);
                list.add(tableProperties);
                index ++;
            }
            tableInfo.setPropertiesList(list);
            return tableInfo;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableInfo;
    }


    public static void writeExcel(List<TableInfo> tableInfoList, String filePath) {

        List<Map<String, String>> rowsSheet1 = new ArrayList<Map<String, String>>();

        for (TableInfo tableInfo : tableInfoList) {
            Map<String, String> row = new LinkedHashMap<String, String>();
            row.put("表名", tableInfo.getTabelName());
            row.put("表备注", tableInfo.getTableComment());
            rowsSheet1.add(row);
        }
        String path = filePath + "\\DataDictionary.xlsx";
        File file = new File(path);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ExcelWriter writer = ExcelUtil.getWriter(path, "sheet1");
        writer.merge(1, "数据库表列表");
        writer.setColumnWidth(0, 100);
        writer.setColumnWidth(1, 100);
        writer.write(rowsSheet1, true);

        writer.setSheet("sheet2");
        writer.merge(5, "数据库详情");
        int currentX = 0;
        int currentY = 1;

        //设置字体信息
        Font font = writer.createFont();
        font.setFontName("微软雅黑");

        //第二个参数表示是否忽略头部样式
        writer.getStyleSet().setFont(font, true);

        writer.setColumnWidth(0,30);
        writer.setColumnWidth(1,50);
        writer.setColumnWidth(2,50);
        writer.setColumnWidth(3,20);
        writer.setColumnWidth(4,20);
        writer.setColumnWidth(5,50);

        for (TableInfo tableInfo : tableInfoList) {

            writer.writeCellValue(0 ,currentY, "表名");
            writer.writeCellValue(1 ,currentY, tableInfo.getTabelName());
            writer.writeCellValue(2 ,currentY, "表备注");
            writer.merge(currentY, currentY,3,5, tableInfo.getTableComment(), false);

            currentY ++;
            writer.writeCellValue(0 ,currentY, "字段名");
            writer.writeCellValue(1 ,currentY, "数据类型");
            writer.writeCellValue(2 ,currentY, "默认值");
            writer.writeCellValue(3 ,currentY, "允许非空");
            writer.writeCellValue(4 ,currentY, "主键约束");
            writer.writeCellValue(5 ,currentY, "备注");
            currentY ++;
            List<TableProperties> tablePropertiesList = tableInfo.getPropertiesList();

            for (TableProperties tableProperties:tablePropertiesList) {
                writer.writeCellValue(0 ,currentY, tableProperties.getColumnName());
                writer.writeCellValue(1 ,currentY, tableProperties.getColumnType());
                writer.writeCellValue(2 ,currentY, tableProperties.getColumnDefault());
                writer.writeCellValue(3 ,currentY, tableProperties.getIsNullAble());
                writer.writeCellValue(4 ,currentY, tableProperties.getColumnKey());
                writer.writeCellValue(5 ,currentY, tableProperties.getColumnComment());
                currentY ++;
            }
            currentY+=4;
        }

        writer.close();
    }
}
