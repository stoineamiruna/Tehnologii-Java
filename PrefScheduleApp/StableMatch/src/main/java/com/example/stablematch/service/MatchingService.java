package com.example.stablematch.service;

import com.example.stablematch.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchingService {
    public MatchingResponseDTO createRandomMatching(MatchingRequestDTO request) {
        log.info("Starting random matching for {} students and {} courses",
                request.getStudentPreferences().size(),
                request.getCourses().size());

        List<AssignmentDTO> assignments = new ArrayList<>();
        Map<String, Integer> courseAssignmentCounts = new HashMap<>();
        Map<String, Integer> courseCapacities = request.getCourses().stream()
                .collect(Collectors.toMap(CourseCapacityDTO::getCourseCode, CourseCapacityDTO::getCapacity));

        request.getCourses().forEach(c -> courseAssignmentCounts.put(c.getCourseCode(), 0));

        List<StudentPreferenceDTO> shuffledStudents = new ArrayList<>(request.getStudentPreferences());
        Collections.shuffle(shuffledStudents);

        for (StudentPreferenceDTO student : shuffledStudents) {
            boolean assigned = false;

            List<String> availableCourses = new ArrayList<>(courseCapacities.keySet());
            Collections.shuffle(availableCourses);

            for (String courseCode : availableCourses) {
                Integer currentCount = courseAssignmentCounts.get(courseCode);
                Integer capacity = courseCapacities.get(courseCode);

                if (currentCount < capacity) {
                    Integer preferenceRank = student.getPreferredCourses().indexOf(courseCode);
                    if (preferenceRank == -1) {
                        preferenceRank = null;
                    }

                    Double studentScore = calculateStudentScore(student, courseCode, request.getInstructorPreferences());

                    assignments.add(AssignmentDTO.builder()
                            .studentCode(student.getStudentCode())
                            .courseCode(courseCode)
                            .preferenceRank(preferenceRank)
                            .studentScore(studentScore)
                            .build());

                    courseAssignmentCounts.put(courseCode, currentCount + 1);
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                log.warn("Could not assign student {}", student.getStudentCode());
            }
        }

        MatchingStatisticsDTO statistics = calculateStatistics(
                request.getStudentPreferences().size(),
                assignments,
                courseAssignmentCounts
        );

        log.info("Random matching completed: {} students assigned out of {}",
                statistics.getAssignedStudents(),
                statistics.getTotalStudents());

        return MatchingResponseDTO.builder()
                .assignments(assignments)
                .statistics(statistics)
                .build();
    }

    public MatchingResponseDTO createStableMatching(MatchingRequestDTO request) {
        log.info("Starting stable matching for {} students and {} courses",
                request.getStudentPreferences().size(),
                request.getCourses().size());

        Map<String, Integer> courseCapacities = request.getCourses().stream()
                .collect(Collectors.toMap(CourseCapacityDTO::getCourseCode, CourseCapacityDTO::getCapacity));

        Map<String, List<StudentPreferenceDTO>> courseAssignments = new HashMap<>();
        request.getCourses().forEach(c -> courseAssignments.put(c.getCourseCode(), new ArrayList<>()));

        Map<String, Map<String, Double>> studentScores = new HashMap<>();
        for (StudentPreferenceDTO student : request.getStudentPreferences()) {
            Map<String, Double> scores = new HashMap<>();
            for (String courseCode : courseCapacities.keySet()) {
                scores.put(courseCode, calculateStudentScore(student, courseCode, request.getInstructorPreferences()));
            }
            studentScores.put(student.getStudentCode(), scores);
        }

        Queue<StudentPreferenceDTO> freeStudents = new LinkedList<>(request.getStudentPreferences());
        Map<String, Integer> studentProposalIndex = new HashMap<>();
        request.getStudentPreferences().forEach(s -> studentProposalIndex.put(s.getStudentCode(), 0));

        while (!freeStudents.isEmpty()) {
            StudentPreferenceDTO student = freeStudents.poll();
            Integer proposalIndex = studentProposalIndex.get(student.getStudentCode());

            if (proposalIndex >= student.getPreferredCourses().size()) {
                continue;
            }

            String courseCode = student.getPreferredCourses().get(proposalIndex);
            studentProposalIndex.put(student.getStudentCode(), proposalIndex + 1);

            List<StudentPreferenceDTO> currentAssignments = courseAssignments.get(courseCode);
            Integer capacity = courseCapacities.get(courseCode);

            if (currentAssignments.size() < capacity) {
                currentAssignments.add(student);
            } else {
                StudentPreferenceDTO worstStudent = findWorstStudent(currentAssignments, courseCode, studentScores);
                Double studentScore = studentScores.get(student.getStudentCode()).get(courseCode);
                Double worstScore = studentScores.get(worstStudent.getStudentCode()).get(courseCode);

                if (studentScore > worstScore) {
                    currentAssignments.remove(worstStudent);
                    currentAssignments.add(student);
                    freeStudents.add(worstStudent);
                } else {
                    freeStudents.add(student);
                }
            }
        }

        List<AssignmentDTO> assignments = new ArrayList<>();
        Map<String, Integer> courseAssignmentCounts = new HashMap<>();

        for (Map.Entry<String, List<StudentPreferenceDTO>> entry : courseAssignments.entrySet()) {
            String courseCode = entry.getKey();
            List<StudentPreferenceDTO> students = entry.getValue();
            courseAssignmentCounts.put(courseCode, students.size());

            for (StudentPreferenceDTO student : students) {
                Integer preferenceRank = student.getPreferredCourses().indexOf(courseCode);
                Double studentScore = studentScores.get(student.getStudentCode()).get(courseCode);

                assignments.add(AssignmentDTO.builder()
                        .studentCode(student.getStudentCode())
                        .courseCode(courseCode)
                        .preferenceRank(preferenceRank)
                        .studentScore(studentScore)
                        .build());
            }
        }

        MatchingStatisticsDTO statistics = calculateStatistics(
                request.getStudentPreferences().size(),
                assignments,
                courseAssignmentCounts
        );

        log.info("Stable matching completed: {} students assigned out of {}",
                statistics.getAssignedStudents(),
                statistics.getTotalStudents());

        return MatchingResponseDTO.builder()
                .assignments(assignments)
                .statistics(statistics)
                .build();
    }

    private StudentPreferenceDTO findWorstStudent(List<StudentPreferenceDTO> students,
                                                  String courseCode,
                                                  Map<String, Map<String, Double>> studentScores) {
        return students.stream()
                .min(Comparator.comparing(s -> studentScores.get(s.getStudentCode()).get(courseCode)))
                .orElseThrow();
    }

    private Double calculateStudentScore(StudentPreferenceDTO student,
                                         String courseCode,
                                         List<InstructorPreferenceDTO> instructorPreferences) {
        InstructorPreferenceDTO instructorPref = instructorPreferences.stream()
                .filter(ip -> ip.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);

        if (instructorPref == null || instructorPref.getGradeWeights() == null ||
                instructorPref.getGradeWeights().isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (Map.Entry<String, Double> weight : instructorPref.getGradeWeights().entrySet()) {
            String compulsoryCourse = weight.getKey();
            Double weightPercentage = weight.getValue();

            if (student.getCourseGrades() != null && student.getCourseGrades().containsKey(compulsoryCourse)) {
                Double grade = student.getCourseGrades().get(compulsoryCourse);
                weightedSum += grade * weightPercentage;
                totalWeight += weightPercentage;
            }
        }

        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    private MatchingStatisticsDTO calculateStatistics(Integer totalStudents,
                                                      List<AssignmentDTO> assignments,
                                                      Map<String, Integer> courseAssignmentCounts) {
        Integer assignedStudents = assignments.size();
        Integer unassignedStudents = totalStudents - assignedStudents;

        Double averagePreferenceRank = assignments.stream()
                .filter(a -> a.getPreferenceRank() != null)
                .mapToInt(AssignmentDTO::getPreferenceRank)
                .average()
                .orElse(0.0);

        return MatchingStatisticsDTO.builder()
                .totalStudents(totalStudents)
                .assignedStudents(assignedStudents)
                .unassignedStudents(unassignedStudents)
                .averagePreferenceRank(averagePreferenceRank)
                .courseAssignmentCounts(courseAssignmentCounts)
                .build();
    }
}