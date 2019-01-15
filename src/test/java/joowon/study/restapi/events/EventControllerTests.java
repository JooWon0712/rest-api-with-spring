package joowon.study.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import joowon.study.restapi.common.RestDocsConfiguration;
import joowon.study.restapi.common.TestDescription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 1, 9, 18))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 2, 9, 18))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 3, 9, 18))
                .endEventDateTime(LocalDateTime.of(2018, 11, 4, 9, 18))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("안양역")
                .build();

        mockMvc.perform(post("/api/events/")  // perform = 요청
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andDo(
                        document("create-event",
                            links(linkWithRel("self").description("link to self"),
                                  linkWithRel("query-events").description("link to query events"),
                                  linkWithRel("update-event").description("link to update an existing"),
                                  linkWithRel("profile").description("link to update an existing")
                            ),
                            requestHeaders(
                                    headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                    headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                            ),
                            requestFields(
                                    fieldWithPath("name").description("name of new event"),
                                    fieldWithPath("description").description("description of new event"),
                                    fieldWithPath("beginEnrollmentDateTime").description("date time of begin enrollment"),
                                    fieldWithPath("closeEnrollmentDateTime").description("date time of close enrollment"),
                                    fieldWithPath("beginEventDateTime").description("date time of begin event"),
                                    fieldWithPath("endEventDateTime").description("date time of end event"),
                                    fieldWithPath("location").description("location of new event"),
                                    fieldWithPath("basePrice").description("base price of new event"),
                                    fieldWithPath("maxPrice").description("max price of new event"),
                                    fieldWithPath("limitOfEnrollment").description("limit of enrollment")
                            ),
                            responseHeaders(
                                    headerWithName(HttpHeaders.LOCATION).description("location header"),
                                    headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                            ),
                            // Relaxed + responseFields
                            // 장점 : 문서의 일부분만 테스트하고 문서화 할수 있다.
                            // 단점 : 정확한 문서를 생성하지 못한다.
                            responseFields(
                                    fieldWithPath("id").description("identifier of new event"),
                                    fieldWithPath("name").description("name of new event"),
                                    fieldWithPath("description").description("description of new event"),
                                    fieldWithPath("beginEnrollmentDateTime").description("date time of begin enrollment"),
                                    fieldWithPath("closeEnrollmentDateTime").description("date time of close enrollment"),
                                    fieldWithPath("beginEventDateTime").description("date time of begin event"),
                                    fieldWithPath("endEventDateTime").description("date time of end event"),
                                    fieldWithPath("location").description("location of new event"),
                                    fieldWithPath("basePrice").description("base price of new event"),
                                    fieldWithPath("maxPrice").description("max price of new event"),
                                    fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                                    fieldWithPath("offline").description("it tells if this event is offline or not"),
                                    fieldWithPath("free").description("it tells if this event is free or not"),
                                    fieldWithPath("eventStatus").description("event status"),
                                    fieldWithPath("_links.self.href").description("link to self"),
                                    fieldWithPath("_links.query-events.href").description("link to query events"),
                                    fieldWithPath("_links.update-event.href").description("link to update an existing"),
                                    fieldWithPath("_links.profile.href").description("link to update an existing")
                            )
                ));
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 1, 9, 18))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 2, 9, 18))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 3, 9, 18))
                .endEventDateTime(LocalDateTime.of(2018, 11, 4, 9, 18))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("안양역")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")  // perform = 요청
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 비어 있는 경우에 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))  // perform = 요청
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 1, 9, 18))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 2, 9, 18))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 4, 9, 18))
                .endEventDateTime(LocalDateTime.of(2018, 11, 3, 9, 18))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("안양역")
                .build();

        this.mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))  // perform = 요청
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
        ;
    }

}
