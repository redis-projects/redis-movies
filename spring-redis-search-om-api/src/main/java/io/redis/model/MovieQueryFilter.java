package io.redis.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MovieQueryFilter {
    String genericCriteria;
    String actors;
    String actorOperator;
    String directors;
    String genres;
    String genreOperator;
    Long releaseYearGTE;
    Long releaseYearLTE;
    Long imdbRatingGTE;
    Long imdbRatingLTE;
    Long metaRatingGTE;
    Long metaRatingLTE;
    Long runtimeGTE;
    Long runtimeLTE;
}