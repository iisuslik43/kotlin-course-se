package ru.hse.spb.texDSL

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.Exception

@DslMarker
annotation class TagMarker

@TagMarker
abstract class Tag(val name: String) {
    abstract fun toPrintStream(out: PrintStream)
}

open class TagWithChildren(name: String, val args: List<String> = emptyList(),
                               val optionalArgs: Map<String, String> = emptyMap()) : Tag(name) {
    override fun toPrintStream(out: PrintStream) {
        out.print("\\begin{$name}")
        args.forEach { out.print("{$it}") }
        optionalArgs.forEach { out.print("[${it.key}=${it.value}]") }
        out.println()
        out.printTags(children)
        out.println("\\end{$name}")
    }

    val children: MutableList<Tag> = mutableListOf()

    operator fun String.unaryPlus() {
        children.add(TextTag(this))
    }
    
    protected fun <T: Tag> initTag(tag: T, init: T.() -> Unit) {
        tag.init()
        children.add(tag)
    }

    fun customTag(tagName: String,
                  vararg optionalArgs: Pair<String, String>, init: CustomTag.() -> Unit) {
        initTag(CustomTag(tagName, optionalArgs.toMap()), init)
    }

    fun math(init: Math.() -> Unit) = initTag(Math(), init)

    fun itemize(init: Itemize.() -> Unit) = initTag(Itemize(), init)

    fun enumerate(init: Enumerate.() -> Unit) = initTag(Enumerate(), init)

    fun flushleft(init: FlushLeft.() -> Unit) = initTag(FlushLeft(), init)

    fun flushright(init: FlushRight.() -> Unit) = initTag(FlushRight(), init)

    fun center(init: Center.() -> Unit) = initTag(Center(), init)
}

class Document : TagWithChildren("document") {

    companion object {
        const val beginDocument = "\\begin{document}"
        const val endDocument = "\\end{document}"
    }

    private var documentClass: DocumentClass? = null
    private val packageList: MutableList<Packages> = mutableListOf()

    fun documentClass(className: String) {
        if (documentClass != null) {
            throw DocumentClassException("Two documentClasses in document")
        }
        documentClass = DocumentClass(className)
    }

    fun usepackage(vararg packages: String) {
        packageList.add(Packages(packages.toList()))
    }

    fun frame(frameTitle: String, vararg optionalArgs: Pair<String, String>,
              init: Frame.() -> Unit) = initTag(Frame(frameTitle, optionalArgs.toMap()), init)

    override fun toString(): String {
        val outBytes = ByteArrayOutputStream()
        val out = PrintStream(outBytes)
        toPrintStream(out)
        return outBytes.toString()
    }

    override fun toPrintStream(out: PrintStream) {
        documentClass?.toPrintStream(out) ?: throw DocumentClassException("DocumentClass has not been defined")
        out.printTags(packageList)
        out.println(beginDocument)
        out.printTags(children)
        out.print(endDocument)
    }
}

data class TextTag(val text: String) : Tag("text") {
    override fun toPrintStream(out: PrintStream) = out.println(text)
}

data class DocumentClass(val className: String) : Tag("documentClass") {
    override fun toPrintStream(out: PrintStream) = out.println("\\documentClass{$className}")
}

data class Packages(val packages: List<String>) : Tag("usepackage") {
    override fun toPrintStream(out: PrintStream) {
        val list = packages.reduce { a, b -> a + ", " + b }
        out.println("\\usepackage{$list}")
    }
}

class Frame(title: String, optionalArgs: Map<String, String> = mapOf()) :
        TagWithChildren("frame", listOf(title), optionalArgs)

open class TagWithItems(name: String) : TagWithChildren(name) {
    fun item(init: Item.() -> Unit) = initTag(Item(), init)
}

class Itemize() : TagWithItems("itemize")

class Enumerate() : TagWithItems("enumerate")

class Item() : TagWithChildren("item") {

    override fun toPrintStream(out: PrintStream) {
        out.print("\\item ")
        out.printTags(children)
    }
}

class Math() : TagWithChildren("math")

class CustomTag(tagName: String, optionalArgs: Map<String, String> = mapOf()) :
        TagWithChildren(tagName, optionalArgs = optionalArgs)

class FlushLeft(): TagWithChildren("flushleft")

class FlushRight(): TagWithChildren("flushright")

class Center(): TagWithChildren("center")

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}

fun PrintStream.printTags(Tags: List<Tag>) {
    for (Tag in Tags) {
        Tag.toPrintStream(this)
    }
}


class DocumentClassException(message: String) : Exception(message)