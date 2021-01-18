package me.mouamle.bot.pdf.loader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BotData {

    BotType type;
    String token;
    String username;
    String responseMessage;

}
