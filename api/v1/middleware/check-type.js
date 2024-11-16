const RELEASE_TYPE = process.env.API_REQUEST_LOCATION;

if (RELEASE_TYPE === undefined) {
  throw new Error('RELEASE_TYPE should be `BETA` or `RELEASE`.');
}

const TOKEN = process.env.JWT_TOKEN;

function getMySQLData() {
  return {
    "url": process.env.MYSQL_URL,
    "port": process.env.MYSQL_PORT || 3306,
    "user": process.env.MYSQL_USER,
    "password": process.env.MYSQL_PASSWORD
  }
}

module.exports = {
  getMySQLData,
  RELEASE_TYPE,
  TOKEN
}
