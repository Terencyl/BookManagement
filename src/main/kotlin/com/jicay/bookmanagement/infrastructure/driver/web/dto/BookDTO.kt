    package com.jicay.bookmanagement.infrastructure.driver.web.dto

    import com.jicay.bookmanagement.domain.model.Book

    data class BookDTO(val title: String, val author: String, val reserved: Boolean = false) {
        fun toDomain(): Book {
            return Book(
                title = this.title,
                author = this.author,
                reserved = this.reserved
            )
        }
    }

    fun Book.toDto() = BookDTO(
        title = this.title,
        author = this.author,
        reserved = this.reserved
    )