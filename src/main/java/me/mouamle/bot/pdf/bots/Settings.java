package me.mouamle.bot.pdf.bots;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.mouamle.bot.pdf.loader.BotData;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @SerializedName("_version")
    private int version;

    @SerializedName("external_url")
    private String externalUrl;

    @SerializedName("internal_url")
    private String internalUrl;

    @SerializedName("bots")
    private List<BotData> bots;

}
