package net.optionfactory.hj.postgres;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

public class PostgreSQL95DialectWithJsonb extends PostgreSQL95Dialect {

    public PostgreSQL95DialectWithJsonb() {
        registerColumnType(Types.OTHER, "jsonb");
    }

}
