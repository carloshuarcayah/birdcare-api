package pe.com.birdcare.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false,columnDefinition = "boolean default true")
    private Boolean active;


    public void enable(){
        this.active=true;
    }

    public void disable(){
        this.active=false;
    }

    public void update(String name, String description){
        validateName(name);
        this.name=name;
        this.description=description;
    }

    public Category(String name, String description){
        validateName(name);
        this.name= name;
        this.description=description;
        this.active=true;
    }

    public void validateName(String name){
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
    }

}
