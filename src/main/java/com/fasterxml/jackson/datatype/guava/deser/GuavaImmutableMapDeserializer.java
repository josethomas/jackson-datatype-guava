package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.collect.ImmutableMap;

abstract class GuavaImmutableMapDeserializer<T extends ImmutableMap<Object, Object>> extends
        GuavaMapDeserializer<T> {

    GuavaImmutableMapDeserializer(MapType type, KeyDeserializer keyDeser, TypeDeserializer typeDeser,
            JsonDeserializer<?> deser) {
        super(type, keyDeser, typeDeser, deser);
    }

    protected abstract ImmutableMap.Builder<Object, Object> createBuilder();

    @Override
    protected T _deserializeEntries(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        final KeyDeserializer keyDes = _keyDeserializer;
        final JsonDeserializer<?> valueDes = _valueDeserializer;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;
    
        ImmutableMap.Builder<Object, Object> builder = createBuilder();
        for (; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken()) {
            // Must point to field name now
            String fieldName = jp.getCurrentName();
            Object key = (keyDes == null) ? fieldName : keyDes.deserializeKey(fieldName, ctxt);
            // And then the value...
            JsonToken t = jp.nextToken();
            // 28-Nov-2010, tatu: Should probably support "ignorable properties" in future...
            Object value;            
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            if (null != value) {
                builder.put(key, value);
            }
        }
        // No class outside of the package will be able to subclass us,
        // and we provide the proper builder for the subclasses we implement.
        @SuppressWarnings("unchecked")
        T map = (T) builder.build();
        return map;
    }

}
