import express, {Response, NextFunction} from "express";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import { Checks } from "../../middleware/Type";
import connection from "../../middleware/MySQLCafeConnection";
import {ResultSetHeader, RowDataPacket} from "mysql2";
import JWTInfo from "../../interfaces/JWTInfo";

const router = express.Router();

import checkAuthentication from "../../middleware/Authentication";
import checkAdministration from "../../middleware/Administration";
import ValidatedRequest from "../../interfaces/ValidatedRequest";
import CustomError, {AuthorizationError} from "../../interfaces/CustomError";

// Gets a list of all users.
router.get("/users", checkAuthentication, checkAdministration, (request : ValidatedRequest, response : Response, next : NextFunction) => {
    const query = "SELECT * FROM users;";
    connection.query(query, (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({
            message: "Successfully retrieved users.",
            users: rows
        });
    });
});

// Login to an account
router.post("/user/login", (request, response, next) => {
    const username = request.query.username;
    const password = request.query.password;

    if (!username) {
        next(new CustomError("Username is missing from the parameters.", 400));
        return;
    }

    if (!password) {
        next(new CustomError("Password is missing from the parameters.", 400));
        return;
    }

    const check_query = "SELECT * FROM users WHERE username = (?);";
    connection.query(check_query, [username], (error, rows : RowDataPacket[]) => {
        if (error) {
            next(error);
            return;
        }

        // Checking If Users Exist
        if (rows.length < 1) {
            next(AuthorizationError);
            return;
        }

        const hashed_password = rows[0].password;

        bcrypt.compare(password.toString(), hashed_password, (error, result) => {
            if (error) {
                next(AuthorizationError);
                return;
            }

            if (result) {
                const signingID : JWTInfo = {
                    user_id: rows[0].user_id,
                    username: rows[0].username,
                    user_type: rows[0].user_type
                }

                const access_token = jwt.sign(
                    signingID,
                    Checks.TOKEN, // TOKEN
                    {
                        expiresIn: "1h"
                    });

                return response.status(200).json({
                    message: "Successfully authenticated.",
                    access_token: access_token,
                    expires_in: "3600"
                });
            }

            next(AuthorizationError);
        });
    });
});

// Creates a user.
router.post('/user/signup', (request, response, next) => {
    const username = request.query.username;
    const plaintextPassword = request.query.password;

    if (!username) {
        next(new CustomError("Username is missing from the parameters.", 400));
        return;
    }

    if (!plaintextPassword) {
        next(new CustomError("Password is missing from the parameters.", 400));
        return;
    }

    // HASHING the password
    bcrypt.hash(plaintextPassword.toString(), 10, (error, hashed_password) => {
        if (error) {
            next(error);
            return;
        }

        const query = "INSERT INTO users (username, password) VALUES (?,?);";
        connection.query(query, [username, hashed_password], (error, rows : ResultSetHeader) => {
            if (error) {
                if (error.code === "ER_DUP_ENTRY") {
                    next(AuthorizationError);
                    return;
                }

                next(error);
                return;
            }

            response.status(201).json({ message: `Successfully created user (${rows.insertId}).` })
        });

    });
});

// Gets a specified user.
router.get("/user/:username", checkAuthentication, checkAdministration, (request, response, next) => {
    const username = request.params.username;

    const query = "SELECT * FROM users WHERE username = (?);";
    connection.query(query, [username], (error, rows : RowDataPacket[], fields) => {
        if (error) {
            next(error);
            return;
        }

        if (rows.length == 0) {
            next(new CustomError("User does not exist.", 404));
            return;
        }

        response.status(200).json({
            message: "Successfully retrieved user.",
            user: rows[0]
        });
    });
});

// Deleting User
router.delete("/user/:username", checkAuthentication, checkAdministration, (request, response, next) => {
    const username = request.params.username;

    const query = "DELETE FROM users WHERE username = (?);";
    connection.query(query, [username], (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({ message: `Successfully deleted user (${username}).` });
    });
});

export default router;
