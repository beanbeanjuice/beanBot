import ValidatedRequest from "../interfaces/ValidatedRequest";
import {Response, NextFunction} from "express";

let checkAuthentication = (request : ValidatedRequest, response : Response, next : NextFunction) => {
    console.log("Checking admin status.");
    try {
        if (!request.user_data) {
            response.status(401).json({
                message: "Authentication Failed"
            });
            return;
        }
        const user_type = request.user_data.user_type;

        if (user_type === 'ADMIN') {
            next();
        } else {
            response.status(401).json({
                message: "Authentication Failed"
            });
        }
    } catch (error) {
        response.status(401).json({
            message: "Authentication Failed"
        });
        return;
    }
};

export default checkAuthentication;
