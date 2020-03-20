package org.elasticsearch.join;

import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.SearchExtBuilder;

import java.io.IOException;
import java.util.Objects;

public class FamilySearchExt extends SearchExtBuilder {
    public static String NAME = "all_children";

    public FamilySearchExt() {
    }

    public FamilySearchExt(StreamInput in) throws IOException {
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
       return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash( NAME);
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }
    public static FamilySearchExt fromXContent(XContentParser parser) throws IOException {
        return new FamilySearchExt();
    }


    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder;
    }
}

