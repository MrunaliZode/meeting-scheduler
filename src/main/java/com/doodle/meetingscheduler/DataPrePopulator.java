package com.doodle.meetingscheduler;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.repository.MeetingRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class DataPrePopulator implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TimeSlotRepository slotRepository;
    private final MeetingRepository meetingRepository;

    public DataPrePopulator(UserRepository userRepository,
                            TimeSlotRepository slotRepository,
                            MeetingRepository meetingRepository) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.meetingRepository = meetingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsersAndSlots();
    }

    private void seedUsersAndSlots() {
        int totalUsers = 200;
        int slotsPerUser = 100;
        Random random = new Random();

        List<User> users = new ArrayList<>();
        if (userRepository.count() < totalUsers) {
            for (int i = 1; i <= totalUsers; i++) {
                User user = new User();
                user.setName("User " + i);
                user.setEmail("user" + i + "@doodle.com");
                users.add(user);
            }
            userRepository.saveAll(users);
        } else {
            users = userRepository.findAll();
        }

        if (slotRepository.count() > 0) return;

        LocalDate currentDate = LocalDate.now();

        for (User user : users) {
            int slotsCreated = 0;
            LocalTime startHour = LocalTime.of(9, 0);
            LocalTime endHour = LocalTime.of(17, 0);

            while (slotsCreated < slotsPerUser) {
                List<TimeSlot> batchSlots = new ArrayList<>();
                List<Meeting> batchMeetings = new ArrayList<>();

                LocalTime slotTime = startHour;
                while (slotTime.isBefore(endHour) && slotsCreated < slotsPerUser) {
                    int rand = random.nextInt(100);

                    if (rand < 80) {
                        // FREE or BUSY slot
                        TimeSlot slot = new TimeSlot();
                        slot.setUser(user);
                        slot.setStartTime(LocalDateTime.of(currentDate, slotTime));
                        slot.setEndTime(LocalDateTime.of(currentDate, slotTime.plusMinutes(30)));
                        slot.setStatus(rand < 40 ? SlotStatus.FREE : SlotStatus.BUSY);
                        batchSlots.add(slot);
                    } else {
                        // BOOKED -> create meeting
                        Meeting meeting = new Meeting();
                        meeting.setTitle("Meeting " + UUID.randomUUID());
                        meeting.setDescription("Auto-generated");

                        Set<User> participants = new HashSet<>();
                        participants.add(user);
                        Collections.shuffle(users);
                        for (int i = 0; i < Math.min(3, users.size()); i++) {
                            participants.add(users.get(i));
                        }
                        meeting.getParticipants().addAll(participants);

                        for (User participant : participants) {
                            TimeSlot slot = new TimeSlot();
                            slot.setUser(participant);
                            slot.setStartTime(LocalDateTime.of(currentDate, slotTime));
                            slot.setEndTime(LocalDateTime.of(currentDate, slotTime.plusMinutes(30)));
                            slot.setStatus(SlotStatus.BOOKED);
                            slot.setMeeting(meeting);
                            meeting.getSlots().add(slot);
                            batchSlots.add(slot);
                        }

                        batchMeetings.add(meeting);
                    }

                    slotsCreated++;
                    slotTime = slotTime.plusMinutes(30);
                }

                if (!batchSlots.isEmpty()) slotRepository.saveAll(batchSlots);
                if (!batchMeetings.isEmpty()) meetingRepository.saveAll(batchMeetings);

                currentDate = currentDate.plusDays(1);
            }
        }
    }
}