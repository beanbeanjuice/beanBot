import express from "express";
import connection from "../../middleware/MySQLCafeBotConnection";
import {ResultSetHeader, RowDataPacket} from "mysql2";
import checkAuthentication from "../../middleware/Authentication";
import checkAdministration from "../../middleware/Administration";
import CustomError from "../../interfaces/CustomError";

const router = express.Router();

// Gets beanCoin donation users.
router.get("/beanCoin/donation_users", checkAuthentication, checkAdministration, (request, response, next) => {
    const query = "SELECT * FROM beancoin_donation_users;";
    connection.query(query, (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({
            message: "Successfully retrieved a list of the active donation users.",
            users: rows
        });
    });
});

// Gets a specific user's time until next donation.
router.get("/beanCoin/donation_users/:user_id", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "SELECT time_until_next_donation FROM beancoin_donation_users WHERE user_id = (?);";
    connection.query(query, [user_id], (error, rows : RowDataPacket[], fields) => {
        if (error) {
            next(error);
            return;
        }

        if (!rows) {
            next(new CustomError(`${user_id} does not exist.`, 404));
            return;
        }

        if (!rows[0]) {
            next(new CustomError("The user does not have an active donation time.", 404));
            return;
        }

        response.status(200).json({
            message: `Successfully retrieved time until next donation for user (${user_id}).`,
            time_until_next_donation: rows[0]
        });
    });
});

// Add a new donation user
router.post("/beanCoin/donation_users/:user_id", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;
    const time_stamp = request.query.time_stamp;

    if (!time_stamp) {
        next(new CustomError("Missing timestamp from parameters.", 400));
        return;
    }

    const query = "INSERT INTO beancoin_donation_users (user_id, time_until_next_donation) VALUES (?,?);";
    connection.query(query, [user_id, time_stamp], (error, rows, fields) => {
        if (error) {
            if (error.code === "ER_DUP_ENTRY") {
                next(new CustomError(`User (${user_id}) already exists.`, 409));
                return;
            }
            next(error);
            return;
        }

        response.status(201).json({message: `Successfully created user (${user_id}).`});
    });

});

// Deletes a beanCoin donation user
router.delete("/beanCoin/donation_users/:user_id", checkAuthentication, checkAdministration, (request, response, next) => {
    const user_id = request.params.user_id;

    const query = "DELETE FROM beancoin_donation_users WHERE user_id = (?);";
    connection.query(query, [user_id], (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({message: "Successfully Removed User (" + user_id + ")"});
    });

});

export default router;
