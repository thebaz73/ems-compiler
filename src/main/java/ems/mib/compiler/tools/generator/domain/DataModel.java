package ems.mib.compiler.tools.generator.domain;

import org.jsmiparser.smi.SmiObjectType;
import org.jsmiparser.smi.SmiRow;
import org.jsmiparser.smi.SmiTable;
import org.jsmiparser.smi.SmiVariable;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: thebaz
 * Date: 2/25/12
 * Time: 6:23 PM
 */
public class DataModel {
    String className;
    String packageName;
    Collection<SmiObjectType> objectTypes;
    Collection<SmiTable> tables;
    Collection<SmiRow> rows;
    Collection<SmiVariable> columns;
    Collection<SmiVariable> scalars;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Collection<SmiObjectType> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(Collection<SmiObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public Collection<SmiTable> getTables() {
        return tables;
    }

    public void setTables(Collection<SmiTable> tables) {
        this.tables = tables;
    }

    public Collection<SmiRow> getRows() {
        return rows;
    }

    public void setRows(Collection<SmiRow> rows) {
        this.rows = rows;
    }

    public Collection<SmiVariable> getColumns() {
        return columns;
    }

    public void setColumns(Collection<SmiVariable> columns) {
        this.columns = columns;
    }

    public Collection<SmiVariable> getScalars() {
        return scalars;
    }

    public void setScalars(Collection<SmiVariable> scalars) {
        this.scalars = scalars;
    }
}
