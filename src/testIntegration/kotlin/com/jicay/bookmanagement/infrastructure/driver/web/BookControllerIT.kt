package com.jicay.bookmanagement.infrastructure.driver.web

import assertk.assertFailure
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import com.jicay.bookmanagement.domain.exceptions.BookReservedException
import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.usecase.BookUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@WebMvcTest
class BookControllerIT {

    @MockkBean
    private lateinit var bookUseCase: BookUseCase

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `rest route get all books`() {
        every { bookUseCase.getAllBooks() } returns listOf(Book("A", "B", true))

        mockMvc.get("/books")
            .andExpect {
                status { isOk() }
                content { content { APPLICATION_JSON } }
                content { json(
                    // language=json
                    """
                        [
                          {
                            "title": "A",
                            "author": "B",
                            "reserved": true
                          }
                        ]
                    """.trimIndent()
                ) }
            }
    }

    @Test
    fun `rest route post book without reservation`() {
        justRun { bookUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "title": "Les misérables",
                  "author": "Victor Hugo"
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        val expected = Book(
            title = "Les misérables",
            author = "Victor Hugo",
            reserved = false
        )

        verify(exactly = 1) { bookUseCase.addBook(expected) }
    }

    @Test
    fun `rest route post book with reservation`(){
        justRun { bookUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "title": "Les misérables",
                  "author": "Victor Hugo",
                  "reserved": true
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
        }

        val expected = Book(
            title = "Les misérables",
            author = "Victor Hugo",
            reserved = true
        )

        verify(exactly = 1) { bookUseCase.addBook(expected) }
    }

    @Test
    fun `rest route post book should return 400 when body is not good`() {
        justRun { bookUseCase.addBook(any()) }

        mockMvc.post("/books") {
            // language=json
            content = """
                {
                  "name": "Les misérables",
                  "author": "Victor Hugo"
                }
            """.trimIndent()
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { bookUseCase.addBook(any()) }
    }
    @Test
    fun `rest route reserve book should reserve a book`() {
        val bookTitle = "Les miserables"

        every { bookUseCase.isBookReserved(bookTitle) } returns false
        every { bookUseCase.reserveBook(bookTitle) } answers {}

        mockMvc.post("/books/$bookTitle/reserve"){
            contentType = APPLICATION_JSON
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `rest route reserve book should throw Exception if book is already reserved`() {
        val bookTitle = "Les miserables"

        every { bookUseCase.isBookReserved(bookTitle) } returns true
        every { bookUseCase.reserveBook(bookTitle) } answers { throw BookReservedException("") }

        assertFailure {
            mockMvc.post("/books/$bookTitle/reserve"){
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
        }
            .isInstanceOf(Exception::class)
    }
}