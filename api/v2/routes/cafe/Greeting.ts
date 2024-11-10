import express from "express";
const router = express.Router();

router.get("/", (request, response, next) => {
    response.status(200).json({
        message: "Hello, world!"
    });
});

export default router;
