package net.optionfactory.hj.postgres;

import java.sql.Types;
import org.hibernate.dialect.PostgreSQL94Dialect;

public class PostgreSQL94DialectWithJsonb extends PostgreSQL94Dialect {

    public PostgreSQL94DialectWithJsonb() {
        registerColumnType(Types.OTHER, "jsonb");
    }

}
