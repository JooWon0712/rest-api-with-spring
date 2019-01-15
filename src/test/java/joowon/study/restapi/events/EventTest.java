package joowon.study.restapi.events;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    @Test
    public void builder() {
        Event event = Event.builder()
                .name("Study Spring REST API")
                .description("Rest API development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        Event event = new Event();
        String name = "Event";
        String description = "description";
        event.setName(name);
        event.setDescription(description);

        assertThat(event.getName()).isEqualTo(name);

        assertThat(event.getDescription()).isEqualTo(description);
    }

    @Test
    public void testFree(){
        // Given
        Event event = Event.builder()
                .basePrice(0)
                .maxPrice(0)
                .build();
        // When
        event.update();

        // Then
        assertThat(event.isFree()).isTrue();

        // Given
        event = Event.builder()
                .basePrice(100)
                .maxPrice(0)
                .build();
        // When
        event.update();

        // Then
        assertThat(event.isFree()).isFalse();

        // Given
        event = Event.builder()
                .basePrice(0)
                .maxPrice(100)
                .build();
        // When
        event.update();

        // Then
        assertThat(event.isFree()).isFalse();
    }

    @Test
    public void testOffline(){
        // Given
        Event event = Event.builder()
                .location("안양역")
                .build();
        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isTrue();

        // Given
        event = Event.builder()
                .build();
        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isFalse();
    }
}