package com.jicay.bookmanagement.domain.usecase

import com.jicay.bookmanagement.domain.exceptions.BookReservedException
import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort

class BookUseCase(
    private val bookPort: BookPort
) {
    fun getAllBooks(): List<Book> {
        return bookPort.getAllBooks().sortedBy {
            it.title.lowercase()
        }
    }

    fun addBook(book: Book) {
        bookPort.createBook(book)
    }

    fun reserveBook(title: String) {
        if (isBookReserved(title)) {
            throw BookReservedException("Le livre avec le titre $title est déjà réservé.")
        }
        else {
            bookPort.reserveBook(title)
        }
    }

    fun isBookReserved(title: String): Boolean {
        return bookPort.isBookReserved(title)
    }
}