package smagellan.test.lombok;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@JsonDeserialize(builder = Child.ChildBuilder.class)
@EqualsAndHashCode(callSuper = true)
public class Child extends Parent {
    @JsonProperty("child_name")
    private String childName;

    @JsonPOJOBuilder(withPrefix = "")
    public static abstract class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
    }
}
