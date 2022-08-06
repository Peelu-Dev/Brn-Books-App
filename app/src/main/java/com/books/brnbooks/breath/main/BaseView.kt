package com.books.brnbooks.breath.main

interface BaseView<T> {
    fun setPresenter(presenter: T);
}