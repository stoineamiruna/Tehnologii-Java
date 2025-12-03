package com.example.prefschedule.service;
import com.example.prefschedule.client.StableMatchClient;
import com.example.prefschedule.dto.matching.*;
import com.example.prefschedule.entity.*;
import com.example.prefschedule.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingOrchestrationService {

    private final StableMatchClient stableMatchClient;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentPreferenceRepository studentPreferenceRepository;
    private final InstructorCoursePreferenceRepository instructorPreferenceRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final PackRepository packRepository;

    @Transactional(readOnly = true)
    public CompletableFuture<MatchingResponseDTO> performMatchingForPack(Long packId, boolean useStableAlgorithm) {
        log.info("Starting matching orchestration for pack ID: {}", packId);

        List<Course> courses = courseRepository.findByPackId(packId);
        if (courses.isEmpty()) {
            log.warn("No courses found for pack ID: {}", packId);
            return CompletableFuture.completedFuture(createEmptyResponse());
        }

        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new RuntimeException("Pack not found"));

        List<Student> students = studentRepository.findByYear(pack.getYear());
        if (students.isEmpty()) {
            log.warn("No students found for year: {}", pack.getYear());
            return CompletableFuture.completedFuture(createEmptyResponse());
        }

        MatchingRequestDTO request = buildMatchingRequest(students, courses, pack);

        if (useStableAlgorithm) {
            return stableMatchClient.createStableMatching(request);
        } else {
            return stableMatchClient.createRandomMatching(request);
        }
    }

    @Transactional(readOnly = true)
    public CompletableFuture<Map<Long, MatchingResponseDTO>> performMatchingForAllPacks(
            Integer year, Integer semester, boolean useStableAlgorithm) {
        log.info("Starting matching for all packs in year {} semester {}", year, semester);

        List<Pack> packs = packRepository.findByYearAndSemester(year, semester);

        if (packs.isEmpty()) {
            log.warn("No packs found for year {} semester {}", year, semester);
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        Map<Long, CompletableFuture<MatchingResponseDTO>> futureMap = packs.stream()
                .collect(Collectors.toMap(
                        Pack::getId,
                        pack -> performMatchingForPack(pack.getId(), useStableAlgorithm)
                ));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futureMap.values().toArray(new CompletableFuture[0])
        );

        return allFutures.thenApply(v ->
                futureMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().join()
                        ))
        );
    }

    private MatchingRequestDTO buildMatchingRequest(List<Student> students,
                                                    List<Course> courses,
                                                    Pack pack) {
        List<StudentPreferenceDTO> studentPreferences = students.stream()
                .map(student -> buildStudentPreference(student, courses, pack))
                .collect(Collectors.toList());

        List<CourseCapacityDTO> courseCapacities = courses.stream()
                .map(course -> CourseCapacityDTO.builder()
                        .courseCode(course.getCode())
                        .capacity(course.getGroupCount() != null ? course.getGroupCount() * 30 : 30)
                        .instructorId(course.getInstructor() != null ?
                                course.getInstructor().getId().toString() : null)
                        .build())
                .collect(Collectors.toList());

        List<InstructorPreferenceDTO> instructorPreferences = courses.stream()
                .map(this::buildInstructorPreference)
                .collect(Collectors.toList());

        return MatchingRequestDTO.builder()
                .studentPreferences(studentPreferences)
                .courses(courseCapacities)
                .instructorPreferences(instructorPreferences)
                .build();
    }

    private StudentPreferenceDTO buildStudentPreference(Student student,
                                                        List<Course> courses,
                                                        Pack pack) {
        List<StudentPreference> preferences = studentPreferenceRepository
                .findByStudentIdAndCoursePackId(student.getId(), pack.getId());

        Map<Long, Integer> courseOrderMap = new HashMap<>();
        for (StudentPreference pref : preferences) {
            courseOrderMap.put(pref.getCourse().getId(), pref.getPreferenceOrder());
        }

        List<String> preferredCourses = courses.stream()
                .sorted(Comparator.comparing(c -> courseOrderMap.getOrDefault(c.getId(), Integer.MAX_VALUE)))
                .map(Course::getCode)
                .collect(Collectors.toList());

        List<StudentGrade> grades = studentGradeRepository.findByStudentCode(student.getCode());
        Map<String, Double> courseGrades = new HashMap<>();

        for (StudentGrade grade : grades) {
            Course course = courseRepository.findByCode(grade.getCourseCode()).orElse(null);
            if (course != null && "COMPULSORY".equals(course.getType())) {
                courseGrades.put(course.getAbbr(), grade.getGrade());
            }
        }

        return StudentPreferenceDTO.builder()
                .studentCode(student.getCode())
                .preferredCourses(preferredCourses)
                .courseGrades(courseGrades)
                .build();
    }

    private InstructorPreferenceDTO buildInstructorPreference(Course course) {
        List<InstructorCoursePreference> preferences =
                instructorPreferenceRepository.findByCourseId(course.getId());

        Map<String, Double> gradeWeights = preferences.stream()
                .collect(Collectors.toMap(
                        InstructorCoursePreference::getCompulsoryCourseAbbr,
                        InstructorCoursePreference::getWeightPercentage
                ));

        return InstructorPreferenceDTO.builder()
                .courseCode(course.getCode())
                .gradeWeights(gradeWeights)
                .build();
    }

    private MatchingResponseDTO createEmptyResponse() {
        return MatchingResponseDTO.builder()
                .assignments(Collections.emptyList())
                .statistics(MatchingStatisticsDTO.builder()
                        .totalStudents(0)
                        .assignedStudents(0)
                        .unassignedStudents(0)
                        .averagePreferenceRank(0.0)
                        .courseAssignmentCounts(Collections.emptyMap())
                        .build())
                .build();
    }
}
