package org.jetbrains.research.tasktracker.config.survey

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Radio button id and value
 */
@Serializable
data class ValueInfo(val id: String, val value: String)

/**
 * This is a class that constructs an input field of a specified type in HTML.
 */
@Serializable
sealed class HtmlQuestion {
    /**
     * `h` tag text
     */
    abstract val text: String
    abstract val elementId: String

    /**
     * `h` tag size. For example, h1, h2, and so on.
     */
    private val textSize: Int = DEFAULT_TEXT_SIZE
        get() = if (field < MIN_TEXT_SIZE || field > MAX_TEXT_SIZE) DEFAULT_TEXT_SIZE else field

    /**
     * Sets 'required' on the input if this field is mandatory to fill out.
     */
    @SerialName("required")
    protected val isRequired: Boolean = false
    internal abstract fun toHtml(): String

    val html: String
        get() = buildString {
            append("<div class=\"box\" id=\"${elementId}___container\">")
            append(toHtml())
            append("</div>")
        }

    @Transient
    private var alreadyRequired = false

    protected fun Any?.asParameter(name: String): String = this?.let { "$name=\"$it\"" } ?: ""
    protected open fun isRequiredString(): String =
        if (isRequired && !alreadyRequired) "required".also { alreadyRequired = true } else ""

    protected fun htmlText() = "<h$textSize ${isRequiredClass()}>$text</h$textSize>"

    private fun isRequiredClass(): String = if (isRequired) "class=\"required\"" else ""

    companion object {
        private const val MIN_TEXT_SIZE = 1
        private const val MAX_TEXT_SIZE = 5
        private const val DEFAULT_TEXT_SIZE = 3
    }
}

@Serializable
@SerialName("Input")
data class InputHtmlQuestion(
    override val text: String,
    override val elementId: String,
    /**
     * input type. text, number, etc.
     */
    private val type: String = defaultType,
    /**
     * min parameter in the input.
     */
    private val min: Int? = null,
    /**
     * max parameter in the input.
     */
    private val max: Int? = null,
    /**
     * step parameter in the input.
     */
    private val step: Float? = null
) :
    HtmlQuestion() {
    override fun toHtml(): String = buildString {
        append(htmlText())
        append("<input type=\"$type\" id=\"$elementId\" name=\"$elementId\" ")
        append("${min.asParameter("min")} ${max.asParameter("max")} ")
        append("${step.asParameter("step")} ${isRequiredString()}><br>")
    }

    companion object {
        private const val defaultType = "text"
    }
}

@Serializable
@SerialName("Radio")
data class RadioHtmlQuestion(
    override val text: String,
    /**
     * **ElementId** here is radio button **name**!!!.
     */
    override val elementId: String,
    @SerialName("info") val infos: List<ValueInfo>
) : HtmlQuestion() {
    override fun toHtml(): String = buildString {
        append(htmlText())
        infos.forEach { (id, value) ->
            append("<label for=\"$id\"><input type=\"radio\" id=\"$id\" ${elementId.asParameter("name")} ")
            append("value=\"$value\" ${isRequiredString()}>")
            append("$value</label><br>")
        }
    }
}

@Serializable
@SerialName("Checkbox")
data class CheckboxHtmlQuestion(
    override val text: String,
    /**
     * **ElementId** here is checkbox button **name**!!!.
     */
    override val elementId: String,
    @SerialName("info") val infos: List<ValueInfo>
) : HtmlQuestion() {
    override fun toHtml(): String = buildString {
        append(htmlText())
        infos.forEach { (id, value) ->
            append("<label for=\"$id\"><input type=\"checkbox\" id=\"$id\" ${elementId.asParameter("name")} ")
            append("value=\"$value\" ${isRequiredString()}>")
            append("$value</label><br>")
        }
    }
}

@Serializable
@SerialName("Textarea")
data class TextAreaHtmlQuestion(
    override val text: String,
    override val elementId: String,
    /**
     * rows parameter in the input.
     */
    val rows: Int? = null,
    /**
     * cols parameter in the input.
     */
    val cols: Int? = null
) : HtmlQuestion() {
    override fun toHtml(): String = buildString {
        append(htmlText())
        append("<textarea name=\"$elementId\" id=\"$elementId\" ${rows.asParameter("rows")} ")
        append("${cols.asParameter("cols")} ${isRequiredString()}></textarea>")
    }

    override fun isRequiredString(): String = if (isRequired) "data-req=\"required\"" else ""
}

/**
 * Represents container in which tou can store sub questions with general title.
 */
@Serializable
@SerialName("Container")
data class HtmlQuestionContainer(override val text: String) : HtmlQuestion() {
    override val elementId: String
        get() = ""

    @SerialName("questions")
    val subQuestions: List<HtmlQuestion> = emptyList()

    override fun toHtml(): String = buildString {
        append(htmlText())
        subQuestions.forEach {
            append(it.toHtml())
        }
    }
}

@Serializable
data class Labels(
    val min: String,
    val max: String
)

@Serializable
@SerialName("Slider")
data class SliderHtmlQuestion(
    override val text: String,
    override val elementId: String,
    val min: Int,
    val max: Int,
    val step: Int = 1,
    val labels: Labels
) : HtmlQuestion() {
    override fun toHtml(): String = buildString {
        append(htmlText())
        append("<input type=\"range\" id=\"$elementId\" name=\"$elementId\" ")
        append("min=\"$min\" max=\"$max\" step=\"$step\" ${isRequiredString()} ")
        append(" style=\"width: 100%; display: block;\">")

        append("<div style=\"display: flex; justify-content: space-between; color: white;\">")
        (min..max step step).forEachIndexed { index, value ->
            append("<div style=\"text-align: center; flex: 1; position: relative;\">")
            append("<div style=\"width: 2px; height: 10px; background: white; margin: auto;\"></div>")
            append("<span style=\"display: block; margin-top: 2px;\">$value</span>")
            append("</div>")
        }
        append("</div>")

        append("<div style=\"display: flex; justify-content: space-between; color: white;\">")
        append("<span>${labels.min}</span>")
        append("<span>${labels.max}</span>")
        append("</div>")
    }
}
