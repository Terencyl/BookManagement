package com.jicay.bookmanagement.domain.usecase

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.jicay.bookmanagement.domain.exceptions.BookReservedException
import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class BookDTOUseCaseTest {

    @InjectMockKs
    private lateinit var bookUseCase: BookUseCase

    @MockK
    private lateinit var bookPort: BookPort

    @Test
    fun `get all books should returns all books sorted by name`() {
        every { bookPort.getAllBooks() } returns listOf(
            Book("Les Misérables", "Victor Hugo"),
            Book("Hamlet", "William Shakespeare")
        )

        val res = bookUseCase.getAllBooks()

        assertThat(res).containsExactly(
            Book("Hamlet", "William Shakespeare"),
            Book("Les Misérables", "Victor Hugo")
        )
    }

    @Test
    fun `add book`() {
        justRun { bookPort.createBook(any()) }

        val book = Book("Les Misérables", "Victor Hugo")

        bookUseCase.addBook(book)

        verify(exactly = 1) { bookPort.createBook(book) }
    }

    @Test
    fun `reserve book`() {
        var existingBook = Book("test", "Author", false)

        every { bookPort.getAllBooks() } returns listOf(existingBook)
        every { bookPort.isBookReserved(existingBook.title) } returns false
        every { bookPort.reserveBook(existingBook.title) } answers {
            existingBook = existingBook.copy(reserved = true)
        }

        bookUseCase.reserveBook(existingBook.title)

        verify { bookPort.reserveBook(existingBook.title) }

        assertThat(existingBook.reserved).isTrue()
    }

    @Test
    fun `reserveBook throws BookReservedException when book is already reserved`() {
        val existingBook = Book("test", "Author", true) // Book is already reserved

        every { bookPort.getAllBooks() } returns listOf(existingBook)
        every { bookPort.isBookReserved(existingBook.title) } returns true // Book is already reserved

        assertFailure {
            bookUseCase.reserveBook(existingBook.title)
        }
            .isInstanceOf(BookReservedException::class)


        // Ensure that bookPort's reserveBook method was not called
        verify(exactly = 0) { bookPort.reserveBook(existingBook.title) }
    }

    @Test
    fun `isBookReserved returns true when book is reserved`() {
        val bookTitle = "ReservedBook"
        every { bookPort.isBookReserved(bookTitle) } returns true

        val isReserved = bookUseCase.isBookReserved(bookTitle)

        assertThat(isReserved).isTrue()
        // Verify that bookPort's isBookReserved method was called with the provided title
        verify { bookPort.isBookReserved(bookTitle) }
    }

    @Test
    fun `isBookReserved returns false when book is not reserved`() {
        val bookTitle = "NotReservedBook"
        every { bookPort.isBookReserved(bookTitle) } returns false

        val isReserved = bookUseCase.isBookReserved(bookTitle)

        assertThat(isReserved).isFalse()

        verify { bookPort.isBookReserved(bookTitle) }
    }
}