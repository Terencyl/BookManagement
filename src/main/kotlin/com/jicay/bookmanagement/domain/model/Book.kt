package com.jicay.bookmanagement.domain.model

data class Book(val title: String, val author: String, val reserved: Boolean = false)
