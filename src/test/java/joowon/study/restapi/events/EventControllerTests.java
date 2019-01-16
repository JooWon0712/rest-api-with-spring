package joowon.study.restapi.events;

import joowon.study.restapi.accounts.Account;
import joowon.study.restapi.accounts.AccountRepository;
import joowon.study.restapi.accounts.AccountRole;
import joowon.study.restapi.accounts.AccountService;
import joowon.study.restapi.common.AppProperties;
import joowon.study.restapi.common.BaseControllerTest;
import joowon.study.restapi.common.TestDescription;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class EventControllerTests extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setup() {
        System.out.println("##############################################");
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

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
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                                    fieldWithPath("manager").description("manager"),
                                    fieldWithPath("_links.self.href").description("link to self"),
                                    fieldWithPath("_links.query-events.href").description("link to query events"),
                                    fieldWithPath("_links.update-event.href").description("link to update an existing"),
                                    fieldWithPath("_links.profile.href").description("link to update an existing")
                            )
                ));
    }

    private String getBearerToken() throws Exception {
        return "Bearer " + getAccessToken();
    }

    private String getAccessToken() throws Exception {
        // Given
        Account admin = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(admin);

        // When & Then
        ResultActions resultActions = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClintId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type","password"));

        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser jsonParser = new Jackson2JsonParser();
        return jsonParser.parseMap(responseBody).get("access_token").toString();
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))  // perform = 요청
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @TestDescription("30개 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generateEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort","name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"
                        ,links(linkWithRel("self").description("link to self"),
                              linkWithRel("first").description("첫번째 페이지 링크"),
                              linkWithRel("prev").description("이번 페이지 링크"),
                              linkWithRel("self").description("현재 페이지 링크"),
                              linkWithRel("next").description("다음 페이지 링크"),
                              linkWithRel("last").description("마지막 페이지 링크"),
                              linkWithRel("profile").description("프로파일 링크")
                        )
                        ,requestParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("한 페이지 크기"),
                                parameterWithName("sort").description("정렬 옵션 (ex : name,DESC )")
                        )
                        ,responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        )
                        ,responseFields(
                                fieldWithPath("_embedded.eventList[].id").description("identifier of new event"),
                                fieldWithPath("_embedded.eventList[].name").description("name of new event"),
                                fieldWithPath("_embedded.eventList[].description").description("description of new event"),
                                fieldWithPath("_embedded.eventList[].beginEnrollmentDateTime").description("date time of begin enrollment"),
                                fieldWithPath("_embedded.eventList[].closeEnrollmentDateTime").description("date time of close enrollment"),
                                fieldWithPath("_embedded.eventList[].beginEventDateTime").description("date time of begin event"),
                                fieldWithPath("_embedded.eventList[].endEventDateTime").description("date time of end event"),
                                fieldWithPath("_embedded.eventList[].location").description("location of new event"),
                                fieldWithPath("_embedded.eventList[].basePrice").description("base price of new event"),
                                fieldWithPath("_embedded.eventList[].maxPrice").description("max price of new event"),
                                fieldWithPath("_embedded.eventList[].limitOfEnrollment").description("limit of enrollment"),
                                fieldWithPath("_embedded.eventList[].offline").description("it tells if this event is offline or not"),
                                fieldWithPath("_embedded.eventList[].free").description("it tells if this event is free or not"),
                                fieldWithPath("_embedded.eventList[].eventStatus").description("event status"),
                                fieldWithPath("_embedded.eventList[].manager").description("manager"),
                                fieldWithPath("_embedded.eventList[]._links.self.href").description("event status"),

                                fieldWithPath("_links.first.href").description("첫번째 페이지 링크"),
                                fieldWithPath("_links.prev.href").description("이번 페이지 링크"),
                                fieldWithPath("_links.self.href").description("현재 페이지 링크"),
                                fieldWithPath("_links.next.href").description("다음 페이지 링크"),
                                fieldWithPath("_links.last.href").description("마지막 페이지 링크"),
                                fieldWithPath("_links.profile.href").description("프로필 링크"),

                                fieldWithPath("page.size").description("한 페이지 크기"),
                                fieldWithPath("page.totalElements").description("총 이벤트 수"),
                                fieldWithPath("page.totalPages").description("총 페이지 수"),
                                fieldWithPath("page.number").description("현재 페이지 번호(페이지 번호는 0부터 시작)"))
                        ))
        ;
    }

    @Test
    @TestDescription("기존 이벤트 하나 조회하기")
    public void getEvent() throws Exception {
        // Given
        Event event = this.generateEvent(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"));
    }

    @Test
    @TestDescription("없는 이벤트를 조회했을때 404 응답받기")
    public void getEvent_404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", 1231231))
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception {
        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "update event";
        eventDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("update-event"));
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 이벤트 수정 실패하기")
    public void updateEvent_400_empty() throws Exception {
        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = new EventDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 이벤트 수정 실패하기")
    public void updateEvent_400_wrong() throws Exception {
        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패하기")
    public void updateEvent_404() throws Exception {
        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/1221313")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("event "+index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 1, 9, 18))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 2, 9, 18))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 3, 9, 18))
                .endEventDateTime(LocalDateTime.of(2018, 11, 4, 9, 18))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("안양역")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return this.eventRepository.save(event);
    }

}
