//package get2gether.model;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.DynamicUpdate;
//
//import java.time.LocalDate;
//import java.util.Set;
//
//@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@DynamicUpdate
//public class Availability {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    @JsonManagedReference
//    @EqualsAndHashCode.Exclude
//    private User user;
//
////    @ElementCollection
////    @CollectionTable(name = "availability_days", joinColumns = @JoinColumn(name = "availability_id"))
////    @Column(name = "available_day")
////    private Set<LocalDate> availableDays;
//}
//
