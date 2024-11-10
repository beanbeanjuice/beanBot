const REQUEST_LOCATION = process.env.API_REQUEST_LOCATION;

if (REQUEST_LOCATION === undefined) {
    throw new Error('RELEASE_TYPE should be `BETA` or `RELEASE`.');
}

const JWT_TOKEN = process.env.JWT_TOKEN;
if (!JWT_TOKEN) {
    throw new Error("Missing JWT Token");
}

interface MySQLData {
    url: string;
    port: number;
    username: string;
    password: string;
}

const mysqlURL = process.env.MYSQL_URL;
const mysqlPort = Number(process.env.MYSQL_PORT);
const mysqlUsername= process.env.MYSQL_USER;
const mysqlPassword = process.env.MYSQL_PASSWORD;

if (!mysqlURL) {
    throw new Error('Missing MYSQL URL.');
}

if (!mysqlPort || isNaN(mysqlPort)) {
    throw new Error('Missing MYSQL Port.');
}

if (!mysqlUsername) {
    throw new Error('Missing MYSQL Username.');
}

if (!mysqlPassword) {
    throw new Error('Missing MYSQL Password.');
}

const MySQLData : MySQLData = {
    url: mysqlURL,
    port: mysqlPort,
    username: mysqlUsername,
    password: mysqlPassword
}

interface IChecks {
    TOKEN: string;
    LOCATION: string;
    MYSQL: MySQLData;
}

export const Checks : IChecks = {
    TOKEN: JWT_TOKEN,
    LOCATION: REQUEST_LOCATION,
    MYSQL: MySQLData
}
