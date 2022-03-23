package com.billy.betterreadsdataloader.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
public class BookController {

    @Autowired
    BooksRepository booksRepository;

    private final String COVER_IMAGE = "https://covers.openlibrary.org/b/id/";

    @GetMapping(value = "/books/{bookId}")
    public String getBook(@PathVariable String bookId, Model model){
        Optional<Books> optionalBook = booksRepository.findById(bookId);
        if(optionalBook.isPresent()){
            Books books = optionalBook.get();
            String coverImageUrl = "/images/no-image.png";
            if(books.getCoverIds() != null & books.getCoverIds().size() > 0){
                coverImageUrl = COVER_IMAGE + books.getCoverIds().get(0) + "-L.png";
            }
            model.addAttribute("coverImage",coverImageUrl);
            model.addAttribute("books",books);
            return "books";
        }
        return "book-not-found";
    }
}
