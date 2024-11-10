import mysql, { PoolOptions } from "mysql2";
import { Checks } from "./Type";

const mysql_database = "cafeBot";

// Creates a MySQL pool. Allows multiple connections.
const pool : PoolOptions = {
    connectionLimit: 200,
    host: Checks.MYSQL.url,
    port: Checks.MYSQL.port,
    user: Checks.MYSQL.username,
    password: Checks.MYSQL.password,
    database: mysql_database,
    supportBigNumbers: true // BigInt not rounded now
};

export default mysql.createPool(pool);
