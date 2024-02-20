package maksim.andrzejewski.SpringBatchTest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
