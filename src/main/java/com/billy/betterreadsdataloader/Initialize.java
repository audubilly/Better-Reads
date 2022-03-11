package com.billy.betterreadsdataloader;

import com.billy.betterreadsdataloader.author.Author;
import com.billy.betterreadsdataloader.author.AuthorRepository;
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
import java.util.stream.Stream;

@Component
public class Initialize {

    @Autowired
    AuthorRepository authorRepository;

    @Value("${datadump.location.author}")
    private String authorDumpLocation;

    @Value("${datadump.location.works}")
    private String worksDumpLocation;


    private void initAuthors(){
        //get path
        Path path = Paths.get(authorDumpLocation);
       try (Stream<String> lines = Files.lines(path)){
           lines.limit(10).forEach(line -> {
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

    }


    @PostConstruct
    public void start(){
    initAuthors();
    initWorks();
    }
}
