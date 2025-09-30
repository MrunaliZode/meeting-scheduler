package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.exceptions.MeetingNotFoundException;
import com.doodle.meetingscheduler.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeetingService meetingService;

    private Meeting meeting;

    @BeforeEach
    void setup() {
        meeting = new Meeting();
        meeting.setId(1L);
        meeting.setTitle("Team Sync");
        meeting.setDescription("Daily standup");
        meeting.setParticipants(Set.of(new User(1L, "Alice", "alice@test.com")));
    }

    // --------- BOOK MEETING ---------
    @Test
    void shouldBookMeetingSuccessfully() throws Exception {
        Mockito.when(meetingService.bookMeeting(any(), any(), any(), anyList(), any(), any()))
                .thenReturn(meeting);

        String requestJson = """
            {
              "userId": 1,
              "title": "Team Sync",
              "description": "Daily standup",
              "participantIds": [1,2,3],
              "from": "2025-10-01T10:00:00",
              "to": "2025-10-01T10:30:00"
            }
        """;

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Team Sync")))
                .andExpect(jsonPath("$.description", is("Daily standup")));
    }

    // --------- GET MEETINGS FOR USERS ---------
    @Test
    void shouldGetMeetingsForUsers() throws Exception {
        Page<Meeting> page = new PageImpl<>(List.of(meeting));
        Mockito.when(meetingService.getMeetingsForUsers(anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/meetings/users")
                        .param("userIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Team Sync")));
    }

    @Test
    void shouldReturnEmptyListWhenNoMeetingsFound() throws Exception {
        Page<Meeting> emptyPage = Page.empty();
        Mockito.when(meetingService.getMeetingsForUsers(anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/meetings/users")
                        .param("userIds", "99"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // --------- GET MEETING BY ID ---------
    @Test
    void shouldGetMeetingById() throws Exception {
        Mockito.when(meetingService.getMeeting(1L)).thenReturn(meeting);

        mockMvc.perform(get("/meetings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Team Sync")));
    }

    @Test
    void shouldReturn404WhenMeetingNotFound() throws Exception {
        Mockito.when(meetingService.getMeeting(99L)).thenThrow(new MeetingNotFoundException("Meeting not found"));

        mockMvc.perform(get("/meetings/99"))
                .andExpect(status().isNotFound());
    }

    // --------- DELETE MEETING ---------
    @Test
    void shouldDeleteMeetingSuccessfully() throws Exception {
        mockMvc.perform(delete("/meetings/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(meetingService).deleteMeeting(1L);
    }
}
