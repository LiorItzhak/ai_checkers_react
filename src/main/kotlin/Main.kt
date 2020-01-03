package chekersApp
import react.dom.*
import chekers.ui.startPage
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            startPage {}
        }
    }
}

