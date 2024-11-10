import jwt from 'jsonwebtoken';
import { Checks } from "./Type";
import ValidatedRequest from "../interfaces/ValidatedRequest";
import { Response, NextFunction} from "express";

let checkAuthentication = (request : ValidatedRequest, response : Response, next : NextFunction) => {
    console.log("Checking authentication.");
    try {
        if (!request.headers.authorization) {
            console.log("No authorization header.");
            response.status(401).json({
                message: "Authentication Failed"
            });
            return;
        }

        const token = request.headers.authorization.substring(7);
        const decoded_token = jwt.verify(token, Checks.TOKEN);
        request.user_data = JSON.parse(JSON.stringify(decoded_token));
        next(); // Called if successfully authenticated.
    } catch (error) {
        console.log(error);
        response.status(401).json({
            message: "Authentication Failed"
        });
        return;
    }
};

export default checkAuthentication;

