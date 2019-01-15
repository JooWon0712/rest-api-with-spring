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

}