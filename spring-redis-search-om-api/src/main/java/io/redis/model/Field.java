package io.redis.model;

import io.redis.type.FieldENUM;
import io.redis.type.FieldTypeENUM;
import io.redis.type.OperatorENUM;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Field {

    FieldENUM name;
    FieldTypeENUM type;
    OperatorENUM filter;

}
