package smagellan.test.mongodb;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

public class MongoJackEntity {
    @ObjectId
    @JsonProperty("_id")
    public String id;

    @JsonProperty("name")
    public String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public MongoJackEntity withId(String id) {
        this.id = id;
        return this;
    }

    public MongoJackEntity withName(String name) {
        this.name = name;
        return this;
    }


    @Override
    public String toString() {
        return "MongoJackEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
