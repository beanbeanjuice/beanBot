const express = require('express');
const router = express.Router();

const getConnection = require('./modules/mysql-connection');
const check_authentication = require('../../middleware/check-auth');
const check_admin = require('../../middleware/check-admin.js');

const check_if_user_exists = (request, response, next) => {
    user_id = request.params.user_id;

    query = "SELECT * FROM birthdays WHERE user_id = (?);";
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
    user_id = request.params.user_id;

    query = "SELECT * FROM birthdays WHERE user_id = (?);";
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
    query = "SELECT * FROM birthdays;";
    connection = getConnection();
    connection.query(query, (error, rows, fields) => {
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
    user_id = request.params.user_id;

    query = "SELECT * FROM birthdays WHERE user_id = (?);";
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
    user_id = request.params.user_id;
    birth_date = request.query.birth_date;
    time_zone = request.query.time_zone;

    // TODO: Somehow add time_zone as well to this.
    if (!birth_date) {
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
    query = "UPDATE birthdays SET birth_date = (?), time_zone = (?) WHERE user_id = (?);";
    getConnection().query(query, [birthday, time_zone, user_id], (error, rows, fields) => {
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

// Updates the mention status of a user.
router.patch("/birthdays/:user_id/mention", check_authentication, check_admin, check_if_user_exists, (request, response, next) => {
    user_id = request.params.user_id;
    already_mentioned = request.query.already_mentioned;

    if (!already_mentioned) {
        response.status(400).json({
            variables: {
                user_id: user_id,
                already_mentioned: already_mentioned || "undefined"
            },
            message: "A Variable is Undefiend"
        });
        return;
    }

    if (already_mentioned == "true") {
        already_mentioned = 1;
    } else if (already_mentioned == "false") {
        already_mentioned = 0;
    } else {

        if (already_mentioned != 0 && already_mentioned != 1) {
            response.status(500).json({
                message: "Only true or false is Available"
            });
            return;
        }
    }

    query = "UPDATE birthdays SET already_mentioned = (?) WHERE user_id = (?);";
    getConnection().query(query, [already_mentioned, user_id], (error, rows, fields) => {
        if (error) {
            next(new Error(error));
            return;
        }

        response.status(200).json({
            message: "Updated the Already Mentioned Status for User (" + user_id + ")"
        });
        return;
    });
});

// TODO: Test this.
// Creates a birthday
router.post("/birthdays/:user_id", check_authentication, check_admin, check_if_user_does_not_exist, (request, response, next) => {
    const user_id = request.params.user_id;
    const birthday = request.query.birthday;
    const time_zone = request.query.time_zone;

    if (!birthday || !time_zone) {
        response.status(400).json({
            variables: {
                user_id: user_id,
                birthday: birthday || "undefined",
                time_zone: time_zone || "undefined"
            },
            message: "A Variable is Undefined"
        });
        return;
    }

    query = "INSERT INTO birthdays (user_id, birth_date, time_zone) VALUES (?,?,?);";
    getConnection().query(query, [user_id, birthday, time_zone], (error, rows, fields) => {
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
    user_id = request.params.user_id;

    query = "DELETE FROM birthdays WHERE user_id = (?);";
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
