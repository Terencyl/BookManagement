package com.jicay.bookmanagement.infrastructure.driver.web

import com.jicay.bookmanagement.domain.usecase.BookUseCase
import com.jicay.bookmanagement.infrastructure.driver.web.dto.BookDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(
    private val bookUseCase: BookUseCase
) {
    @CrossOrigin
    @GetMapping
    fun getAllBooks(): List<BookDTO> {
        return bookUseCase.getAllBooks().map { book ->
            val isReserved = bookUseCase.isBookReserved(book.title)
            BookDTO(
                title = book.title,
                author = book.author,
                isReserved = isReserved
            )
        }
    }

    @CrossOrigin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addBook(@RequestBody bookDTO: BookDTO) {
        bookUseCase.addBook(bookDTO.toDomain())
    }

    @CrossOrigin
    @PostMapping("/{title}/reserve")
    @ResponseStatus(HttpStatus.OK)
    fun reserveBook(@PathVariable title: String) {
        bookUseCase.reserveBook(title)
    }
}