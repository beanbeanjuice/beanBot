const express = require('express');
const router = express.Router();

const getConnection = require('./modules/mysql-connection');
const check_authentication = require('../../middleware/check-auth');
const check_admin = require('../../middleware/check-admin.js');

const check_if_user_exists = (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "SELECT * FROM birthdays WHERE user_id = (?);";
    getConnection().query(query, [user_id], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        if (!rows.length) {
            const user_error = new Error("User (" + user_id + ") Does Not Exist");
            user_error.status = 404;
            next(user_error);
            return;
        }

        next();
    });
}

const check_if_user_does_not_exist = (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "SELECT * FROM birthdays WHERE user_id = (?);";
    getConnection().query(query, [user_id], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        if (!!rows.length) {
            const user_error = new Error("User (" + user_id + ") Already Exists");
            user_error.status = 409;
            next(user_error);
            return;
        }

        next();
    });
}

// Gets all birthdays.
router.get("/birthdays", check_authentication, check_admin, (request, response, next) => {
    const query = "SELECT * FROM birthdays;";
    getConnection().query(query, (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        response.status(200).json({
            birthdays: rows,
            message: "Successfully Retrieved List of Birthdays"
        });
        return;
    });
});

// Gets a user's specific birthday.
router.get("/birthdays/:user_id", check_authentication, check_admin, check_if_user_exists, (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "SELECT * FROM birthdays WHERE user_id = (?);";
    getConnection().query(query, [user_id], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        response.status(200).json({
            birthday: rows[0],
            message: "Successfully Retrieved Birthday Information for User (" + user_id + ")"
        });
        return;
    });
});

// Updates a user's birthday.
router.patch("/birthdays/:user_id", check_authentication, check_admin, check_if_user_exists, (request, response, next) => {
    const user_id = request.params.user_id;
    const birth_date = request.query.birth_date;
    const time_zone = request.query.time_zone;

    if (!birth_date || !time_zone) {
        response.status(400).json({
            variables: {
                user_id: user_id,
                birth_date: birth_date || "undefined",
                time_zone: time_zone || "undefined"
            },
            message: "A Variable is Undefined"
        });
        return;
    }

    // TODO: Test this
    const query = "UPDATE birthdays SET birth_date = (?), time_zone = (?) WHERE user_id = (?);";
    getConnection().query(query, [birth_date, time_zone, user_id], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        response.status(200).json({
            message: "Updated Birthday for User (" + user_id + ")"
        });
        return;
    });
});

// TODO: Test this.
// Creates a birthday
router.post("/birthdays/:user_id", check_authentication, check_admin, check_if_user_does_not_exist, (request, response, next) => {
    const user_id = request.params.user_id;
    const birth_date = request.query.birth_date;
    const time_zone = request.query.time_zone;

    if (!birth_date || !time_zone) {
        response.status(400).json({
            variables: {
                user_id: user_id,
                birth_date: birth_date || "undefined",
                time_zone: time_zone || "undefined"
            },
            message: "A Variable is Undefined"
        });
        return;
    }

    const query = "INSERT INTO birthdays (user_id, birth_date, time_zone) VALUES (?,?,?);";
    getConnection().query(query, [user_id, birth_date, time_zone], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        response.status(201).json({
            message: "Successfully Created a Birthday for User (" + user_id + ")"
        });
        return;
    });
});

// Deletes a user's birthday.
router.delete("/birthdays/:user_id", check_authentication, check_admin, (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "DELETE FROM birthdays WHERE user_id = (?);";
    getConnection().query(query, [user_id], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        response.status(200).json({
            message: "Successfully Deleted Birthday for User (" + user_id + ")"
        });
        return;
    });
});

module.exports = router;
