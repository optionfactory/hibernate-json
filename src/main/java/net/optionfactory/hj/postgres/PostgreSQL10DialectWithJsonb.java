package net.optionfactory.hj.postgres;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

public class PostgreSQL10DialectWithJsonb extends PostgreSQL10Dialect {

    public PostgreSQL10DialectWithJsonb() {
        registerColumnType(Types.OTHER, "jsonb");
    }

}
