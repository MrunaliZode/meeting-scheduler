package com.doodle.meetingscheduler;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataPrePopulator implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TimeSlotRepository slotRepository;

    public DataPrePopulator(UserRepository userRepository, TimeSlotRepository slotRepository) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    public void run(String... args) {

        // Create users if table is empty
        if (userRepository.count() == 0) {
            User alice = new User();
            alice.setName("Alice");
            alice.setEmail("alice@doodle.com");

            User bob = new User();
            bob.setName("Bob");
            bob.setEmail("bob@doodle.com");

            User charlie = new User();
            charlie.setName("Charlie");
            charlie.setEmail("charlie@doodle.com");

            userRepository.saveAll(List.of(alice, bob, charlie));
        }

        // Create time slots for each user if they have none
        userRepository.findAll().forEach(user -> {
            if (slotRepository.countByUserId(user.getId()) == 0) {
                TimeSlot slot1 = new TimeSlot();
                slot1.setUser(user);
                slot1.setStartTime(LocalDateTime.of(2025, 9, 29, 9, 0));
                slot1.setEndTime(LocalDateTime.of(2025, 9, 29, 12, 0));
                slot1.setStatus(SlotStatus.FREE);

                TimeSlot slot2 = new TimeSlot();
                slot2.setUser(user);
                slot2.setStartTime(LocalDateTime.of(2025, 9, 29, 14, 0));
                slot2.setEndTime(LocalDateTime.of(2025, 9, 29, 17, 0));
                slot2.setStatus(SlotStatus.FREE);

                slotRepository.saveAll(List.of(slot1, slot2));
            }
        });

        System.out.println("Demo users and time slots populated successfully!");
    }
}

