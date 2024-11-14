// Load Our App Server Using Express
const express = require('express');
const app = express();
const morgan = require('morgan');
const body_parser = require('body-parser');
const checkType = require('./middleware/check-type');
const server_port = 5100;

// Body Parser - Extracts JSON Data
app.use(body_parser.urlencoded({extended: false}));
app.use(body_parser.json());

// Now serving everything in the public directory.
app.use(express.static('./public'));

// app.use(morgan('combined'));
app.use(morgan('short'));

// Base Directory
app.get("/", (request, response, next) => {
    console.log("Responding to Route");
    response.send("Hello from beanbeanjuice!");
});

// Preventing CORS Errors
app.use((request, response, next) => {
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

const cafe_api_url = "/cafe/api/v1"
const greeting_router = require('./routes/cafe/greeting.js');
const cafe_user_router = require('./routes/cafe/user.js');
app.use(cafe_api_url, cafe_user_router);
app.use(cafe_api_url, greeting_router);

const cafe_bot_api_url = "/cafeBot/api/v1";
const beanCoins_router = require('./routes/cafeBot/beanCoins.js');
const birthdays_router = require('./routes/cafeBot/birthdays.js');
const cafe_router = require('./routes/cafeBot/cafe.js');
const cafeBot_router = require('./routes/cafeBot/cafeBot.js');
const codes_router = require('./routes/cafeBot/codes.js');
const counting_router = require('./routes/cafeBot/counting.js');
const guild_twitch_router = require('./routes/cafeBot/guild_twitch.js');
const guilds_router = require('./routes/cafeBot/guilds.js');
const interactions_router = require('./routes/cafeBot/interactions.js');
const minigames_router = require('./routes/cafeBot/minigames.js');
const mutual_guilds_router = require('./routes/cafeBot/mutualGuilds.js');
const polls_router = require('./routes/cafeBot/polls.js');
const raffles_router = require('./routes/cafeBot/raffles.js');
const voice_binds_router = require('./routes/cafeBot/voice_binds.js');
const welcomes_router = require('./routes/cafeBot/welcomes.js');
const goodbyes_router = require('./routes/cafeBot/goodbyes.js');
const words_router = require('./routes/cafeBot/words.js');
const guild_ai_threads_router = require('./routes/cafeBot/guild_ai_threads.js');
app.use(cafe_bot_api_url, beanCoins_router);
app.use(cafe_bot_api_url, birthdays_router);
app.use(cafe_bot_api_url, cafe_router);
app.use(cafe_bot_api_url, cafeBot_router);
app.use(cafe_bot_api_url, codes_router);
app.use(cafe_bot_api_url, counting_router);
app.use(cafe_bot_api_url, guild_twitch_router);
app.use(cafe_bot_api_url, guilds_router);
app.use(cafe_bot_api_url, interactions_router);
app.use(cafe_bot_api_url, minigames_router);
app.use(cafe_bot_api_url, mutual_guilds_router);
app.use(cafe_bot_api_url, polls_router);
app.use(cafe_bot_api_url, raffles_router);
app.use(cafe_bot_api_url, voice_binds_router);
app.use(cafe_bot_api_url, welcomes_router);
app.use(cafe_bot_api_url, goodbyes_router);
app.use(cafe_bot_api_url, words_router);
app.use(cafe_bot_api_url, guild_ai_threads_router);

const interaction_pictures_router = require('./routes/cafeBot/interaction_pictures/interaction_pictures.js');
app.use(cafe_bot_api_url, interaction_pictures_router);

// Catches Errors that Have No Routes
app.use((request, response, next) => {
    console.log("There has been an error. Route not found.");
    const error = new Error("Route Not Found");
    error.status = 404;
    next(error);
});

// Catches ALL Errors
app.use((error, request, response, next) => {
    response.status(error.status || 500);
    response.json({
        message: error.message
    });
});

// Ping @ localhost:5101
app.listen(server_port, () => {
    console.log(`Server is up on ${checkType.RELEASE_TYPE} and listening on port: ${server_port}.`);
});
