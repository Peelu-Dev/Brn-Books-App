package com.books.brnbooks.breath.main

import com.books.brnbooks.breath.data.Config

interface BreatheContract {

    interface View : BaseView<Presenter> {
        fun loadConfiguration(list: ArrayList<String>);
        fun startAnimation()
        fun stopAnimation()
        fun startTimer();
        fun stopTimer();
        fun hideConfiguration()
        fun showConfiguration()
    }

    interface Presenter : BasePresenter {
        fun startBreathing(config: Config)
        fun stopBreathing()
    }

}