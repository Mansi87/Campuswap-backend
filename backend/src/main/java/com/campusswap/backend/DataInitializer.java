package com.campusswap.backend;

import com.campusswap.backend.model.College;
import com.campusswap.backend.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CollegeRepository collegeRepository;

    @Override
    public void run(String... args) {
        // Add sample college if not exists
        if (collegeRepository.count() == 0) {
            College college = new College();
            college.setName("Test University");
            college.setEmailDomain("test.edu");
            college.setLocation("Test City");
            college.setIsActive(true);
            collegeRepository.save(college);

            System.out.println("✅ Sample college added: " + college.getName());
            System.out.println("📧 Email domain: " + college.getEmailDomain());
            System.out.println("🆔 College ID: " + college.getId());
        }
    }
}
