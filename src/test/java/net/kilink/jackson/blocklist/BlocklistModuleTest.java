package net.kilink.jackson.blocklist;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlocklistModuleTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDisallowSerializationByPackage() {
        Module module = BlocklistModule.builder()
                .packages("net.kilink", "com.google.common.collect")
                .build();
        mapper.registerModule(module);

        assertDoesNotThrow(() -> mapper.writeValueAsString(Arrays.asList(1, 2, 3, 4)));
        assertThrows(JsonMappingException.class, () -> mapper.writeValueAsString(ImmutableMap.of("a", "b")));
        assertThrows(JsonMappingException.class, () -> mapper.writeValueAsString(new Point(1, 2)));
    }

    @Test
    public void testDisallowSerializationByClass() {
        Module module = BlocklistModule.builder()
                .classes(Point.class, ImmutableMap.class)
                .build();
        mapper.registerModule(module);

        assertDoesNotThrow(() -> mapper.writeValueAsString(ImmutableList.of(1, 2, 3, 4)));
        assertThrows(JsonMappingException.class, () -> mapper.writeValueAsString(ImmutableMap.of("a", "b")));
        assertThrows(JsonMappingException.class, () -> mapper.writeValueAsString(new Point(1, 2)));
    }

    @Test
    public void testDisallowSerializationByAnnotation() {
        Module module = BlocklistModule.builder()
                .annotations(DisableSerialization.class)
                .build();
        mapper.registerModule(module);

        assertThrows(JsonMappingException.class, () -> mapper.writeValueAsString(new Point(1, 2)));
    }
}
