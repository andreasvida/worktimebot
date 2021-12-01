Work time bot
-------------

A simple bot for Telegram written in Java, that allows its users to clock in, clock out and review their time balance.

The bot features
- translations (English, Russian, German)
- configureable working hours per week day
- definition of holidays
- file export
- arbitrary comments with time stamp e.g. current work items tickets / diary functionality
- simple persistence
- calculation of current time balance including clock in without clock out.


Usage
-----

Prerequisites

You need a Telegram Client + Java and Maven to compile and run the bot:

1) Download source of Work time bot.
2) Build all in one jar file:
    mvn clean install
3) Search for "BotFather" in Telegram, tell it to create a new bot and remember its BOT-TOKEN for Telegram API calls
4) java -jar timerclockbot-1.0.0.jar BOT-TOKEN

  -> Your bot is up and running and is ready to manage your working time in Telegram.
