package ru.hse.spb.texDSL

import org.junit.Assert.assertEquals
import org.junit.Test

class DSLToTexTest {

    private val docBegin = Document.beginDocument
    private val docEnd = Document.endDocument
    private val emptyDocumentBody = "$docBegin\n$docEnd"
    private val defaultDocumentClass = "\\documentClass{kek}"


    private fun begin(tag: String) = "\\begin{$tag}"
    private fun end(tag: String) = "\\end{$tag}"

    private fun defaultDocument(body: String) = "$defaultDocumentClass\n$docBegin\n$body\n$docEnd"

    @Test
    fun documentClassTest() {
        val doc = document {
            documentClass("kek")
        }
        assertEquals("$defaultDocumentClass\n$emptyDocumentBody",
                doc.toString())
    }

    @Test(expected = DocumentClassException::class)
    fun noDocumentClassTest() {
        document {
        }.toPrintStream(System.out)
    }

    @Test(expected = DocumentClassException::class)
    fun twoDocumentClassesTest() {
        document {
            documentClass("kek")
            documentClass("lol")
        }
    }

    @Test
    fun usepackageTest() {
        val doc = document {
            documentClass("kek")
            usepackage("lol")
        }
        assertEquals("$defaultDocumentClass\n\\usepackage{lol}\n$emptyDocumentBody",
                doc.toString())
    }

    @Test
    fun justText() {
        val doc = document {
            documentClass("kek")
            +"cheburek"
        }
        assertEquals(defaultDocument("cheburek"), doc.toString())
    }

    @Test
    fun emptyFrame() {
        val doc = document {
            documentClass("kek")
            frame("super") {
            }
        }
        assertEquals(defaultDocument("${begin("frame")}{super}\n${end("frame")}"),
                doc.toString())
    }

    @Test
    fun mathTest() {
        val doc = document {
            documentClass("kek")
            math {
                +"\\int"
            }
        }
        assertEquals(defaultDocument("${begin("math")}\n\\int\n${end("math")}"),
                doc.toString())
    }

    @Test
    fun frameTest() {
        val doc = document {
            documentClass("kek")
            frame("super") {
                math {
                    +"\\int"
                }
            }
        }
        assertEquals(defaultDocument("${begin("frame")}{super}\n${begin("math")}\n\\int\n" +
                "${end("math")}\n${end("frame")}"),
                doc.toString())
    }

    @Test
    fun emptyItemize() {
        val doc = document {
            documentClass("kek")
            itemize {
            }
        }
        assertEquals(defaultDocument("${begin("itemize")}\n${end("itemize")}"),
                doc.toString())
    }

    @Test
    fun itemizeTest() {
        val doc = document {
            documentClass("kek")
            itemize {
                item { +"lol" }
                item { +"cheburek" }
            }
        }
        assertEquals(defaultDocument("${begin("itemize")}\n\\item lol\n\\item cheburek\n${end("itemize")}"),
                doc.toString())
    }

    @Test
    fun emptyEnumerate() {
        val doc = document {
            documentClass("kek")
            enumerate {
            }
        }
        assertEquals(defaultDocument("${begin("enumerate")}\n${end("enumerate")}"),
                doc.toString())
    }

    @Test
    fun flushleftTest() {
        val doc = document {
            documentClass("kek")
            flushleft {
                +"lol"
            }
        }
        assertEquals(defaultDocument("${begin("flushleft")}\nlol\n${end("flushleft")}"),
                doc.toString())
    }

    @Test
    fun flushrightTest() {
        val doc = document {
            documentClass("kek")
            flushright {
                +"lol"
            }
        }
        assertEquals(defaultDocument("${begin("flushright")}\nlol\n${end("flushright")}"),
                doc.toString())
    }

    @Test
    fun centerTest() {
        val doc = document {
            documentClass("kek")
            center {
                +"lol"
            }
        }
        assertEquals(defaultDocument("${begin("center")}\nlol\n${end("center")}"),
                doc.toString())
    }

    @Test
    fun customTag() {
        val doc = document {
            documentClass("kek")
            customTag("tag") {
            }
        }
        assertEquals(defaultDocument("${begin("tag")}\n${end("tag")}"),
                doc.toString())
    }

    @Test
    fun customTagWithArgs() {
        val doc = document {
            documentClass("kek")
            customTag("tag", "lol" to "lel") {
            }
        }
        assertEquals(defaultDocument("${begin("tag")}[lol=lel]\n${end("tag")}"),
                doc.toString())
    }

    @Test
    fun enumerateTest() {
        val doc = document {
            documentClass("kek")
            enumerate {
                item { +"lol" }
                item { +"cheburek" }
            }
        }
        assertEquals(defaultDocument("${begin("enumerate")}\n\\item lol\n\\item cheburek\n${end("enumerate")}"),
                doc.toString())
    }

    @Test
    fun bigTest() {
        document {
            documentClass("beamer")
            usepackage("babel", "russian" /* varargs */)
            frame("frametitle", "arg1" to "arg2") {
                itemize {
                    for (row in listOf("a", "b", "kek")) {
                        item { +"$row text" }
                    }
                }

                // begin{pyglist}[language=kotlin]...\end{pyglist}
                customTag("pyglist", "language" to "kotlin") {
                    +"""
               |val a = 1
               |
            """.trimMargin()
                }
            }
        }.toPrintStream(System.out)
    }
}