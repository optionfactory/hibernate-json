package net.optionfactory.hj.mysql;

import java.sql.Types;
import org.hibernate.dialect.MySQL57InnoDBDialect;

public class MySQL57InnoDBDialectWithJson extends MySQL57InnoDBDialect {

    public MySQL57InnoDBDialectWithJson() {
        registerColumnType(Types.JAVA_OBJECT, "json");        
    }
    
}
