package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.dto.CreateMeetingRequest;
import com.doodle.meetingscheduler.dto.MeetingDTO;
import com.doodle.meetingscheduler.dto.UpdateMeetingRequest;
import com.doodle.meetingscheduler.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MeetingControllerTest {

//    @Mock
//    private MeetingService meetingService;
//
//    @InjectMocks
//    private MeetingController meetingController;
//
//    private Meeting meeting;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        meeting = new Meeting();
//        meeting.setId(1L);
//        meeting.setTitle("Team Sync");
//        meeting.setDescription("Weekly team meeting");
////        meeting.setStart(LocalDateTime.now());
////        meeting.setEnd(LocalDateTime.now().plusHours(1));
//    }
//
//    @Test
//    void testBookMeeting() {
//        CreateMeetingRequest request = new CreateMeetingRequest();
//        request.setUserId(1L);
//        request.setTitle("Team Sync");
//        request.setDescription("Weekly team meeting");
//        request.setParticipantIds(Arrays.asList(2L, 3L));
//        request.setFrom(LocalDateTime.now().toString());
//        request.setTo(LocalDateTime.now().plusHours(1).toString());
//
//        when(meetingService.bookMeeting(anyLong(), anyString(), anyString(), anyList(), any(), any()))
//                .thenReturn(meeting);
//
//        ResponseEntity<MeetingDTO> response = meetingController.bookMeeting(request);
//
//        assertEquals(201, response.getStatusCodeValue());
//        assertEquals(meeting.getTitle(), response.getBody().getTitle());
//
//        verify(meetingService, times(1))
//                .bookMeeting(anyLong(), anyString(), anyString(), anyList(), any(), any());
//    }
//
//    @Test
//    void testGetMeetingsForUsers() {
//        Page<Meeting> meetingPage = new PageImpl<>(List.of(meeting));
//        when(meetingService.getMeetingsForUsers(anyList(), any(), any(), anyInt(), anyInt()))
//                .thenReturn(meetingPage);
//
//        ResponseEntity<List<MeetingDTO>> response = meetingController.getMeetingsForUsers(
//                Arrays.asList(1L, 2L), null, null, 0, 10);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(1, response.getBody().size());
//
//        verify(meetingService, times(1))
//                .getMeetingsForUsers(anyList(), any(), any(), anyInt(), anyInt());
//    }
//
//    @Test
//    void testGetMeetingById() {
//        when(meetingService.getMeeting(anyLong())).thenReturn(meeting);
//
//        ResponseEntity<MeetingDTO> response = meetingController.getMeetingById(1L);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(meeting.getTitle(), response.getBody().getTitle());
//
//        verify(meetingService, times(1)).getMeeting(anyLong());
//    }
//
//    @Test
//    void testUpdateMeeting() {
//        UpdateMeetingRequest request = new UpdateMeetingRequest();
//        request.setTitle("Updated Title");
//        request.setDescription("Updated Description");
//
//        meeting.setTitle(request.getTitle());
//        meeting.setDescription(request.getDescription());
//
//        when(meetingService.updateMeeting(anyLong(), anyString(), anyString()))
//                .thenReturn(meeting);
//
//        ResponseEntity<MeetingDTO> response = meetingController.updateMeeting(1L, request);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals("Updated Title", response.getBody().getTitle());
//        assertEquals("Updated Description", response.getBody().getDescription());
//
//        verify(meetingService, times(1))
//                .updateMeeting(anyLong(), anyString(), anyString());
//    }
//
//    @Test
//    void testDeleteMeeting() {
//        doNothing().when(meetingService).deleteMeeting(anyLong());
//
//        ResponseEntity<Void> response = meetingController.delete(1L);
//
//        assertEquals(204, response.getStatusCodeValue());
//
//        verify(meetingService, times(1)).deleteMeeting(anyLong());
//    }
}
