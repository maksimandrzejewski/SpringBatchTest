package maksim.andrzejewski.SpringBatchTest.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id_fk")
    private Set<Privilege> privilegeSet = new HashSet<>();
}
