import express from "express";
import connection from "../../middleware/MySQLCafeBotConnection";
import { RowDataPacket } from "mysql2";

const router = express.Router();

import checkAuthentication from "../../middleware/Authentication";
import checkAdministration from "../../middleware/Administration";
import CustomError from "../../interfaces/CustomError";

// Gets the current bot information.
router.get("/cafeBot", (request, response, next) => {
    const query = "SELECT * FROM bot_information;";
    connection.query(query, (error, rows : RowDataPacket[], fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({
            bot_information: {
                "version": rows[0].version
            },
            message: "Successfully Retrieved the Current Bot Information"
        });
        return;
    });
});

// Updates the cafeBog Version
router.patch("/cafeBot", checkAuthentication, checkAdministration, (request, response, next) => {
    const newVersion = request.query.version;

    if (!newVersion) {
        next(new CustomError("Version number is missing from the parameters.", 400));
        return;
    }

    const query = "UPDATE bot_information SET version = (?) WHERE id = 1;";
    connection.query(query, [newVersion], (error, rows, fields) => {
        if (error) {
            next(error);
            return;
        }

        response.status(200).json({message: `The bot version has been updated to ${newVersion}.`});
        return;
    });
});

export default router;
