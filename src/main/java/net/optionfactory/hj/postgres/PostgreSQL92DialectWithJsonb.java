package net.optionfactory.hj.postgres;

import java.sql.Types;
import org.hibernate.dialect.PostgreSQL92Dialect;

public class PostgreSQL92DialectWithJsonb extends PostgreSQL92Dialect {

    public PostgreSQL92DialectWithJsonb() {
        registerColumnType(Types.OTHER, "jsonb");
    }
    
}
