package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "User")

//atributes: name, password, rut, phone number, email, role, nationality, acc state
public class userEntity {
    // primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    // basic fields
    // must have fields (full length name,  e-mail, pasword, rut, role*)
    @Column(name = "name_user", length = 80, nullable = false)
    private String nameUser;

    @Column(name = "email_user", length = 100, nullable = false, unique = true) // to do: validate emails
    private String emailUser;

    @Column(name = "password_user", length = 255, nullable = false) // to do: add restriccions and conditions for a safe password
    private String passwordUser;

    @Column(name = "rut_user", length = 10, nullable = false, unique = true)
    private String rutUser;







}
