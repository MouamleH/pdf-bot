# pdf-bot
A Telegram bot to group images in a single pdf document.
> The code is messy and not in any way, shape or form, clean.

## Building Your Own Bot

If you don't want to build your own, you will find a running bot in [this channel](https://t.me/s/SwiperTeam)

Otherwise, you need an SSL enabled hosting to get started.

### Downloading
Clone this repo and compile the code using. 
```sh
mvn clean compile assembly:single
```
Or download the `.jar` file from the [release section](https://github.com/MouamleH/pdf-bot/releases/tag/1.0.1)

### Running

Run the app using this command

```sh
java -jar <file-name>.jar
```

At first an error message will show 
stating that you need to fill out some information about your bot in `settings.json`.

The settings file looks like this and will be generated if the app didn't find one.
```json5
{
  "external_url": "", // the external webhook url
  "internal_url": "", // the internal webhook redirect (eg. localhost:PORT)
  "bots": [
    {
      "username": "",
      "token": ""
    }
  ]
}
```

After filling out your bot/s info you need to start the app again, and the bot will start working.
