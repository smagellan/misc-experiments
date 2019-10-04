package smagellan.test.lombok;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@JsonDeserialize(builder = Parent.ParentBuilder.class)
@EqualsAndHashCode
public class Parent {
    @JsonProperty("parent_name")
    private String parentName;

    @JsonPOJOBuilder(withPrefix = "")
    public static abstract class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
    }
}
