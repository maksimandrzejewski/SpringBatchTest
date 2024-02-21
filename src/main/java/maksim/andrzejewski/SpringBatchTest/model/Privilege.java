package maksim.andrzejewski.SpringBatchTest.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PRIVILEGES")
public class Privilege {

    @Id
    @GeneratedValue
    @Column(name = "privilege_id")
    private Long id;

    private String privilegeName;



}
