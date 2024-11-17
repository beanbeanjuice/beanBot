const express = require('express');
const router = express.Router();

const getConnection = require('./modules/mysql-connection');
const check_authentication = require('../../middleware/check-auth');
const check_admin = require('../../middleware/check-admin.js');

// Gets all mutual guilds for a user.
router.get("/mutual_guilds/:user_id", check_authentication, check_admin, (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "SELECT guild_id FROM mutual_guilds WHERE user_id = (?);";
    getConnection().query(query, [user_id], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        response.status(200).json({
            mutual_guilds: rows.map(guild => guild.guild_id),
            message: "Successfully Retrieved Mutual Guilds"
        });
        return;
    });
});

// Inserts a new mutual guild.
router.post("/mutual_guilds/:user_id", check_authentication, check_admin, (request, response, next) => {
    const user_id = request.params.user_id;
    const guild_id = request.query.guild_id;

    const query = "INSERT IGNORE INTO mutual_guilds (user_id, guild_id) VALUES (?, ?);";
    getConnection().query(query, [user_id, guild_id], (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(201).json({
            message: "Mutual Guild Added"
        });
        return;
    });
});

// Delete a mutual guild.
router.delete("/mutual_guilds/:user_id", check_authentication, check_admin, (request, response, next) => {
    const user_id = request.params.user_id;
    const guild_id = request.query.guild_id;

    query = "DELETE FROM mutual_guilds WHERE user_id = (?) AND guild_id = (?);";
    getConnection().query(query, [user_id, guild_id], (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({
            message: "Mutual Guild Removed"
        });
        return;
    });
});

module.exports = router;
