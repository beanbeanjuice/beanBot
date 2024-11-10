import express, { Request, Response, NextFunction } from "express";
import morgan from "morgan";
import bodyParser from "body-parser";
import { Checks } from "./middleware/Type";
import CustomError from "./interfaces/CustomError";

// Load app.
const app = express();

// Body Parser - Extracts JSON Data
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

// Now serving everything in the public directory.
app.use(express.static('./public'));

app.use(morgan('short'));

// Base Directory
app.get("/", (req : Request, res : Response, next : NextFunction) => {
    console.log("Responding to Route");
    res.status(200).send({message : "Hello from beanbeanjuice!"})
});

// Preventing CORS Errors
app.use((request : any, response : any, next : any) => {
    response.header('Access-Control-Allow-Origin', '*');
    response.header('Access-Control-Allow-Headers',
        "Origin, X-Requested-With, Content-Type, Accept, Authorization"
    );

    if (request.method === 'OPTIONS') {
        response.header('Access-Control-Allow-Methods', 'PUT, POST, PATCH, DELETE, GET');
        return response.status(200).json({});
    }
    next();
});

const cafeAPIURL = "/cafe/api/v2"
import greetingRouter from "./routes/cafe/Greeting";
import userRouter from "./routes/cafe/User";
app.use(cafeAPIURL, greetingRouter);
app.use(cafeAPIURL, userRouter);

const cafeBotAPIURL = "/cafeBot/api/v2";
import botInformationRouter from "./routes/cafebot/BotInformation";
import beanCoinDonationsRouter from "./routes/cafebot/BeanCoinDonations";
import birthdaysRouter from "./routes/cafebot/Birthdays";
app.use(cafeBotAPIURL, botInformationRouter);
app.use(cafeBotAPIURL, beanCoinDonationsRouter);
app.use(cafeBotAPIURL, birthdaysRouter);
// const birthdays_router = require('./routes/cafeBot/birthdays.js');
// const cafe_router = require('./routes/cafeBot/cafe.js');
// const codes_router = require('./routes/cafeBot/codes.js');
// const counting_router = require('./routes/cafeBot/counting.js');
// const guild_twitch_router = require('./routes/cafeBot/guild_twitch.js');
// const guilds_router = require('./routes/cafeBot/guilds.js');
// const interactions_router = require('./routes/cafeBot/interactions.js');
// const minigames_router = require('./routes/cafeBot/minigames.js');
// const mutual_guilds_router = require('./routes/cafeBot/mutualGuilds.js');
// const polls_router = require('./routes/cafeBot/polls.js');
// const raffles_router = require('./routes/cafeBot/raffles.js');
// const voice_binds_router = require('./routes/cafeBot/voice_binds.js');
// const welcomes_router = require('./routes/cafeBot/welcomes.js');
// const goodbyes_router = require('./routes/cafeBot/goodbyes.js');
// const words_router = require('./routes/cafeBot/words.js');
// const guild_ai_threads_router = require('./routes/cafeBot/guild_ai_threads.js');
// app.use(cafe_bot_api_url, birthdays_router);
// app.use(cafe_bot_api_url, cafe_router);
// app.use(cafe_bot_api_url, codes_router);
// app.use(cafe_bot_api_url, counting_router);
// app.use(cafe_bot_api_url, guild_twitch_router);
// app.use(cafe_bot_api_url, guilds_router);
// app.use(cafe_bot_api_url, interactions_router);
// app.use(cafe_bot_api_url, minigames_router);
// app.use(cafe_bot_api_url, mutual_guilds_router);
// app.use(cafe_bot_api_url, polls_router);
// app.use(cafe_bot_api_url, raffles_router);
// app.use(cafe_bot_api_url, voice_binds_router);
// app.use(cafe_bot_api_url, welcomes_router);
// app.use(cafe_bot_api_url, goodbyes_router);
// app.use(cafe_bot_api_url, words_router);
// app.use(cafe_bot_api_url, guild_ai_threads_router);
//
// const interaction_pictures_router = require('./routes/cafeBot/interaction_pictures/interaction_pictures.js');
// app.use(cafe_bot_api_url, interaction_pictures_router);

// Catches Errors that Have No Routes
app.use((request : any, response : any, next : any) => {
    console.log("There has been an error. Route not found.");
    response.status(404).json({message: "Route not found."})
});

// Catches ALL Errors
app.use((error : CustomError, request : Request, response : Response, next : NextFunction) => {
    console.log(error);
    response.status(error.status || 500).json({message: error.message});
});

// Ping @ localhost:5100
app.listen(5100, () => {
    console.log("Server is up on " + Checks.LOCATION + " and listening on port: 5100");
});
