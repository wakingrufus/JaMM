package com.github.wakingrufus.jamm.desktop

import javafx.application.Application.launch

class Main {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Jamm::class.java, *args)
        }
    }
}
