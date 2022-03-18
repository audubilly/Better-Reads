package com.billy.betterreadsdataloader;

import com.billy.betterreadsdataloader.author.Author;
import com.billy.betterreadsdataloader.author.AuthorRepository;
import com.billy.betterreadsdataloader.book.Books;
import com.billy.betterreadsdataloader.book.BooksRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Initialize {

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    BooksRepository booksRepository;

    @Value("${datadump.location.author}")
    private String authorDumpLocation;

    @Value("${datadump.location.works}")
    private String worksDumpLocation;


    private void initAuthors(){
        //get path
        Path path = Paths.get(authorDumpLocation);
       try (Stream<String> lines = Files.lines(path)){
           lines.forEach(line -> {
               //Read and parse the line
               String jsonString = line.substring(line.indexOf("{"));
               JSONObject jsonObject = null;
               try {
                   jsonObject = new JSONObject(jsonString);

               //Construct the author object
               Author author = new Author();
               author.setName(jsonObject.optString("name"));
               author.setPersonalName(jsonObject.optString("personal_name"));
               author.setId(jsonObject.optString("key").replace("/authors/",""));

               //persist using repository
                   System.out.println("Saving Authors " + author.getName() + "--------");
               authorRepository.save(author);
               } catch (JSONException e) {
                   e.printStackTrace();
               }
           });

       }catch (IOException e){
           e.printStackTrace();
       }

    }


    private void initWorks(){
        //get path
        Path path = Paths.get(worksDumpLocation);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        try (Stream<String> lines = Files.lines(path)){
            lines.forEach(line -> {
                //Read and parse the line
                String jsonString = line.substring(line.indexOf("{"));

                try {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    //Construct the Book object
                    Books books = new Books();

                    books.setId(jsonObject.optString("key").replace("/works/",""));

                    books.setName(jsonObject.optString("title"));

                    JSONObject descriptionObj = jsonObject.optJSONObject("description");
                    if (descriptionObj != null) {
                        books.setDescription(descriptionObj.optString("value"));
                    }

                    JSONObject publishedDateObj = jsonObject.optJSONObject("created");
                    if (publishedDateObj != null) {
                        String dateStr = publishedDateObj.getString("value");
                        books.setPublishedDate(LocalDate.parse(dateStr,dateTimeFormatter));
                    }

                    JSONArray coversArray = jsonObject.optJSONArray("covers");
                    if (coversArray != null) {
                        List<String> coverIds = new ArrayList<>();
                        for (int i = 0; i < coversArray.length(); i++) {
                            coverIds.add(coversArray.getString(i));
                        }
                        books.setCoverIds(coverIds);
                    }

                    JSONArray authorArray = jsonObject.optJSONArray("authors");
                    if (authorArray != null) {
                        List<String> authorIds = new ArrayList<>();
                        for (int i = 0; i < authorArray.length(); i++) {
                            String authorId = authorArray.getJSONObject(i).getJSONObject("author").getString("key")
                                    .replace("/authors/", "");
                            authorIds.add(authorId);
                        }
                        books.setAuthorIds(authorIds);
                    List<String> authorNames = authorIds.stream().map(id -> authorRepository.findById(id))
                            .map(optionalAuthor -> {
                                if(!optionalAuthor.isPresent()) return "Unknown Author";
                                return optionalAuthor.get().getName();
                            }).collect(Collectors.toList());
                        books.setAuthorNames(authorNames);
                    }

                    //persist using repository
                    System.out.println("Saving Books " + books.getName() + "--------");
                    booksRepository.save(books);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }catch (IOException e){
            e.printStackTrace();
        }

    }


    @PostConstruct
    public void start(){
//    initAuthors();
//    initWorks();
    }
}
