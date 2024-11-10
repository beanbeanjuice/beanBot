import express from "express";
import connection from "../../middleware/MySQLCafeBotConnection";
import {ResultSetHeader, RowDataPacket} from "mysql2";
import checkAuthentication from "../../middleware/Authentication";
import checkAdministration from "../../middleware/Administration";
import CustomError from "../../interfaces/CustomError";

const router = express.Router();

// Gets all birthdays.
router.get("/birthdays", checkAuthentication, checkAdministration, (request, response, next) => {
    const query = "SELECT * FROM birthdays;";
    connection.query(query, (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({
            birthdays: rows,
            message: "Successfully Retrieved List of Birthdays"
        });
        return;
    });
});

// Gets a specific user's birthday.
router.get("/birthdays/:user_id", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "SELECT * FROM birthdays WHERE user_id = (?);";
    connection.query(query, [user_id], (error, rows : RowDataPacket[], fields) => {
        if (error) {
            next(error);
            return;
        }

        if (rows.length == 0) {
            next(new CustomError("User does not exist.", 404));
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
router.patch("/birthdays/:user_id", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;
    const birthday = request.query.birth_date;
    const timezone = request.query.time_zone;

    if (!birthday) {
        next(new CustomError("Missing birthday (MM-DD) from parameters.", 400));
        return;
    }

    const dateRegex = /^\d{1,2}-\d{1,2}$/;
    if (!dateRegex.test(birthday.toString())) {
        next(new CustomError("Birthday is in the incorrect format. (MM-DD)", 400));
        return;
    }

    if (!timezone) {
        next(new CustomError("Missing timezone from parameters.", 400));
        return;
    }

    const query = "UPDATE birthdays SET birth_date = (?), time_zone = (?) WHERE user_id = (?);";
    connection.query(query, [birthday, timezone, user_id], (error, rows : ResultSetHeader, fields) => {
        if (error) {
            next(error);
            return;
        }

        if (rows.affectedRows == 0) {
            next(new CustomError("That user does not exist.", 404));
            return;
        }

        response.status(200).json({
            message: "Updated Birthday for User (" + user_id + ")"
        });
        return;
    });
});

// Updates the mention status of a user.
router.patch("/birthdays/:user_id/mention", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;
    const already_mentioned = request.query.already_mentioned;

    if (!already_mentioned) {
        next(new CustomError("Missing the already mentioned state of the user.", 400));
        return;
    }

    const mentionStatus = already_mentioned === "true";

    const query = "UPDATE birthdays SET already_mentioned = (?) WHERE user_id = (?);";
    connection.query(query, [mentionStatus, user_id], (error, rows : ResultSetHeader, fields) => {
        if (error) {
            next(error);
            return;
        }

        if (rows.affectedRows == 0) {
            next(new CustomError("That user does not exist.", 404));
            return;
        }

        response.status(200).json({message: "Updated the Mentioned Status for User (" + user_id + ")"});
        return;
    });
});

// Creates a birthday
router.post("/birthdays/:user_id", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;
    const birthday = request.query.birth_date;
    const timezone = request.query.time_zone;

    if (!birthday) {
        next(new CustomError("Missing birthday (MM-DD) from parameters.", 400));
        return;
    }

    const dateRegex = /^\d{1,2}-\d{1,2}$/;
    if (!dateRegex.test(birthday.toString())) {
        next(new CustomError("Birthday is in the incorrect format. (MM-DD)", 400));
        return;
    }

    if (!timezone) {
        next(new CustomError("Missing timezone from parameters.", 400));
        return;
    }

    const query = "INSERT INTO birthdays (user_id, birth_date, time_zone) VALUES (?,?,?);";
    connection.query(query, [user_id, birthday, timezone], (error, rows, fields) => {
        if (error) {

            if (error.code === "ER_DUP_ENTRY") {
                next(new CustomError("Birthday already exists for that user.", 409));
                return;
            }

            next(error);
            return;
        }

        response.status(201).json({
            message: "Successfully Created a Birthday for User (" + user_id + ")"
        });
        return;
    });
});

// Deletes a user's birthday.
router.delete("/birthdays/:user_id", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "DELETE FROM birthdays WHERE user_id = (?);";
    connection.query(query, [user_id], (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({message: "Successfully Deleted Birthday for User (" + user_id + ")"});
        return;
    });
});

export default router;
