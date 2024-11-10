import JWTInfo from "./JWTInfo";
import { Request } from "express";

export default interface ValidatedRequest extends Request {
    user_data?: JWTInfo;
}
