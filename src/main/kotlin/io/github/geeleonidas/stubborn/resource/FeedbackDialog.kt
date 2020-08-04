package io.github.geeleonidas.stubborn.resource

import com.google.gson.JsonObject
import io.github.geeleonidas.stubborn.Bimoe
import io.github.geeleonidas.stubborn.Stubborn
import net.minecraft.text.TranslatableText

class FeedbackDialog(id: String, entry: TranslatableText):
    NodeDialog(
        id, listOf(entry), emptyList(),
        emptyList(), emptyMap(), emptyMap()
    ) {
    companion object {
        fun fromJsonOrNull(jsonObject: JsonObject, bimoe: Bimoe): FeedbackDialog? {
            val hasInclude = jsonObject.has("include")
            val hasExclude = jsonObject.has("exclude")
            if (hasInclude || hasExclude) {
                val jsonArray =
                    if (hasInclude)
                        jsonObject["include"].asJsonArray
                    else
                        jsonObject["exclude"].asJsonArray
                var hasElement = hasExclude // false when using "include", true when using "exclude"
                for (i in 0 until jsonArray.size())
                    if (jsonArray[i].asString == bimoe.name) {
                        hasElement = !hasElement
                        break
                    }
                if (!hasElement)
                    return null
            }

            val id = "~${jsonObject["id"].asString}"
            val entry = TranslatableText("dialog.${Stubborn.modId}.${bimoe.lowerCasedName}.$id")

            return FeedbackDialog(id, entry)
        }
    }
}