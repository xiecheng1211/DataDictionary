package entity;

import java.util.List;

/**
 * @author xdc
 * created by 2019/7/15
 */
public class TableInfo {

    private String tabelName;
    private String tableComment;

    private List<TableProperties> propertiesList;

    public List<TableProperties> getPropertiesList() {
        return propertiesList;
    }

    public void setPropertiesList(List<TableProperties> propertiesList) {
        this.propertiesList = propertiesList;
    }

    public String getTabelName() {
        return tabelName;
    }

    public void setTabelName(String tabelName) {
        this.tabelName = tabelName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }
}
