package com.jicay.bookmanagement.infrastructure.driven.adapter

import com.jicay.bookmanagement.domain.exceptions.BookReservedException
import com.jicay.bookmanagement.domain.exceptions.BookNotFoundException
import com.jicay.bookmanagement.domain.model.Book
import com.jicay.bookmanagement.domain.port.BookPort
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class BookDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate): BookPort {
    override fun getAllBooks(): List<Book> {
        return namedParameterJdbcTemplate
            .query("SELECT * FROM BOOK", MapSqlParameterSource()) { rs, _ ->
                Book(
                    title = rs.getString("title"),
                    author = rs.getString("author"),
                    isReserved = rs.getBoolean("is_reserved")
                )
            }
    }

    override fun createBook(book: Book) {
        namedParameterJdbcTemplate
            .update("INSERT INTO BOOK (title, author, is_reserved) values (:title, :author, :isReserved)", mapOf(
                "title" to book.title,
                "author" to book.author,
                "isReserved" to book.isReserved
            ))
    }

    override fun reserveBook(title: String) {
        val isBookReserved = isBookReserved(title)

        when {
            isBookReserved -> throw BookReservedException("Le livre avec le titre $title est déjà réservé.")
            else -> {
                namedParameterJdbcTemplate.update(
                    "UPDATE BOOK SET is_reserved = true WHERE title = :title",
                    mapOf("title" to title)
                )
            }
        }
    }

    override fun isBookReserved(title: String): Boolean {
        return namedParameterJdbcTemplate
            .queryForObject(
                "SELECT is_reserved FROM BOOK WHERE title = :title",
                mapOf("title" to title),
                Boolean::class.java
            ) ?: throw BookNotFoundException("Le livre avec le titre $title n'existe pas.")
    }
}