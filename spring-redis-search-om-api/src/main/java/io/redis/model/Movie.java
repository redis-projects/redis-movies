package io.redis.model;

import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class Movie {

    @Id
    private String movieId;

    @SerializedName(value = "rank", alternate = "Rank")
    private int rank;

    @Searchable(weight = 3.0)
    @SerializedName(value = "title", alternate = "Title")
    private String title;

    @TagIndexed
    @SerializedName(value = "genre", alternate = "Genre")
    private List<String> genre;

    @Searchable(weight = 1.0)
    @SerializedName(value = "description", alternate = "Description")
    private String description;

    @Searchable(weight = 3.0)
    @SerializedName(value = "director", alternate = "Director")
    private String director;

    @TagIndexed
    @SerializedName(value = "actors", alternate = "Actors")
    private List<String> actors;

    @NumericIndexed(sortable = true)
    @SerializedName(value = "year", alternate = "Year")
    private long year;

    @NumericIndexed(sortable = true, fieldName = "runtime")
    @SerializedName(value = "runtime", alternate = "Runtime (Minutes)")
    private long runtime;

    @NumericIndexed(sortable = true, fieldName = "rating")
    @SerializedName(value = "rating", alternate = "Rating")
    private double rating;

    @NumericIndexed(sortable = true, fieldName = "votes")
    @SerializedName(value = "votes", alternate = "Votes")
    private long votes;

    @NumericIndexed(sortable = true, fieldName = "revenue")
    @SerializedName(value = "revenue", alternate = "Revenue (Millions)")
    private double revenue;

    @NumericIndexed(sortable = true, fieldName = "metascore")
    @SerializedName(value = "metascore", alternate = "Metascore")
    private long metascore;

}