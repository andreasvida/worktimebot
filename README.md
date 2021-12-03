Work time bot
-------------

A simple bot for Telegram written in Java, that allows its users to clock in, clock out and review their time balance.

The bot features
- calculation of time balance
- translations (English, Russian, German, Japanese)
- easy to use interface (inline buttons for clock in / clock out)
- configurable working hours per week day
- definition of arbitrary holidays
- file export
- arbitrary comments with time stamp e.g. current work items tickets / diary functionality
- simple server side persistence
- on demand deletion of user data


Try it out
----------

An up-to-date installation is running here: [Open link with telegram client](http://t.me/worktimerecorderbot)

Some screenshots of the welcome screen, language selection and example actions:

![Screenshot 1](docs/1.jpg?raw=true "Screen 1")

![Screenshot 2](docs/2.jpg?raw=true "Screen 2")


Building and using your own bot
-------------------------------

Prerequisites

You need a Telegram Client + Java and Maven to compile and run the bot:

1) Download source of Work time bot.
2) Build all in one jar file:
``` 
   mvn clean install
```
3) Search for "BotFather" in Telegram, tell it to create a new bot and remember its BOT-TOKEN for Telegram API calls
4) Start the program:
```
java -jar timerclockbot-1.0.0.jar BOT-TOKEN
```

  -> Your bot is up and running and is ready to manage your working time in Telegram.
